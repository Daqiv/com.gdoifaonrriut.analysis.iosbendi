package com.dianru.analysis.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.dianru.analysis.bean.DataType;
import com.googlecode.concurrenttrees.radix.node.concrete.bytearray.ByteArrayCharSequence;

public class BufferUtil {
	
	public static byte obj2type(Object val) {
		if(val instanceof Byte) {
			return DataType.BYTE;
		}
		else if(val instanceof Short) {
			return DataType.SHORT;
		}
		else if(val instanceof Integer) {
			return DataType.INT;
		}
		else if(val instanceof Long) {
			return DataType.LONG;
		}
		else if(val instanceof Float) {
			return DataType.FLOAT;
		}
		else if(val instanceof String) {
			return DataType.STRING;
		}
		else if(val instanceof CharSequence) {
			return DataType.BYTES;
		}
		return DataType.NULL;
	}
	
	public static void put(ByteBuffer buf, Object obj) {
		byte type = obj2type(obj);
		buf.put(type);
		switch(type) {
		case DataType.BYTE:
			buf.put((byte)obj);
			break;
		case DataType.SHORT:
			buf.putShort((short)obj);
			break;
		case DataType.INT:
			buf.putInt((int)obj);
			break;
		case DataType.LONG:
			buf.putLong((long)obj);
			break;
		case DataType.FLOAT:
			buf.putFloat((float)obj);
			break;
		case DataType.STRING:
		{
			String val = (String)obj;
			byte[] bytes = val.getBytes();
			buf.putInt(bytes.length);
			buf.put(bytes);
			break;
		}
		case DataType.BYTES:
		{
			CharSequence val = (CharSequence)obj;
			int size = val.length();
			buf.putInt(size);
			for(int i=0;i<size;i++) {
				buf.put((byte)val.charAt(i));
			}
			break;
		}
		default:
			break;
		}
	}
	
	public static Object get(ByteBuffer buf) {
		byte type = buf.get();
		switch(type) {
		case DataType.BYTE:
			return buf.get();
		case DataType.SHORT:
			return buf.getShort();
		case DataType.INT:
			return buf.getInt();
		case DataType.LONG:
			return buf.getLong();
		case DataType.FLOAT:
			return buf.getFloat();
		case DataType.STRING:
		{
			int size = buf.getInt();
			byte[] bytes = new byte[size];
			buf.get(bytes);
			return new String(bytes);
		}
		case DataType.BYTES:
		{
			int size = buf.getInt();
			byte[] bytes = new byte[size];
			buf.get(bytes);
			return new ByteArrayCharSequence(bytes,0,size);
		}
		default:
			return null;
		}
	}
	
	public static ByteBuffer list2buf(List<Object> vals, ByteBuffer buf) {
		buf.putInt(vals.size());
		for(int i=0;i<vals.size();i++) {
			put(buf, vals.get(i));
		}
		buf.flip();
		return buf;
	}
	
	public static List<Object> buf2list(ByteBuffer buf) {
		buf.flip();
		int size = buf.getInt();
		if(size > 40960) return null;
		
		List<Object> vals = new ArrayList<Object>(size);
		for(int i=0;i<size;i++) {
			Object obj = get(buf);
			vals.add(obj);
		}
		return vals;
	}
}
