package com.dianru.analysis.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dianru.analysis.cache.MediaFilterCache;
import com.dianru.analysis.util.JsonUtil;

public class Media extends Common  implements Serializable {

	public static Logger LOG = LogManager.getLogger(Media.class);
	private static final long serialVersionUID = 6490713456309313519L;
	public Media(Map<String, Object> map) {

		this.mid = (int) getValue(map, "mid","int");
		this.uid = (int) getValue(map, "uid","int");
		this.type = (int) getValue(map, "type","int");
		this.title = (String) getValue(map,"title","string");
		this.mtype = (int) getValue(map, "mtype","int");
		this.mstype = (int) getValue(map, "mstype","int");
		this.mlevel = (int) getValue(map, "mlevel","int");
		this.check = (int) getValue(map, "check","int");
		this.state = (int) getValue(map, "state","int");
		this.citys = (String) getValue(map, "citys","string");
		this.hours = (String) getValue(map, "hours","string");
		this.isWangZhuan = (int) getValue(map, "is_wangzhuan","int");
		this.isEnable = (int) getValue(map, "is_enable","int");
		if(this.isWangZhuan == 0) this.isWangZhuan = 1;
		this.shieldedAds = (String) getValue(map, "shielded_ads","string");
		this.isSeesion = (int) getValue(map, "is_session","int");
		this.options = (String) getValue(map, "options","string");
		this.admin_shielded_ads = (String) getValue(map, "admin_shielded_ads","string");
		
		this.rates = new HashMap<Integer, MediaFilter>();
		String ratesStr = (String) getValue(map, "rates","string");
		if (ratesStr != null && !ratesStr.isEmpty()) {
			JSONObject objs = JsonUtil.getJson(ratesStr,String.valueOf(mid));
			if (objs != null) {
				for (int i = 1; i <= Define.TYPES.length-1; i++) {
					JSONObject obj = null;
					try{
						obj = objs.getJSONObject(String.valueOf(i));
					}catch(Exception e){
						continue;
					}
					if (obj == null) {
						MediaFilter mf = getRote(i, 0, 0);
						this.rates.put(i, mf);
					} else {
						int rate = obj.getInt("rate");
						int save = obj.getInt("save");
						MediaFilter mf = getRote(i, rate, save);
						this.rates.put(i, mf);
					}
				}
			}
		}
		if (this.rates.isEmpty()) {
			for (int i = 1; i <= Define.TYPES.length-1; i++) {
				MediaFilter mf = getRote(i, 0, 0);
				this.rates.put(i, mf);
			}
		}
	}

	public MediaFilter getRote(int type, int rate, int save) {
		if ((rate == 0 || save == 0) && type != 0) {
			MediaFilter fl = MediaFilterCache.getInstance().get(type,
					this.mlevel, this.isWangZhuan);
			if (fl != null) {
				if (rate == 0) {
					rate = fl.getRate();// 非网赚比例
				}
				if (save == 0) {
					save = fl.getSave();// 非网赚扣量
				}
			}
		}
		return new MediaFilter(rate, save);
	}

	protected int mid;
	protected int uid;
	protected int type;//'媒体分类 1 应用 2 渠道'
	protected int mtype;
	protected int mstype;
	protected int mlevel;
	protected int check;
	protected int state;//1 投放  2停止  3软删除 4测试（控制墙是否显示） 开发者行为
	protected String citys;
	protected String hours;
	private int isWangZhuan;
	protected String title;
	protected int isEnable;
	protected String shieldedAds;
	protected int isSeesion;
	protected String options;
	protected String admin_shielded_ads;

	protected Map<Integer, MediaFilter> rates;
	
	public String getShieldedAds() {
		return shieldedAds;
	}

	public void setShieldedAds(String shieldedAds) {
		this.shieldedAds = shieldedAds;
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMtype() {
		return mtype;
	}

	public void setMtype(int mtype) {
		this.mtype = mtype;
	}

	public int getMstype() {
		return mstype;
	}

	public void setMstype(int mstype) {
		this.mstype = mstype;
	}

	public int getMlevel() {
		return mlevel;
	}

	public void setMlevel(int mlevel) {
		this.mlevel = mlevel;
	}

	public Map<Integer, MediaFilter> getRates() {
		return rates;
	}

	public void setRates(Map<Integer, MediaFilter> rates) {
		this.rates = rates;
	}

	public int getCheck() {
		return check;
	}

	public void setCheck(int check) {
		this.check = check;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getCitys() {
		return citys;
	}

	public boolean inCitys(String city) {
		if (citys == null || citys.isEmpty())
			return false;
		return citys.indexOf(city) >= 0;
	}

	public void setCitys(String citys) {
		this.citys = citys;
	}

	public String getHours() {
		return hours;
	}

	public boolean inHours(String hour) {
		if (hours == null || hours.isEmpty())
			return false;
		return hours.indexOf(hour) >= 0;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}
	public int getIsWangZhuan() {
		return isWangZhuan;
	}
	public void setIsWangZhuan(int isWangZhuan) {
		this.isWangZhuan = isWangZhuan;
	}
	
	public int getIsEnable() {
		return isEnable;
	}
	public void setIsEnable(int isEnable) {
		this.isEnable = isEnable;
	}

	public int getIsSeesion() {
		return isSeesion;
	}

	public void setIsSeesion(int isSeesion) {
		this.isSeesion = isSeesion;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public String getAdmin_shielded_ads() {
		return admin_shielded_ads;
	}

	public void setAdmin_shielded_ads(String admin_shielded_ads) {
		this.admin_shielded_ads = admin_shielded_ads;
	}
}
