package com.dianru.analysis.tools;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.util.SQLConnection;

public class MonthlyMoney {

	//public static String SQL_MEDIA_QUERY = "SELECT uid,sum(click_cost)+sum(active_cost)+sum(job_cost) as money FROM %s_media_day_%d WHERE created>=%d AND created<%d (click_cost>0 OR active_cost>0 OR job_cost>0) group by uid";
	//将开发者可提现余额转入到开发者余额
	
	public static String SQL_MEDIA_ALL = "UPDATE user_num SET appusebalance=appbalance";
	public static String SQL_MEDIA_UPDATE = "UPDATE user_num SET appusebalance=appusebalance-%f WHERE uid=%d";
	
	public static Logger LOG = LogManager.getLogger(DailyClean.class);

	public static void updateUserNum(String dbname, String type, int year, int date, int today, boolean isFirst) {

		SQLConnection conn = SQLConnection.getInstance(dbname);
		SQLConnection main = SQLConnection.getInstance("main");
		
		if(isFirst) {
			int updated = main.update(SQL_MEDIA_ALL, null);
			LOG.info("set all user appusebalance appbalance " + updated);
		}
		
		/*
		String sqlMediaQuery = String.format(SQL_MEDIA_QUERY, type, year, date, today);
		
		List<Map<String, Object>> ms = conn.queryMap(sqlMediaQuery, null);
		for(Map<String,Object> item : ms) {
			int uid = (int)item.get("uid");
			double money = (double)item.get("money");
			
			String sql = String.format(SQL_MEDIA_UPDATE, money,uid);
			
			int row = main.update(sql, null);
			
			LOG.info("app user "+uid+" appusebalance sub "+money+" result : "+row);
		}
		*/
		
		main.close();
		conn.close();
	}
	
	public static void main(String[] args) {
		
    	long mts = System.currentTimeMillis();
    	Calendar cal = new GregorianCalendar();
    	cal.setTimeInMillis(mts);

    	int year = cal.get(Calendar.YEAR);
    	int month = cal.get(Calendar.MONTH)+1;
    	int tday = cal.get(Calendar.DAY_OF_MONTH);
    	int day = 1;
    	//月初
		int date = year*10000+month*100+day;
		//今天
		int today = year*10000+month*100+tday;
		
		updateUserNum("offerwall","cpa",year,date, today, true);
		updateUserNum("insertscreen","cpc",year,date, today, false);

		LOG.info("daily money cron");
		
		
	}
}
