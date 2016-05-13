package com.dianru.analysis.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.MediaPrice;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class MeidaPriceCache {
	
	private static MeidaPriceCache INSTANCE = new MeidaPriceCache();
	
	public static MeidaPriceCache getInstance() {
		return INSTANCE;
	}
	
	private long update = 0;
	
	public Map<String, MediaPrice> map = new ConcurrentHashMap<String, MediaPrice>();

	public MeidaPriceCache() {
		update();
	}
	
	public void update() {
		SQLConnection conn = SQLConnection.getInstance("main");
		Jedis jedis = RedisConnection.getInstance("main");
		
		//add by chenjun
		//update = (jedis.get("DATA_MEDIAPRICE_UPDATE")==null) ? 0 : Long.parseLong(jedis.get("DATA_MEDIAPRICE_UPDATE"));
		
		String sql = "SELECT `adid`,`appid`,`effect_time`,`price` FROM `media_price` WHERE isable=1 AND update_time>?";
		List<List<Object>> items = conn.queryList(sql, new Object[] { update });

		if (!items.isEmpty()) {
			for (List<Object> vals : items) {
				MediaPrice price = new MediaPrice(vals);
				String key = String.format("%d-%d", price.appid, price.adid);
				map.put(key, price);
				if(jedis != null) jedis.hset("DATA_MEDIA_PRICE", key, price.toString());
			}
		}
		
		conn.close();
		if(jedis != null) RedisConnection.close("main",jedis);
		
		update = System.currentTimeMillis() / 1000-60;
		//if(jedis != null) jedis.set("DATA_MEDIAPRICE_UPDATE", String.valueOf(update));
	}
	
	public MediaPrice get(int appid, int adid) {
		String key = String.format("%d-%d", appid, adid);
		return map.get(key);
	}
}
