package com.dianru.analysis.tools;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.process.util.RemainActiveUtil;
import com.dianru.analysis.process.util.SmoothControlUtil;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.SQLConnection.DataSource;

/**平滑控制投放广告*/
public class MinuteAdsPlanUtil{

	public static Logger LOG = LogManager.getLogger(MinuteAdsPlanUtil.class);
	
	public static class Count{
		public int clickCount = 0;
		public int activeCount = 0;
		public int showCount = 0;
	}
	
	/**设置平滑投放量的redis的key*/
	public static String DATA_SMOOTH_PREFIX = "DATA_SMOOTH_";
	public static String DATA_SMOOTH_FLAG_PREFIX = "DATA_SMOOTH_FLAG_";
	
	static{
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = DATA_SMOOTH_PREFIX + DateUtils.getBeforeDate(1);
			String key2 = DATA_SMOOTH_FLAG_PREFIX + DateUtils.getBeforeDate(1);
			if(jedis.exists(key)){
				Long r = jedis.del(key);
				LOG.debug("clean redis key : " + key + ", result : " + r);
			}
			if(jedis.exists(key2)){
				Long r = jedis.del(key2);
				LOG.debug("clean redis key : " + key2 + ", result : " + r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
	}
	
	/**当前小时的流量分配比率*/
	private static float getFlowRate(int hour,int type,Map<Integer, Integer> hourMap){
		//缺省每个小时都能消耗完全
		float rate = 0f;
		int sum = 0;
		for (int tmp : hourMap.values()) {
			sum += SmoothControlUtil.MODEL[tmp];
		}
		rate = (float)SmoothControlUtil.MODEL[hour]/(float)sum;
		return rate;
	}
	
	/**
	 * 将小时字符串转为map
	 * */
	private static Map<Integer,Integer> parseHoursToMap(String hours){
		Map<Integer,Integer> hourMap = new HashMap<Integer,Integer> ();
		String[] hourArr = hours.split(",");
		
		int curHour = DateUtils.getHour();
		for(String hour : hourArr){
			try {
				int h = Integer.parseInt(hour);
				if(h<curHour)continue;	//跳过比当前小的小时
				
				hourMap.put(h,h);
			} catch (Exception e) {}
		}
		return hourMap;
	}
	
	/***
	 * 设置平滑投放的量
	 * */
	public static void setSmoothDeliveryNum(int adid,String num){
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = DATA_SMOOTH_PREFIX + DateUtils.getBeforeDate(0);
			Long r = jedis.hset(key, String.valueOf(adid), num);
			LOG.debug(String.format("setSmoothDeliveryNum adid:%s,num:%s,result:%s", adid,num,r));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
	}
	
	/***
	 * 设置每小时平滑投放的量生效的标示
	 * */
	public static void setSmoothDeliveryFlag(int adid,String flags){
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = DATA_SMOOTH_FLAG_PREFIX + DateUtils.getBeforeDate(0);
			Long r = jedis.hset(key, String.valueOf(adid), flags);
			LOG.debug(String.format("setSmoothDeliveryFlag adid:%s,num:%s,result:%s", adid,flags,r));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
	}

	/***
	 * 平滑控量
	 * */
	protected static void pauseAdByHourCount() {
		
		SQLConnection conn = null;
		SQLConnection countConn = null;
		String sql = "SELECT ads.adid as adid, ads.type as adtype, ads.state as state, ad_plan.num active_num, "
				+ "ad_plan.click_num as click_num,ad_plan.hours as hours FROM ads,ad_plan WHERE ads.adid=ad_plan.adid "
				+ "AND ad_plan.deliveryType =1 AND ads.state=4 AND (ad_plan.num >= 0 OR ad_plan.click_num>0) "
				+ "AND UNIX_TIMESTAMP() < ad_plan.end";
		
		try {
			//1.查询启动或留存并以平滑方式投放广告的控量
			conn = SQLConnection.getInstance("main");
			
			List<Map<String, Object>> items = conn.queryMap(sql, null);
			if (items != null && !items.isEmpty()) {
				for (Map<String, Object> vals : items) {
					@SuppressWarnings("unused")
					int adid = 0, type = 0, activeNum = 0, clickNum = 0, state = 0;
					String hours = "";
					
					adid = (int) vals.get("adid");
					type = (int) vals.get("adtype");
					activeNum = (int) vals.get("active_num");
					clickNum = (int) vals.get("click_num");
					state = (int) vals.get("state");
					hours = String.valueOf(vals.get("hours"));
					
					//没计算过小时投放量的
					if(!SmoothControlUtil.existsSmoothAd(adid)){
						
						//查询计算已消耗量
						int created = DateUtils.getYYYYMMDD(new Date());
						DataSource ds = SQLConnection
								.getDataSource(Define.DATA_SOURCES[type]);
						String prefix = ds == null ? "" : ds.getPrefix();
						String table = String.format("%s_ad_day_%d", prefix,
								created / 10000);
						
						String querySql = String.format(MinuteAdsPlan.QUERY_STRING, table, adid,created);
						countConn = SQLConnection.getInstance(Define.DATA_SOURCES[type]);
						Map<String, Object> countVals = countConn.queryOneMap(querySql, null);
						if (countVals != null) {
							int active_count = (int) (float) countVals.get("active_count");
							activeNum = (activeNum - active_count) <= 0 ? 0 : activeNum - active_count;
						}
						
						//需要投放的小时
						Map<Integer, Integer> hourMap = parseHoursToMap(hours);
						
						//初始化每小时投放量
						Map<Integer, Integer> hourNum = new HashMap<Integer, Integer>();
						
						//初始化是否投放生效的标示
						Map<Integer, Integer> hourFlag = new HashMap<Integer, Integer>();
						for(int i = 0; i <= 23; i++){
							hourNum.put(i, 0);
							hourFlag.put(i, 0);
						}
						
						//计算每小时的量
						for(int h : hourMap.keySet()){
							float rate = getFlowRate(h, type,hourMap);
							int num = new Float(activeNum * rate).intValue();
							hourNum.put(h, num);
						}
						
						//设置每小时投放量
						String nums = "";
						boolean flag = true;
						for(int num:hourNum.values()){
							if(flag){
								nums += String.valueOf(num);
							}else{
								nums += "," + num;
							}
							flag = false;
						}
						setSmoothDeliveryNum(adid, nums);
						
						//设置每小时投放量是否生效的标示
						String flags = "";
						boolean flag2 = true;
						for(int f:hourFlag.values()){
							if(flag2){
								flags += String.valueOf(f);
							}else{
								flags += "," + f;
							}
							flag2 = false;
						}
						setSmoothDeliveryFlag(adid, flags);
					}
					
					//当前小时未生效
					if(!SmoothControlUtil.checkHourFlag(adid, DateUtils.getHour())){
						//设置当前小时平滑投放量生效
						SmoothControlUtil.setSmoothRemainActive(adid);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(conn != null) conn.close();
			if(countConn != null) countConn.close();
		}
	}
	
	public static int queryAdCount(int type, int adid){
		
		int active_count = 0;
		SQLConnection countConn = null;
		try {
			int created = DateUtils.getYYYYMMDD(new Date());
			DataSource ds = SQLConnection
					.getDataSource(Define.DATA_SOURCES[type]);
			String prefix = ds == null ? "" : ds.getPrefix();
			String table = String.format("%s_ad_day_%d", prefix,
					created / 10000);
			
			String querySql = String.format(MinuteAdsPlan.QUERY_STRING, table, adid,created);
			countConn = SQLConnection.getInstance(Define.DATA_SOURCES[type]);
			Map<String, Object> countVals = countConn.queryOneMap(querySql, null);
			if (countVals != null) {
				active_count = (int) (float) countVals.get("active_count");
			}
		} catch (Exception e) {
		}finally{
			if(countConn != null) {
				countConn.close();
			}
		}
		
		return active_count;
	}
	
	
	/**检查激活超时**/
	protected static void checkActiveTimeOut(){
		
		
		RemainActiveUtil activeUtil = RemainActiveUtil.getInstance();
		Jedis jedis = null;
		Jedis jedisMain = null;
		try {
			jedis = RedisConnection.getInstance("control");
			jedisMain = RedisConnection.getInstance("main");
			
			Set<String> keys = jedis.keys("*");
			if(keys.size() <= 0) return;
			
			String adids = "";
			for(String key : keys){
				String [] arr = key.split("_");
				if(arr.length == 5) {
					int adid = Integer.parseInt(arr[4]);
					adids += adid + ",";
				}
			}
			
			adids = adids.substring(0, adids.length() - 1);
			if(adids.length() <= 0) return;
			
			Set<Integer> ckeys = new HashSet<Integer>();
			SQLConnection conn = null;
			try {
				conn = SQLConnection.getInstance("main");
				String sql = "SELECT a.adid, a.data_type, p.num, a.state from ads a inner join ad_plan p on a.adid = p.adid  where a.adid in ("+adids+")";
				List<Map<String,Object>> items = conn.queryMap(sql, null);
				if (!items.isEmpty()) {
					for (Map<String,Object> vals : items) {
						Ads ads = new Ads(vals);
						if(ads.getState() != 4) {
							ckeys.add(ads.getAdid());
							LOG.debug("check time out state : " + ads.getAdid() + " , " + ads.getState());
						}
						
						int active_count = MinuteAdsPlanUtil.queryAdCount(ads.getDataType(), ads.getAdid());
						if(active_count >= Integer.parseInt(vals.get("num").toString())){
							ckeys.add(ads.getAdid());
							LOG.debug("check time out num : " + ads.getAdid() + " , " + ads.getState());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
			
			//遍历等待激活的idfa列表
			for(String key : keys){
				String [] arr = key.split("_");
				if(arr == null || arr.length != 5) continue;
				
				int adid = Integer.parseInt(arr[4]);
				if(ckeys.contains(adid)) {
					continue;
				}
				Map<String, String> all = jedis.hgetAll(key);
				for(Iterator<Entry<String, String>> it = all.entrySet().iterator(); it.hasNext();){
					Entry<String, String> next = it.next();
					String idfa = next.getKey();
					String timeStr = next.getValue();
					Long t = Long.parseLong(timeStr);
					//判断是否超时
					Long interval = System.currentTimeMillis() / 1000 - t;
					if(t > 0 && interval > RemainActiveUtil.TIME_OUT){
						//将idfa从等待激活的列表中移除
						int res = activeUtil.idfaWaitActive(jedis, "-",adid, idfa);
						if(res > 0) {
							//增加剩余激活的数量
							activeUtil.remainActive(jedisMain, "+",adid, 1);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("checkActiveTimeOut error : " + e.getMessage());
		}finally{
			if(jedis != null) RedisConnection.close("control", jedis);
			if(jedisMain != null) RedisConnection.close("main", jedisMain);
		}
	}
	
	public static void main(String[] args) {
		checkActiveTimeOut();
	}
}
