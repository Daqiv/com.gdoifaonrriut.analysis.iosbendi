package com.dianru.analysis.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedValue extends RedisKeyValueStore {
	
	public CachedValue(String key) {
		super(key);
	}

	private final static Map<String,CachedValue> STORES = new ConcurrentHashMap<String,CachedValue>();

	public static CachedValue getInstance(String key) {

		CachedValue store = STORES.get(key);
		if(store == null) {
			store = new CachedValue(key);
			STORES.put(key, store);
		}
		return store;
	}
}
