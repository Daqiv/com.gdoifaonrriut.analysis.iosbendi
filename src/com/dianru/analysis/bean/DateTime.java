package com.dianru.analysis.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.dianru.analysis.process.BaseProcessor.Index;
import com.dianru.analysis.util.ListUtil;

public class DateTime {
	
	public static byte CTL_SEC	= 0;
	public static byte CTL_MIN 	= 1;
	public static byte CTL_HOUR = 2;
	public static byte CTL_DAY 	= 3;
	public static byte CTL_MON 	= 4;
	public static byte CTL_YEAR = 5;
	
	public int year;
	public int mon;
	public int day;
	
	public int hour;
	public int min;
	public int sec;
	
	public int control = CTL_SEC;
	
	public DateTime(List<Object> vals) {
		this.year = ListUtil.getInt(vals, Index.YEAR);
		this.mon = ListUtil.getInt(vals, Index.MON);
		this.day = ListUtil.getInt(vals, Index.DAY);
		
		this.hour = ListUtil.getInt(vals, Index.HOUR);
		this.min = ListUtil.getInt(vals, Index.MIN);
		this.sec = ListUtil.getInt(vals, Index.SEC);
	}
	
	public DateTime(int year,int mon, int day) {
		this.year = year;
		this.mon = mon;
		this.day = day;
	}
	
	public DateTime(int year,int mon, int day, int hour, int min, int sec) {
		this.year = year;
		this.mon = mon;
		this.day = day;
		
		this.hour = hour;
		this.min = min;
		this.sec = sec;
	}
	
	public int timestamp() {
		//int timestamp = countDay(year,mon,day)*24*60*60 + (int)hour*60*60 + (int)min*60 + sec;
		//timestamp -= TimeZone.getDefault().getRawOffset()/1000;
		GregorianCalendar cal = new GregorianCalendar(year,mon-1,day,hour,min,sec);
		return  (int)(cal.getTimeInMillis()/1000);
		//return timestamp;
	}
	
	public int countYearDay(int year){
		int n=0;
		for(int i=1970;i<year;i++) {
			n += (i%4==0) ? 366 : 365;
		}
		return n;
	}
	
	public static int day(int year, int mon) {
		switch(mon) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 4:
		case 6:
		case 9:
		case 11:
			return 30;
		default:
			if(year%4==0) return 29;
			else return 28;
		}
	}
	
	public int countDay(int year, int month, int day) {
		int n = countYearDay(year);
		n += countMonthDay(year, month);
		n += (day-1);
		return n;
	}
	
	public int countMonthDay(int year,int month) {
		int n = 0;
		for(int i=1;i<month;i++) {
			n += day(year,i);
		}
		return n;
	}
	
	public Object clone()
	{
		DateTime dt = new DateTime(this.year, this.mon, this.day,this.hour, this.min, this.sec);
		dt.control = this.control;
		
		return dt;
	}
	
	public int hashCode() {

		int year = this.year-1970;
		int mon = this.mon-1;
		int day = this.day-1;
		
		if(control == CTL_YEAR) {
			return -year;
		} else if(control == CTL_MON) {
			return -year*12-mon;
		} else if(control == CTL_DAY) {
			return -countDay(year,mon,day);
		} else if(control == CTL_HOUR) {
			return -countDay(year,mon,day)*24-hour;
		} else if(control == CTL_MIN) {
			return -countDay(year,mon,day)*24*60-hour*24*60-min;
		}

		return timestamp();
	}
	
	public static int getDate(int add) {
    	long mts = System.currentTimeMillis();
    	Calendar cal = new GregorianCalendar();
    	cal.setTimeInMillis(mts);
    	if(add != 0) cal.add(Calendar.DAY_OF_MONTH, add);
    	
    	int year = cal.get(Calendar.YEAR);
    	int month = cal.get(Calendar.MONTH)+1;
    	int day = cal.get(Calendar.DAY_OF_MONTH);
    	
    	return year*10000 + month*100 + day;
	}
	
	public static void main(String args[]) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dd = sdf.parse("2014-10-01 12:13:54");

		DateTime dt = new DateTime((short)2014,(byte) 10,(byte)1,(byte)12,(byte)13,(byte)54);
		System.out.println(dt.timestamp());
		System.out.println(sdf.format(dd));
		System.out.println(dd.getTime()/1000);
	}
}
