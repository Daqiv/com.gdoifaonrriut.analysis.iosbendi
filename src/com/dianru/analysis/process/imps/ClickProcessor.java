package com.dianru.analysis.process.imps;

import java.util.Date;
import java.util.List;

//import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.bean.Media;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;
import com.dianru.analysis.count.Counter;
import com.dianru.analysis.count.bean.CountValues;
import com.dianru.analysis.count.bean.DetailHourKeys;
import com.dianru.analysis.parse.imps.ActionParser;
import com.dianru.analysis.process.CallbackProcessor;
import com.dianru.analysis.process.util.RemainActiveUtil;
import com.dianru.analysis.process.util.UserControl;
import com.dianru.analysis.store.CachedValue;
import com.dianru.analysis.store.DBHistoryStore;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.RedisStore;
import com.dianru.analysis.util.DataSave;
import com.dianru.analysis.util.DataSave.DataSaveRole;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.IP2Location;
import com.dianru.analysis.util.IP2Location.IPInfo;
import com.dianru.analysis.util.ListUtil;
import com.dianru.analysis.util.RedisConnection;

public class ClickProcessor extends CallbackProcessor {

	public static Logger LOG = LogManager.getLogger(ClickProcessor.class);

	@Override
	public List<Object>[] process(List<Object> vals) {
		
		int action, type, appid, adid, root = 0;
		String session = "", did = "", ssid = "", localip = "", openudid = ""	, rload = "";
		long disk = 0;
		try {
			action = (int) vals.get(Index.ACTION);
			type = (int) vals.get(Index.TYPE);
			
			appid = (int) vals.get(Index.APPID);
			adid = (int) vals.get(Index.ADID);
		} catch (Exception e) {
			LOG.warn("data item has error");
			return null;
		}
		
		String mac = "";
		String udid = "";
		try{
			session = ListUtil.getString(vals, ActionParser.Index.SESSION);
			did = ListUtil.getString(vals, ActionParser.Index.DID);
			ssid = ListUtil.getString(vals, ActionParser.Index.SSID);
			localip = ListUtil.getString(vals, ActionParser.Index.LOCALIP);
			openudid = ListUtil.getString(vals, ActionParser.Index.OPENUDID);
			disk = ListUtil.getLong(vals, ActionParser.Index.DISK);
			rload = ListUtil.getString(vals, ActionParser.Index.RLOAD);
			root = ListUtil.getInt(vals, ActionParser.Index.ROOT);
			
			mac = ListUtil.getString(vals, Index.MAC);
			udid = ListUtil.getString(vals, Index.UDID);
			
			//time = ListUtil.getInt(vals, ActionParser.Index.TIME);
			//uptime = ListUtil.getInt(vals, ActionParser.Index.UPTIME);
		} catch (Exception e) { }
		
		//控量处理
		int remain = RemainActiveUtil.getRemain(adid);
		if(remain <= 0) {
			LOG.debug("click remain num : " + adid + " , " + remain + " , " + udid);
//			return null;
		}
		
		if (adid == 0 || appid == 0 || action == 0 || type == 0) {
			LOG.warn(String.format("data item has error adid=%d appid=%d action=%d type=%d", adid, appid, action, type));
			return null;
		}

		Media media = MediaCache.getInstance().get(appid);
		if (media == null) {
			LOG.warn("media " + appid + " not found.");
			return null;
		}

		boolean isStop = false;
		Ads ad = AdsCache.getInstance().get(adid);
		if (ad == null) {
			LOG.warn("ad " + adid + " not found.");
			return null;
		}
		
		type = ad.getDataType();
		
		//1 投放  2停止  3软删除 4测试（控制墙是否显示） 开发者行为
		//媒体分类 1 应用app 2 渠道channel
		if(media.getState() != 1 && media.getState() != 4 && media.getType() != 2) {
			isStop = true;
		}
		
		//状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停, 9 留存
		if(ad.getState() == 5 && ad.getIs_hand_stop() == 1){	//手动停止的广告，不用做留存了
			isStop = true;
		} else if(ad.getState() == 5 ||  ad.getState() == 8   ||  ad.getState() == 9){			//留存广告，超过1周的
			int current = (int) (System.currentTimeMillis()/1000);
			if(current - ad.getUpdateTime() > 3600 * 24 * 7){
				isStop = true;
			}
		} else if(ad.getState() != 4 && ad.getState() != 7 && media.getType() != 2) {
			isStop = true;
		}
		
		// 1 应用 3 渠道
		int data_from = media.getType() == 1 ? 1 : 3;
		//1	普通广告 2 渠道广告
		int ad_from = ad.getDataFrom();

		int year = ListUtil.getInt(vals, Index.YEAR);
		int mon = ListUtil.getInt(vals, Index.MON);
		int day = ListUtil.getInt(vals, Index.DAY);
		int hour = ListUtil.getInt(vals, Index.HOUR);

		String osver = String.valueOf(vals.get(Index.OSVER));	//IOS系统版本
		
		int uid = media.getUid();
		int cid = ad.getCid();
		int date = year * 10000 + mon * 100 + day;

		DetailHourKeys ck = DetailHourKeys.create(year, mon, day, hour, type, data_from, ad_from, appid, uid, adid, cid);
		
		/*
		int vunique = checkUnique(date, type, action, adid, appid, mac, udid);
		if(vunique == 1) {
			CountValues cv = CountValues.create(action, 1, 0, 1, 0, 0, 0);
			Counter.getInstance().add(ck, cv);
		}
		*/
		//特殊控量    
		RemainActiveUtil.count(udid, mac, ad);
		
		try {
			//频次控制
			UserControl.controlClick(ad, udid);
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("UserControl click:"+e.getMessage());
		}
		
		CallbackItem item = this.getHistory(year, mon, day, type, adid, mac, udid);
		if (item != null && item.isToday) {	//isToday 表示当天
			
			CountValues cv = CountValues.create(action, 1, 0, 0, 0, 0, 0);
			Counter.getInstance().add(ck, cv);
			
			LOG.warn(String.format("Click action exists for : item.date=%d item.id=%d item.adid=%d item.mac=%s item.udid=%s action=%d adid=%d mac=%s udid=%s",
							item.date, item.id, item.adid, item.mac, item.udid, action, adid, mac, udid));
			
			return null;
		}
		
		// 点击数据入库
		item = CallbackItem.parseFromLog(vals, media, ad);
		item.cid = ad.getCid();
		item.uid = media.getUid();
		
		int invalid = isStop ? 4 : 0;
		
		if(invalid == 0 && ( media.getType() == 1) ) {
			int test = 0;
			if(media.getCitys() != null && !media.getCitys().isEmpty()) {
				IPInfo info = IP2Location.find(item.ip);
				if(info != null && media.inCitys(info.location)) {
					test = 1;
				}
			}
			
			if(media.getHours() != null && !media.getHours().isEmpty()) {
				String strHour = String.valueOf(hour);
				if(media.inHours(strHour)) {
					test += 1;
				}
			}
			if(test == 2) invalid = 8;
		}
		
		//是否启用防作弊	liuhuiya
		if(media.getIsEnable() == 0 ){ //启用
			/**
			 * 1、 验证规则（	第5位替换成5、第8位替换成8、第15位替换成7、第25位替换成4）
			 * 2、验证是否重复 （广告+session）唯一性，按天提重
			 */
			if(did != null && did.length() > 0) {
				invalid = RedisStore.getInstance().addDidCheat(ad, session, did, udid);
			}
			
			/**
			 * 防作弊2：SSID
			 * 收集udid 和 ssid 的映射关系
			 */
			if(ssid != null && ssid.length() > 0) {
				RedisStore.getInstance().addSsidCheat(udid, ssid, localip);
			}
			
			/**
			 * 防作弊3：OPENUDID
			 * 收集udid 和 openudid 的映射关系
			 */
			if(openudid != null && openudid.length() > 0){
				RedisStore.getInstance().addOpenudidCheat(udid, openudid);
			}
			
			//磁盘点击记录
			if(disk > 0){
				RedisStore.getInstance().addClickDisk(mac, udid, adid, disk);
			}
			
			//redownload
			if(rload != null && rload.length() > 0) {
				boolean r = Boolean.parseBoolean(rload);
				if(r){
					 if(invalid == 0) invalid = 16;
				}
			}
			
			//越狱扣量
			if(root == 1){
				 if(invalid == 0) invalid = 17;	
			}
		}
		
		//媒体特殊处理
		if(media.getMid() == 5656) {
			if(osver.contains("7.")){
				 if(invalid == 0) invalid = 10;		//osver系统版本作弊屏蔽 
			}
		}
		
		//评分规则
		//ScoreRule.count(mac, udid, time, uptime);
		
		//计费
		float income = 0;
		float cost = 0;
		
		//计费
		String billing = ad.getBilling();
		if (billing.indexOf('1') >= 0) {
			income = ad.getPriceClickIncome();
			
			cost = this.getPrice(appid, adid);
			if(cost == 0) cost = ad.getPriceClickCost();
			
			item.income = income;
			item.cost = cost;
			
			RedisStore.getInstance().addClick(item.mac + item.udid, adid);
		}
		
		item.invalid = invalid;
		boolean save = isStop;
		
		int actived = 0;
		
		//媒体所有广告前10不扣量，渠道针对单一广告前50不扣量
		//获取actived-->已经激活的数目
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
		if(value != null) {
			actived = Integer.parseInt(value);
		}
		
		
		//确定item.saved
		String s = "";
		DataSaveRole role = DataSave.getRole(media, ad);
		if(invalid > 0 || isStop) {
			save = true;
			item.saved = 1;
			
			if(invalid > 0) {
				s = "invalid";
			}else {
				s = "isStop";
			}
		} else {
			if(type == 1 || type == 2){ //cpa
				int min = (media.getType() == Define.MEDIA_TYPE_APP) ? Define.ACTIVE_SAVE_APP : Define.ACTIVE_SAVE_CHANNEL;
				save = (media.getState() == 4 || actived < min) ? false : DataSave.getSave(role);
			}else{//cpc
				save = (media.getState() == 4) ? false : DataSave.getSave(role);
			}
			item.saved = save ? 1 : 0;
			s = "cpa";
			//cost = item.saved > 0 ? 0 : DataSave.getRate(role, cost);
		}
		
		//计算cost
		cost = (item.saved == 1 || invalid > 0) ? 0 : DataSave.getRate(role, cost);
		if(media.getMid() == 7714 && type > 2) {
			LOG.debug(String.format("test click save : %d, rate : %d, cost=%2f, invalid=%d", role.getSave(), role.getRate() , cost, invalid));
		}
		
		LOG.trace(String.format("insert item to database click mac=%s udid=%s appid=%d adid=%d cost=%2f invalid=%d saved=%s", mac, udid, appid, adid, cost, invalid,s));
		
		//插入callback数据库和redis的value数据库
		DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).put(adid, mac,udid, item);
		
		//存一份redis 唯一键值
		int vcount = 1;
		int vinvalid =  (invalid > 0 ? 1 : 0);
		int vsaved = (save ? 0 : 1);
		
		//添加进入内存的操作
		CountValues cv = CountValues.create(action, vcount, vinvalid, 1, vsaved, income, cost);
		Counter.getInstance().add(ck, cv);
		
		//广告ID：15477 ，特殊回调模式，所以增加点击记录
		if(adid == 15477 ){
			Jedis jMigu = null;
			try {
				jMigu = RedisConnection.getInstance("migu");
				String rKey = "MIGU_"+DateUtils.formatMigu(new Date());
				jMigu.hset(rKey, udid, "1");
			} catch (Exception e) {
			}finally {
				if(jMigu != null) {
					RedisConnection.close("migu", jMigu);
				}
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		CachedValue appcounter = null;
		String appKey = "";
//		if(media.getType() == Define.MEDIA_TYPE_CHANNEL){
			appcounter = CachedValue.getInstance("CHANNEL_ACTIVED_NUM");
			String value = appcounter.get(appKey);
			System.out.println("some" + value);
//			appKey = String.format("%d%d", appid,adid);
	}
}