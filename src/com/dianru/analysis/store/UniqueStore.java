package com.dianru.analysis.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianru.analysis.util.KeyUtil;
import com.dianru.analysis.util.MACUtil;

public class UniqueStore extends KeyValueStore<Long> {

	private final static Map<String,UniqueStore> STORES = new ConcurrentHashMap<String,UniqueStore>();
	
	public static String getPath(String area, int date, int type, int action) {
		return String.format("%s/%d/%d/%d", area, date, type, action);
	}
	
	public static UniqueStore getInstance(String area, int date, int type, int action) {

		String path = getPath(area, date, type, action);
		UniqueStore store = STORES.get(path);
		if(store == null) {
			store = new UniqueStore(path);
			STORES.put(path, store);
		}
		return store;
	}

	public UniqueStore(String path) {
		super(path);
	}
	
	public Long getId(int adid, String key) {
		if(key != null && !key.isEmpty()) {
			CharSequence cs = KeyUtil.parse(key, adid);
			if(cs.length() > 0) {
				Long value = this.get(cs);
				return value;
			}
		}
		return null;
	}

	public long getId(int adid, String mac, String udid) {
		
		if(udid != null && !udid.isEmpty()) {
			Long value = this.getId(adid, udid);
			if(value != null) 
				return value;
		}
		
		mac = MACUtil.parse(mac);
		if(mac != null && !mac.isEmpty()) {
			Long value = this.getId(adid, mac);
			if(value != null) 
				return value;
		}

		return -1;
	}
	
	public boolean exists(int adid, String mac, String udid) {
		long id = this.getId(adid, mac, udid);
		return (id >= 0);
	}
	
	public void putId(int adid, String key, long id) {

		if(key != null && !key.isEmpty()) {

			CharSequence cs = KeyUtil.parse(key,adid);
			if(cs.length() > 0) {
				this.put(cs, id);
			}
		}
	}
	
	public void putId(int adid, String mac, String udid, long id) {

		if(udid != null && !udid.isEmpty()) this.putId(adid, udid, id);
		
		mac = MACUtil.parse(mac);
		if(mac != null && !mac.isEmpty()) this.putId(adid, mac, id);
	}
}