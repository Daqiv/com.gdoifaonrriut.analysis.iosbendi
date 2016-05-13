package com.dianru.analysis.tools;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

/**
 * 重新开始到达预算开始投放的广告
 * @author WenYunlong
 *
 */
public class DailyClean {
	
	public static String SQL_CONTINUE_FORMAT = "UPDATE ads SET state=4,update_time=? WHERE state=8";
	public static String SQL_PAUSE_FORMAT = "UPDATE ads SET state=8,update_time=? WHERE adid=?";
	public static String SQL_PAUSE_ALL = "UPDATE ads SET state=8,update_time=? WHERE state=4 AND adid in (SELECT adid FROM ad_plan WHERE begin>? OR end<?)";
	public static String SQL_CONTINUE_ALL = "UPDATE ads SET state=4,update_time=? WHERE state=8 AND adid in (SELECT adid FROM ad_plan WHERE begin<=? AND end>=?)";
	
	public static Logger LOG = LogManager.getLogger(DailyClean.class);
	
	public static int setAdsStateToPause(int adid) {
		long time = System.currentTimeMillis()/1000;
		
		SQLConnection conn = SQLConnection.getInstance("main");
		int result = conn.update(SQL_PAUSE_FORMAT, new Object[]{time,adid});
		conn.close();

		LOG.info("pause ads : " + adid + " result : " + result);

		return result;
	}

	
	public static void main(String[] args) {
		
		long time = System.currentTimeMillis()/1000;
		String sql = String.format(SQL_CONTINUE_FORMAT, time);
		SQLConnection conn = SQLConnection.getInstance("main");
		int count = conn.update(sql, new Object[]{time});
		
		int pause = conn.update(SQL_PAUSE_ALL, new Object[]{time,time,time});
		int continuz = conn.update(SQL_CONTINUE_ALL, new Object[]{time,time,time});
		
		conn.close();
		
		LOG.info("pause "+pause+" ads by time");
		LOG.info("continue "+continuz+" ads by time plan");
		LOG.info("continue "+count+" ads from pause");
		
		int daySub1 = DateTime.getDate(-1);
		String adsCacheKey = "DATA_ADS_" + daySub1;
		String adsCacheKey2 = "CLICK_ADS_" + daySub1;
		String ipCacheKey = "IP_" + daySub1;
		
		Jedis jedis = RedisConnection.getInstance("main");
		jedis.del(adsCacheKey);
		jedis.del(adsCacheKey2);
		jedis.del(ipCacheKey);
		RedisConnection.close("main", jedis);
		LOG.info("delete "+adsCacheKey+"," +adsCacheKey2 +","+ipCacheKey+" key from 'main' cache");

		int daySub60 = DateTime.getDate(-60);
		jedis = RedisConnection.getInstance("values");
		for(int type=1;type<5;type++) {
			String daySub30Key = "store/"+daySub60+"/"+type+"/2";
			jedis.del(daySub30Key);
			LOG.info("delete "+daySub30Key+" key from 'values' cache");
		}
		RedisConnection.close("values", jedis);
	}
}
