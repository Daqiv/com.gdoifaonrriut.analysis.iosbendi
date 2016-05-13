package com.dianru.analysis.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Media;
import com.dianru.analysis.bean.MediaApp;
import com.dianru.analysis.bean.MediaChannel;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class MediaCache {
	
	public static Logger LOG = LogManager.getLogger(MediaCache.class);
	
	private static MediaCache INSTANCE = new MediaCache();
	
	public static MediaCache getInstance() {
		return INSTANCE;
	}
	
	private Map<Integer,MediaChannel> channelMap = new ConcurrentHashMap<Integer, MediaChannel>();
	private Map<Integer,MediaApp> appMap = new ConcurrentHashMap<Integer, MediaApp>();
	
	private long update = 0;
	public MediaCache() {
		update();
	}
	
	public Iterator<MediaApp> iterator() {
		return appMap.values().iterator();
	}
	
	public void updateApps(SQLConnection conn, Jedis jedis, long lastUpdate) {
		
		/**
		 * @author zhujunwu
		 * 查询sdk_version
		 * sdk_version = 2.0
		 * save和rate全部扣完
		 * */
		String sql = "SELECT a.mid as mid,uid,type,title,mtype,mstype,`mlevel`,`rates`,`check`,`state`,`citys`,`hours`,`ip`,keywords,`is_wangzhuan`,os,callback_url,callback_key,callback_idfa,callback_mac,ratio,unit,offer_wall,free_wall,insert_screen,full_screen,banner,is_wangzhuan,is_enable,sdk_version,update_time,shielded_ads,is_session,options,admin_shielded_ads FROM media a,media_app b WHERE a.type=1 AND a.mid=b.mid AND update_time>?";
		
		List<Map<String,Object>> items = conn.queryMap(sql, new Object[] { update });

		if (!items.isEmpty()) {
			for (Map<String,Object> vals : items) {
				MediaApp media = new MediaApp(vals);
				//int update_time = (int)vals.get("update_time");
				appMap.put(media.getMid(), media);
				
				if(media.getState() == 1 || media.getState() == 4) {
					if(jedis != null) jedis.hset("DATA_MEDIA", String.valueOf(media.getMid()), media.toString());
					LOG.debug("update app " + media.getMid());
				} else {
					if(jedis != null) jedis.hdel("DATA_MEDIA", String.valueOf(media.getMid()));
					LOG.debug("remove media " + media.getMid());
				}
			}
		}
	}
	
	public void updateChannels(SQLConnection conn, Jedis jedis, long lastUpdate) {
		
		String sql = "SELECT a.mid as mid,uid,type,title,mtype,mstype,`mlevel`,`rates`,`check`,`state`,`citys`,`hours`,`ip`,`adid_ins`,`callbacks`,is_wangzhuan,is_enable,update_time,shielded_ads,is_session FROM media a,media_channel b WHERE a.type=2 AND a.mid=b.mid AND update_time>?";
		List<Map<String,Object>> items = conn.queryMap(sql, new Object[] { update });

		if (!items.isEmpty()) {
			for (Map<String,Object> vals : items) {
				MediaChannel media = new MediaChannel(vals);
				//int update_time = (int)vals.get("update_time");
				
				channelMap.put(media.getMid(), media);
				
				if(media.getState() == 1 || media.getState() == 4) {
					if(jedis != null) jedis.hset("DATA_MEDIA", String.valueOf(media.getMid()), media.toString());
					LOG.debug("update channel " + media.getMid());
				} else {
					if(jedis != null) jedis.hdel("DATA_MEDIA", String.valueOf(media.getMid()));
					LOG.debug("remove channel " + media.getMid());
				}
			}
		}
	}

	public void update() {
		SQLConnection conn = SQLConnection.getInstance("main");
		Jedis jedis = RedisConnection.getInstance("main");
		
		String updateString = jedis.get("DATA_MEDIA_UPDATE");
		long redisUpdate = (updateString == null || updateString.isEmpty()) ? 0 : Long.parseLong(updateString);
		
		updateApps(conn,jedis,redisUpdate);
		updateChannels(conn,jedis,redisUpdate);
		conn.close();
		
		update = System.currentTimeMillis()/1000-60;
		jedis.set("DATA_MEDIA_UPDATE", String.valueOf(update));
		
		if(jedis != null) RedisConnection.close("main",jedis);
	}
	
	public void save(OutputStream stream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(stream);
		
		out.writeLong(update);
		out.writeInt(appMap.size());
		for(Iterator<Map.Entry<Integer,MediaApp>> it = appMap.entrySet().iterator();it.hasNext();) {
			Map.Entry<Integer,MediaApp> entry = it.next();
			out.writeInt(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.close();
	}
	
	public void load(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(stream);
		
		long update = in.readLong();
		int size = in.readInt();
		
		for(int i=0;i<size;i++) {
			int key = in.readInt();
			MediaApp value = (MediaApp)in.readObject();
			appMap.put(key, value);
		}
		in.close();
		this.update = update;
		this.update();
	}
	
	public MediaChannel getChannel(int mid) {
		return channelMap.get(mid);
	}
	
	public MediaApp getApp(int mid) {
		return appMap.get(mid);
	}

	public Media get(int mid) {
		Media media = appMap.get(mid);
		if(media != null) return media;
		
		return channelMap.get(mid);
	}
	
	public static void main(String[] args) {
		MediaCache.getInstance().update();
	}
}
