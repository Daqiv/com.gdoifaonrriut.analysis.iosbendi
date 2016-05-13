package com.dianru.analysis.tools;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import com.dianru.analysis.util.HttpUtil;

/**
 * @project : com.dianru.http
 * @author JunWu.zhu
 * @date: 2015年1月9日
 * @email : icerivercomeon@gmail.com
 * @qq : 369990256
 * @description :
 */
public class IdfaHttpRequest {

	public static void readTxt(String file, String createfile) throws Exception {

		FileInputStream fs = null;
		InputStreamReader isr = null;
		LineNumberReader br = null;

		int buflen = 1024;

		fs = new FileInputStream(file); 
		isr = new InputStreamReader(fs);
		br = new LineNumberReader(isr, 1024); 

		FileWriter fw = new FileWriter(createfile); 
		BufferedWriter bw = new BufferedWriter(fw, buflen); 

		String line = "";
		while ((line = br.readLine()) != null) {
			String r = http(line);
			System.out.println(r);
			bw.write(r + "\n");
		}

		br.close();
		isr.close();
		fs.close();

		bw.close();
		fw.close();

		System.out.println("....." + createfile + "..created ");
	}

	public static String http(String idfa) {
		String url = "http://www.tieyou.com/index.php?param=/lianceShike/activateStatus.html&idfa="
				+ idfa;
		String r = HttpUtil.get(url);
		return url + "," + r;
	}

	public static void main(String[] args) {
		try {
			readTxt("J:/idfa/铁友火车票12.30.txt", "J:/idfa/铁友火车票12.30.result.txt");

			readTxt("J:/idfa/铁友火车票12.31.txt", "J:/idfa/铁友火车票12.31.result.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
