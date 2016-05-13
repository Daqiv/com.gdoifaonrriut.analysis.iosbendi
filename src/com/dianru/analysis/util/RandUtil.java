package com.dianru.analysis.util;

import java.util.Random;

public class RandUtil {
	
	public final static Random RANDOM = new Random();
	
	public static boolean isRand(int n) {
		
		if(RANDOM.nextInt(100) < n) {
			return true;
		}
		return false;
	}
}
