package com.dianru.analysis.boot;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

public class BootServerTest {

	public static Logger LOG = LogManager.getLogger(BootServerTest.class);

	public static void push() throws IOException{
		
		Jedis jedis = RedisConnection.getInstance("queue");
		String redisKey = "ACTION_UP_REPORT";
		
//		String click = "2016-04-07 18:59:17 106.2.202.117 sdk click offer_wall \"uid=107&appid=1231&os=android&osver=4.1.2&device=&mac=24:69:a5:a7:93:5d&udid=A0000043E8E677&openudid=3BBC7339D511D72A8AF3E1D9E2D876A4&root=0&appuserid=54032&cid=17651&adid=10388&session=c51a501830cdb87171b2d09e4787656d&version=1.2.1&did=3a7a889ba81ff159195733d042992402&ssid=vfou&localip=192.168.199.195&disk=\"" ;
//		String jump = "2016-04-07 16:38:21 127.0.0.1 sdk jump offer_wall \"uid=8326&appid=7090&os=iphone&osver=7.0.4&device=iPhone&mac=02:00:00:00:00:00&udid=D9EC10A7-DBEC-4BB4-BE8D-79020459AF17&openudid=4d05c1b721321c0d536af39d3ec61cffc121e3aa&root=0&appuserid=22085480&cid=1106&adid=1187&session=2aac56382d8821768b7da7694a5e285c&version=3.3&model=iPhone-4&did=d93cf478b209ebc46a22723ed102052c\"";
//		String active = "2016-04-07 16:39:21 127.0.0.1 process active offer_wall \"adid=1187&mac=02:00:00:00:00:00&udid=D9EC10A7-DBEC-4BB4-BE8D-79020459AF17&openudid=0eb087e9b65d2e30ca1c8fc7f6cf389f5350d225&active_num=0&disk=7270387712\"";		
		String job = "2016-04-07 16:39:21 127.0.0.1 process active offer_wall \"adid=1187&mac=02:00:00:00:00:00&udid=D9EC10A7-DBEC-4BB4-BE8D-79020459AF17&openudid=0eb087e9b65d2e30ca1c8fc7f6cf389f5350d225&active_num=0&disk=7270387712\"";		
		
//		jedis.rpush(redisKey, click);
//		jedis.rpush(redisKey, jump);
//		jedis.rpush(redisKey, active);
		jedis.rpush(redisKey, job);
		RedisConnection.close("queue",jedis);
	}
	
	public static void main(String[] args) {

		try {
			push();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
