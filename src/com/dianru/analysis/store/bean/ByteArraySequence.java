package com.dianru.analysis.store.bean;

import com.googlecode.concurrenttrees.radix.node.concrete.bytearray.ByteArrayCharSequence;

public class ByteArraySequence extends ByteArrayCharSequence {

	public ByteArraySequence(byte[] bytes, int start, int end) {
		super(bytes, start, end);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<this.length();i++) {
			int b = (int) this.charAt(i);
			sb.append(Integer.toHexString(b));
		}
		return sb.toString();
	}
}
