package com.dianru.analysis.callback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Media;
import com.dianru.analysis.bean.MediaApp;
import com.dianru.analysis.cache.AdsCache;
import com.dianru.analysis.cache.MediaCache;
import com.dianru.analysis.cache.MediaFilterCache;
import com.dianru.analysis.cache.MeidaPriceCache;
import com.dianru.analysis.test.DataRepair;
import com.dianru.analysis.tools.Md5;
import com.dianru.analysis.util.DrkeyUtil;
import com.dianru.analysis.util.MACUtil;
import com.dianru.analysis.util.RedisConnection;

public class HandUserScore {

	public static Logger LOG = LogManager.getLogger(HandUserScore.class);

	public static long hash(String value) {
		char chs[] = value.toCharArray();
		
		long hash = 0;
		int len = chs.length;
		for (int i = 0; i < len; i++) {
			hash = ((hash << 5) + hash - 6) + ((long) chs[i]);
		}
		return hash;
	}
	
	public static boolean DEBUG = false;
	public static void main(String[] as) throws IOException {
		
		// 2015/5/27 17:35:0
		//select adid,appid,appuserid,mac,udid,openudid,score from cpa_callback_20150527 where action=4 and create_time >= 1432719300 AND saved = 0 AND invalid = 0 AND appid not in (7329,7789,8092,8079,7970,7715)
		
		// 2015/5/28 10:40:0
		//select adid,appid,appuserid,mac,udid,openudid,score from cpa_callback_20150528 where action=4 and create_time <= 1432780800  AND saved = 0 AND invalid = 0 AND appid not in (7329,7789,8092,8079,7970,7715)
		
		MediaFilterCache.getInstance().update();
		MeidaPriceCache.getInstance().update();
		
		MediaCache.getInstance().update();
		AdsCache.getInstance().update();
		
		try {
			String sql1Path = "/tmp/callback.20150527.17.35.log";
			List<String> list1 = DataRepair.readFile(sql1Path);
			
			String sql2Path = "/tmp/callback.20150528.10.40.log";
			List<String> list2 = DataRepair.readFile(sql2Path);

	        List<String> listFinal = new ArrayList<String>();
	        listFinal.addAll(list1);
	        listFinal.addAll(list2);
	        
			for (String line : listFinal) {
				try {
					String sArr[] = line.split(",");
					
					int adid = sArr[0].isEmpty() || sArr[0].isEmpty() ? 0 : Integer.parseInt(sArr[0]);
					int appid = sArr[1].isEmpty() ? 0 : Integer.parseInt(sArr[1]);
					String appuserid = sArr[2].isEmpty() ? "" : sArr[2];
					String mac = sArr[3].isEmpty() ? "" : sArr[3];
					String udid = sArr[4].isEmpty() ? "" : sArr[4];
					String openudid = sArr[5].isEmpty() ? "" : sArr[5];
					int score = sArr[6].isEmpty() ? 0 : (int) Float.parseFloat(sArr[6]);
					
					send(appid, adid, mac, udid, appuserid, openudid, score, 0);
					int number = new Random().nextInt(3) + 1;
					Thread.sleep(number * 1000);
				} catch (Exception e) {
					LOG.warn(line + " exception : " + e.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.warn(" exception2  : " + e.getMessage());
		}
	}

	public static void send(int appid, int adid, String mac, String udid,
			String appuserid, String openudid, int score, int active_num) {
		
		Media media = MediaCache.getInstance().get(appid);
		Ads ad = AdsCache.getInstance().get(adid);
		
		if (media.getType() == 1) {
			MediaApp app = (MediaApp) media;
			sendScore(app, ad, appuserid, mac, udid, openudid, score,
					ad.getProcess_name(), active_num);
			
			LOG.info("user send score bug " + app.getUid() + " " + appid
					+ " " + appuserid + " " + mac + " " + udid + " " + openudid
					+ " " + score);
		} else {
			if (appuserid != null && !appuserid.isEmpty()) {
				String url;
				try {
					url = URLDecoder.decode(appuserid, "UTF-8");
					if (url.length() > 4) {
						String proto = url.substring(0, 4).toLowerCase();
						if (proto.equals("http")) {
							url = url.replaceAll("&amp;", "&");
							
							JSONObject obj = new JSONObject();
							obj.put("protocol", "http");
							obj.put("method", "get");
							obj.put("url", url);
							LOG.debug("channel callback bug : " + url);

							if(!DEBUG) {
								Jedis jedis = RedisConnection.getInstance("values");
								jedis.rpush("ACTION_HTTP_REQUEST", obj.toString());
								RedisConnection.close("values", jedis);
							}
						} else {
							LOG.error("error proto channel bug " + appid
									+ " callback : " + url);
						}
					} else {
						LOG.error("error proto channel bug" + appid
								+ " callback : " + url);
					}
				} catch (UnsupportedEncodingException e) {
					LOG.error("error on decode channel bug" + appid
							+ " callback : " + appuserid);
				}
			} else {
				LOG.debug("channel has no callback bug" + appid + appuserid);
			}
		}
	}

	public static void sendScore(MediaApp media,Ads ad,String appuserkey,String mac,String udid, String openudid, int score, String process, int active_num){
		
		String offerwall = null;
		switch (ad.getDataType()) {
		case 1:
			offerwall = media.getOfferwall();
			break;
		case 2:
			offerwall = media.getFreewall();
			break;
		case 3:
			offerwall = media.getInsertscreen();
			break;
		case 4:
			offerwall = media.getFullscreen();
			break;
		case 5:
			offerwall = media.getBanner();
			break;
		case 6:
			offerwall = media.getHotscreen();
			break;
		case 7:
			offerwall = media.getVideoscreen();
			break;
		default:
			break;
		}
		if(offerwall == null) {
			LOG.error("send score to user bug : " + media.getMid());
			return;
		}
		
		long hashId = 0;
		if (appuserkey == null || appuserkey.isEmpty())
	    {
			hashId = hash(media.getUid() + openudid);
	    }
	    else
	    {
	    	hashId = hash(media.getUid() + appuserkey);
	    }
		
		String hashKey = String.valueOf(hashId);
		if(!DEBUG) {
			Jedis redis = RedisConnection.getInstance("main");
			String old = redis.hget("DATA_USER_SCORE", hashKey);
			Long result;
			if(old != null) {
				float oldn = Float.parseFloat(old);
				result = redis.hset("DATA_USER_SCORE", hashKey, String.valueOf(score+oldn));
			} else {
				result = redis.hset("DATA_USER_SCORE", hashKey, String.valueOf(score));
			}
			RedisConnection.close("main",redis);
			LOG.info("send score to redis bug : "+result+" : " + media.getMid() + " " + media.getUid() + " " + appuserkey + " " + mac + " " + udid + " " + openudid + " " + score);
		}
		
		JSONObject opts;
		try {
			opts = new JSONObject(offerwall);
			int callback = opts.getInt("callback");
			if(callback == 0) {
				LOG.error("send score to callback bug : " + media.getMid());
				return;
			}
		} catch (Exception e1) {
			LOG.error("send score to callback bug : " + e1.getMessage());
			return;
		}
		
		String callbackUrl = media.getCallbackUrl();
		if(callbackUrl!=null && !callbackUrl.isEmpty()){
			String appHost = callbackUrl;
			String parames = "";
			if(callbackUrl.indexOf("?") > 0){
				appHost = callbackUrl.substring(0,callbackUrl.indexOf("?"));
				parames = callbackUrl.substring(callbackUrl.indexOf("?"),callbackUrl.length());
				parames += "&";
			}else{
				parames += "?";
			}
			//密钥
			String appsecret = media.getCallbackKey();
			String userId = appuserkey;
			String adName = ad.getAdname();
			String deviceid = MACUtil.parse(mac) == null ? udid : mac;
			String appkey = DrkeyUtil.encode(media.getUid(), media.getMid());
			String adskey = DrkeyUtil.encode(ad.getCid(), ad.getAdid());
			
			String drkey = appkey+adskey+deviceid + active_num;
			String time = String.valueOf(System.currentTimeMillis()/1000);
			String httpUrl = "";
			String backJson = "";
			try {
				long hash = hash(drkey);
				String key = parames + String.format("hashid=%s&appid=%d&adid=%d&adname=%s&userid=%s&deviceid=%s&source=dianru&point=%d&time=%s&appsecret=%s", 
						String.valueOf(hash),media.getMid(),ad.getAdid(),adName,userId,deviceid,(int)score,time,appsecret);
				
				parames += String.format("hashid=%s&appid=%d&adid=%d&adname=%s&userid=%s&deviceid=%s&source=dianru&point=%d&time=%s&checksum=%s", 
						String.valueOf(hash),media.getMid(),ad.getAdid(),adName==null?"":URLEncoder.encode(adName, "UTF-8"),userId==null?"":URLEncoder.encode(userId, "UTF-8"),deviceid,(int)score,time,Md5.crypt(key));
				httpUrl = appHost+parames;
				
				JSONObject obj = new JSONObject();
				obj.put("protocol","http");
				obj.put("method","get");
				obj.put("url", httpUrl);
				
				if(!DEBUG) {
					Jedis jedis = RedisConnection.getInstance("values");
					jedis.rpush("ACTION_HTTP_REQUEST", obj.toString());
					RedisConnection.close("values",jedis);
				}
				LOG.info("user score  bug : " + httpUrl + " " + key);
				
			} catch (Exception e) {
				LOG.error(httpUrl+"  "+backJson+" "+e);
			}
		}
	}
}
