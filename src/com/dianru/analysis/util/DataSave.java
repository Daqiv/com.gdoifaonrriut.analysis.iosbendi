package com.dianru.analysis.util;

import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Media;
import com.dianru.analysis.bean.MediaApp;
import com.dianru.analysis.bean.MediaFilter;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;

public class DataSave {
	
	public static Logger LOG = LogManager.getLogger(DataSave.class);
	public final static Random RANDOM = new Random();
	
	public static class DataSaveRole {
		
		private int save;
		private int rate;
		
		public DataSaveRole(int save, int rate) {
			this.save = save;
			this.rate = rate;
		}

		public int getSave() {
			return save;
		}

		public int getRate() {
			return rate;
		}
	}
	/**
	 * 获取分成和扣量比例
	 * @param media
	 * @param ad
	 * @return
	 */
	public static DataSaveRole getRole(Media media,Ads ad) {
		if(media == null) return null;
		
		Map<Integer,MediaFilter> rates = media.getRates();
		MediaFilter mf = rates.get(ad.getDataType());
		
		int save = mf.getSave();
		int rate = mf.getRate();

		if(rate == 0) rate = ad.getRate();
		if(save == 0) save = 10;
		
		//APP Media
		if(media instanceof MediaApp) {
			MediaApp app = (MediaApp)media;
			if("2.0".equals(app.getSdkVersion())) {
				save = 100;
				rate = 0;
			}
		}
 
		return new DataSaveRole(save, rate);
	}
	
	/**
	 * 扣量还有个随机比例
	 */
	public static boolean getSave(DataSaveRole role) {
		if(role == null) return false;

		int n = role.save;
		if(RANDOM.nextInt(100) < n) {
			return true;
		}
		return false;
	}
	
	public static float getRate(DataSaveRole role, float price) {
		if(role == null) return price;

		float n = role.rate;
		return price*n/100.0f;
	}
	
	public static void main(String[] args) {
		Media m = MediaCache.getInstance().get(7454);
		Ads a = AdsCache.getInstance().get(10446);
		DataSaveRole dsr = getRole(m,a);
		System.out.println(dsr);
	}
}
