package com.dianru.analysis.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.store.DBHistoryStore;
import com.dianru.analysis.store.FileStore;

public class BuildClickIndex {

	public static CallbackItem convert(String line) {
		String ps[] = line.split("\\s+");
		if(ps.length != 5 && ps.length != 4) {
			System.out.println("error");
			return null;
		}
		//id type adid mac udid
		CallbackItem item = new CallbackItem();
		item.id = Integer.parseInt(ps[0]);
		item.type = Integer.parseInt(ps[1]);
		item.adid = Integer.parseInt(ps[2]);
		item.mac = ps[3] == null ? null : ps[3].trim();
		item.udid = (ps.length < 5 || ps[4] == null) ? null : ps[4].trim();
		
		return item;
	}
	
	public static void exec(int date, String[] args) throws IOException {
		if(args.length != 1) {
			System.out.println("cpc <text input>");
			return;
		}
		
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		
		int count = 0;
		String line;

		while((line = in.readLine()) != null) {
			CallbackItem item = convert(line);
			if(item == null) continue;
			
			DBHistoryStore.getInstance(FileStore.STORE_STORE, date, item.type, Define.ACTION_CLICK).putId(item.adid, item.mac, item.udid, item.id);
			count++;
			if((count % 10000) == 0) System.out.printf("count : %d\n", count);
			//date,id,type,adid,mac,udid
		}
		in.close();
		
		System.out.printf("from %s done : %d\n", args[0], count);
	}
	
	public static void main(String[] args) {
		int dates[] = {20141020,20141021,20141022,20141023,20141024,20141025,20141026};
		for(int i=0;i<dates.length;i++) {
			int date = dates[i];
			String fts[] = new String[1];
			fts[0] = String.format("/home/exp/cpa_callback_%d.txt", date);
			
			try {
				exec(date, fts);
			} catch (IOException e) {
				System.out.printf("not found : %s\n", fts[0]);
			}
		}
	}
}
