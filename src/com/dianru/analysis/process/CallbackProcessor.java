package com.dianru.analysis.process;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.CallbackItem;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.store.DBHistoryStore;
import com.dianru.analysis.store.FileStore;

public abstract class CallbackProcessor extends BaseProcessor {

	public static Logger LOG = LogManager.getLogger(CallbackProcessor.class);

	public CallbackItem getHistory(int year, int mon, int day, int type, int adid, String mac, String udid) {
		int date = year * 10000 + mon * 100 + day;

		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, mon-1);
		cal.set(Calendar.DAY_OF_MONTH, day);

		for (int i = 0; i < 30; i++) {

			CallbackItem item = DBHistoryStore.getInstance(FileStore.STORE_STORE, date, type, Define.ACTION_CLICK).get(adid, mac,udid);
			if (item != null) {
				if(i==0){
					item.isToday = true;
				}
				return item;
			}
			cal.add(Calendar.DAY_OF_MONTH, -1);

			year = cal.get(Calendar.YEAR);
			mon = cal.get(Calendar.MONTH)+1;
			day = cal.get(Calendar.DAY_OF_MONTH);
			date = year * 10000 + mon * 100 + day;
		}
		return null;
	}
}
