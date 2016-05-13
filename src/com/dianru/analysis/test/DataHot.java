package com.dianru.analysis.test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DataHot {
	
	public static final Random RANDOM = new Random();
	private static int uids[] =      {6651, 8111, 7402, 8230 , 6854 , 1860, 5635, 5478, 4239, 7320, 8614,7334, 8744, 7137, 7015, 7881, 6557, 7093, 8082, 7250, 7684, 6572, 7840};
	private static int appids [] = {5607, 6863, 6370, 6985 , 5761 , 6672, 4927, 5990, 4241, 6915, 7224,6379, 7253, 6087, 5987, 6631, 6232, 5997, 6855, 6163, 6479, 5707, 6709};
	
	private static int  cids[] = {1221, 1221, 1221, 1221};
	private static int  adids[] = {10547, 10548, 10549, 10550};
	
	private static int cid; 
	private static int adid;
	
	public static int getRand( int min, int max){
		int result = min + (int)(Math.random() * ((max - min) + 1));
		return result;
	}
	
	public static void dateDiff(int  total, String start, String end){
		
		System.out.println("total = " + total);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try{
		    Date d1 = df.parse(end);
		    Date d2 = df.parse(start);
		    long diff = d1.getTime() - d2.getTime();
		    long days = diff / (1000 * 60 * 60 * 24) + 1;
		    System.out.println("days = " + days);
		    
		    Integer dayCount[] = new Integer[(int) days];
		    int sum = 0;
		    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		    for(int i = 0; i < days; i++){
		    	int klDiff = getRand(50, 150);
		    	sum += klDiff;
		    	map.put(i, klDiff);
		    }
		    int avgs = (int) ((total - sum) / days);
		    int sumDay = 0;
		    Integer[] keys = map.keySet().toArray(new Integer[0]);
		    for (int i = 0; i < keys.length; i++) {
		    	int day = avgs + map.get(i);
		    	sumDay += day;
		    	
		    	dayCount[i] = day;
		    }
		    if(total != sumDay){
		    	dayCount[keys.length - 1] = dayCount[keys.length - 1] + (total - sumDay);
		    }
		    
		    //int tmp = 0;
		    long tmpDayTime = 0;
		    for (int i = 0; i < keys.length; i++) {
		    	//tmp += dayCount[i];
		    	
		    	long dayTime = d2.getTime() + tmpDayTime;
		    	String d = df.format(dayTime);
		    	
		    	tmpDayTime += 1000 * 60 * 60 * 24;
		    	System.out.println("第" + d + "天激活： " + dayCount[i]);
				getHourRand(dayCount[i] , d);
		    }
		    //System.out.println("sumDay = " + tmp);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	//获取天的随机数据
	public static Integer[] getHourRand (int dayCount, String date){
		
		Integer[] hours = new Integer[24];
		int sum = 0;
		for(int hour = 0 ;hour < 24 ;hour ++ ){
			int tmp = 0;
			if(hour >= 0 && hour <= 7){						//0 - 7  	0.01         0.08
				 tmp = (int) (dayCount * 0.01);
			}else if(hour >= 8 && hour <= 9){				//8 - 9  	0.04	     0.08  
				 tmp = (int) (dayCount * 0.04);
			}else if(hour == 10){ 										//10 - 11		 0.04     0.04
				tmp = (int) (dayCount * 0.04);
			}else if(hour == 11){ 										//10 - 11		 0.05   	0.05
				tmp = (int) (dayCount * 0.05);
			}else if(hour >=12 && hour <= 15){				// 12 - 15			 0.12     0.48
				tmp = (int) (dayCount * 0.12);
			}else if(hour >= 16 && hour <= 20){			//16 - 20 		0.04      0.20
				tmp = (int) (dayCount * 0.04);
			}else if(hour >= 21 && hour <= 22){			//21 - 22 		0.03      0.06
				tmp = (int) (dayCount * 0.03);
			}else if(hour >= 23){										//23 		0.01      0.01
				tmp = (int) (dayCount * 0.01);
			}
			tmp = getRand(tmp-5 < 0?0:tmp-5, tmp + 5);
			hours[hour] = tmp;
			sum += tmp;
		}
		if(dayCount > sum) {
			hours[11] = hours[11] + (dayCount - sum);
		}
		
		int index = RANDOM.nextInt(appids.length - 1);
		//随机媒体
		int appid = appids[index];
		int uid = uids[index];
		
		int lastSum = 0;
		for(int i=0; i<hours.length; i++){
			lastSum += hours[i];
			
			Hour h = new Hour();
			h.setAppid(appid);
			h.setUid(uid);
			
			h.setAdid(adid);
			h.setCid(cid);
			
			//激活
			int active = hours[i];
			h.setActive_count(active);
			int active_unique = (int) (active * ((double)getRand(60, 90) / 100));
			h.setActive_unique(active_unique);
			int active_saved = (int) (active * ((double)getRand(40, 90) / 100));
			h.setActive_saved(active_saved);
			
			//随机点击
			int clickRand = getRand(7, 20);
			int click =  active * (100 / clickRand);
			h.setClick_count(click);
			int click_unique = (int) (click * ((double) getRand(30, 50) / 100));
			h.setClick_unique(click_unique);
			int click_saved = (int) (click_unique * ((double) getRand(70, 80) / 100));
			h.setClick_saved(click_saved);
			
			//随机展示
			int showRand = getRand(2, 7);
			int show =  click * (100 / showRand);
			h.setShow_count(show);
			int show_unique = (int) (show * (double)getRand(30, 50) / 100);
			h.setShow_unique(show_unique);
			int show_saved = (int) (show_unique * (double)getRand(70, 90) / 100);
			h.setShow_saved(show_saved);
			//System.out.println(click +" - "+ click_unique + " - " + h.getClick_saved());
			
			//设置小时
			h.setHour(i);
			
			String sql = h.getSql(3, date);
			//System.out.println(sql);
			
			String filePath = "/Users/liuhuiya/Documents/data";
			String fileName = adid + ".sql";
			try {
				Futil.createFile(filePath, fileName, sql, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//cpa_media_hour_201411     cpa_media_hour_201411
		//cpa_hour_201411     cpa_hour_201412
		//cpa_hour
		//cpa_ad_hour_201411     cpa_ad_hour_201412
		
		System.out.println("分布到消失的总数：" + lastSum);
		return hours;
	}
	
	public static void main(String[] args) throws IOException {
		
		/*
		B 10547   全民枪战			10275
		C 10548   全民封神			10291
		D 10549   超级英雄			10173
		A/E 10550   上行快线			10091
		*/
		
		cid = cids[3];
		adid = adids[3];
		dateDiff(23996,"20141201", "20141231");
	}
}
