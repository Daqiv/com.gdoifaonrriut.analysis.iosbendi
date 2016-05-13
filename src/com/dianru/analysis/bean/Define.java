package com.dianru.analysis.bean;

import java.util.ArrayList;
import java.util.List;

public class Define {
	
	//应用开始
	public final static int ACTIVE_SAVE_APP = 20;
	//渠道开始
	public final static int ACTIVE_SAVE_CHANNEL = 50;
	//激活最大计数
	public final static int ACTIVE_SAVE_MAX = 100;
	
	public final static String[] ACTIONS = {
		"unknow","show","click","jump","active","job"
	};
	public final static int ACTION_UNKNOW 		= 0; 
	public final static int ACTION_SHOW 		= 1;//展示
	public final static int ACTION_CLICK 	    = 2;//点击
	public final static int ACTION_JUMP 	    = 3;//跳转
	public final static int ACTION_ACTIVE   	= 4;//激活
	public final static int ACTION_JOB 			= 5;//任务
	
	public final static int ACTION_JOB_NEXT 	= 6;//多次激活	
	
	public final static String[] FROMS = {
		"unknow","sdk","api","channel","process"
	};
	public final static int FROM_UNKNOW 	= 0;
	public final static int FROM_SDK 		= 1; //SDK
	public final static int FROM_API 		= 2; //API
	public final static int FROM_CHANNEL	= 3; //渠道
	public final static int FROM_PROCESS    = 4; //进程激活
	
	public final static String[] TYPES = {
		"unknow","offer_wall","free_wall","insert_screen","full_screen","banner","hot_screen","video_screen"
	};
	
	public final static String[] DATA_SOURCES = {
		"unknow","offerwall","freewall","insertscreen","fullscreen","banner","hotscreen","videoscreen"
	};
	
	public static String getDataSourceName(int type) {
		if(type >= DATA_SOURCES.length) return "define";
		return DATA_SOURCES[type];
	}
	
	public final static boolean IN = false;
	public final static boolean OUT = true;
	
	public final static String YEAR = "year";//年
	public final static String MON = "mon";//月
	public final static String DAY = "day";//日
	public final static String HOUR = "hour";//时
	public final static String MIN = "min";//分
	public final static String SEC = "sec";//秒
	
	public final static String TYPE = "type";//类型
	public final static String ACTION = "action";//动作
	public final static String FROM = "from";//来源

	public final static String CID = "cid";//广告主
	public final static String ADID = "adid";//广告
	public final static String CTYPE = "ct";//广告分类
	public final static String CSTYPE = "cst";//广告二级分类
	public final static String CLEVEL  = "cl";//广告评级
	public final static String CSTATE = "cs";//广告状态
	public final static String BALLING = "bl";//计费方式

	public final static String UID = "uid";//开发者
	public final static String APPID = "appid";//应用
	public final static String ATYPE = "at";//应用分类
	public final static String ASTYPE = "ast";//应用二级分类
	public final static String ALEVEL  = "al";//应用评级
	public final static String ASTATE = "as";//应用状态
	
	public final static String APP_USER_KEY = "appuserid";
	
	public final static String INDEX = "idx";//展示位置
	public final static String POS = "pos";//点击位置
	
	//载体信息
	public final static String VERSION = "version";//版本信息 version (主版本号.子版本号)
	public final static String SESSION = "session";//版本信息 version (主版本号.子版本号)
	public final static String MODEL = "model";//版本信息 version (主版本号.子版本号)
	public final static String DID = "did";//版本信息 version (主版本号.子版本号)
	public final static String DEVICE = "device";//设备 device [代号 版本]
	public final static String SCREEN = "screen";//分辨率 screen [宽度x高度]
	public final static String OS = "os";//操作系统 os [Android,IOS]
	public final static String OSVER = "osver";//系统版本号 osver (主版本号.子版本号)
	public final static String UDID = "udid";//唯一标识 idfa 
	public final static String OPENUDID = "openudid";//OpenUDID
	public final static String MAC = "mac";//MAC地址 mac
	public final static String HV = "hv";//屏幕方向 hv [0 | 1]
	public final static String LOCAL_IP = "lip";//内网地址 localip
	public final static String NET = "net";//网络类型 net [wifi | 2g | 3g | 4g]
	public final static String ISP = "isp";//运营商 [WL=wlan CU=联通 CM=移动]
	public final static String ROOT = "root";//是否破解 root [1 | 0]
	public final static String PACKAGE = "package";//包名称 package [*.*.*]
	public final static String LOCATION = "loc";//地理位置 location
	public final static String TIME = "time";//设备时间 time [UNIX 时间戳]
	public final static String VER_MAJOR = "vm";//主版本号
	public final static String IDS = "ids";//主版本号
	public final static String UPTIME = "uptime";
	
	public final static String SSID = "ssid";//路由mac
	public final static String LOCALIP = "localip";//内网地址 localip
	
	public final static String ACTIVE_NUM = "active_num"; //激活次数
	public final static String DISK = "disk"; 			   			   //剩余磁盘量
	public final static String RLOAD = "rload"; 			   		   //磁盘
	
	public final static String DRKEY = "drkey";//回调KEY
	public final static String IP = "ip";//来源网关IP
	
	public final static int MARK_SAVED = 0b0001;
	public final static String SAVED = "save";//是否假量
	public final static String SHOW_SAVED = "show_saved";//是否假量
	public final static String CLICK_SAVED = "click_saved";//是否假量
	public final static String JUMP_SAVED = "jump_saved";//是否假量
	public final static String ACTIVE_SAVED = "active_saved";//是否假量
	public final static String JOB_SAVED = "job_saved";
	
	public final static int MARK_COUNT = 0b0011;
	public final static String REAL = "real";//是否假量
	public final static String SHOW_COUNT = "show_count";//是否假量
	public final static String CLICK_COUNT = "click_count";//是否假量
	public final static String JUMP_COUNT = "jump_count";//是否假量
	public final static String ACTIVE_COUNT = "active_count";//是否假量
	public final static String JOB_COUNT = "job_count";
	
	public final static int MARK_INVALID = 0b0100;
	public final static String SHOW_INVALID = "show_invalid";//是否假量
	public final static String CLICK_INVALID = "click_invalid";//是否假量
	public final static String JUMP_INVALID = "jump_invalid";//是否假量
	public final static String ACTIVE_INVALID = "active_invalid";//是否假量
	public final static String JOB_INVALID = "job_invalid";
	
	public final static int MARK_UNIQUE = 0b1000;
	public final static String SHOW_UNIQUE = "show_unique";//是否假量
	public final static String CLICK_UNIQUE = "click_unique";//是否假量
	public final static String JUMP_UNIQUE = "jump_unique";//是否假量
	public final static String ACTIVE_UNIQUE = "active_unique";//是否假量
	public final static String JOB_UNIQUE = "job_unique";
	
	
	public final static String PRICE_INCOME = "price_income"; //广告主价格
	public final static String PRICE_COST = "price_cost"; //投放价格
	
	public final static String IMAGE = "image";
	public final static String CLICK = "click";
	
	public final static String MONEY_INCOME = "money_income"; //收入
	public final static String MONEY_COST = "money_cost"; //成本
	
	public final static String MONEY_CLICK_INCOME = "click_income"; //点击收入
	public final static String MONEY_CLICK_COST = "click_cost"; //点击成本
	
	public final static String MONEY_ACTIVE_INCOME = "active_income"; //回调收入
	public final static String MONEY_ACTIVE_COST = "active_cost";//回调成本
	
	public final static String MONEY_JOB_INCOME = "job_income";//任务收入
	public final static String MONEY_JOB_COST = "job_cost";//任务成本
	
	public final static String DATA_FROM = "data_from" ;//数据来源：1sdk,2api,3channel（自身渠道，别人渠道）
	public final static String AD_FROM = "ad_from";//广告来源：0普通广告，1渠道广告
	
	public final static String DEF_MAC = "02:00:00:00:00:00";
	public final static String DEF_IDFA = "";
	public final static String DEF_ISP = "UN";
	public final static String DEF_CITY = "UN";
	
	public final static String OBJ_NAME_MEDIA = "media";
	public final static String OBJ_NAME_AD = "ad";
	public final static String[] OBJ_NAMES = {
		OBJ_NAME_MEDIA, OBJ_NAME_AD
	};
	public final static int OBJ_MEDIA = 0;
	public final static int OBJ_AD = 1;
	
	public final static int OBJ_FILTER_LEVEL = 3;// 防作弊等级设置
	
	public final static String[] FIELDS = {
		TYPE,ACTION,FROM,
		YEAR,MON,DAY,HOUR,MIN,SEC,
		IP
	};
	public final static String[] SOURCE_FIELDS = {
		OS,DEVICE,OSVER,VERSION,UDID,MAC,OPENUDID,
		ISP,LOCATION,UID,APPID,APP_USER_KEY
	};
	
	public static interface Index {
		public final static int TYPE = 0;
		public final static int ACTION = 1;
		public final static int FROM = 2;
		public final static int YEAR = 3;
		public final static int MON = 4;
		public final static int DAY = 5;
		public final static int HOUR = 6;
		public final static int MIN = 7;
		public final static int SEC = 8;
		public final static int IP = 9;
	};
	
	public static interface SourceIndex extends Index {
		
		public final static int OS = 10;
		public final static int DEVICE = 11;
		public final static int OSVER = 12;
		public final static int VERSION = 13;
		public final static int UDID = 14;
		public final static int MAC = 15;
		public final static int OPENUDID = 16;
		public final static int ISP = 17;
		public final static int LOCATION = 18;
		public final static int UID = 19;
		public final static int APPID = 20;
		public final static int APP_USER_KEY = 21;
	}
	
	/*
	 * CO 中国其他  China Other
	 * CM 移动     China Mobile
	 * CU 联通     China Unicom
	 * CN 电信     China Net
	 * CE 教育网   China Education Network
	 * CT 电信通   China DianXinTong
	 * UN 未知     Unknow   
	 */
	public final static String[] ISPS = {
		"CO", //中国其它
		"CM", //移动
		"CU", //联通
		"CN", //电信
		"CE", //教育网
		"CT"  //电信通
	};
	
	public final static String[] LOCATIONS = {
		"BJ",//北京
		"SH",//上海
		"TJ",//天津
		"CQ",//重庆
		"XG",//香港
		"AM",//澳门
		"AH",//安徽
		"FJ",//福建
		"GD",//广东
		"GX",//广西
		"GZ",//贵州
		"GS",//甘肃
		"HA",//海南
		"HB",//河北
		"HE",//河南
		"HL",//黑龙江
		"HU",//湖北
		"HN",//湖南
		"JL",//吉林
		"JS",//江苏
		"JX",//江西
		"LN",//辽宁
		"NM",//内蒙古
		"NX",//宁夏
		"QH",//青海
		"SA",//陕西
		"SX",//山西
		"SD",//山东
		"SC",//四川
		"XZ",//西藏
		"XJ",//新疆
		"YN",//云南
		"ZJ",//浙江
		"TW",//台湾
		"CN",//中国
		"UN" //国外
	};
	
	public final static List<String> ISP_LIST = new ArrayList<String>();
	public final static List<String> LOCATION_LIST = new ArrayList<String>();
	static {
		for(String def : ISPS) ISP_LIST.add(def);
		for(String def : LOCATIONS) LOCATION_LIST.add(def);
	}
	
	public final static String[] OSS = {
		"ios","android"
	};
	
	public final static String[] DEVICES = {
		"iphone","ipad","aphone","apad"
	};
	
	//作弊软件进程数据
	public final static String [] CHEATS = {
		"org.ioshack.iGrimace", "com.cdwx.bz", "com.touchelf.touchelf", "com.touchsprite.ios"
	};
	
	public final static String [] NORMAL = {
		"com.tencent.mqq","com.tencent.QQMusic","com.tencent.QQPim","com.tencent.xin",
		"com.tencent.","com.tencent.qqmail","com.tencent.QQ-Mobile-Token-2-0","com.tencent.live4iphone",
		"com.alipay.iphoneclient","com.tenpay.mobile.iphone","com.kuwo.KuwoTingting","com.taobao.taobao4iphone",
		"com.taobao.tmall","com.netease.mail","cn.12306.rails12306","net.qihoo.freewifi",
		"com.cmbchina.MPBBank"
	};
	
	public final static String ICON = "icon";//广告ICON
	public final static String TITLE ="title";//广告标题
	public final static String TEXT1 = "text1";//描述
	public final static String TEXT2 = "text2";//激活描述
	public final static String DOWNLOAD = "download";//下载地址(越狱)
	public final static String STORE = "store";//商店下载地址
	
	//	callbackurl,callbacks,ids,psize
	public final static String CALLBACKURL = "callbackurl";//回调地址
	public final static String CALLBACKS = "callbacks"; //回调参数

	public static final String[] AD_OPTIONS = {
		TYPE,IMAGE,TITLE,TEXT1,TEXT2,CLICK,DOWNLOAD,STORE,CALLBACKURL,CALLBACKS
	};
	
	//应用信息
	public final static int MEDIA_TYPE_APP = 1;
	//渠道信息
	public final static int MEDIA_TYPE_CHANNEL = 2;
	public final static String MEDIA_CHANNEL_CALLBACKS = "callbacks" ; //白名单IP（渠道给我发点击/激活)
	public final static String[] MEDIA_INFO_CHANNEL= {
		MEDIA_CHANNEL_CALLBACKS //白名单IP（渠道给我发点击/激活)
	};
	public final static int MEDIA_INFO_CHANNEL_CALLBACKS = 0;
	public static final String[] SPACE_INFOS = {
		"type","state","callback","style"
	};
}
