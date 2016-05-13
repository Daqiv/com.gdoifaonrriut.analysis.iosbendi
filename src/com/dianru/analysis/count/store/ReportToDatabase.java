package com.dianru.analysis.count.store;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.Define;
import com.dianru.analysis.boot.util.TimeCache;
import com.dianru.analysis.count.bean.AdsDayKeys;
import com.dianru.analysis.count.bean.AdsHourKeys;
import com.dianru.analysis.count.bean.CountKeys;
import com.dianru.analysis.count.bean.CountValues;
import com.dianru.analysis.count.bean.DetailDayKeys;
import com.dianru.analysis.count.bean.DetailHourKeys;
import com.dianru.analysis.count.bean.MediaDayKeys;
import com.dianru.analysis.count.bean.MediaHourKeys;
import com.dianru.analysis.count.bean.SumDayKeys;
import com.dianru.analysis.count.bean.SumHourKeys;
import com.dianru.analysis.store.SimpleFileWriter;
import com.dianru.analysis.util.SQLConnection;

public class ReportToDatabase {
	
	public static Logger LOG = LogManager.getLogger(ReportToDatabase.class);
	
	private static ReportToDatabase store = new ReportToDatabase();
	
	public static ReportToDatabase getInstance() {
		return store;
	}

	public void saveError(String dbname, String sql) {
		SimpleFileWriter store = SimpleFileWriter.getInstance("sqlerrors/"+TimeCache.date+"/"+dbname+TimeCache.hour+".sql");
		try {
			store.write(sql +";\n");
		} catch (IOException e) {
		}
	}
	
	public void saveSQL(String dbname, String sql) {
		SimpleFileWriter store = SimpleFileWriter.getInstance("sqls/"+TimeCache.date+"/"+dbname+TimeCache.hour+".sql");
		try {
			store.write(sql +";\n");
		} catch (IOException e) {
		}
	}
	
	public void save(int type, Map<CountKeys,CountValues> map) {
		
		String dbname = Define.getDataSourceName(type);
		
		CountKeys keys = null;
		SQLConnection conn = SQLConnection.getInstance(dbname);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
			String sql = CountSQL.create(entry.getKey(), entry.getValue());
			
			saveSQL(dbname, sql);
			int result = conn.update(sql, null);
			if(result <= 0) {
				saveError(dbname, sql);
			}
			LOG.debug("report to database result : "+result+" sql : " + sql);

			if(keys == null) keys = entry.getKey();
		}
		conn.close();
		
		if(keys instanceof DetailHourKeys) {
			reportAdsHour(type, map);
			reportMediaHour(type, map);
			reportDetailDay(type, map);
			reportSumHour(type, map);
		} else if(keys instanceof AdsHourKeys) {
			reportAdsDay(type, map);
		} else if(keys instanceof MediaHourKeys) {
			reportMediaDay(type, map);
		} else if(keys instanceof SumHourKeys) {
			reportSumDay(type, map);
		}
	}

	private void reportSumDay(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			SumHourKeys key = (SumHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(SumDayKeys.create(key), value);
		}
		store.save();
	}
	
	private void reportSumHour(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			DetailHourKeys key = (DetailHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(SumHourKeys.create(key), value);
		}
		store.save();
	}

	private void reportDetailDay(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			DetailHourKeys key = (DetailHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(DetailDayKeys.create(key), value);
		}
		store.save();
	}

	private void reportMediaDay(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			MediaHourKeys key = (MediaHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(MediaDayKeys.create(key), value);
		}
		store.save();
	}

	private void reportAdsDay(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			AdsHourKeys key = (AdsHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(AdsDayKeys.create(key), value);
		}
		store.save();
	}

	private void reportMediaHour(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			DetailHourKeys key = (DetailHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(MediaHourKeys.create(key), value);
		}
		store.save();
	}

	private void reportAdsHour(int type, Map<CountKeys, CountValues> map) {
		CountStore store = new CountStore(type);
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			DetailHourKeys key = (DetailHourKeys)entry.getKey();
			CountValues value = entry.getValue().clone();
			
			store.put(AdsHourKeys.create(key), value);
		}
		store.save();
	}

	//广告预算控制
	/*
	private void updateAdsToday(int type, Map<CountKeys, CountValues> map) {
		
		Map<AdsRunStateKey,AdsRunState> states = new HashMap<AdsRunStateKey,AdsRunState>();
		
		for(Iterator<Entry<CountKeys,CountValues>> it = map.entrySet().iterator();it.hasNext();) {
			Entry<CountKeys,CountValues> entry = it.next();
		
			AdsDayKeys key = (AdsDayKeys)entry.getKey();
			CountValues value = entry.getValue();
			
			AdsRunStateKey nk = new AdsRunStateKey(key.created, key.adid);
			
			AdsRunState state = states.get(nk);
			if(state == null) {
				if(value.active_income > 0 || value.active_count > 0) {
					state = new AdsRunState(value.active_count, value.active_income+value.job_income);
				}
				if(state != null && (state.getNum() > 0 || state.getMoney() > 0)) states.put(nk, state);
			} else {
				if(value.active_count > 0 || value.active_income > 0) {
					state.add(value.active_count, value.active_income);
				}
			}
		}

		Jedis jedis = RedisConnection.getInstance("main");
		for(Iterator<Entry<AdsRunStateKey, AdsRunState>> it = states.entrySet().iterator();it.hasNext();) {
			Entry<AdsRunStateKey, AdsRunState> entry = it.next();
			AdsRunStateKey key = entry.getKey();

			Ads ad = AdsCache.getInstance().get(key.getAdid());
			if(ad.getState() == 4 && (ad.getNum() > 0 || ad.getMoney() > 0)) {
				
				AdsRunState state = entry.getValue();
				
				String rkey = "DATA_ADS_"+key.getCreated();
				String stateString = jedis.hget(rkey, String.valueOf(key.getAdid()));
				if(stateString == null) {
					jedis.hset(rkey, String.valueOf(key.getAdid()), state.toString());
				} else {
					AdsRunState old = new AdsRunState(stateString);
					old.add(state.getNum(), state.getMoney());
					jedis.hset(rkey, String.valueOf(key.getAdid()), old.toString());
					state = old;
				}
				
				int num = state.getNum();
				float money = state.getMoney();
				
				if((ad.getNum() > 0 && num >= ad.getNum()) || (ad.getMoney() > 0 && money >= ad.getMoney())) {
					DailyClean.setAdsStateToPause(key.getAdid());
				}
			}
		}
		RedisConnection.close("main", jedis);
	}
	*/
}
