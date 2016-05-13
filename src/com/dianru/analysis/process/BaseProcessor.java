package com.dianru.analysis.process;

import java.util.List;

import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.bean.MediaPrice;
import com.dianru.analysis.cache.MeidaPriceCache;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.UniqueStore;
import com.dianru.analysis.util.ListUtil;

public abstract class BaseProcessor implements Processor {
	
	public static int[] DB_VALUES = {
		Index.ACTION, Index.SAVED, Index.UID, Index.APPID, Index.CID, Index.ADID, Index.FROM, 
		Index.MAC, Index.UDID, Index.OPENUDID,Index.IP
	};

	public static interface Index extends Define.SourceIndex {
		public final static int CID = 22;
		public final static int ADID = 23;
		
		public final static int AD_FROM = 24;
		
		public final static int COUNT = 25;
		public final static int INVALID = 26;
		public final static int UNIQUE = 27;
		public final static int SAVED = 28;
		
		public final static int INCOME = 29;
		public final static int COST = 30;

		public final static int INTERVAL = 31;
	}

	public int processMediaType(int from) {
		if(from == 1 || from == 2) return 1;
		else if(from == 3) return 2;
		else return 0;
	}
	
	public int date(List<Object> vals) { 
		int year = ListUtil.getInt(vals, Index.YEAR);
		int mon = ListUtil.getInt(vals, Index.MON);
		int day = ListUtil.getInt(vals, Index.DAY);
		
		return year*10000 + mon *100 + day;
	}
	
	public DateTime datetime(List<Object> vals) { 
		int year = ListUtil.getInt(vals, Index.YEAR);
		int mon = ListUtil.getInt(vals, Index.MON);
		int day = ListUtil.getInt(vals, Index.DAY);
		
		int hour = ListUtil.getInt(vals, Index.HOUR);
		int min = ListUtil.getInt(vals, Index.MIN);
		int sec = ListUtil.getInt(vals, Index.SEC);
		
		return new DateTime(year, mon, day, hour, min, sec);
	}
	
	public int checkUnique(int date, int type, int action, int adid, int appid, String mac, String udid) { 
		UniqueStore store = UniqueStore.getInstance(FileStore.STORE_TEMP, date, type, action);
		Long exists = store.getId(adid, appid+mac+udid);
		if(exists != null) 
			return 0;
		
		store.putId(adid, appid+mac+udid, 1);
		return 1;
	}
	
	public float getPrice(int appid, int adid) {
		MediaPrice price = MeidaPriceCache.getInstance().get(appid, adid);
		if(price == null) return 0;
		
		if(price.effect_time > System.currentTimeMillis()/1000) return 2.0f;
		
		return price.price;
	}
}
