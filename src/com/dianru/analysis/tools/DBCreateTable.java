package com.dianru.analysis.tools;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.SQLConnection.DataSource;

/**创建表
 * @author WenYunlong
 * @date 2014年10月8日
 */
public class DBCreateTable {
	
	public static Logger LOG = LogManager.getLogger(DBCreateTable.class);
	
	public final static int TYPE_DAY = 1;
	public final static int TYPE_MONTH = 2;
	public final static int TYPE_YEAR = 3;

	public final static String[] DATASOURCE_NAMES = new String[]{
		"offerwall","insertscreen"
		/*,"freewall","fullscreen","banner"*/
	};

	private static final String[] TABLE_NAMES = new String[]{
		"callback","hour","day","ad_hour","ad_day","media_hour","media_day","keywords_count"
	};
	
	private static final int[] TABLE_TYPES = new int[] {
		1, 2 , 3, 2, 3, 2, 3, 3
	};
	
	public final static void createTablesByDate(String dsname, int date) {
		DataSource ds = SQLConnection.getDataSource(dsname);
		String prefix = ds.getPrefix();
		
		SQLConnection conn = SQLConnection.getInstance(dsname);
		
		for(int i=0; i<TABLE_NAMES.length; i++) {
			String tname = TABLE_NAMES[i];
			int type = TABLE_TYPES[i];
			
			int mon = (date/100)%100;
			int day = date%100;

			// keywords_count只在cpa中创建
			if("cpc".equals(prefix) && "keywords_count".equals(tname)) {
				continue ;
			}
			
			int split = 0;
			switch(type ) {
			case TYPE_DAY:
				split = date;
				break;
			case TYPE_MONTH:
				split = date/100;
				if(day != 1) continue;
				break;
			case TYPE_YEAR:
				split = date/10000;
				if(mon != 1 || day != 1) continue;
				break;
			default:
				return;
			}
			
			String table = String.format("%s_%s_%d", prefix, tname, split);
			String template = String.format("tpl_%s", tname);
			String sql = String.format("CREATE TABLE `%s` LIKE `%s`", table, template);
			
			System.out.println(sql);
			
			
			conn.execute(sql);
		}
		conn.close();
	}
	
	public static int getCurDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, 1);
		
		int y = cal.get(Calendar.YEAR);
		System.out.println("当前年份:" + y);
		int m = cal.get(Calendar.MONTH)+1;
		System.out.println("当前月份:" + m);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		System.out.println("当前天数:" + d);
		return y*10000 + m*100 + d;
	}

	public static void main(String[] args) {

		if(args.length != 0 && args.length != 1){
			System.out.println("CreateTables [20140101]\n");
			return;
		}
		int date;
		if(args.length == 0) {
			date = getCurDate();
		} else {
			date = Integer.parseInt(args[0]);
		}
		for(String name : DATASOURCE_NAMES) {
			createTablesByDate(name, date);
		}
	}
}
