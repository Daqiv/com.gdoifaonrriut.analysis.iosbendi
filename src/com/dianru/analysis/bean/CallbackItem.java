package com.dianru.analysis.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.dianru.analysis.process.BaseProcessor.Index;
import com.dianru.analysis.util.ListUtil;

public class CallbackItem {

	public long id;
	public int type;
	public int action;
	public int invalid;
	public int saved;
	public int uid;
	public int appid;
	public int cid;
	public int adid;
	public int ad_from;
	public int data_from;
	public String mac;
	public String udid;
	public String openudid;
	public long ip;
	public long open_time;
	public long create_time;
	public float income;
	public float cost;
	public float score;
	public String appuserid;
	public int process;
	public boolean isToday = false;
	public String keywords;

	public int date;

	public static String[] SQL_FIELDS = { "type", "action", "invalid", "saved",
			"uid", "appid", "cid", "adid", "ad_from", "data_from", "mac",
			"udid", "openudid", "ip", "open_time", "create_time", "income",
			"cost", "score", "appuserid", "process", "keywords" };

	public static String SQL_VALUES = "";
	public static String SQL_KEYS = "";
	static {
		StringBuilder sk = new StringBuilder();
		StringBuilder sv = new StringBuilder();

		for (int i = 0; i < SQL_FIELDS.length; i++) {
			String field = SQL_FIELDS[i];

			if (i > 0) {
				sv.append(',');
				sk.append(',');
			}
			sk.append('`');
			sk.append(field);
			sk.append('`');

			sv.append('?');
		}
		SQL_KEYS = sk.toString();
		SQL_VALUES = sv.toString();
	}

	public Object[] toObjects() {
		return new Object[] { type, action, invalid, saved, uid, appid, cid,
				adid, ad_from, data_from, mac, udid, openudid, ip, open_time,
				create_time, income, cost, score, appuserid, process, keywords };
	}

	public static CallbackItem parseFromLog(List<Object> vals, Media media,
			Ads ad) {

		CallbackItem item = new CallbackItem();

		item.type = ListUtil.getInt(vals, Index.TYPE);
		item.action = ListUtil.getInt(vals, Index.ACTION);

		item.uid = ListUtil.getInt(vals, Index.UID);
		item.appid = ListUtil.getInt(vals, Index.APPID);

		item.cid = ListUtil.getInt(vals, Index.CID);
		item.adid = ListUtil.getInt(vals, Index.ADID);

		item.ad_from = ad.getDataFrom();
		item.data_from = media.getType() == 1 ? 1 : 3;

		item.mac = ListUtil.getString(vals, Index.MAC);
		item.udid = ListUtil.getString(vals, Index.UDID);
		item.openudid = ListUtil.getString(vals, Index.OPENUDID);
		item.ip = ListUtil.getLong(vals, Index.IP);
		item.appuserid = ListUtil.getString(vals, Index.APP_USER_KEY);

		if (item.appuserid != null && item.appuserid.length() > 0) {
			try {
				item.appuserid = URLDecoder.decode(item.appuserid, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}

		item.open_time = new DateTime(vals).timestamp();
		item.create_time = 0;

		item.income = 0;
		item.cost = 0;
		item.score = 0;
		item.process = ListUtil.getInt(vals, Index.FROM);

		return item;
	}

	public List<Object> toList() {

		List<Object> objs = new ArrayList<Object>();

		objs.add(this.id);
		objs.add(this.type);
		objs.add(this.action);
		objs.add(this.invalid);
		objs.add(this.saved);

		objs.add(this.uid);
		objs.add(this.appid);
		objs.add(this.cid);
		objs.add(this.adid);

		objs.add(this.ad_from);
		objs.add(this.data_from);

		objs.add(this.mac);
		objs.add(this.udid);
		objs.add(this.openudid);

		objs.add(this.ip);
		objs.add(this.open_time);
		objs.add(this.create_time);

		objs.add(this.income);
		objs.add(this.cost);
		objs.add(this.score);
		objs.add(this.process);
		objs.add(this.keywords);
		return objs;
	}

	public CallbackItem() {
	}

	public static CallbackItem parseFromData(List<Object> vals) {

		CallbackItem item = new CallbackItem();
		if (vals == null)
			return null;
		if (vals.size() < 16)
			return null;

		item.id = ListUtil.getLong(vals, 0);
		item.type = ListUtil.getInt(vals, 1);
		item.action = ListUtil.getInt(vals, 2);
		item.invalid = ListUtil.getInt(vals, 3);
		item.saved = ListUtil.getInt(vals, 4);

		item.uid = ListUtil.getInt(vals, 5);
		item.appid = ListUtil.getInt(vals, 6);
		item.cid = ListUtil.getInt(vals, 7);
		item.adid = ListUtil.getInt(vals, 8);

		item.ad_from = ListUtil.getInt(vals, 9);
		item.data_from = ListUtil.getInt(vals, 10);

		item.mac = ListUtil.getString(vals, 11);
		item.udid = ListUtil.getString(vals, 12);
		item.openudid = ListUtil.getString(vals, 13);
		item.ip = ListUtil.getLong(vals, 14);

		item.open_time = ListUtil.getLong(vals, 15);
		item.create_time = ListUtil.getLong(vals, 16);

		item.income = ListUtil.getFloat(vals, 17);
		item.cost = ListUtil.getFloat(vals, 18);
		item.score = ListUtil.getFloat(vals, 19);

		item.appuserid = ListUtil.getString(vals, 20);

		item.process = ListUtil.getInt(vals, 21);

		return item;
	}

	public static void main(String[] args) {
		System.out.println("SQL_KEYS:" + SQL_KEYS);
		System.out.println("SQL_VALUES:" + SQL_VALUES);
	}
}
