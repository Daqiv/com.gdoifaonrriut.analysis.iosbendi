package com.dianru.analysis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.parse.util.ParseUtil;
import com.dianru.analysis.parse.util.Token;

public class IP2Location {

	public static IPInfo DEF_INFO = new IPInfo(0L, 0xffffffffL, Define.DEF_ISP,
			Define.DEF_CITY);

	private static String filePath = "ipinfo.txt";
	private static IPInfo[] INFOS;

	public static class IPInfo {
		public long begin;
		public long end;

		public String location;
		public String isp;

		public IPInfo(long begin, long end, String location, String isp) {
			this.begin = begin;
			this.end = end;
			this.location = location;
			this.isp = isp;
		}
	}

	/**
	 * 初始化ip库
	 */
	private static void load() {
		try {
			IPInfo items[];

			ArrayList<IPInfo> infos = new ArrayList<IPInfo>();
			InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String temp = null;
			temp = br.readLine();
			while (temp != null) {
				// 进行对象封装
				String[] ips = temp.split(" ");
				infos.add(new IPInfo(Long.valueOf(ips[0]),
						Long.valueOf(ips[1]), ips[2], ips[3]));
				temp = br.readLine();
			}
			br.close();
			items = infos.toArray(new IPInfo[infos.size()]);
			Arrays.sort(items, new Comparator<IPInfo>() {
				@Override
				public int compare(IPInfo o1, IPInfo o2) {
					long o1long = o1.end - o1.begin;
					long o2long = o2.end - o2.begin;
					if (o1long > o2long) {
						return 1;
					} else if (o1long < o2long) {
						return -1;
					} else {
						return o1.begin > o2.begin ? 1
								: o1.begin < o2.begin ? -1 : 0;
					}
				}
			});
			INFOS = items;
		} catch (IOException e) {

		}
	}

	static {
		load();
	}

	public static long ip2long(String ip) {
		Token<Long> token = ParseUtil.parseIP(ip.toCharArray(), 0, ip.length());
		if (token == null)
			return -1;

		return token.data;
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

	public static IPInfo find(long ip) {
		if (INFOS != null) {
			// 状态用来标记Ip库中是否拥有当前Ip信息，默认flase 没有
			for (IPInfo info : INFOS) {
				if (ip >= info.begin && ip <= info.end) {
					return info;
				}
			}
		}
		return DEF_INFO;
	}

	public static IPInfo find(String ipstr) {
		long ip = ip2long(ipstr);
		return find(ip);
	}
	
	public static void main(String[] args) {
		System.out.println(ip2long("117.24.24.251"));
	}
}
