package com.dianru.analysis.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.bean.Ads;
import com.dianru.analysis.bean.Define;
import com.dianru.analysis.bean.ScoreRule;
import com.dianru.analysis.process.util.SmoothControlUtil;
import com.dianru.analysis.tools.MinuteAdsPlan;
import com.dianru.analysis.util.RedisConnection;
import com.dianru.analysis.util.SQLConnection;

public class AdsCache {
	
	public static Logger LOG = LogManager.getLogger(AdsCache.class);
	
	public final static int CLEAR_TIME = 3600 * 24 * 7;
	private static AdsCache INSTANCE = new AdsCache();
	private static int current = 0;
	public static class AdsSorted {
		public String debug = "";
		public String release = "";
		public String pause = "";
		public String autostop = "";
	}
	
	public static AdsCache getInstance() {
		return INSTANCE;
	}
	
	private Map<Integer,Ads> map = new ConcurrentHashMap<Integer, Ads>();
	private static long update = 0;
	public AdsCache() {
		update();
	}
	
	public void save(OutputStream stream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(stream);
		
		out.writeLong(update);
		out.writeInt(map.size());
		for(Iterator<Map.Entry<Integer,Ads>> it = map.entrySet().iterator();it.hasNext();) {
			Map.Entry<Integer,Ads> entry = it.next();
			out.writeInt(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.close();
	}
	
	public void load(InputStream stream) throws IOException, ClassNotFoundException {
		/*
		ObjectInputStream in = new ObjectInputStream(stream);
		
		long update = in.readLong();
		int size = in.readInt();

		for(int i=0;i<size;i++) {
			int key = in.readInt();
			Ads value = (Ads)in.readObject();
			map.put(key, value);
			
			if(!value.getBundleid().isEmpty()){
				ScoreRule.PROCESS.put(value.getAdid(), value.getBundleid());
			}
		}
		in.close();
		AdsCache.update = update;
		*/
		this.update();
	}
	
	public void update() {
		
		SQLConnection conn = null;
		Jedis jedis = null;
		try {
			conn = SQLConnection.getInstance("main");
			jedis = RedisConnection.getInstance("main");
			String sql = "SELECT `type` as data_type,space_type,ads.adid as adid,cid,ctype,cstype,clevel,sort,state,devices,begin,end,control,ad_plan.num installnum,ruleouts,citys,hours,root,options,price_click_income,price_click_cost,price_callback_income,price_callback_cost,price_job_income,price_job_cost,data_from,ruleins,adname,os,osver,budget,money,ads.num as ipnum,`interval`,boot_time_num,num_ad,interval_ad,boot_time_num_ad,rate,billing,levelouts,appstoreid,bundleid,is_talkingdata,keywords,is_aso,aso_pos,channel_num,fwz_levelouts,click_num,process_name,update_time,is_hs_flag,is_hs_report,jobs,is_hand_stop,remain,deliveryType FROM ads,ad_plan WHERE ads.adid=ad_plan.adid AND update_time>?";
			//LOG.debug("select ads sql time : " + update + " : " + current);
			List<Map<String,Object>> items = conn.queryMap(sql, new Object[] { update });
			if (!items.isEmpty()) {
				for (Map<String,Object> vals : items) {
					Ads ads = null;
					try {
						ads = new Ads(vals);
					} catch (Exception e) {
						LOG.debug("exception ads item new");
						continue;
					}
					jedis.hset("DATA_ADS_TABLE",String.valueOf(ads.getCid()), String.valueOf(ads.getAdid()));
					jedis.hset("DATA_ADS_TYPE",String.valueOf(ads.getAdid()), String.valueOf(ads.getDataType()));
					
					jedis.hset("DATA_ADID_PROCESS", String.valueOf(ads.getAdid()), ads.getProcess_name());
					jedis.hset("DATA_PROCESS_BUNDLE", ads.getProcess_name(), ads.getBundleid());
					jedis.hset("DATA_ADID_BUNDLE", String.valueOf(ads.getAdid()), ads.getBundleid());
					
					//先清除换过广告类型的广告
					for(int i = 1; i < Define.TYPES.length;i++){
						if(i == ads.getDataType()){
							continue;
						}
						String adsStr = jedis.hget("DATA_ADS_TYPE_"+i, String.valueOf(ads.getAdid()));
						if(adsStr != null && !adsStr.isEmpty()){
							jedis.hdel("DATA_ADS_TYPE_"+i, String.valueOf(ads.getAdid()));
						}
					}
					
					//清理变化过配置的广告，主要是从平滑到快速的切换清理
					SmoothControlUtil.delSmoothAd(ads.getAdid());
					
					//重新核算剩余量
					MinuteAdsPlan.pauseAdByCount(ads.getAdid());
					
					//广告上线，是否手动停止：0 默认，1手动
					if(ads.getState() == 4 || ads.getState() == 7) {
						LOG.debug("online ads " + ads.getAdid() + ", state " + ads.getState());
						jedis.hset("DATA_ADS_TYPE_"+ads.getDataType(), String.valueOf(ads.getAdid()), ads.toString());
					}else if(ads.getDataType() ==1 && (ads.getState() == 8 || ads.getState() == 9 || (ads.getState() == 5 && ads.getIs_hand_stop() == 0))){
						//判断下线时间
						if(ads.getEnd() + CLEAR_TIME > current && ads.getUpdateTime() + CLEAR_TIME > current){
							LOG.debug("online ads remain" + ads.getAdid() + ", state " + ads.getState());
							jedis.hset("DATA_ADS_TYPE_"+ads.getDataType(), String.valueOf(ads.getAdid()), ads.toString());
						}else {
							jedis.hdel("DATA_ADS_TYPE_"+ads.getDataType(), String.valueOf(ads.getAdid()));
							LOG.debug("delelte ads remain" + ads.getAdid());
						}
					}else {
						jedis.hdel("DATA_ADS_TYPE_"+ads.getDataType(), String.valueOf(ads.getAdid()));
						LOG.debug("delelte ads " + ads.getAdid());
					}
					
					map.put(ads.getAdid(), ads);
					if(!ads.getBundleid().isEmpty()){
						ScoreRule.PROCESS.put(ads.getAdid(), ads.getBundleid());
					}
				}
			}
			
			current = (int) (System.currentTimeMillis() / 1000);
			update = current - 180;
			jedis.set("DATA_ADS_UPDATE", String.valueOf(update));
			sort();
		} catch (Exception e) {
			LOG.error("ads cache error : " + e.getMessage());
		}finally{
			if(jedis != null) RedisConnection.close("main",jedis);
			if(conn != null) conn.close();
		}
	}
	
	public void sort() {
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("main");
			LinkedList<Ads> list = new LinkedList<Ads>();		
			for(Iterator<Ads> it=map.values().iterator();it.hasNext();) {
				try {
					Ads ads = it.next();
					if(ads.getState() == 4 || ads.getState() == 7) {
						list.add(ads);
					}else if(ads.getState() == 8 || ads.getState() == 9 || (ads.getState() == 5 && ads.getIs_hand_stop() == 0)){
						if(ads.getEnd() + CLEAR_TIME > current && ads.getUpdateTime() + CLEAR_TIME > current){
							list.add(ads);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			Comparator<Ads> comparator = new Comparator<Ads>() {
				@Override
				public int compare(Ads o1, Ads o2) {
					
						if(o1.getDataType() < o2.getDataType()) return -1;
						else if(o1.getDataType() > o2.getDataType()) return 1;
		
						if(o1.getState() < o2.getState()) return -1;
						else if(o1.getState() > o2.getState()) return 1;
						
						if(o1.getSort() > o2.getSort()) return -1;
						else if(o1.getSort() < o2.getSort()) return 1;
						
						if(o1.getBilling().indexOf('1') >= 0) {
							if(o1.getPriceClickCost()*(float)o1.getRate() < o2.getPriceClickCost()*(float)o2.getRate()) return 1;
							else if(o1.getPriceClickCost()*(float)o1.getRate() > o2.getPriceClickCost()*(float)o2.getRate()) return -1;
							else return 0;
						} else if(o1.getBilling().indexOf('2') >= 0) {
							if(o1.getPriceCallbackCost()*(float)o1.getRate() < o2.getPriceCallbackCost()*(float)o2.getRate()) return 1;
							else if(o1.getPriceCallbackCost()*(float)o1.getRate() > o2.getPriceCallbackCost()*(float)o2.getRate()) return -1;
							else return 0;
						} else if(o1.getBilling().indexOf('3') >= 0) {
							if(o1.getPriceJobCost()*(float)o1.getRate() < o2.getPriceJobCost()*(float)o2.getRate()) return 1;
							else if(o1.getPriceJobCost()*(float)o1.getRate() > o2.getPriceJobCost()*(float)o2.getRate()) return -1;
							else return 0;
						}
						if(o1.getRate() > o2.getRate()) return 1;
						else if(o1.getRate() > o2.getRate()) return -1;
						else return 0;
				}
			};

			Collections.sort(list, comparator);
			
			Map<Integer,AdsSorted> sorts = new HashMap<Integer,AdsSorted>();
			for(int i=1;i<Define.TYPES.length;i++) {
				sorts.put(i, new AdsSorted());
			}
			for(Iterator<Ads> it=list.iterator();it.hasNext();) {
				try {
					Ads ad = it.next();
					AdsSorted sort = sorts.get(ad.getDataType());
					if(ad.getState() == 4) {	//启动
						if(sort.release.isEmpty()) sort.release = String.valueOf(ad.getAdid());
						else sort.release += ","+String.valueOf(ad.getAdid());
						
						if(sort.debug.isEmpty()) sort.debug = String.valueOf(ad.getAdid());
						else sort.debug += ","+String.valueOf(ad.getAdid());
					}else if(ad.getState() == 7) {	//测试
						if(sort.debug.isEmpty()) sort.debug = String.valueOf(ad.getAdid());
						else sort.debug += ","+String.valueOf(ad.getAdid());
					}else if(ad.getState() == 8 || ad.getState() == 9){	//暂停 和 留存
						if(sort.pause.isEmpty()) sort.pause = String.valueOf(ad.getAdid());
						else sort.pause += ","+String.valueOf(ad.getAdid());
					}else if(ad.getState() == 5){	//自动停止
						if(sort.autostop.isEmpty()) sort.autostop = String.valueOf(ad.getAdid());
						else sort.autostop += ","+String.valueOf(ad.getAdid());
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			
			for(int i=1;i<Define.TYPES.length;i++) {
				try {
					AdsSorted sort = sorts.get(i);
					JSONObject obj = new JSONObject();
					obj.put("debug", sort.debug);
					obj.put("release", sort.release);
					obj.put("pause", sort.pause);
					obj.put("autostop", sort.autostop);
					jedis.hset("DATA_ADS_TYPE_"+i, "sorts", obj.toString());
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null) RedisConnection.close("main", jedis);
		}
	}
	
	public Ads get(int mid) {
		return map.get(mid);
	}
	
	public static void main(String[] args) {
		AdsCache.getInstance();
	}
}
