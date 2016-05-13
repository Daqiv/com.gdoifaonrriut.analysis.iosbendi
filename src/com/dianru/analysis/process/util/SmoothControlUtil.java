package com.dianru.analysis.process.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.tools.MinuteAdsPlanUtil;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

import redis.clients.jedis.Jedis;

/**
 * @author 
 * 		chenjun
 * @category 
 * 		平滑控制：<br>
 * 		1.每天只计算一次平滑广告在每天每小时的投放量，值存redis，0表示未生效，1表示生效<br>
 * 		2.把条件1中状态为0的量，设置到整体精准控量的redis值中，把状态改为1，控量的逻辑走整体控量的逻辑<br>
 * **/
public class SmoothControlUtil {
	
	public static Logger LOG = LogManager.getLogger(SmoothControlUtil.class);
	public static int[] MODEL = {4,3,2,1,1,2,2,3,3,4,5,5,5,10,10,10,5,5,5,5,4,3,2,1};
	
	/**是否是平滑投放的广告**/
	public static boolean isSmoothAd(int adid){
		boolean result = false;
		SQLConnection conn = null;
		try {
			conn = SQLConnection.getInstance("main");
			String sql = "select deliveryType from ad_plan where adid=%s";
			sql = String.format(sql, adid);
			Object one = conn.queryOne(sql, null);
			if("1".equals(one)){
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(conn != null) conn.close();
		}
		return result;
	}
	
	/***
	 *是否存在平滑投放的广告
	 * */
	public static boolean existsSmoothAd(int adid){
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = MinuteAdsPlanUtil.DATA_SMOOTH_PREFIX + DateUtils.getBeforeDate(0);
			String field = String.valueOf(adid);
			if(jedis.hexists(key, field)){
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
		return result;
	}
	
	public static boolean delSmoothAd(int adid){
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			
			String today = DateUtils.getBeforeDate(0);
			String key = MinuteAdsPlanUtil.DATA_SMOOTH_PREFIX + today;
			String fKey = MinuteAdsPlanUtil.DATA_SMOOTH_FLAG_PREFIX + today;
			String field = String.valueOf(adid);
			if(jedis.hexists(key, field)){
				jedis.hdel(key, field);
				jedis.hdel(fKey, field);
			}
			LOG.debug("delSmoothAd adid : " + adid);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
		return result;
	}
	
	/**true:生效**/
	public static boolean checkHourFlag(int adid,int hour){
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = MinuteAdsPlanUtil.DATA_SMOOTH_FLAG_PREFIX + DateUtils.getBeforeDate(0);
			String val = jedis.hget(key, String.valueOf(adid));
			String[] vals = val.split(",");
			if(vals.length != 24){
				return result;
			}
			if("1".equals(vals[hour])){
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
		return result;
	}
	
	/**设置当前小时平滑投放量生效**/
	public static void setSmoothRemainActive(int adid){
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			
			//取当前小时平滑量
			String kn = MinuteAdsPlanUtil.DATA_SMOOTH_PREFIX + DateUtils.getBeforeDate(0);
			String v = jedis.hget(kn, String.valueOf(adid));
			String[] vs = v.split(",");
			if(vs.length != 24){
				return;
			}
			
			//设置平滑量
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
			String field = String.valueOf(adid);
			String num = vs[DateUtils.getHour()];
			String newLine = num + ",0";
			Long r = jedis.hset(key, field, newLine);
			LOG.info(String.format("setSmoothRemainActive adid=%s,num=%s,result=%s", adid,num,r));
			
			//设置生效
			String kf = MinuteAdsPlanUtil.DATA_SMOOTH_FLAG_PREFIX + DateUtils.getBeforeDate(0);
			String val = jedis.hget(kf, String.valueOf(adid));
			String[] vals = val.split(",");
			if(vals.length != 24){
				return;
			}
			vals[DateUtils.getHour()] = "1";
			String flags = "";
			boolean flag = true;
			for(String s : vals){
				if(flag){
					flags += s;
				}else{
					flags += "," + s;
				}
				flag = false;
			}
			MinuteAdsPlanUtil.setSmoothDeliveryFlag(adid, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(jedis != null) {
				RedisConnection.close("main", jedis);
			}
		}
	}
}
