package com.dianru.analysis.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.SQLConnection.DataSource;

/**
 * 同步广告转化率
 * 投放起半小时开始计算，后续频率30分钟
 * @author liuhuiya
 * @time 20150430
 */
public class HalfHourAdsPlan {
	
	private final static String QUERY_STRING = "SELECT sum(click_count) as click_count,sum(click_unique) as click_unique, sum(active_count) as active_count FROM %s WHERE adid=%d AND created=%d AND appid=7329 AND type=1 GROUP BY adid,created";
	public static Logger LOG = LogManager.getLogger(HalfHourAdsPlan.class);
	
	private static void syncAdRateByCount() {
		
		SQLConnection conn = SQLConnection.getInstance("main");
		Jedis jedis = RedisConnection.getInstance("main");
		
		String sql = "SELECT ads.adid as adid, ads.type as adtype, ad_plan.num active_num, ad_plan.click_num as click_num FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND ads.state=4 AND (UNIX_TIMESTAMP() - ads.update_time) > 1800";
		List<Map<String,Object>> items = conn.queryMap(sql, null);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int year = cal.get(Calendar.YEAR);
		int mon = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int created = year*10000+mon*100+day;
		if (!items.isEmpty()) {
			int adid=0,type=0,active_num=0,click_num=0;
			float active_count=0,click_unique=0, click_count=0;
			for (Map<String,Object> vals : items) {
				try {
					adid = (int)vals.get("adid");
					type = (int)vals.get("adtype");
					active_num = (int)vals.get("active_num");
					click_num = (int)vals.get("click_num");
					
					DataSource ds = SQLConnection.getDataSource(Define.DATA_SOURCES[type]);
					String prefix = ds == null ? "" : ds.getPrefix();
					String table = String.format("%s_day_%d", prefix, created/10000);
					
					String querySql = String.format(QUERY_STRING, table, adid, created);
					SQLConnection countConn = SQLConnection.getInstance(Define.DATA_SOURCES[type]);
					
					Map<String,Object> countVals = countConn.queryOneMap(querySql, null);
					if(countVals != null) {
						active_count = (float) countVals.get("active_count");
						click_count =  (float)countVals.get("click_count");	
						
						float rate = active_count / click_count;
						int r = (int) (rate * 100);
						
						LOG.info(String.format("adid=%s, active_count=%s, click_count=%s", adid,active_count,click_count));
						jedis.hset("DATA_ACTIVE_RATE", String.valueOf(adid), String.format("%d", r));
					}
					countConn.close();
				} catch (Exception e) {
					LOG.info(String.format("rate ad exception " + adid + " active : %d/%d click: %d/%d", active_count, active_num, click_unique, click_num));
					LOG.debug(String.format("rate ad exception " + adid + " reason : %s",e.getMessage()));
				}
			}
		}
		RedisConnection.close("main", jedis);
		conn.close();
	}
	
	public static void main(String[] args) {
		syncAdRateByCount();
	}
}
