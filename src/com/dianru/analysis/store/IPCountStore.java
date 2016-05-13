package com.dianru.analysis.store;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

public class IPCountStore {

	public long add(String name, int action, int adid, long ip, long num) {
		Jedis jedis = RedisConnection.getInstance("values");
		String key = String.format("%d-%d-%d", action, adid, ip);
		long value = jedis.hincrBy(name, key, num);
		RedisConnection.close("values", jedis);
		return value;
	}

	private static IPCountStore INSTANCE = new IPCountStore();

	public static IPCountStore getInstance() {
		return INSTANCE;
	}
}
