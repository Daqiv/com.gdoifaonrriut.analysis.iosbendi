package com.dianru.analysis.boot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;
import com.dianru.analysis.cache.MediaFilterCache;
import com.dianru.analysis.cache.MeidaPriceCache;
import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.RedisConnection;

public class ObjectCache extends TimerTask {

	public static Logger LOG = LogManager.getLogger(ObjectCache.class);
	public static String SAVE_PATH = Configuration.getInstance().getProperty("path.dump.dir", "/tmp");
	
	public static void update() {
		MediaFilterCache.getInstance().update();
		MeidaPriceCache.getInstance().update();
		
		MediaCache.getInstance().update();
		AdsCache.getInstance().update();
		
		cleanRestAds();
	}
	
	public void run() {
		update();
		LOG.info("update cache from database");
	}
	
	private static Timer timer = null;
	private static ObjectCache updateTask = new ObjectCache();

	public static void start() {
		if (timer != null)
			return;
		
		load();
		
		timer = new Timer();
		timer.schedule(updateTask, 60000, 60000);
	}
	
	public static void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		
		save();
	}
	
	public static void load(){
		
		try {
			MediaFilterCache.getInstance().update();
			MeidaPriceCache.getInstance().update();
			
			String mPath = SAVE_PATH+"/media.cache";
			File mFile = new File(mPath);
			if(mFile.exists()){
				FileInputStream mIn = new FileInputStream(mPath);
				MediaCache.getInstance().load(mIn);
				mIn.close();
				
				mFile.delete();
				LOG.debug("media load sucess");
			}else {
				MediaCache.getInstance().update();
			}
			
			String aPath = SAVE_PATH+"/ads.cache";
			File aFile = new File(aPath);
			if(aFile.exists()){
				FileInputStream aIn = new FileInputStream(aPath);
				AdsCache.getInstance().load(aIn);
				aIn.close();
				
				aFile.delete();
				LOG.debug("ads load sucess");
			}else {
				AdsCache.getInstance().update();
			}
			
		} catch (Exception e) {
			LOG.error("start load fail" + e.getMessage());
		}
	}
	
	public static void save(){
		
		try {
			FileOutputStream aOut = new FileOutputStream(SAVE_PATH+"/ads.cache");
			AdsCache.getInstance().save(aOut);
			aOut.close();
			LOG.debug("ads save sucess");
			
			FileOutputStream mOut = new FileOutputStream(SAVE_PATH+"/media.cache");
			MediaCache.getInstance().save(mOut);
			mOut.close();
			LOG.debug("media save sucess");
		} catch (IOException e) {
			LOG.error("stop save fail" + e.getMessage());
		}
	}
	
	private static void cleanRestAds() {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			for (int i = 1; i < Define.TYPES.length; i++) {
				String key = "DATA_ADS_TYPE_" + i;
				Map<String, String> ads = jedis.hgetAll(key);
				Collection<String> values = ads.values();
				for (String adsStr : values) {
					JSONObject adsJson = new JSONObject(adsStr);
					long update_time = 0;
					try {
						update_time = adsJson.getLong("update_time");
					} catch (Exception e) {
						LOG.debug("get update_time error:" + adsJson);
						continue;
					}
					if ((adsJson.getInt("state") == 5 || adsJson.getInt("state") == 8 || adsJson.getInt("state") == 9)
							&& (System.currentTimeMillis() / 1000 - update_time) >= 3600 * 24 * 7) {
						jedis.hdel(key, String.valueOf(adsJson.getInt("adid")));
						LOG.debug("remove remain ads from timer " + adsJson.get("adid"));
					}
				}
			}
		}catch(Exception e){
			LOG.error("remove remain ads exception " + e.getMessage());
		}finally {
			if(jedis!= null) RedisConnection.close("main", jedis);
		}
	}
	
	public static void main(String[] args) {
		update();
	}
}