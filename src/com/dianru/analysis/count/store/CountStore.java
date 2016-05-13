package com.dianru.analysis.count.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianru.analysis.count.bean.CountKeys;
import com.dianru.analysis.count.bean.CountValues;

public class CountStore {
	
	private int type;
	
	public CountStore(int type) {
		this.type = type;
	}
	
	private Map<CountKeys,CountValues> map = new ConcurrentHashMap<CountKeys,CountValues>(); 
	
	public void put(CountKeys keys, CountValues vals) {
		CountValues olds = map.get(keys);
		if(olds == null) {
			map.put(keys, vals);
		} else {
			olds.add(vals);
		}
	}

	public void save() {
		ReportToDatabase.getInstance().save(type, map);
	}
}
