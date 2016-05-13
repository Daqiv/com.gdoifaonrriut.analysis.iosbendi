package com.dianru.analysis.store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

public class RedisKeyValueStore extends KeyValueStore<String> {
	
	
	
	public static Logger LOG = LogManager.getLogger(RedisKeyValueStore.class);
	
	public String key;
	public RedisKeyValueStore(String key) {
		super(key);
		this.key = key;
	}
	
	public String get(CharSequence field) {

		if(key != null && !key.isEmpty()) {

			String value = super.get(field);
			if(value != null) return value;
				
			Jedis jedis = RedisConnection.getInstance("values");
			if(jedis == null) {
				LOG.error("connect to redis error");
				return null;
			}
			try {
				value = jedis.hget(key, field.toString());
			} catch(Exception e) {
				return null;
			}
			
			RedisConnection.close("values", jedis);
			if(value != null) {
				super.put(field, value);
			}
			
			return value;
		}
		return null;
	}
	
	public String put(CharSequence field, String value) {

			if(field.length() > 0) {
				
				String old = super.put(field, value);
				
				Jedis jedis = RedisConnection.getInstance("values");
				if(jedis == null) {
					LOG.error("connect to redis error");
					return old;
				}
				try {
					jedis.hset(key, field.toString(), value);
				} catch(Exception e) {
					return old;
				}
				RedisConnection.close("values", jedis);
				
				return old;
			}
			return value;
	}
	
	public static void main(String[] args) {
		CharSequence field = "123";
		System.out.println(field);
	}
}
