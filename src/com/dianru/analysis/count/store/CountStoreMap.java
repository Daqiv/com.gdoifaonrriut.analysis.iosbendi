package com.dianru.analysis.count.store;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CountStoreMap {
	public Map<Integer,CountStore> stores = new ConcurrentHashMap<Integer,CountStore>();

	public CountStore getMap(int type) {
		CountStore store = stores.get(type);
		if(store == null) {
			store = new CountStore(type);
			stores.put(type, store);
		}
		return store;
	}
	
	public void save() {

		for(Iterator<CountStore> it=stores.values().iterator();it.hasNext();) {
			CountStore store = it.next();
			store.save();
		}
	}
}
