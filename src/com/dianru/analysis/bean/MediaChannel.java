package com.dianru.analysis.bean;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class MediaChannel extends Media {

	private static final long serialVersionUID = 1L;
	
	public String adids;
	public String callbacks;
	
	public MediaChannel(Map<String,Object> map) {
		super(map);
		
		this.adids = (String) getValue(map, this.adids, "adids");
		this.callbacks = (String) getValue(map, this.callbacks, "callbacks");
	}
	
	public String getAdids() {
		return adids;
	}
	public void setAdids(String adids) {
		this.adids = adids;
	}
	public String getCallbacks() {
		return callbacks;
	}
	public void setCallbacks(String callbacks) {
		this.callbacks = callbacks;
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
		obj.put("rates", objRates);
		obj.put("callbacks", callbacks);
		obj.put("adids", adids);
		obj.put("state", state);
		obj.put("citys", citys);
		obj.put("hours", hours);
		obj.put("shielded_ads", shieldedAds);
		obj.put("is_session", this.isSeesion);
		
		return obj.toString();
	}
}
