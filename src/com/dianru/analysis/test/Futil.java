package com.dianru.analysis.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class Futil {

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
	
	/**
	 * 新建一个文件并写入内容
	 * 
	 * @param String
	 *            filePath 文件所属文件夹路径
	 * @param String
	 *            fileName 文件名
	 * @param String
	 *            fileContent 内容
	 * @param int
	 *            bufLen 设置缓冲区大小
	 * @param boolean
	 *            isWrite 是否追加写入文件
	 * @return boolean 写入文件是否成功
	 * @throws IOException
	 */
	public static boolean createFile(String filePath, String fileName,
			String fileContent, boolean isWrite) throws IOException {

		// 该文件是否存在，如不存在，则创建该文件
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		boolean flag = false; // 写入文件是否成功
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(filePath + File.separator + fileName, isWrite); // 构建文件写入流
			bw = new BufferedWriter(fw, BUFFER_1024_SIZE); // 构建写入流
			bw.write(fileContent + "\n"); // 写入文件内容
			flag = true;
		} catch (IOException e) {
			System.err.println(".createFile() is error !");
			flag = false;
			throw e;
		} finally {
			if (bw != null) {
				bw.flush();
				bw.close();
			}
			if (fw != null)
				fw.close();
		}

		return flag;
	}
}
