package com.dianru.analysis.tools;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.util.SQLConnection;

public class CpaAdSum {

	public static Logger LOG = LogManager.getLogger(CpaAdSum.class);

	// callback
	private static final String SQL_QUERY_CALLBACK = "select count(*) as active_count,sum(income) as active_income,sum(cost) as active_cost,ad_from,data_from,FROM_UNIXTIME(create_time,'%H') as hour from";
	private static final String SQL_CALLBACK_GROUP_BY = " group by ad_from,data_from,FROM_UNIXTIME(create_time,'%H');";
	// condition
	private static final String SQL_ADID_TYPE = " where adid=%s and type=%s";
	private static final String SQL_AND_ADFROM_DATAFROM = " and ad_from=%s and data_from=%s";
	// ad_hour
	private static final String SQL_UPDATE_AD_HOUR = "update cpa_ad_hour_%s set active_count=%s,active_income=%s,active_cost=%s ";
	private static final String SQL_WHERE_HOUR = " and hour=%s";
	// ad_day
	private static final String SQL_QUERY_AD_HOUR = "select sum(active_count) as active_count,sum(active_income) as active_income,sum(active_cost) as active_cost,ad_from,data_from from";
	private static final String SQL_AD_HOUR_GROUP_BY = " group by ad_from,data_from;";
	private static final String SQL_UPDATE_AD_DAY = "update cpa_ad_day_%s set active_count=%s,active_income=%s,active_cost=%s ";
	private static final String SQL_AND_CREATED = " and created=%s";

	public static double round(Object f) {
		String s = new java.text.DecimalFormat("0.00").format(f);
		return Double.valueOf(s);
	}

	public static long tolong(Object l) {
		BigDecimal big = new BigDecimal(l.toString());
		return big.longValue();
	}

	public static void upd_cpa_ad_hour(int year, int month, int day, int adid,
			int type, int action) {

		String yyyymm = year + "" + (month > 9 ? month : "0" + month); // yyyymm
		String yyyymmdd = yyyymm + (day > 9 ? day : "0" + day); // yyyymmdd

		SQLConnection cpa = SQLConnection.getInstance("offerwall");

		// callback
		StringBuffer callback = new StringBuffer(SQL_QUERY_CALLBACK);
		callback.append(String.format(" cpa_callback_%s", yyyymmdd));
		callback.append(String.format(SQL_ADID_TYPE, adid, type));
		callback.append(String.format(" and action=%s", action));
		callback.append(SQL_CALLBACK_GROUP_BY);

		System.out.println(callback);

		List<Map<String, Object>> maps = cpa
				.queryMap(callback.toString(), null);

		for (Map<String, Object> map : maps) {

			long active_count = (long) map.get("active_count");
			double active_income = round(map.get("active_income"));
			double active_cost = round(map.get("active_cost"));

			int hour = Integer.parseInt(map.get("hour").toString());
			int ad_from = (int) map.get("ad_from");
			int data_from = (int) map.get("data_from");

			StringBuffer update = new StringBuffer();
			update.append(String.format(SQL_UPDATE_AD_HOUR, yyyymm,
					active_count, active_income, active_cost));
			update.append(String.format(SQL_ADID_TYPE, adid, type));
			update.append(String.format(SQL_AND_ADFROM_DATAFROM, ad_from,
					data_from));
			update.append(String.format(SQL_WHERE_HOUR, hour));
			update.append(String.format(SQL_AND_CREATED, yyyymmdd));

			System.out.println(update);
			int i = cpa.update(update.toString(), null);
			System.out.println("is updated :" + i);

		}

		cpa.close();
	}

	public static void upd_cpa_ad_day(int year, int month, int day, int adid,
			int type) {
		String yyyymm = year + "" + (month > 9 ? month : "0" + month); // yyyymm
		String yyyymmdd = yyyymm + (day > 9 ? day : "0" + day); // yyyymmdd

		SQLConnection cpa = SQLConnection.getInstance("offerwall");

		StringBuffer ad_hour = new StringBuffer(SQL_QUERY_AD_HOUR);
		ad_hour.append(String.format(" cpa_ad_hour_%s", yyyymm));
		ad_hour.append(String.format(SQL_ADID_TYPE, adid, type));
		ad_hour.append(String.format(SQL_AND_CREATED, yyyymmdd));
		ad_hour.append(SQL_AD_HOUR_GROUP_BY);

		System.out.println(ad_hour);

		List<Map<String, Object>> maps = cpa.queryMap(ad_hour.toString(), null);

		for (Map<String, Object> map : maps) {

			long active_count = tolong(map.get("active_count"));
			double active_income = round(map.get("active_income"));
			double active_cost = round(map.get("active_cost"));

			int ad_from = (int) map.get("ad_from");
			int data_from = (int) map.get("data_from");

			StringBuffer update = new StringBuffer(String.format(SQL_UPDATE_AD_DAY,year,active_count, active_income,active_cost));
			update.append(String.format(SQL_ADID_TYPE,adid,type));
			update.append(String.format(SQL_AND_ADFROM_DATAFROM,ad_from,data_from));
			update.append(String.format(SQL_AND_CREATED,yyyymmdd));

			System.out.println(update);

			int i = cpa.update(update.toString(), null);
			System.out.println("is updated :" + i);
		}

		cpa.close();
	}

	public static void main(String[] args) {

		int year = 2014;
		int month = 11;
		int day = 11;

		int action = 4;
		int adid = 10131;
		int type = 1;
		
		upd_cpa_ad_hour(year, month, day, adid, type, action);
		System.out.println("---------------------------------------");
		upd_cpa_ad_day(year, month, day, adid, type);
	}

}
