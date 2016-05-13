package com.dianru.analysis.parse.imps;

import java.util.ArrayList;
import java.util.List;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.parse.BaseParser;
import com.dianru.analysis.util.ListUtil;

public class CallbackParser extends BaseParser {

	public static interface Index extends Define.Index {
		public static int CID = 10;
		public static int ADID = 11;
		public static int MAC = 12;
		public static int UDID = 13;
		public static int OPENUDID = 14;
		public static int ACTIVE_NUM = 15;
		public static int DISK = 16;
	}
	
	public final static List<String> FIELD_LIST = new ArrayList<String>();
	static {
		for(String field: Define.FIELDS) FIELD_LIST.add(field);
		
		FIELD_LIST.add(Define.CID);
		FIELD_LIST.add(Define.ADID);
		FIELD_LIST.add(Define.MAC);
		FIELD_LIST.add(Define.UDID);
		FIELD_LIST.add(Define.OPENUDID);
		FIELD_LIST.add(Define.ACTIVE_NUM);
		FIELD_LIST.add(Define.DISK);
	};
	
	@Override
	protected List<String> fields() {
		return FIELD_LIST;
	}
	
	public static void main(String[] args) {
		
		String line = "2015-07-08 18:03:21 127.0.0.1 process active offer_wall \"adid=123&mac=02:00:00:00:00:00&udid=afdafd-afAACA4A2FF5E&openudid=0eb087e9b65d2e30ca1c8fc7f6cf389f5350d225&active_num=12123&disk=7270387712\"";
		
		CallbackParser p = new CallbackParser();
		List<Object> vals = p.parse(line);
		System.out.println(vals);
		
		String udid = ListUtil.getString(vals, CallbackParser.Index.UDID);
		System.out.println("udid : " + udid);
	}	
}