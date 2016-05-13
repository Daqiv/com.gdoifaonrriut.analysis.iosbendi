package com.dianru.analysis.bean;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import com.dianru.analysis.util.SQLConnection;

public class Common  {
	
	//实体数据类型转化
	public Object getValue(Map<String, Object> vals , String name , String type){
		Object val = vals.get(name);
		try {
			if(val != null){
				if(type == "float" ){
					DecimalFormat decimalFormat = new DecimalFormat("#.##");
					String tmp = decimalFormat.format(val);
					return Float.parseFloat(tmp);
				}else {
					return val;
				}
			}else{
				if(type == "int"){
					val = 0;
				}else if(type == "float" ){
					val = Float.valueOf(0);
				}else if(type == "string"){
					val = "";
				}
			}
			return val;
		} catch (Exception e) {
			return val;
		}
	}
	
	public static void main(String[] args) {
		SQLConnection conn = SQLConnection.getInstance("main");
		
		String sql = "SELECT `type` as data_type,space_type,ads.adid as adid,cid,ctype,cstype,clevel,sort,state,devices,begin,end,control,ad_plan.num installnum,ruleouts,citys,hours,root,options,price_click_income,price_click_cost,price_callback_income,price_callback_cost,price_job_income,price_job_cost,data_from,ruleins,adname,os,osver,budget,money,ads.num as ipnum,`interval`,boot_time_num,num_ad,interval_ad,boot_time_num_ad,rate,billing,levelouts,appstoreid,bundleid,is_talkingdata,keywords,is_aso,aso_pos,channel_num,fwz_levelouts,click_num,process_name,update_time,is_hs_flag,is_hs_report,jobs,is_hand_stop FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND update_time>?";
		List<Map<String,Object>> items = conn.queryMap(sql, new Object[] { 0 });
		if (!items.isEmpty()) {
			for (Map<String,Object> vals : items) {
				Ads ads = new Ads(vals);
				
				System.out.println(ads.getAdid() + ", " + ads.getBilling());
			}
		}
		conn.close();
	}
}