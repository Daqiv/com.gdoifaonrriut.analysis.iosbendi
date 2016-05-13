package com.dianru.analysis.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.bean.KeyValue;
import com.dianru.analysis.parse.util.ParseUtil;
import com.dianru.analysis.parse.util.Token;

public abstract class BaseParser implements Parser {
	
	public int parseFrom(String value) {
		for(int i=1;i<Define.FROMS.length;i++) {
			String v = Define.FROMS[i];
			if(v.equals(value)) return i;
		}
		return 0;
	}
	
	public int parseType(String value) {
		for(int i=1;i<Define.TYPES.length;i++) {
			String v = Define.TYPES[i];
			if(v.equals(value)) return i;
		}
		return 0;
	}
	
	public int parseAction(String value) {
		for(int i=1;i<Define.ACTIONS.length;i++) {
			String v = Define.ACTIONS[i];
			if(v.equals(value)) return i;
		}
		return 0;
	}
	
	protected boolean isDefinedISP(String val) {
		return Define.ISP_LIST.contains(val);
	}
	
	protected boolean isDefinedLocation(String val) {
		return Define.LOCATION_LIST.contains(val);
	}
	
	protected float parseFloat(String val) {
		Token<Float> token = ParseUtil.parseFloat(val.toCharArray(), 0, val.length());
		return token == null ? 0.0f : token.data;
	}

	protected int parseOS(String val) {
		val = val.toLowerCase();
		
		for(int i=0;i<Define.OSS.length;i++) {
			if(Define.OSS[i].equals(val)) return i;
		}
		return -1;
	}
	
	protected int parseDevice(String val) {

		val = val.toLowerCase();
		for(int i=0;i<Define.DEVICES.length;i++) {
			if(Define.DEVICES[i].equals(val)) return i;
		}
		return -1;
	}
	
	protected Object parseMAC(String val) {
		String d = "02:00:00:00:00:00";
		if(val.length() != 17 && val.length() != 12) return d;
		return val.length() == 0?d:val;
	}
	
	private Object parseUDID(String val) {
		
		if(val == null || val.length() == 0) return "";
		return val.toUpperCase();
	}
	
	private Object parseOpenUDID(String val) {
		return val;
	}
	
	public int parseInt(String val) {
		try {
			if(val.contains(".")){
				int i = val.indexOf(".");
				val = val.substring(0,i);
			}
			return Integer.parseInt(val);
		} catch(Exception e) {
			return -1;
		}
	}
	
	public long parseLong(String val) {
		try {
			if(val.contains(".")){
				int i = val.indexOf(".");
				val = val.substring(0,i);
			}
			return Long.parseLong(val);
		} catch(Exception e) {
			return -1;
		}
	}
	
	public int parseMediaType(int from) {
		if(from == 1 || from == 2) return 1;
		else if(from == 3) return 2;
		else return 0;
	}
	
	public Integer[] parseInts(String val) {
		if(val == null) return null;
		
		char[] chs = val.toCharArray();
		int end = chs.length;
		int i=0;
		
		List<Integer> ints = new ArrayList<Integer>();
		while(i<end) {
			Token<Integer> token = ParseUtil.parseInt(chs, i, end, ',');
			ints.add(token.data);
			i = token.end+1;
		}
		return ints.toArray(new Integer[ints.size()]);
	}
	
	public Object parseString(String val)  {
		return val == null || val.length() == 0 ? "" : val;
	}
	
	public Object parseField(String field, String val) {

		if(Define.OSVER.equals(field) || Define.VERSION.equals(field)) {
			return parseString(val);
		}
		else if(Define.OS.equals(field)) {
			return parseOS(val);
		}
		else if(Define.DEVICE.equals(field)) {
			return parseDevice(val);
		}
		else if(Define.MAC.equals(field)) {
			return parseMAC(val);
		}
		else if(Define.UDID.equals(field)) {
			return parseUDID(val);
		}
		else if(Define.OPENUDID.equals(field)) {
			return parseOpenUDID(val);
		}
		else if(Define.APP_USER_KEY.equals(field)) {
			return val == null ? "" : val;
		}
		else if(Define.UID.equals(field)) {
			return parseInt(val);
		}
		else if(Define.APPID.equals(field)) {
			return parseInt(val);
		}
		else if(Define.CID.equals(field)) {
			return parseInt(val);
		}
		else if(Define.ADID.equals(field)) {
			return parseInt(val);
		}
		else if(Define.IDS.equals(field)) {
			return parseInts(val);
		}
		else if(Define.DISK.equals(field)) {
			return parseLong(val);
		}
		return val;
	}
	
	public Map<String,Object> parseMap(String line) {
		
		if(line.charAt(0) == '#') return null;
		
		char[] chs = (line+" ").toCharArray();
		int end = chs.length-1;
		int i=0;
		int len = chs.length;
		//每次通过改变游标i进行获取对应对应的位置
		//获取时间
		Token<DateTime> datetime = ParseUtil.parseDateTime(chs,i, len);
		if(datetime == null || datetime.end == end) return null;
		i = ParseUtil.skipSplit(chs, datetime.end, len);
		//获取ip
		Token<Long> ip = ParseUtil.parseIP(chs, i, len);
		if(ip == null || ip.end == end) return null;
		i = ParseUtil.skipSplit(chs, ip.end, len);
		//from
		Token<String> ft = ParseUtil.parseValue(chs, i, len);
		if(ft == null || ft.end == end) return null;
		i = ParseUtil.skipSplit(chs, ft.end, len);
		//获取在sdk   在 from中的位置-1
		int from = this.parseFrom(ft.data);
		//action
		Token<String> at = ParseUtil.parseValue(chs, i, len);
		if(at == null || at.end == end) return null;
		i = ParseUtil.skipSplit(chs, at.end, len);
		int action = this.parseAction(at.data);
		//type
		Token<String> tt = ParseUtil.parseValue(chs, i, len);
		if(tt == null || tt.end == end) return null;
		i = ParseUtil.skipSplit(chs, tt.end, len);
		int type = this.parseType(tt.data);
		
		//取出当前qs中还有多少值
		Token<String> qs = ParseUtil.parseRange(chs, i, len);
		if(qs==null) return null;
		
		Map<String,Object> map = new HashMap<String,Object>();
		//遍历取出字段中的值并放入map中
		for(i=qs.begin+1;i < end;i++) {
			Token<KeyValue> token = ParseUtil.parseKeyValue(chs, i, qs.end-1);
			if(token == null) break;

			Object val = parseField(token.data.key, (String)token.data.value);
			map.put(token.data.key, val);
			
			i = token.end;
		}

		map.put("year", datetime.data.year);
		map.put("mon", datetime.data.mon);
		map.put("day", datetime.data.day);
		map.put("hour", datetime.data.hour);
		map.put("min", datetime.data.min);
		map.put("sec", datetime.data.sec);
		map.put("ip", ip.data);
		
		map.put("action", action);
		map.put("type", type);
		map.put("from", from);

		return map;
	}
	
	protected abstract List<String> fields();
	
	@Override
	public List<Object> parse(String line) {
		Map<String,Object> map = parseMap(line);
		if(map == null || map.isEmpty()) return null;
		List<Object> vals = new ArrayList<Object>();
		for(String key : fields()) {
			Object val = map.get(key);
			vals.add(val);
		}
		return vals;
	}
	
	public static void main(String[] args) {
		String a = "2.00";
		int i = a.indexOf(".");
		a = a.substring(0, i);
		int parseInt = Integer.parseInt(a);
		System.out.println(parseInt);
	}
}
