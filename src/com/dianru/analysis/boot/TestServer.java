package com.dianru.analysis.boot;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.boot.util.ObjectCache;
import com.dianru.analysis.boot.util.ReportTimer;
import com.dianru.analysis.boot.util.TaskRunnable;
import com.dianru.analysis.boot.util.TimeCache;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.RedisQueue;
import com.dianru.analysis.store.SimpleFileWriter;
import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.ProcessUtil;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class TestServer {

	public static Logger LOG = LogManager.getLogger(TestServer.class);

	private static boolean SERVER_LOOP = true;
	
	public static SimpleFileWriter getLogFile() {
		String filePath = String.format("data/%d/%02d.log", TimeCache.date, TimeCache.hour);
		SimpleFileWriter logFile = SimpleFileWriter.getInstance(filePath);
		return logFile;
	}

	public static void main(String[] args) {

		String filePath = Configuration.getInstance().getProperty("path.app.pid", "");
		File fileForPid = new File(filePath);
		if(fileForPid.exists()) {
			LOG.error("server start error pid file : "+fileForPid+" exists");
		}
		
		int pid = ProcessUtil.saveProcessId(fileForPid);
		
		LOG.info("server start with pid : " + pid);
		
		TimeCache.start();
		ReportTimer.start();
		ObjectCache.start();

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
		
		while (SERVER_LOOP) {

			LOG.trace("Queue begin popup...");
			line = queue.pop("ACTION_UP_REPORT");
			LOG.trace("Queue popup done");
			
			if (line == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

				LOG.trace("Queue is empty");
				continue;
			}

			if(TimeCache.date != date) {
				logFile.close();
				date = TimeCache.date*100+TimeCache.hour;
				logFile = getLogFile();
			}
			
			LOG.trace("Write log ...");
			try {
				logFile.write(line + '\n');
			} catch (IOException e1) {
			}

			// 2014-10-14 00:00:01 581.831.143.310 sdk active offer_wall
			if(executorService == null) {
				TaskRunnable.exec(line);
			} else {
				executorService.execute(new TaskRunnable(line));
			}
		}
		
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error("wait executor service exception : " + e.toString());
		}
		
		TimeCache.stop();
		ReportTimer.stop();
		ObjectCache.stop();

		SimpleFileWriter.closeAll();
		FileStore.closeAll();
		
		RedisConnection.shutdown();
		SQLConnection.shutdown();
		
		if(fileForPid.exists()) fileForPid.delete();
		LOG.info("server shutdown");
	}
}
