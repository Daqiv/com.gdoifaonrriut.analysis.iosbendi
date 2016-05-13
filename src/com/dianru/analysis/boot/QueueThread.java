package com.dianru.analysis.boot;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.boot.util.TaskRunnable;
import com.dianru.analysis.boot.util.TimeCache;
import com.dianru.analysis.store.RedisQueue;
import com.dianru.analysis.store.SimpleFileWriter;
import com.dianru.analysis.util.Configuration;

public class QueueThread extends Thread {
	
	private boolean loop = true;
	
	public static Logger LOG = LogManager.getLogger(QueueThread.class);
	
	public static SimpleFileWriter getLogFile() {
		String filePath = String.format("data/%d/%02d.log", TimeCache.date, TimeCache.hour);
		SimpleFileWriter logFile = SimpleFileWriter.getInstance(filePath);
		return logFile;
	}
	
	private String queueName;
	
	public QueueThread(String queueName) {
		this.queueName = queueName;
	}
	
	public void quit() {
		loop = false;
	}
	
	public void run() {
		
		RedisQueue queue = RedisQueue.getInstance();

		String line;
		int date = TimeCache.date*100+TimeCache.hour;
		
		SimpleFileWriter logFile = getLogFile();
		
		int nThreads = Configuration.getInstance().getInt("server.threads", 1);
		
		ExecutorService executorService = null;
		if(nThreads > 1) {
			executorService = Executors.newFixedThreadPool(nThreads);
		} else if(nThreads == 0) {
			executorService = Executors.newCachedThreadPool();
		}
		
		while (loop) {
			line = queue.pop(queueName);
			if (line == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ie) {
				}
				continue;
			}
			if(TimeCache.date != date) {
				logFile.close();
				date = TimeCache.date*100+TimeCache.hour;
				logFile = getLogFile();
			}
			try {
				logFile.write(line + '\n');
			} catch (IOException e1) {
			}
			// 2014-10-14 00:00:01 581.831.143.310 sdk active offer_wall
			if(executorService == null) {
				try {
					TaskRunnable.exec(line);
				} catch (Exception e) {
					LOG.trace("exec "+e);
				}
			} else {
				executorService.execute(new TaskRunnable(line));
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
