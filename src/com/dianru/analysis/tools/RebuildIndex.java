package com.dianru.analysis.tools;

import java.io.File;
import java.util.List;

import com.dianru.analysis.store.FileStore;

public class RebuildIndex {
	public static void main(String[] args) {
		
		if(args.length != 2) {
			System.out.println("com.dianru.analysis.tools.RebuildIndex : <src> <des>");
			return;
		}
		
		File f1 = new File(FileStore.workdir+'/'+args[0]);
		File f2 = new File(FileStore.workdir+'/'+args[1]);
		if(!f1.exists()) {
			System.out.println("com.dianru.analysis.tools.RebuildIndex : src file not exists");
			return;
		}
		
		if(f2.exists()) {
			System.out.println("com.dianru.analysis.tools.RebuildIndex : dst file exists");
			return;
		}
		//TODO 这儿为什么脱离了管理器创建了两个未知的东西
//		FileStore src = new FileStore(args[0]);
//		FileStore des = new FileStore(args[1]);
		
//		List<Object> items = null;
//		try {
//			items = src.read();
//		} catch(Exception e) {
//			
//		} finally {
//			if(items != null) {
//				int size = items.size();
//				if(size % 2 != 0) items.remove(size-1);
//				for(Object item : items) {
//					des.write(item);
//				}
//			}
//		}
//		src.close();
//		des.close();
	}
}
