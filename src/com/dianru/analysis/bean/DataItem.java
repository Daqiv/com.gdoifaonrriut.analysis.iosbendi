package com.dianru.analysis.bean;

public class DataItem {
	
	public final static int DATA_TYPE_UNKNOW 	= 0x00; 
	public final static int DATA_TYPE_SHOW 		= 0x01;//展示
	public final static int DATA_TYPE_CLICK 	= 0x02;//点击
	public final static int DATA_TYPE_SCLICK 	= 0x03;//二次点击
	public final static int DATA_TYPE_CALLBACK  = 0x04;//回调
	
	public final static String DEF_MAC = "";
	
	public static int parseDataType(String type) {
		int result = DATA_TYPE_UNKNOW;
		if("show".equals(type)) {
			return DATA_TYPE_SHOW;
		} else if ("click".equals(type)) {
			return DATA_TYPE_CLICK;
		} else if ("sclick".equals(type)) {
			return DATA_TYPE_SCLICK;
		} else if ("callback".equals(type)) {
			return DATA_TYPE_CALLBACK;
		}
		return result;
	}
}
