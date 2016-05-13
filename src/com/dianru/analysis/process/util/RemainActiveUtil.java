package com.dianru.analysis.process.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.tools.MinuteAdsPlan;
import com.dianru.analysis.tools.MinuteAdsPlanUtil;
import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.RedisConnection;

/**
 * @author chenjun
 * @category 快速任务：第二种控量规则：<br>
 * 			  前置条件：快速任务，去掉点击控量
 *           1.剩余激活量小于设置的值时，启用第二种控量方式<br>
 *           2.每次jump减少剩余激活量，计入idfa列表和时间戳<br> 检测存在ADID的最新激活量，并重置为1
 *           3.每次active后，从idfa列表中移除对应的idfa<br>
 *           4.每分钟检测idfa列表中时间戳，超时的idfa数量逆增到剩余激活量，并从idfa列表中移除<br>
 *           5.监控 giveUp 队列，giveUp的idfa数量逆增到剩余激活量,并从idfa列表中移除<br>
 * */
public class RemainActiveUtil {

	public static Logger LOG = LogManager.getLogger(RemainActiveUtil.class);

	/** 剩余激活量的阀值 **/
	public static int LESS_REMAIN_ACTIVE = 1000;
	/** 等待激活超时时间 **/
	public static int TIME_OUT = 60 * 20; // 秒
	public static String DATA_ACTIVE_REMAIN = "DATA_ACTIVE_REMAIN";
	public static String DATA_ACTIVE_REMAIN2 = "DATA_ACTIVE_REMAIN2";
	
	/** redis key : 剩余激活量小于配置的阀值的广告 **/
	public static final String REMAIN_ACTIVE_AD = "REMAIN_ACTIVE_AD";
	/** redis key : 等待激活的idfa列表 **/
	public static final String IDFA_WAIT_ACTIVE_PREFIX = DateUtils.getBeforeDate(0) + "_IDFA_WAIT_ACTIVE_";
	
	public static final String IDFA_WAIT_ACTIVE_QUEUE_PREFIX = DateUtils.getBeforeDate(0) + "_IDFA_WAIT_QUEUE_ACTIVE_";
	public static final String IDFA_WAIT_ACTIVE_HASH_PREFIX = DateUtils.getBeforeDate(0) + "_IDFA_WAIT_HASH_ACTIVE_";
	
	static {
		Jedis jedis = null;
		try {
			Configuration conf = Configuration.getInstance();
			DATA_ACTIVE_REMAIN = conf.getProperty("key.active.remain", "DATA_ACTIVE_REMAIN");
			String s = conf.getProperty("active.less", "1000");
			String t = conf.getProperty("active.timeout", "1200");					//关联PHP checkremain 函数
			LESS_REMAIN_ACTIVE = Integer.parseInt(s);
			TIME_OUT = Integer.parseInt(t);
			
			// 清除昨天的idfa列表
			jedis = RedisConnection.getInstance("control");
			String cleanKey = DateUtils.getBeforeDate(1) + "_IDFA_WAIT_ACTIVE_*";
			Set<String> keys = jedis.keys(cleanKey);
			if(keys != null && keys.size() > 0){
				for (String key : keys) {
					jedis.del(key);
				}
			}
			//每天凌晨(00:00)删除 REMAIN_ACTIVE_AD，重新计算一天的控量
			int h = DateUtils.getHour();
			int m = DateUtils.getMin();
			if(h == 0 && m == 0) {
				jedis.del(REMAIN_ACTIVE_AD);
				LOG.debug("REMAIN_ACTIVE_AD DEL");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug("REMAIN_ACTIVE_AD DEL " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
	}
	
	private RemainActiveUtil() {}
	public static RemainActiveUtil getInstance() {
		return new RemainActiveUtil();
	}
	
	/** 检测剩余激活量是否小于配置的阀值 **/
	public boolean checkRemainActive(int num) {
		boolean result = false;
		if (num <= LESS_REMAIN_ACTIVE) {
			result = true;
		}
		return result;
	}

	/** 添加或移除剩余激活量小于配置的阀值的adid **/
	public void remainActiveAd(String operate, int adid) {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = REMAIN_ACTIVE_AD;
			String field = String.valueOf(adid);
			String value = "0"; // 新增是0，dump后设成1，并更新最新active_num
			if ("+".equals(operate)) {
				Long r = jedis.hset(key, field, value);
				LOG.info(String.format("addRemainActiveAd adid:%s,result:%s",adid, r));
			} else if ("-".equals(operate)) {
				if(jedis.hexists(key, field)){
					Long r = jedis.hdel(key, field);
					LOG.info(String.format("removeRemainActiveAd adid:%s,result:%s", adid, r));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("remainActiveAd error : " + e.getMessage());
		} finally {
			if(jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
	}

	/** 设置广告是否dump过的状态 0：false 1：true **/
	public void setRemainActiveAdState(int adid, int state) {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = REMAIN_ACTIVE_AD;
			String field = String.valueOf(adid);
			String value = String.valueOf(state); 		// 新增是0，dump后设成1，然后更新最新active_num
			Long r = jedis.hset(key, field, value);
			LOG.info(String.format("setRemainActiveAdDump adid:%s,result:%s", adid, r));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("setRemainActiveAdDump error : " + e.getMessage());
		} finally {
			RedisConnection.close("control", jedis);
		}
	}

	/** 是否是剩余激活量小于配置阀值的ad **/
	public boolean isRemainActiveAd(int adid) {
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = REMAIN_ACTIVE_AD;
			String val = jedis.hget(key, String.valueOf(adid));
			if ("1".equals(val)) {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("isRemainActiveAd error : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		return result;
	}
	
	/**
	 * 重置状态
	 * @param adid
	 */
	public static void reSetRemainActiveAd(int adid) {
		
		//删除已有数据，重新dump数据
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = REMAIN_ACTIVE_AD;
			String value = String.valueOf(adid);
			boolean exists = jedis.hexists(key, value);
			if(exists){
				jedis.hset(key, value, "0");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("isRemainActiveAd error : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		
		//删除平滑广告放量
		Jedis jedis2 = null;
		try {
			jedis2 = RedisConnection.getInstance("main");
			String key = MinuteAdsPlanUtil.DATA_SMOOTH_PREFIX + DateUtils.getBeforeDate(0);
			String field = String.valueOf(adid);
			if(jedis2.hexists(key, field)){
				jedis2.hdel(key, field);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis2 != null) RedisConnection.close("main", jedis2);
		}
	}

	/** 获取所有剩余激活量小于配置的阀值的广告 **/
	public Set<String> allRemainActiveAd() {
		Set<String> adids = new HashSet<String>();
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			adids = jedis.hkeys(REMAIN_ACTIVE_AD);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("isRemainActiveAd error : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		return adids;
	}
	
	public static int removeAcitveQueue(Jedis jedis, int adid, boolean isAll){
		
		/*
		String qkey = IDFA_WAIT_ACTIVE_QUEUE_PREFIX + adid; 
		String hkey = IDFA_WAIT_ACTIVE_HASH_PREFIX + adid; 
		int ctime = (int) (System.currentTimeMillis() / 1000);
		
		int len = 0;
		while (true) {
			String value = jedis.lindex(qkey, 0);
			if(value == null){
				break;
			}
			
			String arr[] = value.split(",");
			String idfa = arr[0];
			int itime = Integer.parseInt(arr[1]);
			
			String stime = jedis.hget(hkey, idfa);
			int utime = Integer.parseInt(stime);
			
			if(ctime - utime > RemainActiveUtil.TIME_OUT){
				jedis.lpop(qkey);
				jedis.hdel(hkey, idfa);
				len++;
				
				if(!isAll){
					return 1;
				}
			}
			
			if(ctime - itime > RemainActiveUtil.TIME_OUT){
				break;
			}
		}
		return len;
		*/
		return 0;
	}

	public void idfaWaitActiveQueue(String operate, int adid, String idfa) {
		/*
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String qkey = IDFA_WAIT_ACTIVE_QUEUE_PREFIX + adid; 
			String hkey = IDFA_WAIT_ACTIVE_HASH_PREFIX + adid; 
			String field = String.valueOf(idfa);
			String value = String.valueOf(System.currentTimeMillis() / 1000);
			
			Ads ad = AdsCache.getInstance().get(adid);
			if (ad == null) {
				return;
			}
			
			String queue = String.format("%s,%s", field,value);
			if ("+".equals(operate)) {
				if(jedis.hexists(hkey, field)){
					jedis.hset(hkey, field, value);
					jedis.rpush(qkey, queue);
					return;
				}
				
				if(jedis.hlen(hkey) >= ad.getNum()){
					int n = removeAcitveQueue(jedis, adid, false);
					if(n == 0) {
						return;
					}
				}
				
				jedis.rpush(qkey, queue);
				jedis.hset(hkey, field, value);
				
				RemainActiveUtil.updateRemainActive( adid, ad.getNum(), jedis.hlen(hkey).intValue());
				
//				LOG.info(String.format("addIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			} else if ("-".equals(operate)) {
				
				Long r = jedis.hdel(hkey, field);
				RemainActiveUtil.updateRemainActive( adid, ad.getNum(), jedis.hlen(hkey).intValue());
				
//				LOG.info(String.format("removeIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			} else if ("=".equals(operate)) {
				jedis.hset(hkey, field, String.valueOf(Integer.MAX_VALUE));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("idfaWaitActive error : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		*/
	}
	
	/** 将idfa添加或移除到等待激活的列表 **/
	public int idfaWaitActive(String operate, int adid, String idfa) {
		Jedis jedis = null;
		Long r = 0l;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = IDFA_WAIT_ACTIVE_PREFIX + adid; // REDIS KEY 新增时间天
			String field = String.valueOf(idfa);
			String value = String.valueOf(System.currentTimeMillis() / 1000);
			if ("+".equals(operate)) {
				r = jedis.hset(key, field, value);
				LOG.info(String.format("addIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			} else if ("-".equals(operate)) {
				r = jedis.hdel(key, field);
				LOG.info(String.format("removeIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			}
			return r.intValue();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("idfaWaitActive error : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		return r.intValue();
	}
	
	public int idfaWaitActive(Jedis jedis, String operate, int adid, String idfa) {
		Long r = 0l;
		try {
			String key = IDFA_WAIT_ACTIVE_PREFIX + adid; // REDIS KEY 新增时间天
			String field = String.valueOf(idfa);
			String value = String.valueOf(System.currentTimeMillis() / 1000);
			if ("+".equals(operate)) {
				r = jedis.hset(key, field, value);
				LOG.info(String.format("addIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			} else if ("-".equals(operate)) {
				r = jedis.hdel(key, field);
				LOG.info(String.format("removeIdfaWaitActive adid:%s,idfa:%s,result:%s", adid, idfa, r));
			}
			return r.intValue();
		} catch (Exception e) {
		}
		return r.intValue();
	}

	public String getRemainActive(int adid) {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
			String field = String.valueOf(adid);
//			String line = jedis.hget(key, field);
			String r = jedis.hget(key, field);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("reduceRemainActive : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("main", jedis);
			}
		}
		return "0,0";
	}
	
	public static void updateRemainActive(int adid, int max, int cur) {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN2;
			String field = String.valueOf(adid);
			
			String remain = String.format("%d,%d", max-cur, cur);
			Long r = jedis.hset(key, field, remain);
			LOG.info(String.format("adid : %d remainstr2:%s,res : %s", adid , remain, r));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("reduceRemainActive : " + e.getMessage());
		}finally {
			if (jedis != null) {
				RedisConnection.close("main", jedis);
			}
		}
	}
	
	/** 增加或减少剩余激活的数量 **/
	public int remainActive(String operate, int adid, int num) {
		Jedis jedis = null;
		Long r = 0l;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN; 
			String field = String.valueOf(adid);
			String line = jedis.hget(key, field);
			if(line.length() == 0 || line.split(",").length != 2) {
				LOG.error("remainActiveAd error : " + line + ", " + field);
				return r.intValue();
			}
			String newLine = "";
			if ("+".equals(operate)) {
				newLine = remainActiveOperate(operate, line, num);
			} else if ("-".equals(operate)) {
				newLine = remainActiveOperate(operate, line, num);
			}
			r = jedis.hset(key, field, newLine);
			LOG.info(String.format("adid : %d operate:%s,remainActive : %s,%s-->%s,result:%s", adid , operate, num, line, newLine, r));
			
			return  r.intValue();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("reduceRemainActive : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("main", jedis);
			}
		}
		return r.intValue();
	}
	
	public int remainActive(Jedis jedis, String operate, int adid, int num) {
		Long r = 0l;
		try {
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
			String field = String.valueOf(adid);
			String line = jedis.hget(key, field);
			if(line.length() == 0 || line.split(",").length != 2) {
				LOG.error("remainActiveAd error : " + line + ", " + field);
				return r.intValue();
			}
			String newLine = "";
			if ("+".equals(operate)) {
				newLine = remainActiveOperate(operate, line, num);
			} else if ("-".equals(operate)) {
				newLine = remainActiveOperate(operate, line, num);
			}
			r = jedis.hset(key, field, newLine);
			LOG.info(String.format("adid : %d operate:%s,remainActive : %s,%s-->%s,result:%s", adid , operate, num, line, newLine, r));
			return  r.intValue();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("reduceRemainActive : " + e.getMessage());
		}
		return r.intValue();
	}

	/** 广告剩余激活量的 + - 运算 **/
	private String remainActiveOperate(String operate, String line, int num) {
		String result = line;
		String[] valArr = line.split(",");
		if (valArr.length != 2){
			return result;
		}
		try {
			int remain = Integer.parseInt(valArr[0]);
			int actived = Integer.parseInt(valArr[1]);
			if ("+".equals(operate)) {
				if(actived > 0) {
					remain += num;
					actived = (actived - num < 0) ? 0 : (actived - num);
				}
			} else if ("-".equals(operate)) {
				if(remain > 0) {
					remain = (remain - num < 0) ? 0 : (remain - num);
					actived += num;
				}
			}
			result = String.valueOf(remain) + "," + String.valueOf(actived);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("remainActiveOperate " + e.getMessage());
		}
		return result;
	}
	
	private static String remainActiveOperate2(String operate, String line, int num) {
		String result = line;
		String[] valArr = line.split(",");
		if (valArr.length != 2){
			return result;
		}
		try {
			int remain = Integer.parseInt(valArr[0]);
			int actived = Integer.parseInt(valArr[1]);
			if ("+".equals(operate)) {
				if(actived > 0) {
					remain += num;
					actived = (actived - num < 0) ? 0 : (actived - num);
				}
			} else if ("-".equals(operate)) {
				if(remain > 0) {
					remain = (remain - num < 0) ? 0 : (remain - num);
					actived += num;
				}
			}
			result = String.valueOf(remain) + "," + String.valueOf(actived);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("remainActiveOperate " + e.getMessage());
		}
		return result;
	}

	/** 检查idfa列表中是否存在 **/
	public boolean isIdfaExists(int adid, String idfa) {
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			String key = IDFA_WAIT_ACTIVE_PREFIX + adid;
			String field = idfa;
			result = jedis.hexists(key, field);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("isIdfaExists : " + e.getMessage());
		} finally {
			if (jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		return result;
	}
	
	/** 放弃激活 **/
	public static void giveUp(String line) {
		
		String[] vals = line.split(",");
		if (vals.length != 2)
			return;
		
		int res = 0;
		RemainActiveUtil activeUtil = RemainActiveUtil.getInstance();
		try {
			int adid = Integer.parseInt(vals[0]);
			String idfa = vals[1].toLowerCase().replace("-", "");
			
			String result = "";
			Jedis jedis = null;
			try {
				jedis = RedisConnection.getInstance("control");
				
				String key = IDFA_WAIT_ACTIVE_PREFIX + adid;
				Ads ads = AdsCache.getInstance().get(adid);
				if(ads.getState() != 4) {
					return;
				}
				
				result = jedis.hget(key, idfa);
				
				if(result == null || result.length() == 0) {
					return;
				}
				
//				Long interval = System.currentTimeMillis() / 1000 - Integer.parseInt(result);
//				if(interval > RemainActiveUtil.TIME_OUT){
//					activeUtil.idfaWaitActive("-", adid, idfa);
//					return;
//				}
				
				// 将idfa从等待激活的列表中移除
				res = activeUtil.idfaWaitActive(jedis, "-", adid, idfa);
				
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("isIdfaExists : " + e.getMessage());
			} finally {
				if (jedis != null) {
					RedisConnection.close("control", jedis);
				}
			}
			
		
			if(res > 0) {
				// 增加剩余激活的数量
				activeUtil.remainActive("+", adid, 1);
			}
			LOG.info(String.format("active give up adid:%s,idfa:%s", adid, idfa));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("giveUp error : " + e.getMessage());
		}
	}

	/** dump内存数据 **/
	public static void dumpRemainActiveAd() {
		
		Set<String> adids = new HashSet<String>();
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("control");
			Map<String, String> map = jedis.hgetAll(REMAIN_ACTIVE_AD);
			// 1.查找所有未dump过的广告
			for (Iterator<Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> entry = it.next();
				if ("0".equals(entry.getValue())) {
					adids.add(entry.getKey());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("isRemainActiveAd error : " + e.getMessage());
		} finally {
			if(jedis != null) {
				RedisConnection.close("control", jedis);
			}
		}
		
		// 2更新redis中的RemainActive
		MinuteAdsPlan.dumpRemainActive(adids);
	}
	//获取在redis当中针对当前adid存在的余量
	public static int getRemain(int adid){
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
			String field = String.valueOf(adid);
			String line = jedis.hget(key, field);
			return Integer.parseInt(line.split(",")[0]);
		}catch(Exception e){
		}finally{
			if(jedis != null) RedisConnection.close("main", jedis);
		}
		return 0;
	}
	
	public static void count(String udid , String mac, Ads ad){
		try {
			//是否到达可投放状态，排除即将上线等广告
			if(ad.getState() != 4 || ad.getIsHsFlag() != 3) return;	//不是快速任务不用超时
			
			String[] hourArr = ad.getHours().split(",");
			int curHour = DateUtils.getHour();
			boolean flag = false;
			for(String hour : hourArr){
				try {
					int h = Integer.parseInt(hour);
					if(curHour == h){
						flag = true;
					}
				} catch (Exception e) {}
			}
			if(!flag){
				return;
			}
			
			//1.
			int adid = ad.getAdid();
			RemainActiveUtil activeUtil = RemainActiveUtil.getInstance();
			String idfa = udid.toLowerCase().replaceAll("-", "");
			if(!activeUtil.isIdfaExists(adid, idfa)){		
				//2.首次任务跳转才计数
				Jedis jedisProcess = null;
				String macNew = mac;
				int active_num = 0;
				try {
					jedisProcess = RedisConnection.getInstance("process");
					String keyProcess = "DATA_DEVICE_ADID";
					String field = String.format("%s%s%d",macNew , udid , adid);
					String info = jedisProcess.hget(keyProcess, field);
					if(info != null && info.length() > 0) {
						String[] infoArr = info.split(",");
						if("1".equals(infoArr[0])){
							active_num = -1;
						}else if(!("0".equals(infoArr[2]))){
							active_num = Integer.parseInt(infoArr[2]);
						}
					}
				} catch (Exception e) {
					LOG.debug(String.format("update redis first mac=%s udid=%s adid=%d active_num=%s exception=%s", macNew, udid, adid, active_num, e.getMessage()));
				}finally{
					if(jedisProcess != null)RedisConnection.close("process",jedisProcess);
				}
				
				if(active_num == 0 && getRemain(adid) > 0){
					//添加至idfa激活等待列表
					int res = activeUtil.idfaWaitActive("+",adid, idfa);
					if(res > 0){
						//消耗剩余激活量
						activeUtil.remainActive("-",adid, 1);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("remain count error : " + e.getMessage());
		}
	}

	public static void main(String[] args) {

		remainActiveOperate2("-", "0,10", 1);
	}
}
