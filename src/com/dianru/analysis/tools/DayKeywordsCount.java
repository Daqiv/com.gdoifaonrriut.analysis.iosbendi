package com.dianru.analysis.tools;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.util.Configuration;
import com.dianru.analysis.util.DateUtils;
import com.dianru.analysis.util.FileUtils;
import com.dianru.analysis.util.SQLConnection;

/**
 * 统计使用关键词激活任务的次数（使用shell定时调用）
 * 
 * @author wangff
 */
public class DayKeywordsCount {

	private static Logger LOG = LogManager.getLogger(DayKeywordsCount.class);
	
	public static String WORKDIR = Configuration.getInstance().getProperty("path.workdir", "/tmp/workdir/store");// 统计时间间隔存放目录
	public static String intevalFilePath ="/IntervalTime/interval.txt"; //文件位置
	
	private static final String SQL_QUERY_INSERTALL = "INSERT INTO cpa_keywords_count_%s (adid, keywords, num, count_date) "
			+ "SELECT adid, keywords, COUNT(*) num, %s count_date FROM `cpa_callback_%s` "
			+ "WHERE action = 4  AND create_time > %s AND keywords IS NOT NULL and keywords <> ' ' GROUP BY adid, keywords ON DUPLICATE KEY UPDATE num = values(num)+num;";
	

	public static boolean execute(String dateTime) {

		boolean flag = false;
		String year = dateTime.substring(0, 4);
		String lastMillis = "0";

		// 创建目录
		String filePath = WORKDIR + intevalFilePath ;
		File file = new File(filePath);
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// 表示取得cpa的conn
		SQLConnection conn = SQLConnection.getInstance("offerwall");
		try {
			
			String result = FileUtils.readFile(filePath) ;
			
			Long tTime = 0L ;		// 明天的00:00时间
			
			if(result != null && !"".equals(result)) {
				String[] temp = result.split(",") ;
				if(temp.length == 2) {
					tTime = new Long(temp[1].substring(0, temp[1].length()-1)) ;
					lastMillis = temp[0];
				}
			} else {
				tTime = getTomorrowSecond() / 1000 ;
			}
			
			// 如果当前时间大于明天凌晨时间，则更新昨天数据与今天数据
			if(System.currentTimeMillis()/1000 - tTime > 0) {
				
				// 重置file中的tomm时间
				try {
					FileUtils.writeFile(filePath, System.currentTimeMillis() / 1000 +","+String.valueOf(getTomorrowSecond() / 1000));
				} catch (Exception e) {
					LOG.error("Fail reset tomorrow Zero Time" + e.getMessage());
				}
				// 更新昨天的数据,查询关键词量级表数据
				execSql(year, DateUtils.getBeforeDate(1), lastMillis) ;
				// 更新今天的数据,查询关键词量级表数据
				execSql(year, dateTime, lastMillis) ;
			} else {
				// 写入文件执行时间
				try {
					FileUtils.writeFile(filePath, System.currentTimeMillis() / 1000 +","+String.valueOf(tTime));
				} catch (Exception e) {
					LOG.error("Fail write FileTime " + e.getMessage());
				}
				execSql(year, dateTime, lastMillis) ;
			}

		} catch (IOException e1) {
			LOG.error("keywords count error: " + e1) ;
		} finally {
			if (conn != null)
				conn.close();
		}

		return flag;
	}
	
	
	
	/*
	 * 执行sql查询
	 */
	public static void execSql(String year, String dateTime, String lastMillis) {
		
		SQLConnection conn = null ;
		String qSql = null ;
		
		try {
			// 表示取得cpa的conn
			conn = SQLConnection.getInstance("offerwall");
			
			// 查询关键词量级表数据
			qSql = String.format(SQL_QUERY_INSERTALL, year, dateTime,
					dateTime, lastMillis);
			// 执行
			// 记录执行时间，这种情况可能会多统计数据
			// writeQueryTime(file) ;
			LOG.info("execute --> " + qSql);
			conn.execute(qSql);
		} catch (Exception e) {
			LOG.error("error:" + qSql + e);
		} finally {
			if(conn!=null) conn.close() ; 
		}
		
	}
	
	
	/**
	 * 获取明天的00:00毫秒值
	 * @return
	 */
	public static long getTomorrowSecond() {
		
		Long result = null ;
		
		Calendar c = new GregorianCalendar() ;
		c.setTime(new Date()) ;
		c.add(Calendar.DATE, 1) ;	// 加1天
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd") ;
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		String tomm = sdf1.format(c.getTime()) ;
		tomm = tomm + " 00:00:00" ;
		try {
			Date date = sdf2.parse(tomm) ;
			result = date.getTime() ;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result ;
	}

	
	public static void main(String[] args) {
		String dateTime = "";
		if (args.length == 1) {
			dateTime = args[0];
		} else {
			dateTime = String.valueOf(DateUtils.getYYYYMMDD());
			// 统计关键词数据
			execute(dateTime);
		}
		
	}
}
