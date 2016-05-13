package com.dianru.analysis.bean;

import java.util.Arrays;

public class CharArray implements CharSequence {
	public char[] chars;
	
	public CharArray(char[] chars) {
		this.chars = chars;
	}
	
	public CharArray(char[] chars, int offset, int end) {
		this.chars = Arrays.copyOfRange(chars, offset, end);
	}
	
	@Override
	public int length() {
		return this.chars.length;
	}

	@Override
	public char charAt(int index) {
		return chars[index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if(end > this.chars.length || start > this.chars.length || start < 0) return null;
		return new CharArray(chars, start, end);
	}
}
