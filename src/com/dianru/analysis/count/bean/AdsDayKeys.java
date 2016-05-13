package com.dianru.analysis.count.bean;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.SQLConnection.DataSource;

public class AdsDayKeys implements CountKeys {
	public int created;
	
	public int year;
	public int mon;
	public int day;

	public int type;
	public int data_from;
	public int ad_from;

	public int adid;
	public int cid;
	
	public static String SQL_FIELDS[] = {
		"created","year","mon","day",
		"type","data_from","ad_from",
		"adid","cid"
	};
	
	public String getTable() {
		DataSource ds = SQLConnection.getDataSource(Define.DATA_SOURCES[type]);
		String prefix = ds == null ? "" : ds.getPrefix();
		return String.format("%s_ad_day_%d", prefix, created/100/100);
	}
	
	public String[] getFileds() {
		return SQL_FIELDS;
	}
	
	public Object[] getValues() {
		return new Object[] {
			created,year,mon,day,
			type,data_from, ad_from,
			adid,cid
		};
	}
	
	protected AdsDayKeys() {
		this.year = 0;
		this.mon = 0;
		this.day = 0;

		this.type = 0;
		this.data_from = 0;
		this.ad_from = 0;

		this.adid = 0;
		this.cid = 0;
		
		this.created = 0;
	}
	
	public static AdsDayKeys create(AdsHourKeys from) {
		
		AdsDayKeys item = new AdsDayKeys();

		item.year = from.year;
		item.mon = from.mon;
		item.day = from.day;

		item.type = from.type;
		item.data_from = from.data_from;
		item.ad_from = from.ad_from;

		item.adid = from.adid;
		item.cid = from.cid;
		
		item.created = item.year*10000 + item.mon *100 + item.day;
		
		return item;
	}
	
	private int hash = 0;
	public int hashCode() {
		if(hash != 0) return hash;
		
		Object[] vals = this.getValues();
		int h = 0;
		int off = 0;
		int len = vals.length;

		for (int i=1; i<len;i++) {
			h = 31*h + (int)vals[off++];
	    }
		hash = h;
		return hash;
	}
	
	public boolean equals(Object obj) {
		AdsDayKeys that = (AdsDayKeys)obj;

		if(this.created != that.created) return false;
		if(this.type != that.type) return false;
		if(this.ad_from != that.ad_from) return false;
		if(this.data_from != that.data_from) return false;
		if(this.adid != that.adid) return false;
		if(this.cid != that.cid) return false;
		
		return true;
	}
}
