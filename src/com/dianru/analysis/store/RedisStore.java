package com.dianru.analysis.store;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.util.Base64;
import com.dianru.analysis.util.IPUtil;
import com.dianru.analysis.util.RedisConnection;

public class RedisStore {
	
	private static RedisStore STORE = new RedisStore();
	
	public static RedisStore getInstance() {
		return STORE;
	}
	
	public boolean updateCount(int adid, int num, float money) {
		
		Jedis jedis = RedisConnection.getInstance("main");
		if(jedis == null) return false;
		
		String key = "ADID_TODAY_ACTIVE_NUM";
		String field = String.valueOf(adid);
		String value = String.format("%d,%02f", num, money);
		
		jedis.hset(key, field, value);
		RedisConnection.close("main",jedis);
		return true;
	}
	
	public boolean addClick(String field, int adid) {
		Jedis jedis = RedisConnection.getInstance("main");
		if(jedis == null) return false;
		
		String key = "DATA_CLICK_INFO";
		
		String value = jedis.hget(key, field);
		if(value != null && !value.isEmpty()) {
			value += ',';
			value += adid;
		}else {
			value = String.valueOf(adid);
		}
		
		jedis.hset(key, field, value);
		RedisConnection.close("main",jedis);
		return true;
	}
	
	public boolean addActive(String field, int adid) {
		
		//MAC replace :  AND UDID replace -
		field = field.replace(":", ""); 
		field = field.replace("-", ""); 
		
		Jedis jedis = RedisConnection.getInstance("main");
		if(jedis == null) return false;
		
		//String key = "DATA_INSTALL_INFO";
		String key = "DATA_INSTALL_ADID";
		
		String value = jedis.hget(key, field);
		if(value != null && !value.isEmpty()) {
			value += ',';
			value += adid;
		}else {
			value = String.valueOf(adid);
		}
		
		jedis.hset(key, field, value);
		RedisConnection.close("main",jedis);
		return true;
	}
	
	/**
	 * 添加激活作弊限制
	 * 同1个媒体应用id，同1个appuseid，同1个广告id，1天之内只能激活1次
	 * from liuqing
	 */
	public boolean isCheat(int adid, int appid, String appuserid,String udid){
		
		String field = String.format("%d%d%s", adid,appid, appuserid.toLowerCase());
		Jedis jedis = RedisConnection.getInstance("cheat");
		if(jedis == null){
			return false;
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String date = df.format(new Date());//获取当天日期
		
		String key = "CHEAT__APPUSERID_"+ date;
		boolean exists = jedis.hexists(key, field);
		if(exists) {
			RedisConnection.close("cheat",jedis);
			return true;
		}
		
		jedis.hset(key, field, udid.replace("-", "").toLowerCase());
		RedisConnection.close("cheat",jedis);
		return false;
	}
	
	public boolean isExist(String find , String source){
		String [] s = source.split(",");
		for (String str : s) {
			if(str.equals(find)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isActive(String field, int adid) {
		
		field = field.replace(":", ""); 
		field = field.replace("-", ""); 
		
		boolean result = false;
		Jedis jedis = RedisConnection.getInstance("main");
		if(jedis == null) return result;
		
		String key1 = "DATA_INSTALL_ADID";
		String value1 = jedis.hget(key1, field);
		if(value1 != null && !value1.isEmpty()) {
			//判断是否存在
			if(isExist(String.valueOf(adid), value1)){
				result = true;
			}
		}
		
		String key2 = "DATA_INSTALL_INFO";
		String value2 = jedis.hget(key2, field);
		if(value2 != null && !value2.isEmpty()) {
			//判断是否存在
			if(isExist(String.valueOf(adid), value2)){
				result = true;
			}
		}
		RedisConnection.close("main",jedis);
		return result;
	}
	
	//完成所有任务记录
	public boolean addJob(String field, int adid) {
		
		boolean result = false;
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			
			//MAC replace :  AND UDID replace -
			field = field.replace(":", ""); 
			field = field.replace("-", ""); 
			
			String key = "DATA_INSTALL_JOB";
			String value = jedis.hget(key, field);
			if(value != null && !value.isEmpty()) {
				value += ',';
				value += adid;
			}else {
				value = String.valueOf(adid);
			}
			jedis.hset(key, field, value);
			result = true;
		} catch (Exception e) {
		}finally {
			if(jedis != null ) RedisConnection.close("main",jedis);
		}
		return result;
	}
	
	//检查是否存在作弊软件
	public boolean checkCheatSoft(String mac, String udid){
		
		boolean flag = false;
		Jedis jedis = null;
		try {
			String key = String.format("%s%s", mac.replace(":", ""), udid.replace("-", ""));
			jedis = RedisConnection.getInstance("softs");
			for (String process : Define.CHEATS) {
				flag = jedis.hexists(key, process);
				if(flag){
					break;
				}
			}
		}catch (Exception ex) {
		}finally {
			if(jedis != null) {
				RedisConnection.close("softs", jedis);
			}
		}
		return flag;
	}
	
	//正常软件
	public boolean checkNormalSoft(String mac, String udid){
		
		boolean flag = false;
		Jedis jedis = null;
		try {
			String key = String.format("%s%s", mac.replace(":", ""), udid.replace("-", ""));
			jedis = RedisConnection.getInstance("softs");
			for (String process : Define.NORMAL) {
				flag = jedis.hexists(key, process);
				if(flag){
					break;
				}
			}
		}catch (Exception ex) {
		}finally {
			if(jedis != null) {
				RedisConnection.close("softs", jedis);
			}
		}
		return flag;
	}
	
	//did
	public int  addDidCheat(Ads ad, String session ,String did, String udid){
		
		int invalid = 0;
		Jedis jedis = null;
		JSONObject optJson = null;
		try {
				String optStr = ad.getOptions();
				optJson = new JSONObject(optStr);
				jedis = RedisConnection.getInstance("check");
				//开启session防作弊
				if(optJson != null && optJson.get("is_session") != null && "1".equals(optJson.get("is_session").toString())){
					//session 作弊
					if(session.length() > 0 && (session.charAt(4) != '5' || session.charAt(7) != '8' || session.charAt(14) != '7' || session.charAt(24) != '4')){
							 invalid = 11;		//session 
					}
				}
				
				String _udid = udid.replaceAll("-", "").toLowerCase();
				String _did = did.replaceAll("-", "").toLowerCase();
				String udid_did = "CHEAT_UDID_DID";
				if(!jedis.hexists(udid_did, _udid)){
					jedis.hset(udid_did, _udid, _did);
				}
		} catch (Exception e) {
		}finally {
			if(jedis != null) {
				RedisConnection.close("check", jedis);
			}
		}
		return invalid;
	}
	public int checkDidCheat(Ads ad, String udid) {
		
		int invalid = 0;
		Jedis _jedis = null;
		JSONObject optJson = null;
		try {
			String optStr = ad.getOptions();
			optJson = new JSONObject(optStr);
			_jedis = RedisConnection.getInstance("check");
			
			int adid = ad.getAdid();
			String _udid = udid.replaceAll("-", "").toLowerCase();
			String udid_did = "CHEAT_UDID_DID";
			String _did = _jedis.hget(udid_did, _udid);
			
			if(_did != null && _did.length() > 0) {
				//did 激活是否包含adid
				Boolean exists = _jedis.sismember(_did, String.valueOf(adid));	
				
				//开启session防作弊
				if(optJson != null && optJson.get("is_session") != null && "1".equals(optJson.get("is_session").toString())){
					if(exists){
						//包含标记假量
						invalid = 9;
					}else {
						_jedis.sadd(_did, String.valueOf(adid));
					}
				}else {
					if(!exists){
						_jedis.sadd(_did, String.valueOf(adid));
					}
				}
			}
		} catch (Exception e) {
		}finally {
			if(_jedis != null) {
				RedisConnection.close("check", _jedis);
			}
		}
		return  invalid;
	}
	
	//ssid
	public void addSsidCheat(String udid, String ssid, String localip){
		Jedis ssidJedis = null;
		try {
				ssidJedis = RedisConnection.getInstance("ssid");
				String _udid = udid.replaceAll("-", "").toLowerCase();
				
				ssid = Base64.decode(ssid);
				String [] ssidArr = ssid.split("-");
				String _ssid = ssidArr[ssidArr.length - 1].replaceAll(":", "").toLowerCase();
				
				long lip = IPUtil.ip2long(localip);
				String udid_ssid = "CHEAT_UDID_SSID";
				if(!ssidJedis.hexists(udid_ssid, _udid)){
					ssidJedis.hset(udid_ssid, _udid, _ssid+""+lip);
				}
		} catch (Exception e) {
		}finally {
			if(ssidJedis != null) {
				RedisConnection.close("ssid", ssidJedis);
			}
		}
	}
	public int checkSsidCheat(Ads ad, String udid) {
		int invalid = 0;
		Jedis _ssidJedis = null;
		JSONObject optJson = null;
		try {
			String optStr = ad.getOptions();
			optJson = new JSONObject(optStr);
			_ssidJedis = RedisConnection.getInstance("ssid");
			
			int adid = ad.getAdid();
			String _udid = udid.replaceAll("-", "").toLowerCase();
			String udid_ssid = "CHEAT_UDID_SSID";
			String _ssid = _ssidJedis.hget(udid_ssid, _udid);
			
			if(_ssid != null && _ssid.length() > 0) {
				
				//_ssid 激活是否包含adid
				Boolean exists = _ssidJedis.sismember(_ssid, String.valueOf(adid));	
				//开启session防作弊
				if(optJson != null && optJson.get("is_ssid") != null && "1".equals(optJson.get("is_ssid").toString())){
					if(exists){
						//包含标记假量
						invalid = 13;			//ssid 
					}else {
						_ssidJedis.sadd(_ssid, String.valueOf(adid));
					}
				}else {
					if(!exists){
						_ssidJedis.sadd(_ssid, String.valueOf(adid));
					}
				}
			}
		} catch (Exception e) {
		}finally {
			if(_ssidJedis != null) {
				RedisConnection.close("ssid", _ssidJedis);
			}
		}
		return invalid;
	}
	
	//openudid udid 防作弊
	public void addOpenudidCheat(String udid, String openudid){
		Jedis openJedis = null;
		try {
				openJedis = RedisConnection.getInstance("openudid");
				
				String _udid = udid.replaceAll("-", "").toLowerCase();
				String _openudid = openudid.replaceAll(":", "").replaceAll("-", "").toLowerCase();
				
				String udid_openudid = "CHEAT_UDID_OPENUDID";
				if(!openJedis.hexists(udid_openudid, _udid)){
					openJedis.hset(udid_openudid, _udid, _openudid);
				}
		} catch (Exception e) {
		}finally {
			if(openJedis != null) {
				RedisConnection.close("openudid", openJedis);
			}
		}
	}
	public int checkOpenudidCheat(Ads ad, String udid){
		int invalid = 0; 
		Jedis openJedis = null;
		JSONObject optJson = null;
		try {
			String optStr = ad.getOptions();
			optJson = new JSONObject(optStr);
			openJedis = RedisConnection.getInstance("openudid");
			
			int adid = ad.getAdid();
			String _udid = udid.replaceAll("-", "").toLowerCase();
			String udid_open = "CHEAT_UDID_OPENUDID";
			String _open = openJedis.hget(udid_open, _udid);
			
			if(_open != null && _open.length() > 0) {
				
				//_ssid 激活是否包含adid
				Boolean exists = openJedis.sismember(_open, String.valueOf(adid));	
				
				//开启session防作弊
				if(optJson != null && optJson.get("is_openudid") != null && "1".equals(optJson.get("is_openudid").toString())){
					if(exists){
						//包含标记假量
						invalid = 12;			//openudid
					}else {
						openJedis.sadd(_open, String.valueOf(adid));
					}
				}else {
					if(!exists){
						openJedis.sadd(_open, String.valueOf(adid));
					}
				}
			}
		} catch (Exception e) {
		}finally {
			if(openJedis != null) {
				RedisConnection.close("openudid", openJedis);
			}
		}
		return invalid;
	}
	
	//点击广告新增磁盘变化
	public void addClickDisk(String mac, String udid , int adid, long disk){
		
		Jedis diskJedis = null;
		try {
				diskJedis = RedisConnection.getInstance("cheat");
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
				String today = df.format(new Date());//获取当天日期
				
				String redisKey = "CHEAT_DISK_"+today;
				String field = String.format("%s%s%d", mac.replaceAll(":", "").toLowerCase(), udid.replaceAll("-", "").toLowerCase(),adid);
				diskJedis.hset(redisKey, field, String.valueOf(disk));
		}catch (Exception ex) {
		}finally {
			if(diskJedis != null) {
				RedisConnection.close("cheat", diskJedis);
			}
		}
	}
	
	//激活检测磁盘前后变化
	public boolean checkActiveDisk(String mac, String udid , Ads ad, long disk){
		
		boolean flag = false;
		Jedis diskJedis = null;
		JSONObject optJson = null;
		try {
				diskJedis = RedisConnection.getInstance("cheat");
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
				String today = df.format(new Date());//获取当天日期
				
				String redisKey = "CHEAT_DISK_"+today;
				String field = String.format("%s%s%d", mac.replaceAll(":", "").toLowerCase(), udid.replaceAll("-", "").toLowerCase(),ad.getAdid());
				String value = diskJedis.hget(redisKey, field);
				if(value != null && value.length() > 0) {
					String optStr = ad.getOptions();
					optJson = new JSONObject(optStr);
					
					float psize = Float.parseFloat(optJson.get("psize").toString());
					long lsize  = (long) (psize * 1024 * 1024);
					long clickDisk = Long.parseLong(value);
					if(clickDisk - lsize < disk){
						flag = true;
					}
				}
		}catch (Exception ex) {
		}finally {
			if(diskJedis != null) {
				RedisConnection.close("cheat", diskJedis);
			}
		}
		return flag;
	}
	
	public static void main(String[] args) {
		
		String udid = "DE7D16FA-EBEE-4C7C-B510-97189B5169FE";
		String _udid = udid.replaceAll("-", "").toLowerCase();
		System.out.println(_udid);
	}
}