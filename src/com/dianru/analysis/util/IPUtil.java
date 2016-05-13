package com.dianru.analysis.util;

public class IPUtil {
	
	public static long ip2long(String ip) {
		String[] ps = ip.split("\\.");
		if (ps.length != 4) {
			return 0;
		}
		long addr = Long.valueOf(ps[0]);
		for(int i=1;i<4;i++) {
			addr <<= 8;
			addr |= Long.valueOf(ps[i]);
		}

		return addr;
	}

	public static String long2ip(long ip) {
		StringBuilder sb = new StringBuilder();
		sb.append((ip >> 24) & 0xff);
		sb.append('.');
		sb.append((ip >> 16) & 0xff);
		sb.append('.');
		sb.append((ip >> 8) & 0xff);
		sb.append('.');
		sb.append(ip & 0xff);
		return sb.toString();
	}
}
