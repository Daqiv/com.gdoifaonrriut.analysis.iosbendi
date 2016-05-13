package com.dianru.analysis.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.util.KeyUtil;
import com.dianru.analysis.util.MACUtil;

public class DBHistoryStore  extends RedisKeyValueStore {
	
	private final static Map<String,DBHistoryStore> STORES = new ConcurrentHashMap<String,DBHistoryStore>();
	
	public static String getPath(String area, int date, int type, int action) {
		return String.format("%s/%d/%d/%d", area, date, type, action);
	}
	
	public static DBHistoryStore getInstance(String area, int date, int type, int action) {

		String path = getPath(area, date, type, action);
		DBHistoryStore store = STORES.get(path);
		if(store == null) {
			store = new DBHistoryStore(path, date, type);
			STORES.put(path, store);
		}
		return store;
	}

	private int type;
	private int date;
	
	public DBHistoryStore(String path, int date, int type) {
		super(path);
		
		this.date = date;
		this.type = type;
	}
	
	public Long getId(int adid, String key) {
		if(key != null && !key.isEmpty()) {
			CharSequence cs = KeyUtil.parse(key, adid);
			if(cs.length() > 0) {
				String value = this.get(cs);
				return (value == null || value.isEmpty()) ? -1 : Long.parseLong(value);
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

	public CallbackItem get(int adid, String mac, String udid) {
		long id = this.getId(adid, mac, udid);
		if(id >= 0) {
			CallbackItem item = DBCallbackStore.getInstance(type,date).get(id);
			if(item == null) {
				LOG.error("callback item " + type + " " + date + " " + id + " not found");
			}
			return item;
		}
		return null;
	}
	
	public boolean exists(int adid, String mac, String udid) {
		long id = this.getId(adid, mac, udid);
		return (id >= 0);
	}
	
	public void putId(int adid, String key, long id) {
		if(id < 0) {
			return;
		}
		if(key != null && !key.isEmpty()) {

			CharSequence cs = KeyUtil.parse(key,adid);
			if(cs.length() > 0) {
				this.put(cs, String.valueOf(id));
			}
		}
	}
	
	public void putId(int adid, String mac, String udid, long id) {

		if(udid != null && !udid.isEmpty()) this.putId(adid, udid, id);
		
		mac = MACUtil.parse(mac);
		if(mac != null && !mac.isEmpty()) this.putId(adid, mac, id);
	}

	public long put(int adid, String mac, String udid, CallbackItem item) {
		
		long id = DBCallbackStore.getInstance(type,date).add(item);
		LOG.trace("insert redis  adid="+adid+" mac="+mac+" udid="+udid+" id : " + id);
		//long id = DetailStore.getInstance(String.format("%d/%d",date,type)).put(item.toList());
		this.putId(adid, mac, udid, id);
		return id;
	}

	public int update(CallbackItem item) {
		return DBCallbackStore.getInstance(type,date).update(item);
		//return DetailStore.getInstance(String.format("%d/%d",date,type)).update(item.id, item.toList());
	}
}
