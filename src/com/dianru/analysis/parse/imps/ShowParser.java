package com.dianru.analysis.parse.imps;

import java.util.ArrayList;
import java.util.List;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.parse.BaseParser;

public class ShowParser extends BaseParser implements Define.SourceIndex {

	public static interface Index extends Define.SourceIndex {
		public static int IDS = 22;
	}
	
	public final static List<String> FIELD_LIST = new ArrayList<String>();
	static {
		for(String field: Define.FIELDS) FIELD_LIST.add(field);
		for(String field: Define.SOURCE_FIELDS) FIELD_LIST.add(field);
		
		FIELD_LIST.add(Define.IDS);
	};

	@Override
	protected List<String> fields() {
		return FIELD_LIST;
	}
	
	public static void main(String[] args) {
		
		
		String line = "2016-01-24 00:00:19 223.102.2.0 sdk show offer_wall \"uid=10465&appid=8079&os=ios&osver=9.0.2&device=iPhone&mac=02:00:00:00:00:00&udid=67DA2E97-7706-4143-9610-6A67BB0D96B2&openudid=b90a0a2cac04b64ffd52b2f819e82ed19845c6d0&root=false&appuserid=719397&ids=111&session=&version=1.3&model=iPhone8,2\"";
		
		ShowParser p = new ShowParser();
		List<Object> vals = p.parse(line);
		System.out.println(vals);
		
		int action = (int) vals.get(ShowParser.Index.ACTION);
		System.out.println(action);
	}
}
