package com.dianru.analysis.tools;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.process.util.RemainActiveUtil;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.SQLConnection.DataSource;
import com.dianru.analysis.util.StringUtils;

//按广告计划停止广告

public class MinuteAdsPlan extends MinuteAdsPlanUtil{

	public final static String QUERY_STRING = "SELECT sum(click_count) as click_count,sum(click_unique) as click_unique, sum(active_count) as active_count FROM %s WHERE adid=%d AND created=%d GROUP BY adid,created";
	private final static String EXPIRE_QUERY_STRING = "SELECT ads.adid FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND ads.state=4 AND UNIX_TIMESTAMP()>ad_plan.`end`";
	private final static String EXPIRE_UPDATE_STRING = "UPDATE ads SET ads.state=5,ads.update_time=UNIX_TIMESTAMP() WHERE ads.adid=?";

	public static Logger LOG = LogManager.getLogger(MinuteAdsPlan.class);

	// 到时间停止
	private static void stopAdByTime() {
		
		SQLConnection conn = SQLConnection.getInstance("main");
		List<Map<String, Object>> items = conn.queryMap(EXPIRE_QUERY_STRING,
				null);
		if (items != null && items.size() > 0) {
			for (Map<String, Object> vals : items) {
				int obj = (int) vals.get("adid");

				int result = conn.update(EXPIRE_UPDATE_STRING,
						new Object[] { obj });
				LOG.info("stop ad " + obj + " by begin end result : " + result);
			}
		}
		conn.close();
	}

	// 点击控量
	public static void pauseAdByCount(int pAdid) {
		
		SQLConnection conn = null;
		Jedis rjedis = null;
		Jedis jedis = null;
		try {
			conn = SQLConnection.getInstance("main");
			jedis = RedisConnection.getInstance("main");
			rjedis = RedisConnection.getInstance("control");
			
			String sql = "SELECT ads.adid as adid, ads.state as state, ads.type as adtype, ad_plan.num active_num, ad_plan.click_num as click_num, is_hs_flag, is_hs_report, deliveryType FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND ads.state=4 AND (ad_plan.num >= 0 OR ad_plan.click_num>0)";
			if(pAdid > 0) {
				sql = "SELECT ads.adid as adid, ads.state as state, ads.type as adtype, ad_plan.num active_num, ad_plan.click_num as click_num, is_hs_flag, is_hs_report, deliveryType FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND (ad_plan.num >= 0 OR ad_plan.click_num>0) AND ads.adid = " + pAdid;
			}
			
			List<Map<String, Object>> items = conn.queryMap(sql, null);
			int created = DateUtils.getYYYYMMDD(new Date());
			if (!items.isEmpty()) {
				for (Map<String, Object> vals : items) {
					int adid = 0 , state = 0, type = 0, active_num = 0, click_num = 0, active_count = 0, click_unique = 0, is_hs_flag = 0, deliveryType = 0; //, is_hs_report = 0
					SQLConnection countConn = null;
					try {
						adid = (int) vals.get("adid");
						type = (int) vals.get("adtype");
						active_num = (int) vals.get("active_num");
						click_num = (int) vals.get("click_num");
						is_hs_flag = (int) vals.get("is_hs_flag");
						//is_hs_report = (int) vals.get("is_hs_report");
						deliveryType = (int) vals.get("deliveryType");
						state = (int) vals.get("state");
						
						DataSource ds = SQLConnection
								.getDataSource(Define.DATA_SOURCES[type]);
						String prefix = ds == null ? "" : ds.getPrefix();
						String table = String.format("%s_ad_day_%d", prefix,
								created / 10000);
						
						String querySql = String.format(QUERY_STRING, table, adid,created);
						countConn = SQLConnection.getInstance(Define.DATA_SOURCES[type]);
						Map<String, Object> countVals = countConn.queryOneMap(querySql, null);
						if (countVals != null) {
							active_count = (int) (float) countVals.get("active_count");
							click_unique = (int) (float) countVals.get("click_unique"); // 独立
						}
						
						//如果是启动状态广告，放行重新计算剩余量。否则剩余量为0
						if(state != 4){
							String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
							String field = String.valueOf(adid);
							if(active_count > 0 && jedis.hexists(key, field)){
								String rm = jedis.hget(key, field);
								int t = Integer.parseInt(rm.split(",")[1]);
								//int t = (active_num - active_count > 0 ? active_count  : active_num);
								jedis.hset(key, field, String.format("%s,%s", 0,t));
								LOG.debug(String.format("pause ad state : %d,%s,%s,%d",adid, 0,t,state));
							}else{
								jedis.hset(key, field, String.format("%s,%s", 0,0));
								LOG.debug(String.format("pause ad state : %d,%s,%s,%d",adid, 0,0,state));
							}
							continue;
						}
						
						if ((active_num > 0 && active_count >= active_num) || (click_num > 0 && click_unique >= click_num)) {
							
							/*
							//删除超时IDFA   20160224_IDFA_WAIT_ACTIVE_16275
							String rkey =  DateUtils.getBeforeDate(0)+"_IDFA_WAIT_ACTIVE_"+adid;
							long sr = rjedis.del(rkey);
							LOG.debug(String.format("pause ad del : %s, %s", rkey, String.valueOf(sr)));
							*/
							
							//将广告剩余量设置成0
							String key = RemainActiveUtil.DATA_ACTIVE_REMAIN;
							String field = String.valueOf(adid);
							jedis.hset(key, field, String.format("%s,%s", 0,active_num));
							LOG.debug(String.format("pause ad update : %d,%d,%d,%d,%d",adid, 0 ,active_num, active_count, click_unique));
							
							DailyClean.setAdsStateToPause(adid);
							continue;
						}
						
						// 剩余激活数，已经激活数
						int remain = active_num - active_count > 0 ? active_num - active_count : 0;
						if (active_num == 0) {
							active_num = 1000000; // 默认不限量
							remain = active_num - active_count;
						}
						
						LOG.info("adid:" + adid + " remain active_count：" + remain);
						//最大不能超过限量active_count
						int tmp_count = active_count > active_num ? active_num : active_count;
						if (click_num > 0 && click_unique >= click_num) {
							remain = 0;
							tmp_count = active_count;
						}
						
						String remainStr = String.valueOf(remain + "," + tmp_count);
						if(is_hs_flag == 3 && deliveryType == 0) {
							if(pAdid > 0) {
								//快速任务，快速投放 
								//结合缓存计算激活数目
								int tmp = 0, ta = 0;
								String rm = jedis.hget(RemainActiveUtil.DATA_ACTIVE_REMAIN, String.valueOf(adid));
								if(rm == null || rm.length() == 0){
									tmp = tmp_count;
									ta = tmp;
								}else {
									tmp = Integer.parseInt(rm.split(",")[1]);
									ta = (active_num - tmp) <= 0 ? active_num :  tmp;
								}
								//计算余量
								int tr = (active_num - tmp) <= 0 ? 0 : (active_num - tmp) ;
								remainStr = String.valueOf(tr + "," + ta);
								Long hr = jedis.hset(RemainActiveUtil.DATA_ACTIVE_REMAIN, String.valueOf(adid), remainStr);
								LOG.debug(String.format("pause ad state : %d,%s,%d,res: %s",adid, remainStr,state, hr));
							}
						}else if(is_hs_flag == 3 && deliveryType == 1){ 	
							if(pAdid > 0) {
								//平滑投放广告
								jedis.hset(RemainActiveUtil.DATA_ACTIVE_REMAIN, String.valueOf(adid), String.format("%s,%s", 0,active_num));
							}
						}else{
							//上线时候，去掉click控量，将下面代码放入else中
							jedis.hset("DATA_ACTIVE_REMAIN", String.valueOf(adid), remainStr);
						}
					} catch (Exception e) {
						e.printStackTrace();
						LOG.info(String.format("pause ad exception " + adid
								+ " active : %d/%d click: %d/%d", active_count,
								active_num, click_unique, click_num));
						LOG.debug(String.format("pause ad exception " + adid
								+ " reason : %s", e.getMessage()));
					}finally{
						if(countConn != null) countConn.close();
					}
				}
			}
		} catch (Exception e) {
			LOG.error("pauseAdByCount error : " + e.getMessage());
		}finally{
			if(jedis != null) RedisConnection.close("main", jedis);
			if(rjedis != null) RedisConnection.close("control", rjedis);
			if(conn != null) conn.close();
		}
	}
	
	/**更新redis中的剩余激活量,完成激活量   并修改广告dump状态**/
	public static void dumpRemainActive(Set<String> set) {

		if(set == null || set.size() == 0) return;
		
		boolean flag = true;
		String in = " and ads.adid in(";
		for(String s : set){
			if(flag){
				in += s;
			}else{
				in += "," + s;
			}
			flag = false;
		}
		in += ")";
		
		SQLConnection conn = null;
		Jedis jedis = null;
		try {
			conn = SQLConnection.getInstance("main");
			jedis = RedisConnection.getInstance("main");
			String sql = "SELECT ads.adid as adid, ads.type as adtype, ad_plan.num active_num,"
					+ " ad_plan.click_num as click_num FROM ads,ad_plan WHERE ads.adid=ad_plan.adid "
					+ "AND ads.state=4 AND (ad_plan.num >= 0 OR ad_plan.click_num>0)"
					+ in;
			List<Map<String, Object>> items = conn.queryMap(sql, null);
	
			int created = DateUtils.getYYYYMMDD(new Date());
			if (!items.isEmpty()) {
				int adid = 0, type = 0, active_num = 0, click_num = 0, active_count = 0, click_unique = 0;
				for (Map<String, Object> vals : items) {
					try {
						adid = (int) vals.get("adid");
						type = (int) vals.get("adtype");
						active_num = (int) vals.get("active_num");
						click_num = (int) vals.get("click_num");
	
						DataSource ds = SQLConnection.getDataSource(Define.DATA_SOURCES[type]);
						String prefix = ds == null ? "" : ds.getPrefix();
						String table = String.format("%s_ad_day_%d", prefix, created / 10000);
						
						String querySql = String.format(QUERY_STRING, table, adid,created);
						SQLConnection countConn = SQLConnection.getInstance(Define.DATA_SOURCES[type]);
						Map<String, Object> countVals = countConn.queryOneMap(querySql, null);
						if (countVals != null) {
							active_count = (int) (float) countVals.get("active_count");
							click_unique = (int) (float) countVals.get("click_unique"); // 独立
	
							// 剩余激活数，已经激活数
							int remain = active_num - active_count > 0 ? active_num - active_count : 0;
							if (active_num == 0) {
								active_num = 1000000; // 默认不限量
								remain = active_num - active_count;
							}
							LOG.info("adid:" + adid + " remain active_count："+ remain);
	
							int tmp_count = active_count > active_num ? active_num : active_count;
							if (click_num > 0 && click_unique >= click_num) {
								remain = 0;
								tmp_count = active_count;
							}
							String remainStr = String.valueOf(remain + ","+ tmp_count);
							
							RemainActiveUtil activeUtil = RemainActiveUtil.getInstance();
							jedis.hset(RemainActiveUtil.DATA_ACTIVE_REMAIN, String.valueOf(adid),remainStr);
							
							//将广告的dump标示改为1
							activeUtil.setRemainActiveAdState(adid,1);
						}
						countConn.close();
					} catch (Exception e) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("dump remain update " + e.getMessage());
		}finally{
			if(jedis != null ){
				RedisConnection.close("main", jedis);
			}
			if(conn != null) {
				conn.close();
			}
		}
	}

	// 到一定量，更换关键词
	public static void changeKeywordsByCount() {
		
		SQLConnection conn = SQLConnection.getInstance("main");
		if(conn==null) return;
		
		int created = DateUtils.getYYYYMMDD(new Date());
		
		// 1.查询出启动或者调试的广告的adid,type,options,jobs,keywords,待变换的keywords
		String sql1 = "select ext.adid , ext.count_keywords , ads.type, ads.options, ads.jobs, ads.keywords "
				+ "from ads_extended as ext ,ads as ads "
				+ "where ext.adid = ads.adid and (ads.state = 4 or ads.state = 7) and ext.count_keywords <> ''";
		List<Map<String, Object>> items = conn.queryMap(sql1, null);
		for (Map<String, Object> map : items) {
			try {
				String count_keywords = map.get("count_keywords").toString();
				if (count_keywords == null || count_keywords.length() == 0
						|| count_keywords.equals("[]")) {
					continue;
				}
				
				String adid = map.get("adid").toString();
				if("".equals(map.get("type"))) 
					continue;
				int type = Integer.parseInt(map.get("type").toString());
				String options = map.get("options").toString();
				String jobs = map.get("jobs").toString();
				
				String keywords = map.get("keywords").toString();
				keywords = StringUtils.decodeUnicode(keywords);
				String tmpKeyWords = keywords;
				
				DataSource ds = SQLConnection.getDataSource(Define.DATA_SOURCES[type]);
				String prefix = ds == null ? "" : ds.getPrefix();
				String datebase = "cpc".equals(prefix) ? "dianru_cpc": "dianru_cpa";
				String table = String.format("%s.%s_ad_day_%d", datebase,prefix, created / 10000);
				
				// 2.广告当天的激活量和点击量	
				String queryActiveCount = "SELECT sum(active_count) as active_count,sum(click_count) as click_count "
											+ "FROM %s WHERE adid=%s and created = %s GROUP BY adid,created";
				queryActiveCount = String.format(queryActiveCount, table, adid, created);
				Map<String, Object> result = conn.queryOneMap(queryActiveCount, null);
				if (result == null)
					continue;
				if("".equals(result.get("active_count")) || "".equals(result.get("click_count"))) 
					continue;
				int a_count = (int) (float) result.get("active_count");
				int c_count = (int) (float) result.get("click_count");
				String sql3 = "";
				JSONArray jsonArray = new JSONArray(count_keywords);
				for (int i = 0; i < jsonArray.length(); i++) {
					
					String jsonStr = jsonArray.get(i).toString();
					//激活,点击控制量
					int num = 0;
					int cNum = 0;
					JSONObject obj = new JSONObject(jsonStr);
					if(obj != null && !obj.toString().isEmpty()){
						try {
							num = Integer.parseInt(obj.get("key_count").toString());
							cNum = Integer.parseInt(obj.get("click_count").toString());
						} catch (Exception e) {}
					}else{
						continue;
					}
					
					String words = obj.get("key_words").toString();
					words = StringUtils.decodeUnicode(words);
					
					String key_pos = obj.get("key_pos").toString();
					
					// 3.激活或者点击达到量，替换关键词,展示位
					if ((a_count >= num) || (c_count >= cNum)) {
						JSONObject opJson = new JSONObject(options);
						Object text2Obj = opJson.get("text2");
						if(text2Obj != null && !text2Obj.toString().isEmpty()){
							String text2 = text2Obj.toString();
							text2 = text2.replaceAll(tmpKeyWords, words);
							opJson.put("text2", text2);
						}
						options = StringUtils.decodeUnicode(opJson.toString());
						
						jobs = jobs.replaceAll(tmpKeyWords, words);
						jobs = StringUtils.decodeUnicode(jobs);
						
						tmpKeyWords = words;
						String update_time = String.valueOf(new Date().getTime() / 1000);
						sql3 = String.format("update ads set keywords = '%s',options = '%s',jobs = '%s',"
											+ "aso_pos = %s,update_time = %s where adid = %s",
											tmpKeyWords, options,jobs,key_pos,update_time, adid);
					}
				}
				
				if(sql3.length() > 0 && !keywords.equals(tmpKeyWords)) {
					int row = conn.execute(sql3, null);
					LOG.debug("ads:" + adid + " keywords:" + tmpKeyWords + ",active_count:" + a_count + ", row : " + row);
				}
			} catch (Exception e) {
				LOG.error("change keywords fail" + e.getMessage());
				continue;
			}
		}
		conn.close();
	}
	
	/**
	 * 每天凌晨23:59分钟，将所有关键词替换成第一个
	 */
	public static void firstKeywords(){
		
		//时间判断
		int hour = DateUtils.getHour();
		int min = DateUtils.getMin();
		if(!(hour == 23 && min == 59)){
			return;
		}
		
		//将所有关键词替换成第一个
		SQLConnection conn = null;
		try{
			conn = SQLConnection.getInstance("main");
			//查询出启动或者调试的广告的adid,options,jobs,keywords,待变换的keywords
			String query = "select ext.adid , ext.count_keywords , ads.options, ads.jobs, ads.keywords "
					+ "from ads_extended as ext ,ads as ads "
					+ "where ext.adid = ads.adid and (ads.state = 4 or ads.state = 7) and ext.count_keywords <> ''";
			List<Map<String, Object>> items = conn.queryMap(query, null);
			
			for (Map<String, Object> map : items) {
				String count_keywords = map.get("count_keywords").toString();
				if (count_keywords == null || count_keywords.length() == 0 || count_keywords.equals("[]")) {
					continue;
				}
				
				String adid = map.get("adid").toString();
				String options = map.get("options").toString();
				String jobs = map.get("jobs").toString();
				
				String keywords = map.get("keywords").toString();
				keywords = StringUtils.decodeUnicode(keywords);
				
				JSONArray jsonArray = new JSONArray(count_keywords);
				
				//第一个关键词
				String jsonStr = jsonArray.get(0).toString();
				if(jsonStr == null || jsonStr.length() == 0) 
					continue;
				
				JSONObject obj = new JSONObject(jsonStr);
				String words = obj.get("key_words").toString();
				words = StringUtils.decodeUnicode(words);
				
				String key_pos = obj.get("key_pos").toString();
				
				//还原jobs ,options, 关键词,展示位
				JSONObject opJson = new JSONObject(options);
				Object text2Obj = opJson.get("text2");
				if(text2Obj != null && !text2Obj.toString().isEmpty()){
					String text2 = text2Obj.toString();
					text2 = StringUtils.decodeUnicode(text2);
					text2 = text2.replaceAll(keywords, words);
					opJson.put("text2", text2);
				}
				options = StringUtils.decodeUnicode(opJson.toString());
				
				jobs = jobs.replaceAll(keywords, words);
				jobs = StringUtils.decodeUnicode(jobs);
				
				keywords = words;
				String update_time = String.valueOf(new Date().getTime() / 1000);
				
				String firstKeyWords = "update ads set keywords = '%s' , options = '%s', jobs = '%s', aso_pos = %s, update_time = %s where adid = %s";
				firstKeyWords = String.format(firstKeyWords, keywords, options,jobs,key_pos,update_time, adid);
				conn.execute(firstKeyWords, null);
				
				LOG.debug("change to first keywords,adid:" + adid + ",keywords :" + keywords);
			}
		}catch(Exception e){
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}
	
	public static void main(String[] args) {
		// 点击控量，不要开启
		//--------pauseAdByCount();
		
		// 计划时间停止
		stopAdByTime();
		// 到一定量，更换关键词
		changeKeywordsByCount();
		//firstKeywords
		firstKeywords();
		//控制平滑投放广告
		pauseAdByHourCount();
		//检查激活超时
		checkActiveTimeOut();
	}
}
