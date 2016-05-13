package com.dianru.analysis.boot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.boot.util.GiveUpRunnable;
import com.dianru.analysis.store.RedisQueue;
import com.dianru.analysis.util.Configuration;

public class GiveUpThread extends Thread {
	
	private boolean loop = true;
	
	public static Logger LOG = LogManager.getLogger(GiveUpThread.class);
	
	private String queueName;
	
	public GiveUpThread(String queueName) {
		this.queueName = queueName;
	}
	
	public void quit() {
		loop = false;
	}
	
	public void run() {
		
		RedisQueue queue = RedisQueue.getInstance();
		
		String line;
		int nThreads = Configuration.getInstance().getInt("server.threads", 1);
		
		ExecutorService executorService = null;
		if(nThreads > 1) {
			executorService = Executors.newFixedThreadPool(nThreads);
		} else {
			executorService = Executors.newCachedThreadPool();
		}
		
		while (loop) {
			line = queue.pop(queueName);
			if (line == null) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}
			
			if(executorService == null) {
				try {
					GiveUpRunnable.exec(line);
				} catch (Exception e) {
					LOG.trace("exec "+e);
				}
			} else {
				executorService.execute(new GiveUpRunnable(line));
			}
		}
		
		if(executorService != null) {
			executorService.shutdown();
			try {
				executorService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOG.error("wait executor service exception : " + e.toString());
			}
		}
	}
}
