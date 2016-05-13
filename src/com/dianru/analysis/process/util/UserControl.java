package com.dianru.analysis.process.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.RedisConnection;

import redis.clients.jedis.Jedis;

/***
 * 频次控制
 * */
public class UserControl {
	public static Logger LOG = LogManager.getLogger(UserControl.class);
	//控制天展示
	public static final String CONTROL_DAY_SHOW = "CONTROL_DAY_SHOW_";
	//控制小时展示
	public static final String CONTROL_HOUR_SHOW = "CONTROL_HOUR_SHOW_";
	//控制天点击
	public static final String CONTROL_DAY_CLICK = "CONTROL_DAY_CLICK_";
	//控制小时点击
	public static final String CONTROL_HOUR_CLICK = "CONTROL_HOUR_CLICK_";
	
	/**
	 * @param type : 1展示 2点击
	 * */
	public static boolean checkTimesDay(int adid,String idfa,int times,int type){
		boolean finish = false;
		Jedis jedis = RedisConnection.getInstance("freq");
		String key = "";
		if(type == 1){
			key = CONTROL_DAY_SHOW + adid + "_" + DateUtils.getYYYYMMDD();
		}else if(type == 2){
			key = CONTROL_DAY_CLICK + adid + "_" + DateUtils.getYYYYMMDD();
		}
//		Redis Hincrby 命令用于为哈希表中的字段值加上指定增量值。增1是因为确实增加1了
		Long count = jedis.hincrBy(key, idfa, 1);
		
		if(count >= times){
			finish = true;
		}
		RedisConnection.close("freq", jedis);
		LOG.info(String.format("Show UserControl check adid=%s,showTimesType=%s,showTimes:%s,idfa=%s,count=%s,times=%s"
				,adid,type,times,idfa,count,times));
		return finish;
	}
	
	/**
	 * @param type : 1展示 2点击
	 * */
	public static boolean checkTimesHour(int adid,String idfa,int times,int type){
		boolean finish = false;
		Jedis jedis = RedisConnection.getInstance("freq");
		String key = "";
		if(type == 1){
			key = CONTROL_HOUR_SHOW + adid + "_" + DateUtils.getYYYYMMDD() + "_" + DateUtils.getHour();
		}else if(type == 2){
			key = CONTROL_HOUR_CLICK + adid + "_" + DateUtils.getYYYYMMDD() + "_" + DateUtils.getHour();
		}
		Long count = jedis.hincrBy(key, idfa, 1);
		if(count >= times){
			finish = true;
		}
		RedisConnection.close("freq", jedis);
		LOG.info(String.format("Show UserControl check adid=%s,showTimesType=%s,showTimes:%s,idfa=%s,count=%s,times=%s"
				,adid,type,times,idfa,count,times));
		return finish;
	}
	
	/**
	 * 控制展示频次
	 * 2016年3月4日 下午6:45:32
	 * @param ad  广告
	 * @param idfa  用户ID
	 */
	public static void controlShow(Ads ad,String idfa){
		if(ad.getDataType() < 3)  return;
		
		try {
			String options = ad.getOptions();
			int adid = ad.getAdid();
			JSONObject json = new JSONObject(options);
			if(!json.has("show_times_type") || !json.has("show_times")){
				LOG.info(String.format("adid=%s,don't have show_times_type or showTimes arguments"
						,adid));
				return;
			}	
			Object showTypeObj = json.get("show_times_type");
			Object showTimesObj = json.get("show_times");
			//如果广告没有设置频次限制 则不做操作
			if(showTypeObj==null || showTimesObj==null){
				LOG.error(String.format("adid=%s,showTimes or showType is null"
						,adid));
				return;
			}
			if("".equals(showTypeObj.toString()) || "".equals(showTimesObj.toString()) ){
				LOG.error(String.format("adid=%s,showTimes or showType is empty"
						,adid));
				return;
			}
			int showTimesType = Integer.parseInt(showTypeObj.toString());
			int showTimes = Integer.parseInt(showTimesObj.toString());
			//频次控制为零则表示不做限制
			if(showTimes==0){
				LOG.info(String.format("adid=%s,showTimes is 0"
						,adid));
				return;
			}
			if(showTimesType == 1){ //按天控制
				UserControl.checkTimesDay(adid, idfa, showTimes, 1);
			}else if(showTimesType == 2){ //按小时控制
				UserControl.checkTimesHour(adid, idfa, showTimes, 1);
			}else{ //默认
				UserControl.checkTimesHour(adid, idfa, 5, 1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("UserControl show:"+e.getMessage());
		}
		
	}
	/**
	 * 控制点击频次
	 * 2016年3月4日 下午6:45:32
	 * @param ad  广告
	 * @param idfa  用户ID
	 */
	public static void controlClick(Ads ad,String idfa){
		if(ad.getDataType() < 3)  return;
		
		try{
			String options = ad.getOptions();
			int adid = ad.getAdid();
			JSONObject json = new JSONObject(options);
			if(!json.has("click_times_type") || !json.has("click_times")){
				LOG.info(String.format("adid=%s,don't have click_times_type or click_times arguments"
						,adid));
				return;
			}	
			Object clickTypeObj = json.get("click_times_type");
			Object clickTimesObj = json.get("click_times");
			//如果广告没有设置频次限制 则不做操作
			if(clickTypeObj==null || clickTimesObj==null){
				LOG.error(String.format("adid=%s,clickTimes or clickType is null"
						,adid));
				return;
			}
			if("".equals(clickTypeObj.toString()) || "".equals(clickTimesObj.toString()) ){
				LOG.error(String.format("adid=%s,clickTimes or clickType is empty"
						,adid));
				return;
			}
			int clickTimesType = Integer.parseInt(clickTypeObj.toString());
			int clickTimes = Integer.parseInt(clickTimesObj.toString());
			//频次控制为零则表示不做限制
			if(clickTimes==0){
				LOG.info(String.format("adid=%s,clickTimes is 0"
						,adid));
				return;
			}
			if(clickTimesType == 1){ //按天控制
				UserControl.checkTimesDay(adid, idfa, clickTimes, 2);
			}else if(clickTimesType == 2){ //按小时控制
				UserControl.checkTimesHour(adid, idfa, clickTimes, 2);
			}else{ //默认
				UserControl.checkTimesHour(adid, idfa, 3, 2);
			}
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("UserControl show:"+e.getMessage());
		}
		
	}
	public static void cleanYesterdayKey(){
		/*Jedis jedis = RedisConnection.getInstance("values");
		String yesterday = StringUtils.getBeforeDate(1);
		Set<String> keys = jedis.keys("*" + yesterday + "*");
		if(keys == null || keys.isEmpty()) return ;
		for(String key : keys){
			jedis.del(key);
		}
		RedisConnection.close("values", jedis);*/
	}
}
