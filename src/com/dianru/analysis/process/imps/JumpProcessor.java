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
import com.dianru.analysis.store.DBHistoryStore;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.RedisStore;
import com.dianru.analysis.util.DataSave;
import com.dianru.analysis.util.DataSave.DataSaveRole;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.ListUtil;
import com.dianru.analysis.util.RedisConnection;

public class JumpProcessor extends CallbackProcessor {

	public static Logger LOG = LogManager.getLogger(JumpProcessor.class);

	@Override
	public List<Object>[] process(List<Object> vals) {
		
		int action, type, appid, adid, root = 0;
		String session = "",did = "", ssid = "", localip = "", openudid = "" , rload = "";
		long disk = 0;
		try {
			action = (int) vals.get(Index.ACTION);
			type = (int) vals.get(Index.TYPE);
			
			appid = (int) vals.get(Index.APPID);
			adid = (int) vals.get(Index.ADID);
		} catch (Exception e) {
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
		} catch (Exception e) {}
		
		//控量处理
		int remain = RemainActiveUtil.getRemain(adid);
		if(remain <= 0) {
			LOG.debug("jump remain num : " + adid + " , " + remain + " , " + udid);
//			return null;
		}
		
		if (adid == 0 || appid == 0 || action == 0 || type == 0) {
			LOG.trace(String.format("data item has error adid=%d appid=%d action=%d type=%d", adid, appid, action, type));
			return null;
		}
		
		Media media = MediaCache.getInstance().get(appid);
		if (media == null) {
			LOG.debug("media " + appid + " not found.");
			return null;
		}

		Ads ad = AdsCache.getInstance().get(adid);
		if (ad == null) {
			LOG.debug("ad " + adid + " not found.");
			return null;
		}

		boolean isStop = false;
		//1 投放  2停止  3软删除 4测试（控制墙是否显示） 开发者行为
		//媒体分类 1 应用 2 渠道
		if(media.getState() != 1 && media.getState() != 4 && media.getType() != 2) {//应用媒体中 停止和软删除的媒体
			isStop = true;
		}
		
		//状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停, 9 留存
		if(ad.getState() == 5 && ad.getIs_hand_stop() == 1){	//手动停止的广告，不用做留存了
			isStop = true;
		} else if(ad.getState() == 5 ||  ad.getState() == 8   ||  ad.getState() == 9){			//留存广告(停止和暂停的)，超过1周的
			int current = (int) (System.currentTimeMillis()/1000);
			if(current - ad.getUpdateTime() > 3600 * 24 * 7){
				isStop = true;
			}
		} else if(ad.getState() != 4 && ad.getState() != 7 && media.getType() != 2) { //应用媒体中不是启动和调试状态的
			isStop = true;
		}

		// 1 应用 3 渠道
		int data_from = media.getType() == 1 ? 1 : 3;
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
		int unique = checkUnique(date, type, action, adid, appid, mac, udid);
		int saved = 1;
		
		boolean save = false;
		if(isStop) {
			save = true;
			saved = 0;
		} /*else if(unique == 1) {
			DataSaveRole role = DataSave.getRole(media, ad);
			save = DataSave.getSave(role);
			if(save) saved = 0;
		}*/
		
		//特殊控量    
		RemainActiveUtil.count(udid, mac, ad);
		
		if(unique == 1) {
			CallbackItem item = this.getHistory(year, mon, day, type, adid, mac, udid);
			//DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).exists(adid, mac, udid);
			if (item == null) {
				
				item = CallbackItem.parseFromLog(vals, media, ad);
				item.cid = ad.getCid();
				item.uid = media.getUid();
				
				/*
				if(media.getState() != 1 && media.getState() != 4 && media.getType() != 2) {
					isStop = true;
				}
				
				if(ad.getState() != 4 && ad.getState() != 7 && media.getType() != 2) {
					isStop = true;
				}
				*/
				
				int invalid =  0; //(media.getState() == 1) ? 0 : 4;
				//是否启用防作弊	liuhuiya
				if(media.getIsEnable() == 0 ){ //启用
					/**
					 * DID SESSION
					 */
					if(did != null && did.length() > 0) {
						invalid = RedisStore.getInstance().addDidCheat(ad, session, did, udid);
					}
					
					/**
					 * 防作弊2：SSID
					 * 收集udid 和 ssid 的映射关系
					 */
					if(ssid != null && ssid != null) {
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
				
				item.invalid = item.invalid == 0 ? invalid : item.invalid;
				if(item.invalid > 0){
					item.saved = 1;
				}else {
					DataSaveRole role = DataSave.getRole(media, ad);
					save = DataSave.getSave(role);
					item.saved = save ? 1 : 0;
				}
				if(save || item.saved > 0) saved = 0;
				item.action = Define.ACTION_CLICK;
				
				LOG.trace(String.format("update item to database jump mac=%s udid=%s appid=%d adid=%d invalid=%d saved=%d", mac, udid, appid, adid, invalid,item.saved));
				DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).put(adid, mac,udid, item);
				CountValues cv = CountValues.create(Define.ACTION_CLICK, 1, 0, 1, saved, 0, 0);
				Counter.getInstance().add(ck, cv);
			}
		}
		
		int vcount = 1;
		int vunique = 1;
		int vsaved = saved;
		/*
		if(RAND.nextInt(100) < 16) {
			vcount = vunique = vsaved = 0;
		}
		*/
		CountValues cv = CountValues.create(action, vcount, 0, vunique, vsaved, 0, 0);
		Counter.getInstance().add(ck, cv);
		
		//广告ID：15477 ，特殊回调模式，所以增加点击记录
		if(adid == 15477 ) {
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
}
