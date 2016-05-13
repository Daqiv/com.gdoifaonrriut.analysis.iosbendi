package com.dianru.analysis.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
	
	public static Logger LOG = LogManager.getLogger(RedisConnection.class);
	
	public static class RedisSource {

		private String password = null;
		private JedisPool pool = null;
		
		public RedisSource(String name) {
			Configuration conf = Configuration.getInstance();
			
			String host = conf.getString("redis."+name+".host", "127.0.0.1");
			int port = conf.getInt("redis."+name+".port", 6379);
			String password = conf.getString("redis."+name+".password", null);

			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxActive(1024);
			config.setMaxIdle(10);
			config.setMaxWait(1800000L);
			config.setTimeBetweenEvictionRunsMillis(180000L);
			config.setTestOnBorrow(true);
			
			pool = new JedisPool(config, host, port, 180000);
			
			this.password = password;
		}

		public String getPassword() {
			return password;
		}
		
		public JedisPool getPool() {
			return pool;
		}
	}
	
	private static Map<String,RedisSource> MAP = new ConcurrentHashMap<String,RedisSource>();

	static {
		Configuration conf = Configuration.getInstance();
		String namesString = conf.getString("redis.names", "main");
		String[] names = namesString.split(",");
		for(String name : names) {
			name = name.trim();
			RedisSource rs = new RedisSource(name);
			MAP.put(name, rs);
		}
	}
	
	public static void shutdown() {
		for(Iterator<RedisSource> it = MAP.values().iterator();it.hasNext();) {
			RedisSource rs = it.next();
			JedisPool pool = rs.getPool();
			pool.destroy();
		}
	}
	
	public static void close(String name, Jedis jedis) {
		
		if(jedis == null){
			return;
		}
		
		RedisSource rs = MAP.get(name);
		if(rs == null) {
			System.out.println("unknow redis source : "+name);
		}
		
		JedisPool pool = rs.getPool();
		if(pool == null) jedis.disconnect();
		
		try {
			pool.returnResource(jedis);//返还到连接池
		} catch (Exception e) {
			//释放redis对象
			pool.returnBrokenResource(jedis); 
		}
		jedis = null;
	}
	
	public static Jedis getInstance(String name) {
		
		RedisSource rs = MAP.get(name);
		if(rs == null) {
			LOG.error("unknow redis source : " + name);
			return null;
		}
		
		JedisPool pool = rs.getPool();
		if(rs == null || pool == null) {
			LOG.error("get redis connection : " + name);
			return null;
		}
		
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			for(int i=0;i<5;i++) {
				if(jedis != null) {
					break;
				} else {
					LOG.warn("redis connection : " + name + " retry : " + i);
					Thread.sleep(1000);
				}
			}
			if(jedis == null) {
				LOG.error("redis connection error");
				return null;
			}
			
			String password = rs.getPassword();
			
			if (password != null && !password.isEmpty()){
				jedis.auth(password);
			}
			
		} catch (Exception e) {
			if(jedis != null){
				//释放redis对象
				pool.returnBrokenResource(jedis); 
			}
		}
		return jedis;
	}
}