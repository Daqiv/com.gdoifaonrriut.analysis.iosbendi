package com.dianru.analysis.util;

public class MACUtil {
	
	public static String parse(String mac) {
		
		if(mac == null) return null;
		mac = mac.trim();
		if(mac.isEmpty()) return null;

		if(mac.equals("02:00:00:00:00:00")) return null;
		if(mac.equals("020000000000")) return null;
		
		return mac;
	}
}
