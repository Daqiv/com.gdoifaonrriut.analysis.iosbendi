package com.dianru.analysis.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import com.dianru.analysis.util.RedisConnection;

/**
 * 数据修复
 * @author liuhuiya
 *
 */
public class DataRepair {
	
	public static int BUFFER_1024_SIZE = 1024;
	public static List<String> readFile(String absPath) throws IOException {
		
		String encoding = "utf-8";
		
		List<String> list = new ArrayList<String>();
		// 文件路径是否为空 , 是否存在该文件
		if (null == absPath || absPath.trim().equals("")) {
			return list;
		}
		File file = new File(absPath);
		if (!file.exists()) {
			return list;
		}
		
		FileInputStream fs = null;
		InputStreamReader isr = null;
		LineNumberReader br = null;
		try {
			fs = new FileInputStream(file); // 构造文件流
			if (encoding == null || encoding.trim().equals("")) { // 文件流编码
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding.trim());
			}
			br = new LineNumberReader(isr, BUFFER_1024_SIZE); // 读取文件

			String data = "";
			while ((data = br.readLine()) != null) {
				list.add(data);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (null != br) {
					br.close();
				}
				if (null != isr) {
					isr.close();
				}
				if (null != fs) {
					fs.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return list;
	}
	
	public static void push() throws IOException{
		
		Jedis jedis = RedisConnection.getInstance("queue");
		String absPath = "/Users/liuhuiya/Documents/data/active3.log";
		String redisKey = "ACTION_UP_REPORT";
		List<String> list = readFile(absPath);
		for (String str : list) {
			//String line  = "2015-04-04 21:36:30 180.153.132.38 sdk active offer_wall \"adid=2037&mac=&udid=74D7F966-F3E6-46B0-816B-A17AB83E9242&openudid=\"";
			System.out.println(str);
			jedis.rpush(redisKey, str);
		}
		RedisConnection.close("queue",jedis);
	}
	
	public static void main(String[] args) throws IOException {
		push();
	}
}
