package com.dianru.analysis.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class JsonUtil {
	public static Logger LOG = LogManager.getLogger(JsonUtil.class);
	
	public static JSONObject getJson(){
		return new JSONObject(); 
	}
	
	public static JSONObject getJson(String jsonStr,String mid){
		if(jsonStr==null||"".equals(jsonStr)){
			return getJson();
		}
		JSONObject json = null;
		try {
			json = new JSONObject(jsonStr);
		} catch (Exception e) {
			json = getJson();
			LOG.error(mid + "||||" + e);
		}
		return json;
	}
	public static JSONObject getJson(String jsonStr){
		if(jsonStr==null||"".equals(jsonStr)){
			return getJson();
		}
		JSONObject json = null;
		try {
			json = new JSONObject(jsonStr);
		} catch (Exception e) {
			json = getJson();
			LOG.error(e);
		}
		return json;
	}
	
	public static JSONObject getJson(Map<?, ?> map){
		if(map.isEmpty()){
			return getJson();
		}
		JSONObject json = null;
		try {
			json = new JSONObject(map);
		} catch (Exception e) {
			json = getJson();
			LOG.error(e);
		}
		return json;
	}
}
