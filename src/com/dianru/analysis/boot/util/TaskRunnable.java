package com.dianru.analysis.boot.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.parse.Parser;
import com.dianru.analysis.parse.Parsers;
import com.dianru.analysis.process.Processor;
import com.dianru.analysis.process.Processors;

public class TaskRunnable implements Runnable {

	public static Logger LOG = LogManager.getLogger(TaskRunnable.class);

	public static void exec(String line) {
		
		String name = Parsers.parseType(line);

		Parser parser = Parsers.get(name);

		Processor processor = Processors.get(name);
		if (parser == null || processor == null) {
			LOG.trace("parser or process not found : " + name);
			return;
		}
		
		LOG.trace("parse line : " + line);
		List<Object> vals = parser.parse(line);
		if (vals != null) {
			LOG.trace("process line : " + line);
			processor.process(vals);
			LOG.trace("process done");
		} else {
			LOG.trace("parse log item error : " + line);
		}
	}
	
	private String line;
	
	public TaskRunnable(String line) {
		this.line = line;
	}

	@Override
	public void run() {
		exec(this.line);
	}
}
