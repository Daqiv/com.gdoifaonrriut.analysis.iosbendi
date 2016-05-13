package com.dianru.analysis.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.json.JSONObject;

import com.dianru.analysis.util.ListUtil;

public class MediaPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	public MediaPrice(List<Object> vals) {
		this.adid = ListUtil.getInt(vals, 0);
		this.appid = ListUtil.getInt(vals, 1);
		this.effect_time = ListUtil.getInt(vals, 2);
		this.price = ListUtil.getFloat(vals, 3);
	}
	
	public int adid;
	public int appid;
	public float price; 
	public int effect_time;
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		adid = in.readInt();
		appid = in.readInt();
		effect_time = in.readInt();
		price = in.readFloat();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{	
		out.writeInt(adid);
		out.writeInt(appid);
		out.writeInt(effect_time);
		out.writeFloat(price);
	}
	
	public String toString() {
		JSONObject obj = new JSONObject();
		obj.put("effect_time", effect_time);
		obj.put("price", price);
		return obj.toString();
	}
}
