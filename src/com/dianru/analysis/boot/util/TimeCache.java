package com.dianru.analysis.boot.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeCache extends TimerTask{

	public static Logger LOG = LogManager.getLogger(ReportTimer.class);
	public static long time = 0;
	public static int  date;
	public static int  year;
	public static int  month;
	public static int  day;
	public static int  hour;

    public void run(){
    	update();
    }
    
    public static void update() {
    	long mts = System.currentTimeMillis();
    	
    	time = mts/1000;
    	
    	Calendar cal = new GregorianCalendar();
    	cal.setTimeInMillis(mts);
    	
    	year = cal.get(Calendar.YEAR);
    	month = cal.get(Calendar.MONTH)+1;
    	day = cal.get(Calendar.DAY_OF_MONTH);
    	hour = cal.get(Calendar.HOUR_OF_DAY);
    	date = year*10000 + month*100 + day;
    }
    
    private static Timer timer = null;
    private static TimeCache timeCached = new TimeCache();
    
    public static void start() {
    	if(timer != null) return;
  
    	update();
    	
    	timer = new Timer();
        timer.schedule(timeCached, 1000, 1000);
    }
    
    public static void stop() {
    	if(timer != null) {
    		timer.cancel();
    		timer = null;
    	}
    }
}
