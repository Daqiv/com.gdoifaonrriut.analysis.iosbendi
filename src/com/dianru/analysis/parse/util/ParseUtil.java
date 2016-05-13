package com.dianru.analysis.parse.util;

import java.util.HashMap;
import java.util.Map;

import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.bean.KeyValue;

public class ParseUtil {
	
	public static int skip(char[] chs, int idx, int end) {
		int i = skipValue(chs,idx,end);
		return skipSplit(chs,i, end);
	}
	
	public static int skipSplit(char[] chs, int idx, int end) {
		int i=idx;
		for(;i < end;i++) {
			if(chs[i] != ' ' && chs[i] != '\t') break;
		}
		return i;
	}
	
	public static int skipValue(char[] chs, int idx, int end) {
		int i=idx;
		for(;i < end;i++) {
			if(chs[i] == ' ' || chs[i] == '\t') break;
		}
		return i;
	}
	
	public static Token<Long> parseIP(char[] chs, int idx, int end) {

		if(chs[idx] == '-') return new Token<Long>(idx,idx+1,0L); 
		
		int sn = 0; int tn = 0;
		int i = idx; long data = 0;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch== '.' || ch == ' ' || i == end-1) {

				data = data << 8;
				data += sn;

				sn=0;
				tn++;
				if(tn == 5) return null;
				
				if(chs[i] == ' ' || i==end-1)
					break;
			} else if(ch >= '0' && ch<= '9') {
				sn = sn*10+(ch-'0');
				if(sn > 255) 
					return null;
			} else {
				return null;
			}
		}
		if(tn != 4) return null;
		
		return new Token<Long>(idx,i,data);
	}
	
	//获取时间的开始和结束位置，以及时间的具体变量
	public static Token<DateTime> parseDate(char[] chs, final int idx, int end) {
		//v为便利获取到的值，ts为获取到的第几个值，vl为每次进行获取值是便利经过的字符串
		int i = idx; int v=0; int ts=0; int vl=0;
		int vs[] = new int[3];
		
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch >= '0' && ch <= '9') {
				v = v*10+(ch-'0');
				vl++;
			} else if(ch == '-' || ch == ' ' || i==end-1) {
				if(vl == 0) return null;
				if(ts == 0) if(v > 9999 || v < 1970) return null;
				else if(ts == 1) if(v > 12 || v < 1) return null;
				else if(ts == 2) if(v > 31 || v < 1) return null;
				else return null;
				vs[ts] = v;
				
				v = vl = 0;
				ts++;
				if(ts == 4) return null;
				
				if(ch == ' ' || i==end-1) break;
			} else return null;
		}
		if(ts != 3) return null;
		
		DateTime data = new DateTime(vs[0],vs[1],vs[2]);
		
		return new Token<DateTime>(idx, i, data);
	}
	
	public static Token<DateTime> parseTime(char[] chs, int idx, int end) {
		int i = idx; int v=0; int ts=0; int vl=0;
		int vs[] = new int[3];
		
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch >= '0' && ch <= '9') {
				v = v*10+(ch-'0');
				vl++;
			} else if(ch == ':' || ch == ' ' || i==end-1) {
				if(vl == 0) return null;
				if(ts == 0) if(v > 24 || v < 0) return null;
				else if(ts == 1) if(v > 60 || v < 0) return null;
				else if(ts == 2) if(v > 60 || v < 0) return null;
				else return null;
				vs[ts] = v;
				
				v = vl = 0;
				ts++;
				if(ts == '4') return null;
				
				if(ch == ' ' || i==end-1) break;
			} else return null;
		}
		if(ts != 3) return null;
		
		DateTime data = new DateTime(0,0,0,vs[0],vs[1],vs[2]);
		
		return new Token<DateTime>(idx, i, data);
	}
	//获取日期和时间并返回封装的对象
	public static Token<DateTime> parseDateTime(char[] chs, final int idx, int end) {
		int i = idx;
		Token<DateTime> date = parseDate(chs, idx, end);
		if(date == null) return null;
		i = ParseUtil.skipSplit(chs, date.end, end);
		
		Token<DateTime> time = parseTime(chs, i, end);
		if(time == null) return null;
		i = time.end;
		
		time.begin = date.begin;
		time.data.year = date.data.year;
		time.data.mon = date.data.mon;
		time.data.day = date.data.day;
		
		return time;
	}
	
	public static Token<Integer> parseInt(char[] chs, int idx, int end) {
		return parseInt(chs,idx,end,' ');
	}
	
	public static Token<Integer> parseInt(char[] chs, int idx, int end, char split) {
		int i = idx; int v = 0; int cs = 0;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == split) {
				break;
			} else if(ch >= '0' && ch <= '9') {
				v = v*10+(ch-'0');
				cs++;
				if(v > Integer.MAX_VALUE) return null;
				if(i==end-1) break;
			} else {
				return null;
			}
		}
		if(cs == 0) return null;
		
		return new Token<Integer>(idx, i, v);
	}
	
	public static Token<Long> parseLong(char[] chs, int idx, int end) {
		int i = idx; long v = 0; int cs = 0;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == ' ' || i==end-1) {
				break;
			} else if(ch >= '0' && ch <= '9') {
				v = v*10+(ch-'0');
				cs++;
				if(v > Long.MAX_VALUE) return null;
			} else {
				return null;
			}
		}
		if(cs == 0) return null;
		
		return new Token<Long>(idx, i, v);
	}
	
	public static Token<Float> parseFloat(char[] chs, int idx, int end) {
		int i = idx; float v1 = 0, v2=0; int cs1 = 0, cs2 = 0; int ds = 0;;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == ' ' || i==end-1) {
				break;
			} else if(ch >= '0' && ch <= '9') {
				if(ds == 0) {
					v1 = v1*10+(ch-'0');
					cs1++;
				}
				else if(ds == 1) {
					v2 = v2*10+(ch-'0');
					cs2++;
				}
				
				if(v1 > Float.MAX_VALUE) return null;
			} else if(ch == '.') {
				ds++;
			} else {
				return null;
			}
		}
		if(cs1 == 0 && (ds > 0 && cs2 == 0)) return null;
		
		float v = v1;
		if(cs2 > 0) {
			int n = (int) Math.pow((double)10,(double)cs2);
			v += v2/n;
		}
		return new Token<Float>(idx, i, v);
	}
	
	public static Token<String>  parseRange(char[] chs, int idx, int end) {
		boolean hasBegin = false;
		boolean hasEnd = false;
		
		if(chs[idx] != '"') {
			idx += 1;
			hasBegin = true;
		}
		
		int i = idx;
		for(;i < end;i++) {
			char ch = chs[i];
			if(i==end-1) {
				break;
			} else if(hasBegin && ch == '"') {
				hasEnd = true;
				break;
			}
		}
		if(hasBegin && !hasEnd) return null;
		
		if((hasBegin && i-idx < 2) || (hasBegin && i-idx < 0)) return null;

		return new Token<String>(idx, i, null);
	}
	
	public static Token<String> parseString(char[] chs, int idx, int end) {
		
		Token<String> range = parseRange(chs,idx,end);
		if(range == null) return null;
		
		range.data = new String(chs, range.begin+1,range.end-range.begin-1);
		
		return range;
	}
	//以空格获取各个字段
	public static Token<String> parseValue(char[] chs, int idx, int end) {
		int i = idx;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == ' ' || i==end-1) {
				int len = i-idx + (i==end-1 ? 1 : 0);
				return new Token<String>(idx,i,new String(chs,idx,len));
			}
		}
		return null;
	}
	
	public static Token<KeyValue> parseKeyValue(char[] chs, int idx, int end) {
		int i = idx; int begin = idx;
		String key = null;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == '=') {
				if(i-idx > 0) {
					key = new String(chs,idx,i-idx);
					break;
				}
			}
		}
		
		idx = i+1;
		if(key == null) return null;
		
		String value = null;
		for(;i < end;i++) {
			char ch = chs[i];
			if(ch == '&' || i==end-1) {
				if(i-idx >= 0) {
					int len = i-idx + (i==end-1 ? 1 : 0);
					value = new String(chs,idx,len);
				}
				break;
			}
		}
		KeyValue kv = new KeyValue(key,value);
		
		return new Token<KeyValue>(begin,i,kv);
	}

	public static Map<String, String> parseQueryString(char[] chs, int idx, int end) {

		int i = idx;
		Map<String, String> map = new HashMap<String, String>();
		for(;i < end;i++) {
			Token<KeyValue> token = ParseUtil.parseKeyValue(chs, i, end);
			if(token == null) break;
			
			map.put(token.data.key, (String)token.data.value);
			i = token.end;
		}
		return null;
	}
	
	public static KeyValue parseUrl(char[] chs, int idx, int end) {
		char ch;
		int i = idx;
		for(;i < end;i++) {
			ch = chs[i];
			if(ch == '?') break;
		}
		String key = new String(chs, i, i-idx+1);
		if(i == end -1) return null;
		
		idx = i;
		for(;i < end;i++) {
			ch = chs[i];
			if(ch == ' ' || i == end-1) break;
		}
		int len = i-idx + (i==end-1 ? 1 : 0);
		String value = new String(chs, i, len);

		return new KeyValue(key,value);
	}
}
