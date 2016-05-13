package com.dianru.analysis.bean;

public class Source {
	//载体信息
	public float version;//版本信息 version (主版本号.子版本号)
	public String device;//设备 device [代号 版本]
	public String screen;//分辨率 screen [宽度x高度]
	public String os;//操作系统 os [Android,IOS]
	public float osver;//系统版本号 osver (主版本号.子版本号)
	public String udid;//唯一标识 idfa 
	public String openudid;//OpenUDID
	public String mac;//MAC地址 mac
	public String hv;//屏幕方向 hv [0 | 1]
	public long lip;//内网地址 localip
	public String net;//网络类型 net [wifi | 2g | 3g | 4g]
	public String isp;//运营商 [WL=wlan CU=联通 CM=移动]
	public int root;//是否破解 root [1 | 0]
	public String packagz;//包名称 package [*.*.*]
	public String loc;//地理位置 location
	public long time;//设备时间 time [UNIX 时间戳]
	public int vm;//主版本号
}
