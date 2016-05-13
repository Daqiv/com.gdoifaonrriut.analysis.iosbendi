package com.dianru.analysis.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

public class ScoreRule {
	
	public static Logger LOG = LogManager.getLogger(ScoreRule.class);
	public static Map<Integer,String> PROCESS = new ConcurrentHashMap<Integer, String>();
	
	//阶梯
	static int STEP = 3;
	
	//进程数量，排除安装数量
	static int process_num = 30;
	static int process_rate = 40;
	
	//留存日期
	static int remain_num = 30;
	static int remain_rate = 40;
	
	//开机启动时间
	static int boot_num = 7;
	static int boot_rate = 20;
	
	//ID 评分，默认全给
	static int ids_num = 1;
	static int ids_rate = 0;
	
	static class Process {
		
		private int num;
		private float pscore;
		
		public Process() {
			super();
		}

		public Process(int num, float pscore) {
			super();
			
			this.num = num;
			this.pscore = pscore;
		}

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public float getPscore() {
			return pscore;
		}

		public void setPscore(float pscore) {
			this.pscore = pscore;
		}
	}
	
	//计算得分
	public static void count(String mac,String udid,int time, int uptime){
		
		if(time <=0 || uptime <= 0 || mac.length() == 0 || udid.length() == 0) {
			return;
		}
		
		Jedis jedis = null;
		try {
			jedis = RedisConnection.getInstance("rule");
			String idfa = udid.replace("-", "");
			idfa = idfa.toLowerCase();
			
			//总评分
			float score = 0f;
			
			//进程数
			Process p = processFun(mac, udid);
			float pScore = p.getPscore();
			int pNum = p.getNum();
			
			String redisText = jedis.hget("RULE_UDID_SCORE", idfa);
			if(redisText != null && redisText.length() > 0){
				String []tmp = redisText.split(",");
				try {
					float prevScore = Float.parseFloat(tmp[1]);
					int prevNum = Integer.parseInt(tmp[2]);
					
					//更新
					if(pNum - prevNum <= STEP) {
						pScore = prevScore;
						pNum = prevNum;
					}
				} catch (Exception e) { }
			}
			
			//留存数
			float rScore = remainFun(udid);
			
			//开机启动时间		
			float bScore = bootFun(time, uptime);
			
			//ID评分
			float idScore = (float)ids_rate/(float)100;
			
			score = pScore  + rScore+ bScore + idScore;
			String scoreText = String.format("%.2f,%.2f,%d", score, pScore, pNum);
			
			long result = jedis.hset("RULE_UDID_SCORE", idfa, String.valueOf(scoreText));
			LOG.debug(String.format("score rule udid:%s process:%.2f remain:%.2f boot: %.2f ids: %.2f score:%s  result:%d", udid, pScore, rScore, bScore, idScore, scoreText, result));
		} catch (Exception e) {
			LOG.error("score rule error : " + e.getMessage());
			e.printStackTrace();
		}finally{
			if(jedis != null) {
				RedisConnection.close("rule", jedis);
			}
		}
	}
	
	public static Process processFun(String mac, String udid){
		
		Process p = new Process();
		float pScore = 0;
		Jedis jedis = null;
		Map<String, String> hm = null;
		try {
			String key = String.format("%s%s", mac.replace(":", ""), udid.replace("-", ""));
			jedis = RedisConnection.getInstance("softs");
			hm =  jedis.hgetAll(key);
			if(hm != null && hm.size() > 0){
				int hmBefore = hm.size();
				
				int tmpContain = 0;
				for (String k : hm.keySet()) {
					if(PROCESS.containsValue(k.trim())){
						tmpContain ++;
					}
				}
				int size = hmBefore - tmpContain;
				if(size >= process_num){
					pScore = (float)process_rate / (float)100;
				}else {
					float a = (float)size/(float)process_num;
					float b =  (float)process_rate / (float)100;
					pScore =  (float)a*(float)b;
				}
				p.setNum(size);
			}else {
				pScore = (float)process_rate / (float)100;
			}
		} catch (Exception e) {
			LOG.error("score process " + udid + " error : " + e.getMessage());
			pScore = (float)process_rate / (float)100;
		}finally{
			if(jedis != null) {
				RedisConnection.close("softs", jedis);
			}
		}
		p.setPscore(pScore);
		return p;
	}
	
	public static float remainFun(String udid){
		
		float rScore = 0;
		Jedis jedis = null;
		try {
			udid = udid.replace("-", "");
			udid = udid.toLowerCase();
			jedis = RedisConnection.getInstance("remain");
			String count = jedis.hget("DMP_UDID_COUNT", udid);
			if(count == null || count.length() == 0) {
			}else {
				int num = Integer.parseInt(count);
				if(num >= remain_num) {
					rScore = (float)remain_rate / (float)100;
				}else {
					float a = (float)num/(float)remain_num;
					float b =  (float)remain_rate / (float)100;
					rScore =  (float)a*(float)b;
				}
			}
		} catch (Exception e) {
			LOG.error("score remain error : " + e.getMessage());
			rScore = (float)remain_rate / (float)100;
		}finally{
			if(jedis != null) {
				RedisConnection.close("remain", jedis);
			}
		}
		return rScore;
	}
	
	public static float bootFun(int time, int uptime){
		
		float bootScore = 0;
		int diff = (int) Math.ceil((time - uptime)/(3600*24));
		if(diff >= boot_num){
			bootScore = (float)boot_rate / (float)100;
		}else {
			if(diff <= 0) {
				bootScore = 0;
			}else {
				float a = (float)diff/(float)boot_num;
				float b =  (float)boot_rate / (float)100;
				bootScore =  (float)a*(float)b;
			}
		}
		return bootScore;
	}
	
	public static void main(String[] args) {
		
		int diff = (int) Math.ceil((1453736662 - 1453645314)/(3600*24));
		System.out.println(diff);
	}
}