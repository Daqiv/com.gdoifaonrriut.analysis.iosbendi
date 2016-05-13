package com.dianru.analysis.util;

import java.util.List;

import com.dianru.analysis.store.FileStore;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.SmartArrayBasedNodeFactory;

public class KeyValueStore<T>{
	
	private RadixTree<T> tree = null;
	private FileStore store = null;

	public KeyValueStore(String type) {
		tree = new ConcurrentRadixTree<T>(new SmartArrayBasedNodeFactory() , false);
		
		if(type != null) {
			store = FileStore.getInstance(type+".idx");
			load();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void load() {
		List<Object> kvs =  store.read();
		if(kvs == null) return;
		
		for(int i=0;i<kvs.size();i+=2) {
			tree.put((CharSequence)kvs.get(i), (T)kvs.get(i+1));
		}
	}

	public T put(CharSequence key, T value) {
		
		T o = tree.put(key, value);
		
		if(store != null) store.write(key, value);
		
		return o;
	}
	
	public T get(CharSequence key) {
		return tree.getValueForExactKey(key);
	}
	
	public boolean remove(CharSequence key) {
		return tree.remove(key);
	}
}
