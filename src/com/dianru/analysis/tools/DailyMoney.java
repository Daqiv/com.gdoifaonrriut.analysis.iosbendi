package com.dianru.analysis.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.DateTime;
import com.dianru.analysis.util.SQLConnection;

public class DailyMoney {

	public static String SQL_ADS_QUERY = "SELECT cid,sum(click_income)+sum(active_income)+sum(job_income) as money FROM %s_ad_day_%d WHERE created=%d AND (click_income>0 OR active_income>0 OR job_income>0) GROUP BY cid";
	public static String SQL_ADS_UPDATE = "UPDATE user_num SET adsbalance=adsbalance-?,adamount=adamount+? WHERE uid=?";

	public static String SQL_MEDIA_QUERY = "SELECT uid,sum(click_cost)+sum(active_cost)+sum(job_cost) as money FROM %s_media_day_%d WHERE created=%d AND (click_cost>0 OR active_cost>0 OR job_cost>0) group by uid";
	public static String SQL_MEDIA_UPDATE = "UPDATE user_num SET appbalance=appbalance+?,appamount=appamount+? WHERE uid=?";

	public static Logger LOG = LogManager.getLogger(DailyMoney.class);
	/*
	 * @author : junwu.zhu
	 */
	public static final String SQL_QUERY_USER_NUM_IN = "select uid,adsbalance,adamount,appbalance,appamount from user_num where uid in(%s)";
	public static final String SQL_INSERT_USER_NUM_DAILY_MONEY = "insert into user_num_daily_money(uid,daily,type,time_create,adsbalance_upd_before,adsbalance_add,adsbalance_upd_after,adsamount_upd_before,adsamount_add,adsamount_upd_after,appbalance_upd_before,appbalance_add,appbalance_upd_after,appamount_upd_before,appsamount_add,appamount_upd_after) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static class UserNumDailyMoney {
		int uid;//用户ID
		int daily;//统计日
		String type; //数据来源 (cpc/cpa)

		double adsbalance_upd_before = 0;//广告主更新余额(前)
		double adsbalance_add = 0;//广告主余额减少数

		double adsamount_upd_before = 0;//广告主累计金额更新(前)
		double adsamount_add = 0;//广告主累计金额累加数

		double appbalance_upd_before = 0;//开发者更新余额(前)
		double appbalance_add = 0;//开发者更新余额累加数

		double appamount_upd_before = 0;//开发者累计金额更新(前)
		double appamount_add = 0;//开发者累计金额累加数

		public Object[] toParams() {
			Date time_create = new Date();
			Object[] parmas = { uid,daily,type,time_create,
					adsbalance_upd_before,adsbalance_add, adsbalance_upd_before - adsbalance_add,
					adsamount_upd_before, adsamount_add,adsamount_upd_before + adsamount_add,
					appbalance_upd_before, appbalance_add,appbalance_upd_before + appbalance_add,
					appamount_upd_before, appamount_add,appamount_upd_before + appamount_add };

			return parmas;
		}
	}

	public static double round(Object f) {
		String s = new java.text.DecimalFormat("0.00").format(f);
		return Double.valueOf(s);
	}

	public static void updateUserNum(String dbname, String type, int year,
			int date, Map<Integer, UserNumDailyMoney> dailymoneys) {

		SQLConnection conn = SQLConnection.getInstance(dbname);
		SQLConnection main = SQLConnection.getInstance("main");

		String sqlAdsQuery = String.format(SQL_ADS_QUERY, type, year, date);
		List<Map<String, Object>> cs = conn.queryMap(sqlAdsQuery, null);
		for (Map<String, Object> item : cs) {
			int uid = (int) item.get("cid");
			double money = (double) item.get("money");

			LOG.info("ad user " + uid + " adsbalance sub " + money + " adamount add " + money);
			main.update(SQL_ADS_UPDATE, new Object[] { money, money, uid });
			/*
			 * @author : junwu.zhu : 广告主余额和累加金额
			 */
			UserNumDailyMoney dm = new UserNumDailyMoney();
			dm.uid = uid;
			dm.type = type;
			dm.adsbalance_add = money;
			dm.adsamount_add = money;
			dailymoneys.put(uid, dm);
		}

		String sqlMediaQuery = String.format(SQL_MEDIA_QUERY, type, year, date);
		List<Map<String, Object>> ms = conn.queryMap(sqlMediaQuery, null);
		for (Map<String, Object> item : ms) {
			int uid = (int) item.get("uid");
			double money = (double) item.get("money");

			LOG.info("app user " + uid + " appbalance add " + money + " appamount add " + money);
			main.update(SQL_MEDIA_UPDATE, new Object[] { money, money, uid });
			/*
			 * @author : junwu.zhu : 开发者余额和累加金额
			 */
			UserNumDailyMoney dm = new UserNumDailyMoney();
			dm.uid = uid;
			dm.type = type;
			dm.appbalance_add = money;
			dm.appamount_add = money;
			dailymoneys.put(uid, dm);
		}

		main.close();
		conn.close();
	}

	/*
	 * @author : junwu.zhu
	 */
	public static void insertUserNumDailyMoney(int date,
			Map<Integer, UserNumDailyMoney> dailymoneys) {
		if (null != dailymoneys && !dailymoneys.isEmpty()) {

			SQLConnection main = SQLConnection.getInstance("main");
			
			/*
			 * 1.拼接uid,uid.....查询user_num
			 */
			StringBuffer fmParams = new StringBuffer();
			for (int uid : dailymoneys.keySet()) {
				fmParams.append("," + uid);
			}
			List<Map<String, Object>> usernums = main.queryMap(String.format(SQL_QUERY_USER_NUM_IN,fmParams.substring(1)), null);
			/*
			 * 2.更新userNumDailyMoney对象实例
			 */
			List<Object[]> insertDailymoneys = new ArrayList<Object[]>();
			for (Map<String, Object> usernum : usernums) {
				int uid = (int) usernum.get("uid");
				UserNumDailyMoney undm = dailymoneys.get(uid);
				if (undm == null) {
					continue;
				}
				undm.adsbalance_upd_before = round(usernum.get("adsbalance"));
				undm.adsamount_upd_before = round(usernum.get("adamount"));

				undm.appbalance_upd_before = round(usernum.get("appbalance"));
				undm.appamount_upd_before = round(usernum.get("appamount"));
				undm.daily = date;

				insertDailymoneys.add(undm.toParams());
			}
			/*
			 * 3.批量增加
			 */
			main.batch(SQL_INSERT_USER_NUM_DAILY_MONEY, insertDailymoneys);
			main.close();
		}
	}

	public static void main(String[] args) {

		int date = DateTime.getDate(-1);
		int year = date / 10000;

		Map<Integer, UserNumDailyMoney> dailymoneys = new HashMap<Integer, UserNumDailyMoney>();

		updateUserNum("offerwall", "cpa", year, date, dailymoneys);
		updateUserNum("insertscreen", "cpc", year, date, dailymoneys);

		insertUserNumDailyMoney(date, dailymoneys);
		
		dailymoneys.clear();

		LOG.info("daily money cron");
	}
}
