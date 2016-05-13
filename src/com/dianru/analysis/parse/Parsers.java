package com.dianru.analysis.parse;

import java.util.HashMap;
import java.util.Map;

import com.dianru.analysis.parse.imps.ActionParser;
import com.dianru.analysis.parse.imps.CallbackParser;
import com.dianru.analysis.parse.imps.ShowParser;

public class Parsers {
	private final static Map<String,Parser> PARSER_MAP = new HashMap<String,Parser>();
	
	private final static ShowParser SHOW_PARSER = new ShowParser();
	private final static ActionParser ACTION_PARSER = new ActionParser();
	private final static CallbackParser CALLBACK_PARSER = new CallbackParser();
	
	private static String[] PARSER_NAMES;
	static {

		PARSER_MAP.put("show", SHOW_PARSER);
	
		PARSER_MAP.put("click", ACTION_PARSER);
		PARSER_MAP.put("jump", ACTION_PARSER);
		
		PARSER_MAP.put("active", CALLBACK_PARSER);
		PARSER_MAP.put("job", CALLBACK_PARSER);

		PARSER_NAMES = PARSER_MAP.keySet().toArray(new String[PARSER_MAP.size()]);
	}
	
	public final static  String[] names() {
		return PARSER_NAMES;
	}
	
	public final static Parser get(String name) {
		return PARSER_MAP.get(name);
	}
	
	public final static String parseType(String line) {
		int c = 0;
		int b = 0, e = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ') {
				c++;

				if (c == 4) {
					b = i;
				} else if (c == 5) {
					e = i;
					break;
				}
			}
		}
		int len = e - b;
		if (len == 0) {
			return null;
		}

		String name = line.substring(b + 1, e);
		return name;
	}
	
	public static void main(String[] args) {
		String line = "2016-04-07 18:59:17 106.2.202.117 sdk click lock_screen \"uid=107&appid=54&os=android&osver=4.1.2&device=&mac=24:69:a5:a7:93:5d&udid=A0000043E8E677&openudid=3BBC7339D511D72A8AF3E1D9E2D876A4&root=0&appuserid=54032&cid=17651&adid=9563&session=c51a501830cdb87171b2d09e4787656d&version=1.2.1&did=3a7a889ba81ff159195733d042992402&ssid=vfou&localip=192.168.199.195&disk=\"";
		System.out.println(parseType(line));
	}
}
