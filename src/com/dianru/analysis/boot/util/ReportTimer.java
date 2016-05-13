package com.dianru.analysis.boot.util;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.count.Counter;
import com.dianru.analysis.count.store.CountStoreMap;
import com.dianru.analysis.tools.MinuteAdsPlan;

public class ReportTimer extends TimerTask{

	public static Logger LOG = LogManager.getLogger(ReportTimer.class);
	
    public void run(){
    	LOG.info("begin report to database...");
    	CountStoreMap store = Counter.getInstance().switchStore();
    	if(store == null) {
    		LOG.info("count map is empty");
    		return;
    	}
    	store.save();
    	
    	//每分钟Dump数据后，重新核算剩余量和激活量
		MinuteAdsPlan.pauseAdByCount(0);
		LOG.info("report to database done");
    }
    
    private static Timer timer = null;
    private static ReportTimer storeTask = new ReportTimer();
    
    public static void start() {
    	if(timer != null) return;
    	
    	timer = new Timer();
        timer.schedule(storeTask, 60000, 60000);
    }
    
    public static void stop() {
    	
    	if(timer != null) {
    		timer.cancel();
    		timer = null;
    	}
    	
    	storeTask.run();
    }
}
