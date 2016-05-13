package com.dianru.analysis.count.store;

import com.dianru.analysis.count.bean.CountKeys;
import com.dianru.analysis.count.bean.CountValues;

public class CountSQL {

	public static String create(CountKeys ck, CountValues cv) {
		
		String table = ck.getTable();
		
		String kfs[] = ck.getFileds();
		Object kvs[] = ck.getValues();
		
		String vfs[] = cv.getFileds();
		Object vvs[] = cv.getValues();
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(table);
		sb.append("(");
		for(int i=0;i<kfs.length;i++) {
			if(i > 0) sb.append(',');
			sb.append('`');
			sb.append(kfs[i]);
			sb.append('`');
		}
		for(int i=0;i<vfs.length;i++) {
			sb.append(',');
			sb.append('`');
			sb.append(vfs[i]);
			sb.append('`');
		}
		sb.append(") VALUES (");
		for(int i=0;i<kvs.length;i++) {
			if(i > 0) sb.append(',');
			sb.append('\'');
			sb.append(kvs[i]);
			sb.append('\'');
		}
		for(int i=0;i<vvs.length;i++) {
			sb.append(',');
			sb.append('\'');
			sb.append(vvs[i]);
			sb.append('\'');
		}
		sb.append(") ON DUPLICATE KEY UPDATE ");
		for(int i=0;i<vfs.length;i++) {
			if(i > 0) sb.append(',');
			sb.append('`');
			sb.append(vfs[i]);
			sb.append('`');
			sb.append('=');
			sb.append('`');
			sb.append(vfs[i]);
			sb.append('`');
			sb.append('+');
			sb.append('\'');
			sb.append(vvs[i]);
			sb.append('\'');
		}
		
		return sb.toString();
	}
	
}
