package com.dianru.analysis.tools;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.io.FileLineReader;
import com.dianru.analysis.parse.Parser;
import com.dianru.analysis.parse.Parsers;
import com.dianru.analysis.process.Processor;
import com.dianru.analysis.process.Processors;
import com.dianru.analysis.count.Counter;
import com.dianru.analysis.util.Configuration;

public class FileParocess {
	
	private static int STATUS_COUNT = 0;
	private static int STATUS_PARSE = 0;
	private static int STATUS_PROCESS = 0;
	private static long STATUS_PARSE_TIME = 0;
	private static long STATUS_PROCESS_TIME = 0;

	public static Logger LOG = LogManager.getLogger(FileParocess.class);
	
	public boolean test(FileLineReader flr) {

		String[] lines = flr.getLines(1000);
		if (lines == null || lines.length == 0) {
			LOG.info("work dir is empty sleep");
			return false;
		}

		Parser parser = Parsers.get(flr.fileExt);
		Processor processor = Processors.get(flr.fileExt);
		
		long begin, end;
		for (String line : lines) {
			if (STATUS_COUNT % 1000 == 0)
				System.out.println(STATUS_COUNT);

			STATUS_COUNT++;

			begin = System.currentTimeMillis();
			List<Object> vals = parser.parse(line);
			end = System.currentTimeMillis();
			STATUS_PARSE_TIME += (end - begin);

			if (vals != null)
				STATUS_PARSE++;
			else
				continue;

			begin = System.currentTimeMillis();
			processor.process(vals);
			STATUS_PROCESS++;

			end = System.currentTimeMillis();

			STATUS_PROCESS_TIME += (end - begin);
		}
		return true;
	}

	public static void main(String[] args) {

		FileLineReader flrs[] = new FileLineReader[5];

		String dir = Configuration.getInstance().getProperty("path.input.dir", "/tmp/workdir/data");
		flrs[0] = new FileLineReader(dir, new String[] { "show" });
		flrs[1] = new FileLineReader(dir, new String[] { "click" });
		flrs[2] = new FileLineReader(dir, new String[] { "jump" });
		flrs[3] = new FileLineReader(dir, new String[] { "active" });
		flrs[4] = new FileLineReader(dir, new String[] { "job" });

		FileParocess count = new FileParocess();
		for (int i=0;i<flrs.length;i++) {
			FileLineReader flr = flrs[i];
			while (count.test(flr))
				;
			System.out
					.printf("status : count=%d parse=(%d cost %d ms) process=(%d cost %d ms)\n",
							STATUS_COUNT, STATUS_PARSE, STATUS_PARSE_TIME,
							STATUS_PROCESS, STATUS_PROCESS_TIME);
		}

		try {
			Counter.getInstance().switchStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
