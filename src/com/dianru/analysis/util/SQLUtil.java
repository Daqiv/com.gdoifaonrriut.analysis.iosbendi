package com.dianru.analysis.util;

public class SQLUtil {
	
	public static String insert(String[] keys, Object[] vals) {
		if(keys.length != vals.length) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(int i=0;i<keys.length;i++) {
			String key = keys[i];
			if(i != 0) sb.append(",");
			
			sb.append(key);
		}
		sb.append(")VALUES(");
		
		for(int i=0;i<vals.length;i++) {
			Object val = vals[i];
			if(i != 0) sb.append(",");
			
			sb.append('\'');
			sb.append(val);
			sb.append('\'');
		}
		sb.append(')');
		
		return sb.toString();
	}
	
	public static String insert(String[] keys1,String[] keys2, Object[] vals1, Object[] vals2) {
		if(keys1.length != vals1.length) return null;
		if(keys2.length != vals2.length) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(int i=0;i<keys1.length;i++) {
			String key = keys1[i];
			if(i != 0) sb.append(",");
			
			sb.append(key);
		}
		for(int i=0;i<keys2.length;i++) {
			String key = keys2[i];
			sb.append(",");
			sb.append(key);
		}
		sb.append(")VALUES(");
		
		for(int i=0;i<vals1.length;i++) {
			Object val = vals1[i];
			if(i != 0) sb.append(",");
			
			sb.append('\'');
			sb.append(val);
			sb.append('\'');
		}
		
		for(int i=0;i<vals2.length;i++) {
			Object val = vals2[i];
			sb.append(",");
			sb.append('\'');
			sb.append(val);
			sb.append('\'');
		}
		sb.append(')');
		
		return sb.toString();
	}
	
	public static String update(String[] keys, Object[] vals) {
		if(keys.length != vals.length) return null;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<keys.length;i++) {
			String key = keys[i];
			Object val = vals[i];
			if(i != 0) sb.append(",");
			
			sb.append(key);
			sb.append("='");
			sb.append(val);
			sb.append("'");
		}
		
		return sb.toString();
	}

	public static String add(String[] keys, Object[] vals) {
		if(keys.length != vals.length) return null;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<keys.length;i++) {
			String key = keys[i];
			Object val = vals[i];
			if(i != 0) sb.append(",");
			
			sb.append(key);
			sb.append("=(");
			sb.append(key);
			sb.append('+');
			sb.append(val);
			sb.append(")");
		}
		
		return sb.toString();
	}
	
	public static String where(String[] keys, Object[] vals) {
		
		if(keys.length != vals.length) return null;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<keys.length;i++) {
			String key = keys[i];
			Object val = vals[i];
			if(i != 0) sb.append(" AND ");
			
			sb.append(key);
			sb.append("='");
			sb.append(val);
			sb.append("'");
		}
		
		return sb.toString();
	}
}
