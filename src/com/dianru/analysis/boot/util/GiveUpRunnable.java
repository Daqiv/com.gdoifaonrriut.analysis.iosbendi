package com.dianru.analysis.boot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.process.util.RemainActiveUtil;

public class GiveUpRunnable implements Runnable {

	public static Logger LOG = LogManager.getLogger(GiveUpRunnable.class);

	public static void exec(String line) {
		
		//放弃暂时不加量级，只有超时加量
		RemainActiveUtil.giveUp(line);
	}
	
	private String line;
	
	public GiveUpRunnable(String line) {
		this.line = line;
	}

	@Override
	public void run() {
		exec(this.line);
	}
}
