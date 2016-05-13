package com.dianru.analysis.parse.imps;

import java.util.ArrayList;
import java.util.List;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.parse.BaseParser;
import com.dianru.analysis.util.ListUtil;

public class ActionParser extends BaseParser {

	public static interface Index extends Define.SourceIndex {
		public static int CID = 22;
		public static int ADID = 23;
		public static int SESSION = 24;
		public static int MODEL = 25;
		public static int DID = 26;
		public static int SSID = 27;
		public static int LOCALIP = 28;
		public static int DISK = 29;
		public static int RLOAD = 30;
		public static int ROOT = 31;
		public static int TIME=32;
		public static int UPTIME=33;
	}

	public final static List<String> FIELD_LIST = new ArrayList<String>();
	static {
		for (String field : Define.FIELDS)
			FIELD_LIST.add(field);
		for (String field : Define.SOURCE_FIELDS)
			FIELD_LIST.add(field);
		
		FIELD_LIST.add(Define.CID);
		FIELD_LIST.add(Define.ADID);
		FIELD_LIST.add(Define.SESSION);
		FIELD_LIST.add(Define.MODEL);
		FIELD_LIST.add(Define.DID);
		FIELD_LIST.add(Define.SSID);
		FIELD_LIST.add(Define.LOCALIP);
		FIELD_LIST.add(Define.DISK);
		FIELD_LIST.add(Define.RLOAD);
		FIELD_LIST.add(Define.ROOT);
		FIELD_LIST.add(Define.TIME);
		FIELD_LIST.add(Define.UPTIME);
	};

	@Override
	protected List<String> fields() {
		return FIELD_LIST;
	}

	public static void main(String[] args) {
		
		String line = "2016-03-10 08:58:42 114.112.195.66 sdk click offer_wall \"uid=10465&appid=8527&os=ios&osver=8.4&device=iPhone&mac=02:00:00:00:00:00&udid=0AF93A4D-1A3A-41D5-B0D3-03B4C434C1DA&openudid=721b96e13e00b471cbd461b78850cccf35806c42&root=0&appuserid=580872&cid=13145&adid=16233&session=ea4651882a959e7969a888d046cc996d&version=1.3&model=iPhone-5&did=bc875c4522dfc6a93be2ee437a28c95e&ssid=RElBTlJVX0dPLWMwOmEwOmJiOmNlOmMyOmMw&localip=192.168.1.126&disk=&rload=&time=1457571522&uptime=1451703744\"";
		
		ActionParser p = new ActionParser();
		List<Object> vals = p.parse(line);
		System.out.println(vals);
		
		int time = ListUtil.getInt(vals, ActionParser.Index.TIME);
		int uptime = ListUtil.getInt(vals, ActionParser.Index.ADID);
		
		System.out.println(time + " - " + uptime);
		
	}	
}
