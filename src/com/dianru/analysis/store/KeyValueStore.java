package com.dianru.analysis.store;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.boot.BootServer;
import com.dianru.analysis.util.FileUtils;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.SmartArrayBasedNodeFactory;

public class KeyValueStore<T> {

	private static Logger LOG = LogManager.getLogger(KeyValueStore.class);

	private final static Map<String, String> treeKey = new ConcurrentHashMap<String, String>();

	private final static String fileSuffix = ".idx";

	private final static String fileTempSuffix = "temp.idx";

	public RadixTree<T> tree = null;
	private FileStore store = null;
	private FileStore storetemp = null;
	private String type;

	public KeyValueStore(String type) {
		tree = new ConcurrentRadixTree<T>(new SmartArrayBasedNodeFactory(),
				false);
		if (type != null) {
			this.type = type;
			store = FileStore.getInstance(type + fileSuffix);
			load();
		}
	}

	@SuppressWarnings("unchecked")
	private void load() {
		List<Object> kvs = store.read();
		if (kvs == null)
			return;
		for (int i = 0; i < kvs.size(); i += 2) {
			Object ok = kvs.get(i);
			CharSequence key = null;
			if (ok != null && (ok instanceof CharSequence)) {
				key = (CharSequence) ok;
				treeKey.put(String.valueOf(key), "");
				if (i + 1 >= kvs.size()) {
					System.out.println("Index file error : " + store.name);
					break;
				}
				T val = (T) kvs.get(i + 1);
				if (key != null && val != null) {
					tree.put(key, val);
				}
			}
		}
		cpmpactFile();
	}

	/**
	 * 将重复的值针对文件缓存重新写入
	 */
	private void cpmpactFile() {

		String tempFile = FileStore.workdir + "/" + type + fileTempSuffix;
		String file = FileStore.workdir + "/" + type + fileSuffix;
		if (new File(tempFile).exists()) {
			FileUtils.deleteFile(tempFile);
		}
		storetemp = FileStore.getInstance(type + fileTempSuffix);
		for (String set : treeKey.keySet()) {
			storetemp.write(set, tree.getValueForExactKey(set));
		}
		//将文件持有对象关闭掉才可继续进行创建删除和易名的操作
		store.close();
		storetemp.close();
		FileUtils.deleteFile(file);
		FileUtils.renameFile(FileStore.workdir, type + fileTempSuffix, type
				+ fileSuffix);
		store = FileStore.getInstance(type + fileSuffix);
	}

	public T put(CharSequence key, T value) {

		// 插入新值会更更新旧的值
		T o = tree.put(key, value);

		// 新值和旧值一样的情况下不需要写入文件
		// 达到的效果能减少一部分相同的数据存入
		if (o != null) {
			if (o.hashCode() == value.hashCode()) {
				if (o.equals(value)) {
					LOG.debug("the value is same key:" + String.valueOf(key) + "|o:" + String.valueOf(o) + "|value:" + String.valueOf(value));
					return o;
				}
			}
		}
	
		// 新值和旧值不一样可以写入文件
		if (store != null)
			store.write(key, value);

		return o;
	}

	public T get(CharSequence key) {
		return tree.getValueForExactKey(key);
	}

	public boolean remove(CharSequence key) {
		return tree.remove(key);
	}

	public static void testWithTree() {
		// 会直接覆盖新值
		KeyValueStore<String> testKeyValueStore = new KeyValueStore<String>(
				"CHANNEL_ACTIVED_NUM");
		long startl = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			String key = String.format("%d-%d-%d", i, i - 1 > 0 ? i - 1 : 0,
					i - 2 > 0 ? i - 2 : 0);
			 String value = String.valueOf(i+(i-1>0?i-1:0)+(i-2>0?i-2:0));
//			String value = String.valueOf(3 * i);
			String oldvalue = testKeyValueStore.put(key, value);

		}
		long startl1 = System.currentTimeMillis();
		String value = testKeyValueStore.get("100-99-98");
		long endl = System.currentTimeMillis();
		LOG.info("read cost:" + String.valueOf((endl - startl1)) + "|put cost:"
				+ String.valueOf((startl1 - startl)) + "|value:" + value);
	}


	public static void main(String[] args) {
		testWithTree();
	}
}
