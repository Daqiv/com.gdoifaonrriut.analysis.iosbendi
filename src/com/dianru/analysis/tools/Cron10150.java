package com.dianru.analysis.tools;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;


public class Cron10150 {
	
	public static void cron(){
		
		String key = "ACTION_10150_REPORT";
		String up = "ACTION_UP_REPORT";
		
		String name = "queue";
		Jedis jedis = RedisConnection.getInstance(name);
		Long l = jedis.llen(key);
		if(l > 0){
			for(int i=0;i < l;i++){
				String value = jedis.lpop(key);
				jedis.rpush(up, value);
			}
		}
		RedisConnection.close(name, jedis);
		RedisConnection.shutdown();
	}
	
	public static void main(String[] args) {
		cron();
	}
}
