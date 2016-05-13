package com.dianru.analysis.process.imps;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Media;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;
import com.dianru.analysis.count.Counter;
import com.dianru.analysis.count.bean.CountValues;
import com.dianru.analysis.count.bean.DetailHourKeys;
import com.dianru.analysis.parse.imps.ShowParser;
import com.dianru.analysis.process.BaseProcessor;
import com.dianru.analysis.process.util.RemainActiveUtil;
import com.dianru.analysis.process.util.UserControl;
import com.dianru.analysis.util.DataSave;
import com.dianru.analysis.util.DataSave.DataSaveRole;
import com.dianru.analysis.util.ListUtil;

public class ShowProcessor extends BaseProcessor {
	public static Logger LOG = LogManager.getLogger(ShowProcessor.class);

	@Override
	public List<Object>[] process(List<Object> vals) {

		int action = (int) vals.get(ShowParser.Index.ACTION);

		int appid = vals.get(ShowParser.Index.APPID) == null ? 0 : (int) vals.get(ShowParser.Index.APPID);

		Integer ids[] = (Integer[]) vals.get(ShowParser.Index.IDS);
		vals.remove(ShowParser.Index.IDS);
		
		if (appid == 0 || action == 0 || ids == null || ids.length == 0) {
			return null;
		}

		Media media = MediaCache.getInstance().get(appid);
		if (media == null) {
			LOG.warn("media "+appid+" not found");
			return null;
		}
		
		boolean isStop = false;
		if(media.getState() != 1 && media.getState() != 4) {
			isStop = true;
		}

		int year = ListUtil.getInt(vals, Index.YEAR);
		int mon = ListUtil.getInt(vals, Index.MON);
		int day = ListUtil.getInt(vals, Index.DAY);
		int hour = ListUtil.getInt(vals, Index.HOUR);
		
		//1 应用 3 渠道
		int data_from = media.getType() == 1 ? 1 : 3;
		
		String mac = ListUtil.getString(vals, Index.MAC);
		String udid = ListUtil.getString(vals, Index.UDID);
		
		int uid = media.getUid();
		int date = year * 10000 + mon *100 + day;
		
		//媒体新增请求数，独立请求数
		int reqUnique = this.checkUnique(date, 0, action, 0, appid, mac, udid);
		for (int i = 0; i < ids.length; i++) {
			if(i > 0 && reqUnique == 1){
				reqUnique = 0;
			}
			
			int adid = ids[i];
			Ads ad = AdsCache.getInstance().get(adid);
			if (ad == null) {
				LOG.warn("ad "+adid+" object not found");
				continue;
			}
			
			if(ad.getState() != 4 && ad.getState() != 7) {
				isStop = true;
			}
			
			int ad_from = ad.getDataFrom();
			int cid = ad.getCid();

			int type = ad.getDataType();
			DataSaveRole role = DataSave.getRole(media,ad);
			
			boolean save = false;
			if(type == 1 || type == 2){//cpa 扣量，cpc不扣
				save = DataSave.getSave(role);
			}
			
			int unique = this.checkUnique(date, type, action, adid, appid, mac, udid);
			int saved = unique == 0 || save || isStop ? 0 : 1;
			
			DetailHourKeys ck = DetailHourKeys.create(year,mon,day,hour,type,data_from,ad_from,appid,uid,adid,cid);
			CountValues cv = CountValues.create(action, 1, reqUnique, unique, saved, 0, 0);
			
			Counter.getInstance().add(ck, cv);
			
			//平滑控量    
			if(ad.getDeliveryType() == 1){
				RemainActiveUtil.count(udid, mac, ad);
			}
			
			//频次控制-------------------------------------------------------
			try {
				UserControl.controlShow(ad, udid);
			} catch (Exception e) {
				LOG.error("UserControl show:"+e.getMessage());
			}
		}
		
		return null;
	}
}