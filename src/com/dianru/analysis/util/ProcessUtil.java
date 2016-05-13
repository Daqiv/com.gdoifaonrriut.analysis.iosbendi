package com.dianru.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class ProcessUtil {
	public static int getCurrentProcessId() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // 格式: "pid@hostname"
		System.out.println(name);
		try {
			return Integer.parseInt(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static int saveProcessId(File filePath) {
		try {
			int pid = getCurrentProcessId();
			
			PrintStream out = new PrintStream(new FileOutputStream(filePath));
			out.print(String.valueOf(pid));
			out.close();
			return pid;
		} catch (FileNotFoundException e) {
			return -1;
		}
	}
}
