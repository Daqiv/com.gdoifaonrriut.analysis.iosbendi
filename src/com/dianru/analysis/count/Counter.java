package com.dianru.analysis.count;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dianru.analysis.count.bean.CountValues;
import com.dianru.analysis.count.bean.DetailHourKeys;
import com.dianru.analysis.count.store.CountStoreMap;

public class Counter {
	
	public static Counter counter = new Counter();
	public static Counter getInstance() {
		return counter;
	}

	private Queue<CountStoreMap> storeQueue = new ConcurrentLinkedQueue<CountStoreMap>();
	
	public CountStoreMap switchStore() {
		if(storeQueue.size() == 0) return null;
		
		CountStoreMap store = storeQueue.poll();
		storeQueue.add(new CountStoreMap());

		return store;
	}
	
	public CountStoreMap getStore() {
		if(storeQueue.isEmpty()) {
			CountStoreMap store = new CountStoreMap();
			storeQueue.add(store);
			return store;
		}
		return storeQueue.peek();
	}

	public void add(DetailHourKeys ck, CountValues cv) {
		CountStoreMap storeMap = getStore();
		storeMap.getMap(ck.type).put(ck, cv);
	}
}
