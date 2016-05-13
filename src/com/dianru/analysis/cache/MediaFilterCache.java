package com.dianru.analysis.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.MediaApp;
import com.dianru.analysis.bean.MediaFilter;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class MediaFilterCache {
	
	private static MediaFilterCache INSTANCE = new MediaFilterCache();
	
	public static Logger LOG = LogManager.getLogger(MediaCache.class);
	
	public static MediaFilterCache getInstance() {
		return INSTANCE;
	}

	private Map<String,MediaFilter> map = new ConcurrentHashMap<String, MediaFilter>();
	
	private long update = 0;
	
	public MediaFilterCache() {
		update();
	}

	public void update() {
		SQLConnection conn = SQLConnection.getInstance("main");

		//add by chenjun
		//update = (jedis.get("DATA_MEDIAFILTER_UPDATE")==null) ? 0 : Long.parseLong(jedis.get("DATA_MEDIAFILTER_UPDATE"));
		
		List<List<Object>> items = conn.queryList("SELECT type,level,rate,save,fwz_rate,fwz_save FROM filters_level WHERE update_time>?", update);
		if (!items.isEmpty()) {
			MediaFilter wl = null;
			MediaFilter fl = null;
			for (List<Object> vals : items) {
				int type = (int)vals.get(0);
				int level = (int)vals.get(1);
				int rate = (int)vals.get(2);
				int save = (int)vals.get(3);
				int fwz_rate = (int)vals.get(4);
				int fwz_save = (int)vals.get(5);

				wl = new MediaFilter(rate, save);
				fl = new MediaFilter(fwz_rate, fwz_save);
				
				String key = String.format("%d-%d-%d", type, level, 1);
				map.put(key, wl);
				
				key = String.format("%d-%d-%d", type, level, 2);
				map.put(key, fl);
			}
		}
		
		//long lastUpdate = update;
		//update = System.currentTimeMillis()/1000-300;
		
		conn.close();
		if(update == 0) return;		//lastUpdate//刚启动media还没有更新

		MediaCache mc = MediaCache.getInstance();
		Jedis jedis = RedisConnection.getInstance("main");
		
		for(Iterator<MediaApp> it = mc.iterator();it.hasNext();) {
			MediaApp media = it.next();
			Map<Integer, MediaFilter> mr = media.getRates();
			
			if(mr != null) {
				boolean update = false;
				
				for(Iterator<Entry<Integer, MediaFilter>> sit = mr.entrySet().iterator();sit.hasNext();) {
					Entry<Integer, MediaFilter> entry = sit.next();
					
					int level = entry.getKey();
					MediaFilter ol = entry.getValue();
					MediaFilter fl = get(media.getType(), level, media.getIsWangZhuan());
					
					if(ol == null || fl == null) continue;
					//这儿更新完成之后会直接修改mediaApp当中map的值
					if(ol.getRate() != fl.getRate() || ol.getSave() != fl.getSave()) {
						ol.setRate(fl.getRate());
						ol.setSave(fl.getSave());
						update = true;
					}
				}
				//并不是每次都需要更新，所以需要添加
				if(update) {
					if(jedis != null) jedis.hset("DATA_MEDIA", String.valueOf(media.getMid()), media.toString());
					LOG.debug("update app " + media.getMid());
				}
			}
		}
		if(jedis != null) RedisConnection.close("main",jedis);
	}
	
	public MediaFilter get(int type, int level, int isWangZhuan) {
		String key = String.format("%d-%d-%d", type, level, isWangZhuan);
		return map.get(key);
	}
	
	public static void main(String[] args) {
		MediaFilterCache.getInstance().update();
	}
}
