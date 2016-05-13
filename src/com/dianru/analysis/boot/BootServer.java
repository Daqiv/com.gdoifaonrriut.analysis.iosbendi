package com.dianru.analysis.boot;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.dianru.analysis.boot.util.ObjectCache;
import com.dianru.analysis.boot.util.ReportTimer;
import com.dianru.analysis.boot.util.TimeCache;
import com.dianru.analysis.store.FileStore;
import com.dianru.analysis.store.SimpleFileWriter;
import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.ProcessUtil;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class BootServer {
	
	public static Logger LOG = LogManager.getLogger(BootServer.class);
	
	public static boolean SERVER_LOOP = true;
	
	public static class ShutdownSignal implements SignalHandler {

		@Override
		public void handle(Signal sign) {
			SERVER_LOOP = false;
			LOG.info("server recv signal : " + sign.getName());
		} 
	}
	
	public static void main(String[] args) {
		
		String filePath = Configuration.getInstance().getProperty("path.app.pid", "");
		File fileForPid = new File(filePath);
		if(fileForPid.exists()) {
			LOG.error("server start error pid file : "+fileForPid+" exists");
		}
		
		int pid = ProcessUtil.saveProcessId(fileForPid);
		LOG.info("server start with pid : " + pid);
		
//		Signal sign = new Signal("USR2");
//        Signal.handle(sign, new ShutdownSignal()); 
		
		TimeCache.start();
		ReportTimer.start();
		ObjectCache.start();
		
		QueueThread otherQueueThread = new QueueThread("ACTION_UP_REPORT");
		QueueThread showQueueThread = new QueueThread("ACTION_SHOW_REPORT");
		GiveUpThread giveUpThread = new GiveUpThread("ACTION_GIVE_UP");
		
		otherQueueThread.start();
		showQueueThread.start();
		giveUpThread.start();
		
		while(SERVER_LOOP) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		showQueueThread.quit();
		otherQueueThread.quit();
		giveUpThread.quit();
		
		try {
			showQueueThread.join();
			otherQueueThread.join();
			giveUpThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
