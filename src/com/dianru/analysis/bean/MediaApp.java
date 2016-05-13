package com.dianru.analysis.bean;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class MediaApp extends Media {

	private static final long serialVersionUID = 1L;
	
	private String keywords;
	private int os;
	
	private String callbackUrl;
	private String callbackKey;
	private String callbackIdfa;
	private String callbackMac;
	
	private float ratio;
	private String unit;
	
	private String offerwall;
	private String freewall;
	private String insertscreen;
	private String fullscreen;
	private String banner;
	private String hotscreen;
	private String videoscreen;
	/**
	 * @author zhujunwu
	 * 查询sdk_version
	 * sdk_version = 2.0
	 * save和rate全部扣完
	 * */
	private String sdkVersion; // SDK版本

	public MediaApp(Map<String,Object> map) {
		
		super(map);
		this.keywords = (String) getValue(map, "keywords","string");
		this.os = (int) getValue(map, "os","int");
		
		this.callbackUrl = (String) getValue(map, "callback_url","string");
		this.callbackKey = (String) getValue(map,"callback_key","string");
		this.callbackIdfa = (String) getValue(map, "callback_idfa","string");
		this.callbackMac = (String) getValue(map, "callback_mac","string");
		
		this.ratio = (float) getValue(map, "ratio" , "float");
		this.unit = (String) getValue(map, "unit","string");
		
		this.offerwall = (String) getValue(map, "offer_wall","string");
		this.freewall = (String) getValue(map,"free_wall","string");
		this.insertscreen = (String) getValue(map, "insert_screen","string");
		this.fullscreen = (String) getValue(map, "full_screen","string");
		this.banner = (String) getValue(map, "banner","string");
		
		this.hotscreen = (String) getValue(map, "offer_wall","string");
		this.videoscreen = (String) getValue(map, "insert_screen","string");
		
		this.sdkVersion = (String) getValue(map, "sdk_version","string");
		this.admin_shielded_ads = (String) getValue(map, "admin_shielded_ads","string");
	}
	
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public int getOs() {
		return os;
	}
	public void setOs(int os) {
		this.os = os;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getCallbackKey() {
		return callbackKey;
	}
	public void setCallbackKey(String callbackKey) {
		this.callbackKey = callbackKey;
	}
	public String getCallback_idfa() {
		return callbackIdfa;
	}
	public void setCallback_idfa(String callbackIdfa) {
		this.callbackIdfa = callbackIdfa;
	}
	public String getCallbackMac() {
		return callbackMac;
	}
	public void setCallbackMac(String callbackMac) {
		this.callbackMac = callbackMac;
	}
	public float getRatio() {
		return ratio;
	}
	public void setRatio(float ratio) {
		this.ratio = ratio;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getOfferwall() {
		return offerwall;
	}
	public void setOfferwall(String offerwall) {
		this.offerwall = offerwall;
	}
	public String getFreewall() {
		return freewall;
	}
	public void setFreewall(String freewall) {
		this.freewall = freewall;
	}
	public String getInsertscreen() {
		return insertscreen;
	}
	public void setInsert_screen(String insertscreen) {
		this.insertscreen = insertscreen;
	}
	public String getFullscreen() {
		return fullscreen;
	}
	public void setFullscreen(String fullscreen) {
		this.fullscreen = fullscreen;
	}
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getHotscreen() {
		return hotscreen;
	}
	public void setHotscreen(String hotscreen) {
		this.hotscreen = hotscreen;
	}
	public String getVideoscreen() {
		return videoscreen;
	}
	public void setVideoscreen(String videoscreen) {
		this.videoscreen = videoscreen;
	}
	public String getSdkVersion() {
		return sdkVersion;
	}
	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}
	
	public String toString() {
		JSONObject obj = new JSONObject();
		
		JSONObject objRates = new JSONObject();
		for(Iterator<Entry<Integer,MediaFilter>> it = rates.entrySet().iterator();it.hasNext();) {
			Entry<Integer,MediaFilter> entry = it.next();
			Integer key = entry.getKey();
			MediaFilter value = entry.getValue();
			
			JSONObject objf = new JSONObject();

			objf.put("rate", value.getRate());
			objf.put("save", value.getSave());
			
			objRates.put(String.valueOf(key), objf);
		}
		
		obj.put("appid", mid);
		obj.put("uid", uid);
		obj.put("type", type);
		obj.put("mlevel", mlevel);
		obj.put("ratio", ratio);
		obj.put("unit", unit);
		
		obj.put("rates", objRates);
		obj.put("state", state);
		obj.put("offerwall",offerwall);
		obj.put("freewall",freewall);
		obj.put("insertscreen",insertscreen);
		obj.put("fullscreen",fullscreen);
		obj.put("banner;", banner);
		
		obj.put("hotscreen;", hotscreen);
		obj.put("videoscreen;", videoscreen);
		obj.put("sdkVersion", sdkVersion);
		obj.put("is_wangzhuan", this.getIsWangZhuan());
		obj.put("citys", citys);
		obj.put("hours", hours);
		obj.put("shielded_ads", shieldedAds);
		obj.put("is_session", this.isSeesion);
		obj.put("admin_shielded_ads", this.admin_shielded_ads);
		
		return obj.toString();
	}
}
