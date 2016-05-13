package com.dianru.analysis.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianru.analysis.util.Configuration;

public class SimpleFileWriter extends BufferedWriter {
	
	public static String WORKDIR = Configuration.getInstance().getProperty("path.workdir", "/tmp/workdir/store");

	protected String name;
	
	public SimpleFileWriter(File file, String name) throws IOException {
		super(new FileWriter(file, true));
		this.name = name;
	}

	public static Map<String,SimpleFileWriter> STORES = new ConcurrentHashMap<String,SimpleFileWriter>();
	
	public static SimpleFileWriter getInstance(String name) {
		SimpleFileWriter store = STORES.get(name);
		if(store == null) {
			//创建父目录
			File file = new File(WORKDIR + "/" + name);
			File dir = file.getParentFile();
			if(!dir.exists()) dir.mkdirs();
			//创建管理器是管理针对同一个文件的SimpleFileWriter
			try {
				store = new SimpleFileWriter(file, name);
				STORES.put(name, store);
			} catch (IOException e) {
				store = null;
			}
		}
		return store;
	}
	
	public synchronized void write(String text) throws IOException {
		super.write(text);
	}
	
	public void close() {
		try {
			super.close();
		} catch (IOException e) {
			
		}
		STORES.remove(this.name);
	}
	
	public static void closeAll() {
		for(SimpleFileWriter store : STORES.values()) {
			store.close();
		}
	}
}
