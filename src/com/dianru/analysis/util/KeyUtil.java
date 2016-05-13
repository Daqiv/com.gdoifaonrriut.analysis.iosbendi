package com.dianru.analysis.util;

import java.nio.ByteBuffer;
import java.util.List;

import com.dianru.analysis.store.bean.ByteArraySequence;

public class KeyUtil {
	
	public static CharSequence parse(String val, int id) {
		if(val == null) return null;
		
		char[] chs = val.toCharArray();
		char ch;
		byte n = 0;
		int c = 0;
		int last = chs.length - 1;
		byte[] vs = new byte[chs.length+8];
		int vi = 0;

		for (int i = 0; i < chs.length; i++) {
			ch = chs[i];
			if (ch >= '0' && ch <= '9') {
				n = (byte) ((n << 4) | (ch - '0'));
				c++;
			} else if (ch >= 'A' && ch <= 'F') {
				n = (byte) ((n << 4) | (ch - 'A' + 10));
				c++;
			} else if (ch >= 'a' && ch <= 'f') {
				n = (byte) ((n << 4) | (ch - 'a' + 10));
				c++;
			}

			if (c == 2 || i == last) {
				vs[vi++] = n;
				n = 0;
				c = 0;
			}
		}
		if(vi == 0) return null;
		
		int len = vi-1;
		byte[] ks = int2bytes(id);
		for(int i=0;i<ks.length;i++) vs[len+i] = ks[i];

		return new ByteArraySequence(vs, 0, len + ks.length);
	}

	private static byte[] int2bytes(int id) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (id & 0xff);
		bytes[1] = (byte) ((id >> 8) & 0xff);
		bytes[2] = (byte) ((id >> 16) & 0xff);
		bytes[3] = (byte) ((id >> 24) & 0xff);
		return bytes;
	}

	public static CharSequence parse(String val) {
		if(val == null) return null;
		
		char[] chs = val.toCharArray();
		char ch;
		byte n = 0;
		int c = 0;
		int last = chs.length - 1;
		byte[] vs = new byte[chs.length];
		int vi = 0;

		for (int i = 0; i < chs.length; i++) {
			ch = chs[i];
			if (ch >= '0' && ch <= '9') {
				n = (byte) ((n << 4) | (ch - '0'));
				c++;
			} else if (ch >= 'A' && ch <= 'F') {
				n = (byte) ((n << 4) | (ch - 'A' + 10));
				c++;
			} else if (ch >= 'a' && ch <= 'f') {
				n = (byte) ((n << 4) | (ch - 'a' + 10));
				c++;
			}

			if (c == 2 || i == last) {
				vs[vi++] = n;
				n = 0;
				c = 0;
			}
		}
		if(vi == 0) return null;
		
		return new ByteArraySequence(vs, 0, vi - 1);
	}

	public static CharSequence make(int size, Object... vals) {

		ByteBuffer buf = ByteBuffer.allocate(size);

		for (Object val : vals) {
			if (val instanceof Byte) {
				buf.put((byte) val);
			} else if (val instanceof Short) {
				buf.putShort((short) val);
			} else if (val instanceof Integer) {
				buf.putInt((int) val);
			} else if (val instanceof Long) {
				buf.putLong((long) val);
			} else if (val instanceof Float) {
				buf.putFloat((float) val);
			} else if (val instanceof String) {
				String str = (String) val;
				byte[] bytes = str.getBytes();
				buf.putInt(bytes.length);
				buf.put(bytes);
			} else if (val instanceof CharSequence) {
				CharSequence cs = (CharSequence) val;
				int len = cs.length();
				buf.putInt(len);
				for (int i = 0; i < len; i++) {
					buf.put((byte) cs.charAt(i));
				}
				break;
			} else {
				buf.put((byte) 0);
			}
		}
		
		return new ByteArraySequence(buf.array(), 0, buf.position());
	}

	public static CharSequence make(List<Object> values, int[] keys) {
		Object[] vals = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			vals[i] = values.get(keys[i]);
		}
		return KeyUtil.make(keys.length * 16, vals);
	}
	
	public static String path(String name, int id) {
		return name + "/" + id;
	}
	
	public static void main(String[] args) {
		int adid = 10388;
		String udid = "A0000043E8E677";
		System.out.println(parse(udid,adid));
		System.out.println(int2bytes(adid));
	}
}
