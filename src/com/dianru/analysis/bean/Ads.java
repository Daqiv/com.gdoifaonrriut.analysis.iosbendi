package com.dianru.analysis.bean;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

public class Ads extends Common implements Serializable {

	private static final long serialVersionUID = -8438261930942665353L;
	
	public Ads() {
		super();
	}

	public Ads(Map<String, Object> vals) {
		this.dataType = (int) getValue(vals, "data_type" , "int");
		this.spaceType = (int) getValue(vals, "space_type", "int");
		this.adid = (int) getValue(vals, "adid", "int");
		this.cid = (int) getValue(vals , "cid" , "int");
		this.ctype = (int) getValue(vals, "ctype" , "int");
		this.cstype = (int) getValue(vals, "cstype" , "int");
		this.clevel = (int) getValue(vals, "clevel" , "int");
		this.sort = (int) getValue(vals,"sort" , "int");
		this.state = (int) getValue(vals, "state" , "int");
		this.devices = (String) getValue(vals, "devices","string");
		this.begin = (int) getValue(vals, "begin" , "int");
		this.end = (int) getValue(vals, "end" , "int");
		this.control = (String) getValue(vals, "control","string");
		this.num = (int) getValue(vals,  "installnum", "int");		//installnum
		this.channelNum = (int) getValue(vals, "channel_num", "int");
		this.ruleouts = (String) getValue(vals,"ruleouts","string");
		this.citys = (String) getValue(vals, "citys","string");
		this.hours = (String) getValue(vals, "hours","string");
		this.root = (int) getValue(vals, "root", "int");
		
		this.priceClickIncome = (float) getValue(vals, "price_click_income","float");
		this.priceClickCost = (float) getValue(vals, "price_click_cost","float");
		this.priceCallbackIncome = (float) getValue(vals,  "price_callback_income","float");
		this.priceCallbackCost = (float) getValue(vals, "price_callback_cost","float");
		this.priceJobIncome = (float) getValue(vals,  "price_job_income","float");
		this.priceJobCost = (float) getValue(vals,  "price_job_cost","float");
		
		this.dataFrom = (int) getValue(vals, "data_from", "int");
		this.ruleins = (String) getValue(vals,  "ruleins","string");
		this.adname = (String) getValue(vals, "adname","string");
		this.os = (int) getValue(vals, "os", "int");
		this.osver = (String) getValue(vals,  "osver","string");
		this.budget = (int) getValue(vals, "budget", "int");
		this.money = (float) getValue(vals, "money","float");
		this.ipNum = (int) getValue(vals, "ipnum", "int");
		this.interval = (int) getValue(vals, "interval", "int");
		this.bootTimeNum = (int) getValue(vals, "boot_time_num", "int");
		this.ipNumAd = (int) getValue(vals, "num_ad", "int");
		this.intervalAd = (int) getValue(vals, "interval_ad", "int");
		this.bootTimeNumAd = (int) getValue(vals,"boot_time_num_ad", "int");
		this.rate = (int) getValue(vals,"rate", "int");
		this.billing = (String) getValue(vals, "billing","string");
		this.levelouts = (String) getValue(vals,"levelouts","string");
		this.appstoreid = (int) getValue(vals, "appstoreid", "int");
		this.bundleid = (String) getValue(vals, "bundleid","string");
		this.isTalkingdata = (int) getValue(vals,"is_talkingdata", "int");
		
		this.keywords = (String) getValue(vals,"keywords","string");
		this.isAso = (int) getValue(vals, "is_aso", "int");
		this.asoPos = (int) getValue(vals, "aso_pos", "int");
		this.fwzLevelouts = (String) getValue(vals, "fwz_levelouts","string");
		this.options = (String) getValue(vals, "options","string");
		
		this.click_num = (int) getValue(vals, "click_num", "int");
		this.process_name = (String) getValue(vals, "process_name","string");
		this.isHsFlag = (int) getValue(vals, "is_hs_flag", "int");
		this.isHsReport = (int) getValue(vals,"is_hs_report", "int");
		this.update_time = (int) getValue(vals, "update_time", "int");
		this.jobs = (String) getValue(vals, "jobs","string");
		this.is_hand_stop = (int) getValue(vals, "is_hand_stop", "int");
		this.remain = (String) getValue(vals, "remain", "string");
		this.deliveryType = (int) getValue(vals, "deliveryType", "int");
	}
	private int type; //广告类型：积分墙1 免费墙2 插屏3 全屏4 广告条5
	private int dataType;// 数据类型，1积分墙，2推荐墙
	private int spaceType;// 投放类型，1积分墙，2推荐墙
	private int adid;
	private int cid;// 广告主ID
	private int ctype;// 分类
	private int cstype;// 子分类
	private int clevel;// 评级
	private int sort;// 排位号
	private int state;//'状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停
	private String devices;// 投放设备
	private int begin;// 投放开始时间
	private int end;// 投放结束时间
	private String control;// 投放控制
	private int num;// 投放数量
	private int channelNum;// 渠道数量
	private String ruleouts;// 排除应用
	private String citys;// 投放地区(all全部投放)
	private String hours;// 投放小时(all全部投放)
	private int root;// 是否破解 0否 1 是
	private String options;//广告物料JSON信息(插屏:image和click_url)；(积分墙:icon,title,text1,text2,download,store,callbackurl,callbacks,ids,psize)
	private float priceClickIncome;// 接入单价click
	private float priceClickCost;// 投放单价click
	private float priceCallbackIncome;// 接入单价callback
	private float priceCallbackCost;// 投放单价callback，值为price_income_callback的0.7
	private float priceJobIncome;// 接入单价job
	private float priceJobCost;// 投放单价job，值为cost_callback的0.5
	private int dataFrom;// 广告来源：1普通广告，2渠道广告
	private String ruleins;// 投放应用，添加后就只投放这些应用
	private String adname;
	private int os;// 广告平台 1android 2ios
	private String osver;// 投放系统版本 ios5,ios6,ios7
	private int budget;// 预算状态 0无预算 1日预算
	private float money;// 预算金额
	private int ipNum;// ip数量
	private int interval;// 时间间隔
	private int bootTimeNum;// 相同时间启动数量
	private int ipNumAd;// IP 数量（广告主）
	private int intervalAd;// 时间间隔（广告主）
	private int bootTimeNumAd;// 相同时间启动数量（广告主）
	private int rate; // 分成比率（100以内整型）
	private String billing; // 计费方式
	private String levelouts;
	private int appstoreid;
	private String bundleid;//bundleid 广告包名
	private int isTalkingdata;

	private String fwzLevelouts;

	private String keywords;//关键词，逗号分隔
	private int isAso;
	private int asoPos;

	private int click_num;// 点击投放数量
	private String process_name;// 进程名称
	private int isHsFlag; //1.独家 2.首发 3.100%返现
	private int isHsReport;//运行列表是否加入报表0.默认不加1.加入
	//add by chenjun
	private int update_time;//更新时间
	private String jobs;	
	private int is_hand_stop;
	private String remain;
	private int deliveryType;
	
	public int getUpdateTime() {
		return update_time;
	}
	
	public void setUpdateTime(int update_time) {
		this.update_time = update_time;
	}

	public int getIsHsReport() {
		return isHsReport;
	}

	public void setIsHsReport(int isHsReport) {
		this.isHsReport = isHsReport;
	}

	public int getIsHsFlag() {
		return isHsFlag;
	}

	public void setIsHsFlag(int isHsFlag) {
		this.isHsFlag = isHsFlag;
	}

	public int getAppstoreid() {
		return appstoreid;
	}

	public void setAppstoreid(int appstoreid) {
		this.appstoreid = appstoreid;
	}

	public String getBundleid() {
		return bundleid;
	}

	public void setBundleid(String bundleid) {
		this.bundleid = bundleid;
	}

	public int getIsTalkingdata() {
		return isTalkingdata;
	}

	public void setIsTalkingdata(int isTalkingdata) {
		this.isTalkingdata = isTalkingdata;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getIsAso() {
		return isAso;
	}

	public void setIsAso(int isAso) {
		this.isAso = isAso;
	}

	public int getAsoPos() {
		return asoPos;
	}

	public void setAsoPos(int asoPos) {
		this.asoPos = asoPos;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getSpaceType() {
		return spaceType;
	}

	public void setSpaceType(int spaceType) {
		this.spaceType = spaceType;
	}

	public int getAdid() {
		return adid;
	}

	public void setAdid(int adid) {
		this.adid = adid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getCtype() {
		return ctype;
	}

	public void setCtype(int ctype) {
		this.ctype = ctype;
	}

	public int getCstype() {
		return cstype;
	}

	public void setCstype(int cstype) {
		this.cstype = cstype;
	}

	public int getClevel() {
		return clevel;
	}

	public void setClevel(int clevel) {
		this.clevel = clevel;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getDevices() {
		return devices;
	}

	public void setDevices(String devices) {
		this.devices = devices;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getChannelNum() {
		return channelNum;
	}

	public void setChannelNum(int channelNum) {
		this.channelNum = channelNum;
	}

	public String getRuleouts() {
		return ruleouts;
	}

	public void setRuleouts(String ruleouts) {
		this.ruleouts = ruleouts;
	}

	public String getLevelouts() {
		return levelouts;
	}

	public void setLevelouts(String levelouts) {
		this.levelouts = levelouts;
	}

	public String getCitys() {
		return citys;
	}

	public void setCitys(String citys) {
		this.citys = citys;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public int getRoot() {
		return root;
	}

	public void setRoot(int root) {
		this.root = root;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public float getPriceClickIncome() {
		return priceClickIncome;
	}

	public void setPriceClickIncome(float priceClickIncome) {
		this.priceClickIncome = priceClickIncome;
	}

	public float getPriceClickCost() {
		return priceClickCost;
	}

	public void setPriceClickCost(float priceClickCost) {
		this.priceClickCost = priceClickCost;
	}

	public float getPriceCallbackIncome() {
		return priceCallbackIncome;
	}

	public void setPriceCallbackIncome(float priceCallbackIncome) {
		this.priceCallbackIncome = priceCallbackIncome;
	}

	public float getPriceCallbackCost() {
		return priceCallbackCost;
	}

	public void setPriceCallbackCost(float priceCallbackCost) {
		this.priceCallbackCost = priceCallbackCost;
	}

	public float getPriceJobIncome() {
		return priceJobIncome;
	}

	public void setPriceJobIncome(float priceJobIncome) {
		this.priceJobIncome = priceJobIncome;
	}

	public float getPriceJobCost() {
		return priceJobCost;
	}

	public void setPriceJobCost(float priceJobCost) {
		this.priceJobCost = priceJobCost;
	}

	public int getDataFrom() {
		return dataFrom;
	}

	public void setDataFrom(int dataFrom) {
		this.dataFrom = dataFrom;
	}

	public String getRuleins() {
		return ruleins;
	}

	public void setRuleins(String ruleins) {
		this.ruleins = ruleins;
	}

	public String getAdname() {
		return adname;
	}

	public void setAdname(String adname) {
		this.adname = adname;
	}

	public int getOs() {
		return os;
	}

	public void setOs(int os) {
		this.os = os;
	}

	public String getOsver() {
		return osver;
	}

	public void setOsver(String osver) {
		this.osver = osver;
	}

	public int getBudget() {
		return budget;
	}

	public void setBudget(int budget) {
		this.budget = budget;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float money) {
		this.money = money;
	}

	public int getIpNum() {
		return ipNum;
	}

	public void setIpNum(int ipNum) {
		this.ipNum = ipNum;
	}

	public int getIpNumAd() {
		return ipNum;
	}

	public void setIpNumAd(int ipNum) {
		this.ipNum = ipNum;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getBootTimeNum() {
		return bootTimeNum;
	}

	public void setBootTimeNum(int bootTimeNum) {
		this.bootTimeNum = bootTimeNum;
	}

	public int getNumAd() {
		return ipNumAd;
	}

	public void setNumAd(int ipNumAd) {
		this.ipNumAd = ipNumAd;
	}

	public int getIntervalAd() {
		return intervalAd;
	}

	public void setIntervalAd(int intervalAd) {
		this.intervalAd = intervalAd;
	}

	public int getBootTimeNumAd() {
		return bootTimeNumAd;
	}

	public void setBootTimeNumAd(int bootTimeNumAd) {
		this.bootTimeNumAd = bootTimeNumAd;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public String getBilling() {
		return billing;
	}

	public void setBilling(String billing) {
		this.billing = billing;
	}
	
	public String getFwzLevelouts() {
		return fwzLevelouts;
	}

	public void setFwzLevelouts(String fwzLevelouts) {
		this.fwzLevelouts = fwzLevelouts;
	}

	public int getClick_num() {
		return click_num;
	}

	public void setClick_num(int click_num) {
		this.click_num = click_num;
	}

	public String getProcess_name() {
		return process_name;
	}

	public void setProcess_name(String process_name) {
		this.process_name = process_name;
	}

	public String getJobs() {
		return jobs;
	}

	public void setJobs(String jobs) {
		this.jobs = jobs;
	}

	public int getIs_hand_stop() {
		return is_hand_stop;
	}

	public void setIs_hand_stop(int is_hand_stop) {
		this.is_hand_stop = is_hand_stop;
	}

	public String getRemain() {
		return remain;
	}

	public void setRemain(String remain) {
		this.remain = remain;
	}

	public int getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(int update_time) {
		this.update_time = update_time;
	}

	public int getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(int deliveryType) {
		this.deliveryType = deliveryType;
	}

	public String toString() {
		JSONObject obj = new JSONObject();
		obj.put("adid", adid);
		obj.put("cid", cid);
		obj.put("os", os);
		obj.put("devices", devices);
		obj.put("osver", osver);
		obj.put("citys", citys);
		obj.put("hours", hours);
		obj.put("ruleouts", this.ruleouts);
		obj.put("ruleins", this.ruleins);
		obj.put("levelouts", this.levelouts);
		obj.put("begin", this.begin);
		obj.put("end", this.end);
		obj.put("root", this.root);
		obj.put("num", this.num);
		obj.put("money", Float.valueOf(this.money));
		obj.put("budget", this.budget);
		obj.put("type", this.dataType);
		obj.put("price_callback_cost", Float.valueOf(priceCallbackCost));
		obj.put("options", options);
		obj.put("appstoreid", appstoreid);
		obj.put("bundleid", bundleid);
		obj.put("is_talkingdata", isTalkingdata);
		obj.put("state", state);
		obj.put("keywords", keywords);
		obj.put("is_aso", isAso);
		obj.put("aso_pos", asoPos);
		
		obj.put("ip_ad_num", this.ipNumAd);
		obj.put("ip_ad_interval", this.intervalAd);
		obj.put("ip_ad_boot_time_num", this.bootTimeNumAd);

		obj.put("fwz_levelouts", this.fwzLevelouts);
		obj.put("click_num", this.click_num);
		obj.put("process_name", this.process_name);
		obj.put("is_hs_flag", this.isHsFlag);
		obj.put("is_hs_report", this.isHsReport);
		obj.put("sort", this.sort);
		obj.put("price_click_cost", this.priceClickCost);
		obj.put("update_time", this.update_time);
		obj.put("jobs", this.jobs);
		obj.put("is_hand_stop", this.is_hand_stop);
		obj.put("remain", this.remain);
		obj.put("deliveryType", this.deliveryType);
		return obj.toString();
	}
}
