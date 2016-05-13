package com.dianru.analysis.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.util.BufferUtil;
import com.dianru.analysis.util.Configuration;

public class FileStore {

	public final static String STORE_STORE = "store";
	public final static String STORE_TEMP = "temp";

	public static Logger LOG = LogManager.getLogger(FileStore.class);
	
	public static String workdir = Configuration.getInstance().getProperty(
			"path.workdir", "/tmp/workdir");

	protected RandomAccessFile stream;
	protected String name;
	private FileChannel in;
	private FileChannel out;
	
	//改成私有构造
	private FileStore(String name) {
		this.name = name;

		File file = new File(workdir + "/" + name);
		File dir = file.getParentFile();

		if (!dir.exists())
			dir.mkdirs();

		try {
			stream = new RandomAccessFile(file, "rw");
			 in = stream.getChannel();
			 out = stream.getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public long write(String text) {
		byte[] bytes = text.getBytes();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		return write(buf);
	}

	public long write(byte[] bytes) {
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		return write(buf);
	}
	
	public long write(Object... objs) {
		//重新分配字节缓冲区
		ByteBuffer buf = ByteBuffer.allocateDirect(4096);
		for (int i = 0; i < objs.length; i++) {
			Object obj = objs[i];
			//将对象放入缓冲区
			BufferUtil.put(buf, obj);
		}
		//在一系列通道读取或放置 操作之后，调用此方法为一系列通道写入或相对获取 操作做好准备
		buf.flip();
		return write(buf);
	}

	public synchronized long write(ByteBuffer buf) {
		try {
			//设置次通道的位置
			out.position(stream.length());
			long position = out.position();
			//channel.lock(position, buf.limit(), false);
			out.write(buf);
			
			return position;
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("write file " + this.name + " error" + e);
			return -1;
		}
	}

	public synchronized int read(ByteBuffer buf) {
		try {
			int size = in.read(buf);
			return size;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<Object> read() {

		try {
			//获取文件长度的大小
			long size = stream.length();
			/**
			 * 这儿是不是一下读的太多了
			 */
			//分配和字节长度大小一样的自己自己缓冲区
			ByteBuffer buf = ByteBuffer.allocateDirect((int) size);
			
			//读取到内存中
			int len = in.read(buf);
			//读取的字节数
			if(len == 0) {
				return null;
			}
			
			buf.flip();

			List<Object> objs = new ArrayList<Object>();
			for (int i = 0; i < size; i++) {
				//如果读取的位置超过buf的限制长度跳出
				if (buf.position() >= buf.limit())
					break;

				try {
					//按照数字或者字符串争取读出
					Object obj = BufferUtil.get(buf);
					objs.add(obj);
				} catch(Exception e) {
					System.out.println("Read file error : " + name);
					break;
				}
			}
			return objs;
			
		} catch (IOException e) {
		}

		return null;
	}

	public void close() {
		try {
			stream.close();
			in.close();
			out.close();
		} catch (IOException e) {
		}
		STORES.remove(name);
	}

	public static Map<String, FileStore> STORES = new ConcurrentHashMap<String, FileStore>();

	public static FileStore getInstance(String name) {
		FileStore store = STORES.get(name);
		if (store == null) {
			store = new FileStore(name);
			STORES.put(name, store);
		}
		return store;
	}

	public static void closeAll() {
		for (FileStore store : STORES.values()) {
			store.close();
		}
	}
	
	/**
	 * 哈哈，我是天才在读取内容的时候进行重新复写，不得不佩服自己的智慧了
	 * 启动线程来执行
	 */
	
}
