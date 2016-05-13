package com.dianru.analysis.bean;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Cheat {
	
	public static String KEY_OPENUDID = "is_openudid";
	public static String KEY_INSTALL = "redownload";
	public static String KEY_SSID = "is_ssid";
	public static String KEY_RATE = "cheat_buckle_proportion";
	public static String KEY_IG = "is_ig";
	public static String KEY_DISK = "is_disk";
	public static String KEY_SESSION = "is_session";
	public static String KEY_AD_RATE = "proportion";
	public static String KEY_PROCESS = "is_process_exception";
	
	/**
	 * media:
	 * {"is_openudid":1,"redownload":1,"is_ssid":1,"cheat_buckle_proportion":23,
	 *  "is_ig":1,"is_disk":1,"is_session":1,"is_process_exception":1}
	 * **/
	public final static String[] FILEDS = { KEY_OPENUDID, KEY_INSTALL, KEY_SSID,
		KEY_RATE, KEY_IG, KEY_DISK ,KEY_SESSION, KEY_PROCESS};
	
	/***
	 * ads:
	 * {"is_session":1,"is_openudid":1,"is_ssid":1,
	 *  "redownload":1,"proportion":54,"is_ig":1,"is_disk":1,"is_process_exception":1}
	 * */
	public final static String[] FILEDS_ADS = { KEY_OPENUDID, KEY_SSID, KEY_SESSION, 
			KEY_IG, KEY_DISK, KEY_AD_RATE, KEY_PROCESS};
	
	/**
	 * 对于没有的字段会自动封装为0
	 * @param options
	 * @return
	 */
	public static Map<String, Integer> parseMediaOptions(String options) {
		
		Map<String, Integer> MAP = new HashMap<String, Integer>();
		JSONObject optJson = null;
		try {
			optJson = new JSONObject(options);
			if (optJson != null) {
				for (String item : FILEDS) {
					try {
						//如果没有这个字段会直接报错
						if (optJson.get(item) != null) {
							MAP.put(item, Integer.parseInt(optJson.get(item).toString()));
						}
					} catch (Exception e) {
						MAP.put(item, 0);
					}
				}
			}
		} catch (ParseException e1) {
			for (String item : FILEDS) {
				MAP.put(item, 0);
			}
		}
		return MAP;
	}
	
	//add by chenjun
	public static Map<String, Integer> parseAdOptions(String options) {
		Map<String, Integer> MAP = new HashMap<String, Integer>();
		JSONObject optJson = null;
		try {
			optJson = new JSONObject(options);
			if (optJson != null) {
				for (String item : FILEDS_ADS) {
					try {
						if (optJson.get(item) != null) {
							MAP.put(item, Integer.parseInt(optJson.get(item).toString()));
						}
					} catch (Exception e) {
						MAP.put(item, 0);
					}
				}
			}
		} catch (ParseException e1) {
			for (String item : FILEDS_ADS) {
				MAP.put(item, 0);
			}
		}
		return MAP;
	}
	
	public static void main(String[] args) {
		String json = "{\"is_session\":0}";
		Map<String, Integer> MAP = parseAdOptions(json);
		System.out.println(MAP.get(Cheat.KEY_OPENUDID));
	}
}
