package com.dianru.analysis.process;

import java.util.HashMap;
import java.util.Map;

import com.dianru.analysis.process.imps.ActiveProcessor;
import com.dianru.analysis.process.imps.ClickProcessor;
import com.dianru.analysis.process.imps.JobProcessor;
import com.dianru.analysis.process.imps.JumpProcessor;
import com.dianru.analysis.process.imps.ShowProcessor;

public class Processors {
	private final static Map<String,Processor> MAP = new HashMap<String,Processor>();
	private static String[] NAMES;
	
	private static ShowProcessor SHOW_PROCESSOR = new ShowProcessor();
	private static ClickProcessor CLICK_PROCESSOR = new ClickProcessor();
	private static JumpProcessor JUMP_PROCESSOR = new JumpProcessor();
	private static ActiveProcessor ACTIVE_PROCESSOR = new ActiveProcessor();
	private static JobProcessor JOB_PROCESSOR = new JobProcessor();
	
	static {
		MAP.put("show", SHOW_PROCESSOR);
		MAP.put("click", CLICK_PROCESSOR);
		MAP.put("jump", JUMP_PROCESSOR);
		MAP.put("active", ACTIVE_PROCESSOR);
		MAP.put("job", JOB_PROCESSOR);
		
		NAMES = MAP.keySet().toArray(new String[MAP.size()]);
	}
	
	public final static  String[] names() {
		return NAMES;
	}
	
	public final static Processor get(String name) {
		return MAP.get(name);
	}
}
