package com.dianru.analysis.process.imps;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.bean.Cheat;
import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.bean.Media;
import com.dianru.analysis.bean.MediaApp;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;
import com.dianru.analysis.callback.UserScore;
import com.dianru.analysis.count.Counter;
import com.dianru.analysis.count.bean.CountValues;
import com.dianru.analysis.count.bean.DetailHourKeys;
import com.dianru.analysis.parse.imps.CallbackParser;
import com.dianru.analysis.process.CallbackProcessor;
import com.dianru.analysis.process.util.RemainActiveUtil;
import com.dianru.analysis.store.CachedValue;
import com.dianru.analysis.store.DBHistoryStore;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.IPCountStore;
import com.dianru.analysis.store.RedisStore;
import com.dianru.analysis.tools.Md5;
import com.dianru.analysis.util.DataSave;
import com.dianru.analysis.util.DataSave.DataSaveRole;
import com.dianru.analysis.util.JsonUtil;
import com.dianru.analysis.util.ListUtil;
import com.dianru.analysis.util.RandUtil;
import com.dianru.analysis.util.RedisConnection;

public class ActiveProcessor extends CallbackProcessor {

	public static Logger LOG = LogManager.getLogger(ActiveProcessor.class);
	
	@Override
	public List<Object>[] process(List<Object> vals) {
		
		int action = 0, type = 0, adid = 0, active_num = 0;
		String mac, udid, openudid;
		long disk = 0;
		
		action = ListUtil.getInt(vals, CallbackParser.Index.ACTION);
		adid = ListUtil.getInt(vals, CallbackParser.Index.ADID);
		mac = ListUtil.getString(vals, CallbackParser.Index.MAC);
		udid = ListUtil.getString(vals, CallbackParser.Index.UDID);
		openudid = ListUtil.getString(vals, CallbackParser.Index.OPENUDID);
		int source = ListUtil.getInt(vals, Index.FROM);
		type = ListUtil.getInt(vals, CallbackParser.Index.TYPE);
		try{
			active_num = ListUtil.getInt(vals, CallbackParser.Index.ACTIVE_NUM);
			disk = ListUtil.getLong(vals, CallbackParser.Index.DISK);
		}catch(Exception e) {}
		
		if (action == 0 || adid == 0) {
			LOG.trace(String.format("data item has error adid=%d action=%d type=%d", adid,action, type));
			return null;
		}
		if (active_num != 0) {
			LOG.trace(String.format("active active_num error adid=%d action=%d type=%d", adid,action, type));
			return null;
		}
		if (mac == null && udid == null && openudid == null) {
			LOG.debug(String.format("data item has error mac=%s udid=%s openudid=%s", mac,udid, openudid));
			return null;
		}
		
		int year = ListUtil.getInt(vals, Index.YEAR);
		int mon = ListUtil.getInt(vals, Index.MON);
		int day = ListUtil.getInt(vals, Index.DAY);
		int hour =  ListUtil.getInt(vals, Index.HOUR);

		
		Ads ad = AdsCache.getInstance().get(adid);
		if (ad == null) {
			LOG.debug("ad " + adid + " not found.");
			return null;
		}
		type = ad.getDataType();
		CallbackItem item = this.getHistory(year, mon, day, type, adid, mac, udid);
		// 不存在点击,已经激活
		if (item == null) {
			LOG.warn(String
					.format("Pre action not found for : action=%d adid=%d mac=%s udid=%s openudid=%s",
							action, adid, mac, udid, openudid));
			return null;
		}
		
		//点击和跳转都不存在
		if (item.action != Define.ACTION_CLICK && item.action != Define.ACTION_JUMP && item.action != Define.ACTION_ACTIVE) {
			LOG.warn(String
					.format("actived for : item.date=%d item.action=%d item.mac=%s item.udid=%s action=%d adid=%d mac=%s udid=%s openudid=%s",
							item.date, item.action, item.mac, item.udid ,action, adid, mac, udid, openudid));
			return null;
		}
		int created = this.datetime(vals).timestamp();

		//广告下线后，我们只接收广告主三天内的激活，给媒体下发只发1天内的激活
		//广告停止3天后不返数据，ad.getState() != 5  && ad.getState() != 8 &&
		if(ad.getState() != 4 && ad.getState() != 7 && (created - item.open_time) > 60*60*24*3) {
			LOG.warn(String
					.format("click timeout : item.date=%d item.action=%d item.mac=%s item.udid=%s action=%d adid=%d mac=%s udid=%s openudid=%s",
							item.date, item.action, item.mac, item.udid ,action, adid, mac, udid, openudid));
			return null;
		}
		
		item.cid = ad.getCid();

		int appid = item.appid;
		Media media = MediaCache.getInstance().get(appid);
		if (media == null) {
			LOG.debug("media " + item.appid + " not found.");
			return null;
		}
		
		int data_from = media.getType() == 1 ? 1 : 3;
		int ad_from = ad.getDataFrom();

		float income = 0;
		float cost = 0;
		
		//多次激活对应价格读取
		int len = 1;
		try {
			String json = ad.getJobs();
			JSONObject jsonObj = JsonUtil.getJson(json);
			JSONArray jobs = jsonObj.getJSONArray("jobs");
			len = jobs.length();
			if(active_num < 0 || active_num > len){ //兼容老版本sdk jobs.len = 0 的情况
				LOG.debug("job active_num error,num："+ active_num+" jobs:" + len);
				return null;
			}
			//获取当前完成的任务
			JSONObject job = (JSONObject) jobs.get(active_num);
			String billing = ad.getBilling();
			//callback的计价方式
			if (billing.indexOf('2') >= 0) {
				String sIncome = "0";
				if(jsonObj.get("income").toString().isEmpty() || jsonObj.get("income").toString().length() == 0){
					LOG.debug("active income error :  adid : " + adid);
				}else {
					sIncome = jsonObj.get("income").toString();
				}
				income = Float.parseFloat(sIncome);
				//如果单价为0 income接入单价callback
				if(income == 0){
					income = ad.getPriceCallbackIncome();
				}
				//获取媒体中的价钱，多次激活的价格
				cost = this.getPrice(appid, adid);
				if (cost == 0){
					cost = Float.parseFloat(job.get("const").toString());
					if(cost == 0){
						cost = ad.getPriceCallbackCost();
					}
				}
			}
		} catch (Exception e) {
			income = ad.getPriceCallbackIncome();
			cost = ad.getPriceCallbackCost();
			LOG.warn(String.format("active adid=%d,exception=%s", adid,e.getMessage()));
		}
		
		// 查找当前激活使用的关键词
		item.keywords = ad.getKeywords();
		
		//广告已经激活
		if(item.action == Define.ACTION_ACTIVE) {
			//广告由进程激活后广告主回调激活
			if((ad.getProcess_name() != null && !ad.getProcess_name().isEmpty()) && (item.process == Define.FROM_PROCESS || item.process == Define.FROM_API) && source != Define.FROM_PROCESS) {
				//上报数据但不计入广告主数据
				if(ad.getIsHsReport() == 2 || ad.getIsHsReport() == 0) {
					
					//广告主记费
					DetailHourKeys ck = DetailHourKeys.create(year, mon, day, hour, type,
						item.data_from, item.ad_from, item.appid, item.uid, item.adid, item.cid);
					
					LOG.warn(String.format("advertisers cover process ：action=%d, item.income=%.2f, income=%.2f" , action, item.income, income));
					CountValues cv = CountValues.create(action, 1, item.invalid > 0 ? 1 : 0, 0, 0, income, 0);
					Counter.getInstance().add(ck, cv);
					
					//更新激活来源
					item.process = Define.FROM_SDK;
					DBHistoryStore.getInstance(FileStore.STORE_STORE, item.date, type, Define.ACTION_CLICK).update(item);
				}
			}
			return null;
		} else {
			//找到的点击，判断历史是否已经激活
			boolean isHistoryActive = RedisStore.getInstance().isActive(item.mac + item.udid, adid);
			if(isHistoryActive){
				LOG.warn(String
						.format("history active exists : action=%d adid=%d mac=%s udid=%s openudid=%s", action, adid, mac, udid, openudid));
				return null;
			}
		}
		
		DateTime callback = datetime(vals);
		int interval = (int) (callback.timestamp() - item.open_time);
		long ipcount = 0;
		
		int date = this.date(vals);
		//同一IP同一广告IP数不得超过设定值（刘卿）
		ipcount = IPCountStore.getInstance().add("IP_" + date, action, adid, item.ip, 1);
		
		int invalid = 0;
		//IP 数量 对应数据库中的num字段
		int ipNum = ad.getIpNum();
		//获取间隔
		int intervalNum = ad.getInterval();
		// TODO: check boot time num
		// int boottimeNum = ad.getBootTimeNum();
		
		//ip 假量
		//media.type == 1 应用 2 渠道  media.state == 1 投放  2停止  3软删除 4测试（控制墙是否显示） 开发者行为
		if (media.getType() != 2 && media.getState() != 4) {
			if (ipNum > 0 && ipcount > ipNum) {
				invalid |= 1;
			}
		}
		
		//时间假量
		if (intervalNum > 0 && interval < intervalNum) {
			invalid |= 2;
		}
		
		//今日Appuserid已经激活该广告，假量
		if(item.appid == 7823) {
			LOG.trace("process item ischeat");
			boolean isCheat = RedisStore.getInstance().isCheat(item.adid, item.appid, item.appuserid, item.udid);
			if(isCheat){
				LOG.warn(String.format("appuserid today actived : action=%d adid=%d mac=%s udid=%s openudid=%s", action, adid, mac, udid, openudid));
				invalid |= 3;
			}
		}
		
		//是否启用防作弊	liuhuiya
		if(media.getIsEnable() == 0 ){ //启用
			
			//获取广告防作弊设置   ad by chenjun
			Map<String, Integer> ADS_OPTIONS = Cheat.parseAdOptions(ad.getOptions());
			int adRate = ADS_OPTIONS.get(Cheat.KEY_AD_RATE);
			boolean  isAdCheck = RandUtil.isRand(adRate);
			//如果没有相应字段或者设置为0--》
			if(adRate == 0){
				isAdCheck = true;
			}
	
			//获取媒体防作弊设置
			Map<String, Integer> OPTIONS = Cheat.parseMediaOptions(media.getOptions());
			int mRate = OPTIONS.get(Cheat.KEY_RATE);
			boolean  isCheck = RandUtil.isRand(mRate);
			//如果没有相应字段或者设置为0--》
			if(mRate == 0) {
				isCheck = true;
			}
			
			/**
			 * 激活DID防作弊判断针对广告和媒体
			 */
			if(invalid == 0 && ADS_OPTIONS.get(Cheat.KEY_SESSION) == 1){ //add by chenjun
				int didResult = RedisStore.getInstance().checkDidCheat(ad, udid);
				if(didResult > 0 && isAdCheck) {
					invalid = didResult;
				}
			}
			if(invalid == 0 && OPTIONS.get(Cheat.KEY_SESSION) == 1){
				int didResult = RedisStore.getInstance().checkDidCheat(ad, udid);
				if(didResult > 0 && isCheck) {
					invalid = didResult;
				}
			}
			
			/**
			 * 防作弊：SSID 激活判断
			 */
			if(invalid == 0 && ADS_OPTIONS.get(Cheat.KEY_SSID) == 1){ //add by chenjun
				int ssidResult = RedisStore.getInstance().checkSsidCheat(ad, udid);
				if(ssidResult > 0 && isAdCheck) {
					invalid = ssidResult;
				}
			}
			if(invalid == 0 && OPTIONS.get(Cheat.KEY_SSID) == 1){
				int ssidResult = RedisStore.getInstance().checkSsidCheat(ad, udid);
				if(ssidResult > 0 && isCheck) {
					invalid = ssidResult;
				}
			}
			
			/**
			 * 防作弊：OPENUDID 激活判断
			 */
			if(invalid == 0 && ADS_OPTIONS.get(Cheat.KEY_OPENUDID) == 1){ //add by chenjun
				int ssidResult = RedisStore.getInstance().checkOpenudidCheat(ad, udid);
				if(ssidResult > 0 && isAdCheck) {
					invalid = ssidResult;
				}
			}
			if(invalid == 0 && OPTIONS.get(Cheat.KEY_OPENUDID) == 1){
				int ssidResult = RedisStore.getInstance().checkOpenudidCheat(ad, udid);
				if(ssidResult > 0 && isCheck) {
					invalid = ssidResult;
				}
			}
			
			/**
			 * 防作弊软件激活的判断
			 */
			if(invalid == 0 && ADS_OPTIONS.get(Cheat.KEY_IG) == 1){ //add by chenjun
				boolean isCheat = RedisStore.getInstance().checkCheatSoft(item.mac, item.udid);
				if(isCheat) {
					invalid = 14;		
				}
			}
			if(invalid == 0 && OPTIONS.get(Cheat.KEY_IG) == 1){
				boolean isCheat = RedisStore.getInstance().checkCheatSoft(item.mac, item.udid);
				if(isCheat) {
					invalid = 14;		
				}
			}
			
			/**
			 * 磁盘空间变化
			 */
			if(invalid == 0 && disk > 0 && ADS_OPTIONS.get(Cheat.KEY_DISK) == 1){ //add by chenjun
				boolean flag = RedisStore.getInstance().checkActiveDisk(mac, udid, ad, disk);
				if(flag){
					invalid = 15;		
				}
			}
			if(invalid == 0 && disk > 0 && OPTIONS.get(Cheat.KEY_DISK) == 1) {
				boolean flag = RedisStore.getInstance().checkActiveDisk(mac, udid, ad, disk);
				if(flag){
					invalid = 15;		
				}
			}
			
			/**
			 * 进程异常
			 */
			if(invalid == 0 &&  ADS_OPTIONS.get(Cheat.KEY_PROCESS) == 1) {
				boolean isNormal = RedisStore.getInstance().checkNormalSoft(item.mac, item.udid);
				if(!isNormal && isAdCheck) {
					invalid = 18;		
				}
			}
			if(invalid == 0 &&  OPTIONS.get(Cheat.KEY_PROCESS) == 1) {
				boolean isNormal = RedisStore.getInstance().checkNormalSoft(item.mac, item.udid);
				if(!isNormal && isCheck) {
					invalid = 18;		
				}
			}
		}
		
		//超过24小时不返回数据，ad.getState() != 5 && ad.getState() != 8 && 
		if(ad.getState() != 4 && ad.getState() != 7 && (created - item.open_time) > 60*60*24) {
			invalid = 8;
		}
		
		item.invalid = item.invalid ==0 ? invalid : item.invalid;
		item.saved = item.saved == 0 ? (item.invalid>0?1:0) :item.saved;
		
		//乐抢评分浮动金额
		float ruleCost = 0f;
		try {
			JSONObject optJson = new JSONObject(ad.getOptions());
			if(optJson != null && optJson.get("float_money") != null) {
			
			float float_moeny = Float.parseFloat(optJson.get("float_money").toString());
				//前面读取任务的时候就计算出了cost和income变量
				//来源只要是sdk或者process之中其一即可
				if(cost > 0 && (source == Define.FROM_SDK || source == Define.FROM_PROCESS)) {	//	if(media.getMid() == 8079 || media.getMid() == 7970) {
					if(float_moeny > 0) {
						Jedis jedis = null;
						try {
							jedis = RedisConnection.getInstance("rule");
							String idfa = udid.replace("-", "");
							idfa = idfa.toLowerCase();
							
							String redisText = jedis.hget("RULE_UDID_SCORE", idfa);
							if(redisText == null || redisText.length() == 0) {
								redisText = "0,0,0";
							} 
							String []tmp = redisText.split(",");
							float ruleScore = Float.parseFloat(tmp[0]);
							
							float t = float_moeny * ruleScore;
							float cost2 = cost + t;
							LOG.debug(String.format("active rule score udid:%s adid:%d float_moeny:%.2f ruleScore: %.2f cost:%.2f cost2:%.2f", udid, adid, float_moeny,ruleScore, cost, cost2));
							
							cost = cost2;
							ruleCost = cost2;
						}catch(Exception e){
							ruleCost = -1f;
							LOG.debug(String.format("active rule score udid exception:%s adid:%d float_moeny:%.2f %s", udid, adid, float_moeny, e.getMessage()));
						}finally{
							if(jedis != null) {
								RedisConnection.close("rule", jedis);
							}
						}
					}
				} else if(cost > 0 && (source == Define.FROM_API)) {		//快速任务API激活，不走用户评分规则
					cost = cost + float_moeny;
				}
			}
		} catch (Exception e) {
			LOG.error("float money error : " + e.getMessage());
		}
		DataSaveRole role = DataSave.getRole(media, ad);
		cost = (item.saved == 1 || invalid > 0) ? 0 : DataSave.getRate(role, cost);
		
		float score = 0;
		if (cost > 0 && media.getType() == 1) {
			MediaApp app = (MediaApp) media;
			float ratio = app.getRatio();
			if (ratio > 0) {
				score = cost * ratio;
			}
		}
		
		int unique = 1;
		int count = 1;
		int saved = item.saved > 0 || invalid > 0 ? 0 : 1;
		
		int uid = media.getUid();
		int cid = ad.getCid();
		
		//进程激活
		if((ad.getProcess_name() != null && !ad.getProcess_name().isEmpty()) && (source == Define.FROM_PROCESS || source == Define.FROM_API)) {
			//上报数据但不计入广告主数据
			if(ad.getIsHsReport() == 2 || ad.getIsHsReport() == 0) {
				count = 0;
				income = 0;
			}
			unique = 1;
		}

		//激活统计
		DetailHourKeys ck = DetailHourKeys.create(year, mon, day, hour, type,
				data_from, ad_from, appid, uid, adid, cid);
		CountValues cv = CountValues.create(action, count, invalid > 0 ? 1 : 0,
				unique, saved, income, cost);
		Counter.getInstance().add(ck, cv);
		
		//数据激活
		item.create_time = this.datetime(vals).timestamp();
		item.action = action;

		item.income = income;
		item.cost = cost;
		item.score = score;
		item.process = source;
		
		LOG.info(String.format("active for : action=%d adid=%d mac=%s udid=%s openudid=%s invalid=%d saved=%d cost=%.2f ruleCost=%.2f",action, adid, mac, udid, openudid, invalid, saved,cost,ruleCost));
		//如果时间不等于今天的时间，那么说明点击实在以前历史
		if (date == item.date) {
			int result = DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).update(item);
			if(result <= 0) {
				LOG.error(String.format("update active if : action=%d adid=%d mac=%s udid=%s openudid=%s",action, adid, mac, udid, openudid));
				return null;
			}
		} else {
			//为什么是put是因为这张表是天表，所以历史数据的话需要更新到当天当中
			long result = DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).put(adid, mac, udid, item);
			if(result <= 0) {
				LOG.error(String.format("update active else : action=%d adid=%d mac=%s udid=%s openudid=%s",action, adid, mac, udid, openudid));
				return null;
			}
		}
		//将激活信息存入redis当中，对应前面的isActive函数
		RedisStore.getInstance().addActive(item.mac + item.udid, adid);
		
		//先将自媒体appid 排除，后续再修改 
		//if(media.getMid() != 7329 && media.getMid() != 7789 && media.getMid() != 8092  && media.getMid() != 8079 
		//		&& media.getMid() != 7970  && media.getMid() != 7715 ) {
			
			//同步激活时间，用于控制深度任务的显示
			//1.获取DATA_DEVICE_ADID信息
			Jedis jedisProcess = null;
			String macNew = mac;
			try {
				jedisProcess = RedisConnection.getInstance("process");
				String keyProcess = "DATA_DEVICE_ADID";
				String field = String.format("%s%s%d",macNew , udid , adid);
				String info = jedisProcess.hget(keyProcess, field);
				
				int isAllFinish = (len == active_num + 1) ? 1 : 0;
				int atime = (int) (System.currentTimeMillis()/1000);
				//设置的是下次要执行的任务，如果没有进行初设，如果有的话进行，判断当前任务是否已经全部完成，
				//如果没有完成进行参数甄别，任务书加1和是否已经完成
				if(info==null) {
					String v = String.format("%d,%d,%d,%d,%d", isAllFinish,atime,active_num+1,atime,atime);
					jedisProcess.hset(keyProcess, field, v);
					LOG.info(String.format("insert device active : mac=%s udid=%s adid=%d value=%s",macNew, udid, adid, v));
				}else {
					String[] infoArr = info.split(",");
					if(infoArr[0].equals("0")) {
						int ctime = infoArr[1]==null?atime:Integer.parseInt(infoArr[1]);
						int fctime = 0;
						try {
							fctime = infoArr[4]==null?0:Integer.parseInt(infoArr[4]);
						} catch (Exception e) {
							fctime = 0;
						}
						String v = String.format("%d,%d,%d,%d,%d", isAllFinish,ctime,active_num+1,atime,fctime);
						jedisProcess.hset(keyProcess, field, v);
						LOG.info(String.format("update device active : mac=%s udid=%s adid=%d front=%s after=%s", macNew, udid, adid, info, v));
					}
				}
			} catch (Exception e) {
				LOG.warn(String.format("update redis process action=%d mac=%s udid=%s adid=%d appid=%d active_num=%s exception=%s",action, macNew, udid, adid, appid, active_num, e.getMessage()));
			}finally{
				if(jedisProcess != null)RedisConnection.close("process",jedisProcess);
			}
		//}
		
		//媒体所有广告前10不扣量，渠道针对单一广告前50不扣量
		CachedValue appcounter = null;
		String appKey = "";
		if(media.getType() == Define.MEDIA_TYPE_CHANNEL){
			appcounter = CachedValue.getInstance("CHANNEL_ACTIVED_NUM");
			appKey = String.format("%d%d", appid,adid);
		}else {
			appcounter = CachedValue.getInstance("APP_ACTIVED_NUM");			
			appKey = String.valueOf(appid);
		}
		
		String value = appcounter.get(appKey);
		if (value != null) {
			int v = Integer.parseInt(value);
			if (v < Define.ACTIVE_SAVE_MAX) {
				appcounter.put(appKey, String.valueOf(v + 1));
			}
		} else {
			appcounter.put(appKey, "1");
		}
		
		//下发
		if (item.saved == 1 || item.invalid > 0) {
			LOG.info("media active save : " + item.uid + " " + item.appid + " " + item.appuserid + " " + item.mac + " " +
					item.udid + " " +item.openudid + " " +item.score);
		} else {

			if (media.getType() == 1) {
				//if(item.score > 0) {
					MediaApp app = (MediaApp) media;
					UserScore.sendScore(app, ad, item.appuserid, item.mac,
						item.udid, item.openudid, item.score,ad.getProcess_name(),active_num);
				//}
				LOG.info("user send score " + item.uid + " " + item.appid + " " + item.appuserid + " " + item.mac + " " +
						item.udid + " " +item.openudid + " " +item.score);
			} else {
				if (item.appuserid != null && !item.appuserid.isEmpty()) {
					String url;
					try {
						url = URLDecoder.decode(item.appuserid, "UTF-8");

						if (url.length() > 4) {
							String proto = url.substring(0, 4).toLowerCase();
							if (proto.equals("http")) {
								url = url.replaceAll("&amp;", "&");

								JSONObject obj = new JSONObject();
								obj.put("protocol", "http");
								obj.put("method", "get");
								obj.put("url", url);
								LOG.debug("channel callback : " + url);

								Jedis jedis = RedisConnection
										.getInstance("values");
								jedis.rpush("ACTION_HTTP_REQUEST",
										obj.toString());
								RedisConnection.close("values", jedis);
							} else {
								LOG.error("error proto channel "+item.appid+" callback : " + url);
							}
						} else {
							LOG.error("error proto channel "+item.appid+" callback : " + url);
						}
					} catch (UnsupportedEncodingException e) {
						LOG.error("error on decode channel "+item.appid+" callback : "
								+ item.appuserid);
					}
				} else {	
					LOG.debug("channel has no callback" + item.appid
							+ item.appuserid);
				}
			}
		}
		
		//快速精准控量
		//快速任务完成之后删除
		if(ad.getIsHsFlag() == 3) {
			RemainActiveUtil activeUtil = RemainActiveUtil.getInstance();
			String tidfa = udid.toLowerCase().replaceAll("-", "");
			if(!activeUtil.isIdfaExists(adid, tidfa)){
				LOG.debug("active remain num : " + adid + " , " + udid);
				return null;
			}else {
				//从idfa激活等待列表中移除对应的idfa
				activeUtil.idfaWaitActive("-",adid, tidfa);
			}
		}
		
		//快速任务实时上报IDFA给广告主
		JSONObject optJson;
		try {
			optJson = new JSONObject(ad.getOptions());
			//从options当中获取上报的url
			if(optJson != null) {
				String reportIdfaUrl  = String.valueOf(optJson.get("report_idfa_url"));
				String reportIdfaKey = String.valueOf(optJson.get("report_idfa_key"));
				if(reportIdfaUrl != null && reportIdfaUrl.length() > 0) {
					if(reportIdfaKey == null || reportIdfaKey.length() == 0){
						reportIdfaKey = "";
					}
					
					//组织上报
					String httpUrl = "";
					String params = "";
					if(reportIdfaUrl.indexOf("?") > 0){
						params = String.format("idfa=%s&key=%s", udid.toUpperCase(), reportIdfaKey);
						httpUrl = String.format("%s&idfa=%s&sign=%s", reportIdfaUrl, udid.toUpperCase(), Md5.crypt(params));
					}else{
						params = String.format("idfa=%s&key=%s", udid.toUpperCase(), reportIdfaKey);
						httpUrl = String.format("%s?idfa=%s&sign=%s", reportIdfaUrl, udid.toUpperCase(), Md5.crypt(params));
					}
					
					JSONObject obj = new JSONObject();
					obj.put("protocol","http");
					obj.put("method","get");
					obj.put("type", "sdk");
					obj.put("url", httpUrl);
					
					LOG.debug("report idfa : " + httpUrl + ", adid : " + ad.getAdid());
					
					Jedis jedis = RedisConnection.getInstance("report");
					jedis.rpush("REPORT_IDFA_TOCP", obj.toString());
					RedisConnection.close("report",jedis);
				}
			}
		} catch (Exception e) {}
		
		return null;
	}
}
