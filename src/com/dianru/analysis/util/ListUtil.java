package com.dianru.analysis.util;

import java.util.List;

public class ListUtil {
	
	public static int getInt(List<?> vals, int idx) {
		Object obj = vals.get(idx);
		if(obj == null) return 0;
		
		return Integer.parseInt(obj.toString());
	}
	
	public static long getLong(List<?> vals, int idx) {
		Object obj = vals.get(idx);
		if(obj == null) return 0;
		
		return Long.parseLong(obj.toString());
	}
	
	public static String getString(List<?> vals, int idx) {
		Object obj = vals.get(idx);
		if(obj == null) return "";
		
		return (String)obj;
	}

	public static float getFloat(List<Object> vals, int idx) {
		Object obj = vals.get(idx);
		if(obj == null) return 0.0f;
		
		return (float)obj;
	}
}
