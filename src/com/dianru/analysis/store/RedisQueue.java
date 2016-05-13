package com.dianru.analysis.store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

public class RedisQueue {
	
	public static Logger LOG = LogManager.getLogger(RedisQueue.class);
	
	public final static RedisQueue INSTANCE = new RedisQueue();
	
	public final static RedisQueue getInstance() {
		return INSTANCE;
	}

	public String pop(String name) {
		Jedis jedis = RedisConnection.getInstance("queue");
		if(jedis == null) {
			LOG.warn("get connection faild");
			return null;
		}
		
		try {
			String line = jedis.lpop(name);
			return line;
		} catch (Exception e) {
			LOG.warn("queue pop faild");
			return null;
		} finally {
			RedisConnection.close("queue",jedis);
		}
	}
}
