package com.dianru.analysis.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.dianru.analysis.util.Configuration;

public class ObjectFileStore {
	
	public static String WORKDIR = Configuration.getInstance().getProperty("path.workdir", "/tmp/workdir/store");

	public static Object read(String name) {

		File file = new File(WORKDIR+'/'+name+".obj");
		if(!file.exists() || !file.isFile() || !file.canRead()) return null;
		
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void write(String name, Object obj) {
		File file = new File(WORKDIR+'/'+name+".obj");
		if(file.exists()) file.delete();
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(obj);
			out.close();
		} catch (Exception e) {
		}
	}
}
