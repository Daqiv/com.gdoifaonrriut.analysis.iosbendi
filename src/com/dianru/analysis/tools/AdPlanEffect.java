package com.dianru.analysis.tools;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dianru.analysis.util.SQLConnection;
import com.dianru.analysis.util.StringUtils;

/**
 * @project : com.dianru.analysis
 * @author JunWu.zhu
 * @date: 2014.12.10
 * @QQ : 369990256
 * @description : 广告计划 生效
 */

public class AdPlanEffect {

	public static Logger LOG = LogManager.getLogger(AdPlanEffect.class);

	// private static final String SQL_QUERY_AD_PLAN =
	// "SELECT adid,begin_plan,end_plan,click_money,click_money_income,active_money,active_money_income,click_num_plan,active_num_plan,jobs from ad_plan where is_effect=1 and begin_plan<=%s and end_plan>%s;";
	// private static final String SQL_QUERY_AD_PLAN =
	// "SELECT a.adid,begin_plan,end_plan,click_money,click_money_income,active_money,active_money_income,click_num_plan,active_num_plan,b.jobs from ad_plan a inner join ads b on a.adid=b.adid where is_effect=1 and begin_plan<=%s and end_plan>%s;";

	// 一个广告对应多个广告预设，从广告预设表ad_plans_set表中查询广告预设信息,将满足预设状态的广告信息更新到ads、ad_plan和ads_extended中
	private static final String SQL_QUERY_AD_PLANS_SET = "SELECT p.adid,begin_plan,end_plan,options_plan,jobs_plan,options,ads_extended from ad_plans_set p,ads a where p.adid=a.adid and is_effect=0 and begin_plan<=%s and end_plan>%s;";

	// 将预设信息更新到ads和ad_plan、ads_extended表中
	private static final String SQL_UPDATE_ADS = "update ads set state=4,price_click_cost=%s,price_click_income=%s,price_callback_cost=%s,price_callback_income=%s,update_time=%s,is_talkingdata=%s,is_aso=%s,aso_pos=%s,keywords='%s',sort=%s,remark='%s',osver='%s',devices='%s',options='%s',jobs='%s' where adid=%s;";
	private static final String SQL_UPDATE_AD_PLAN = "update ad_plan set is_effect=2,begin=%s,end=%s,root=%s,hours='%s',click_num=%s,num=%s,deliveryType=%s,ruleouts='%s',ruleins='%s',levelouts='%s',fwz_levelouts='%s',citys='%s',remain='%s' where adid=%s;";
	private static final String SQL_INSERT_UPDATE_ADS_EXTENDED = "insert into ads_extended (adid,count_keywords) values (%s,'%s') ON DUPLICATE KEY UPDATE count_keywords='%s';";

	private static final String SQL_UPDATE_AD_PLANS_SET = " update ad_plans_set set is_effect=2 where adid=%s and begin_plan<=%s and end_plan>%s; ";

	/**
	 * <ul>
	 * <li>1.条件:[is_effect=1(生效),begin_plan<=unix系统时间戳
	 * end_plan>=unix系统时间戳]查询ad_plan返回adid
	 * ,begin_plan,end_plan,click_money,click_money_income
	 * ,active_money,active_money_income,click_num_plan,active_num_plan</li>
	 * <li>2.根据adid更新ads表price_click_cost=[click_money],price_callback_cost=[
	 * active_money
	 * ],price_click_income=[click_money_income],price_callback_income
	 * =[active_money_income]</li>
	 * <li>3.根据adid更新ad_plan表is_effect=2(已生效),click_num=[click_num_plan],num=[
	 * active_num_plan],begin=[begin_plan],end=[end_plan]</li>
	 * </ul>
	 */
	public static void execute(String timestamp) {
		// 1
		String query = String.format(SQL_QUERY_AD_PLANS_SET, timestamp,
				timestamp);
		LOG.debug(query);

		SQLConnection conn = SQLConnection.getInstance("main");

		// 查询出满足时间状态的所有的预设
		List<Map<String, Object>> items = conn.queryMap(query, null);

		// 遍历获取单条预设
		for (Map<String, Object> item : items) {
			int adid = (int) item.get("adid");

			String options_plan = "";
			Object opObj_plan = item.get("options_plan");
			if (opObj_plan != null && !opObj_plan.toString().isEmpty()) {
				options_plan = opObj_plan.toString();
			}

			// 初始化
			JSONObject opJson_plan = null;
			float price_click_cost = 0;
			float price_click_income = 0;
			float price_callback_cost = 0;
			float price_callback_income = 0;

			int root = 0;
			String hours = "";
			int click_num = 0;
			int num = 0;
			int is_talkingdata = 0;
			int deliveryType = 0;
			int is_aso = 0;
			int aso_pos = 0;
			String keywords = "";
			int sort = 0;
			String ruleouts = "";
			String ruleins = "";
			String remark = "";
			String levelouts = "";
			String fwz_levelouts = "";
			String remain = "";
			float float_money = 0;
			int process_num = 0;
			String osver = "";
			String devices = "";
			String citys = "";

			try {
				opJson_plan = new JSONObject(options_plan);
				Object price_click_costObj = opJson_plan
						.get("price_click_cost");
				if (price_click_costObj != null
						&& !price_click_costObj.toString().isEmpty()) {
					price_click_cost = Float.parseFloat(price_click_costObj
							.toString());// 点击价格
				}

				Object price_click_incomeObj = opJson_plan
						.get("price_click_income");
				if (price_click_incomeObj != null
						&& !price_click_incomeObj.toString().isEmpty()) {
					price_click_income = Float.parseFloat(price_click_incomeObj
							.toString());// 点击接入价格
				}

				root = opJson_plan.getInt("root");// 是否越狱

				hours = (String) opJson_plan.get("hours");// 投放小时，默认全选，不显示
				click_num = opJson_plan.getInt("click_num");// 点击控量
				num = opJson_plan.getInt("num");// 激活控量
				is_talkingdata = opJson_plan.getInt("is_talkingdata");// TalkingData，默认不上报（2），不显示
				deliveryType = opJson_plan.getInt("deliveryType");// 投放进度
				is_aso = opJson_plan.getInt("is_aso");// 跳转方式
				aso_pos = opJson_plan.getInt("aso_pos");// 跳转方式
				keywords = (String) opJson_plan.get("keywords");// 关键词
				sort = opJson_plan.getInt("sort");// 投放列表位置（对应原基本信息中排位号）
				ruleouts = (String) opJson_plan.get("ruleouts");// 排除应用
				ruleins = (String) opJson_plan.get("ruleins");// 投放应用
				remark = (String) opJson_plan.get("remark");// 投放说明
				levelouts = (String) opJson_plan.get("levelouts");// 网赚排除评级
				fwz_levelouts = (String) opJson_plan.get("fwz_levelouts");// 非网赚排除评级
				remain = (String) opJson_plan.get("remain");// 留存定投

				Object float_moneyObj = opJson_plan.get("float_money");
				if (float_moneyObj != null
						&& !float_moneyObj.toString().isEmpty()) {
					float_money = Float.parseFloat(float_moneyObj.toString());// 浮动金额
				}

				Object process_numObj = opJson_plan.get("process_num");
				if (process_numObj != null
						&& !process_numObj.toString().isEmpty()) {
					process_num = (int) process_numObj;// 进程数量

				}
				osver = (String) opJson_plan.get("osver");// 投放系统版本
				devices = (String) opJson_plan.get("devices");// 广告投放设备
				citys = (String) opJson_plan.get("citys");// 广告投放设备
			} catch (Exception e2) {
				LOG.error("ad plan Error: " + e2.getMessage());
				continue;
			}

			// 更新ads表中options中float_money、process_num字段信息值
			JSONObject opJson = null;
			String options = "";
			Object opObj = item.get("options");
			if (opObj != null && !opObj.toString().isEmpty()) {
				options = opObj.toString();
			}

			try {
				opJson = new JSONObject(options);
				String float_moneyStr = String.format("%.2f", float_money)
						.replaceAll(",", ".");// 取两位小数
				// 从ads_plan_set中的options_plan中取出浮动金额和进程数更新ads中字段options对应数值
				options = opJson.put("float_money", float_moneyStr).toString();
				options = opJson.put("process_num", process_num).toString();
			} catch (Exception e1) {
				LOG.error("optionsJson error :" + e1.getMessage());
				continue;
			}
			options = StringUtils.decodeUnicode(options);

			// 从ads_plans_set中取出jobs_plan更新到ads中jobs字段、并取出jobs_plan的激活接入价格和激
			//活投放价格更新ads中price_callback_cost和price_callback_income
			String jobs_plan = "";
			Object jobs_planObj = item.get("jobs_plan");
			if (jobs_planObj != null && !jobs_planObj.toString().isEmpty()) {
				jobs_plan = jobs_planObj.toString();
			}
			JSONObject jobJson = null;
			try {
				jobJson = new JSONObject(jobs_plan);
				Object incomeObj = jobJson.get("income");
				if (incomeObj != null && !incomeObj.toString().isEmpty()) {
					price_callback_income = Float.parseFloat(incomeObj
							.toString());// 激活接入价格
				}
				JSONArray jsonArray = jobJson.getJSONArray("jobs");
				if (jsonArray != null && jsonArray.length() > 0) {
					JSONObject job = new JSONObject(jsonArray.get(0).toString());
					Object constObj = job.get("const");
					if (constObj != null && !constObj.toString().isEmpty()) {
						price_callback_cost = Float.parseFloat(constObj
								.toString()); // 激活投放价格
					}

				}
			} catch (Exception e) {
				LOG.error("jobsJson error :" + e.getMessage());
				continue;
			}
			jobs_plan = StringUtils.decodeUnicode(jobs_plan);

			String updatead = String.format(SQL_UPDATE_ADS, price_click_cost,
					price_click_income, price_callback_cost,
					price_callback_income, timestamp, is_talkingdata, is_aso,
					aso_pos, keywords, sort, remark, osver, devices, options,
					jobs_plan, adid);
			int i = conn.update(updatead, null);
			LOG.info(updatead + "==>is updated:" + i);

			// 3
			int begin = (int) item.get("begin_plan");
			int end = (int) item.get("end_plan");

			String updatead_plan = String.format(SQL_UPDATE_AD_PLAN, begin,
					end, root, hours, click_num, num, deliveryType, ruleouts,
					ruleins, levelouts, fwz_levelouts, citys, remain, adid);
			int j = conn.update(updatead_plan, null);
			LOG.info(updatead_plan + "==>is updated:" + j);

			// 更新预设状态为已过期
			String updatead_plans_set = String.format(SQL_UPDATE_AD_PLANS_SET,
					adid, timestamp, timestamp);
			int l = conn.update(updatead_plans_set, null);
			LOG.info(updatead_plan + "==>is updated:" + l);

			// 更新ads_extended表中count_keywords字段信息
			String count_keywords = "";
			Object count_keywordsObj = item.get("ads_extended");// ads_plan_set中的ads_extended就是count_key_words
			if (count_keywordsObj != null
					&& !count_keywordsObj.toString().isEmpty()) {
				count_keywords = count_keywordsObj.toString();
			}
			count_keywords = StringUtils.decodeUnicode(count_keywords);
			String updateads_extended = String.format(
					SQL_INSERT_UPDATE_ADS_EXTENDED, adid, count_keywords,
					count_keywords);
			int k = conn.update(updateads_extended, null);
			LOG.info(updatead_plan + "==>is updated:" + k);
		}
		conn.close();
	}

	public static void main(String[] args) {
		execute("UNIX_TIMESTAMP()");
	}
}
