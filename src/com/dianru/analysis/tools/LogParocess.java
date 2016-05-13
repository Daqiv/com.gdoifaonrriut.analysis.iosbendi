package com.dianru.analysis.tools;

import com.dianru.analysis.boot.util.ObjectCache;
import com.dianru.analysis.boot.util.TaskRunnable;
import com.dianru.analysis.count.Counter;
import com.dianru.analysis.count.store.CountStoreMap;
import com.dianru.analysis.io.FileLineReader;
import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class LogParocess {
	
	public static void report() {
		System.out.println("begin report to database...");
    	CountStoreMap store = Counter.getInstance().switchStore();
    	if(store == null) {
    		System.out.println("count map is empty");
    		return;
    	}
    	store.save();
    	System.out.println("report to database done");
	}

	public static void main(String[] args) {
		String dir = Configuration.getInstance().getProperty("path.input.dir", "/tmp/workdir/data");
		
		ObjectCache.update();
		
		FileLineReader flr = new FileLineReader(dir, new String[] { "log" });
		
		int count = 0;
		for(String[] lines = flr.getLines(1000); lines != null && lines.length>0; lines = flr.getLines(1000)) {
			for (String line : lines) {
				count++;
				
				TaskRunnable.exec(line);
			}
			if(count % 1000 == 0) System.out.println("process : " + count);
			
			if(count % 10000 == 0) {
				report();
			}
		}
		
		report();

		SQLConnection.shutdown();
		RedisConnection.shutdown();
		
		System.out.println("process : " + count + " done");
	}
}
