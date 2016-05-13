package com.dianru.analysis.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class FileLineReader {
	
	public final static String CONF_WORK_DIR_KEY = "com.dianru.analysis.input.dir";
    private FilenameFilter extFilter;
    private File workdir;
    private File tempFile;
    private BufferedReader reader;
    
    public String fileExt;

	public FileLineReader(String dir, final String[] exts) {
		workdir = new File(dir);
		if(!workdir.exists() || !workdir.isDirectory()) {
			workdir = null;
			return;
		}
		
		extFilter = new FilenameFilter() {
	    	@Override
	    	 public boolean accept(File dir, String name)
	    	 {
	    		int idx = name.lastIndexOf('.');
	    		//这个写错了吧，应该是判断文件的存在.而且.后面还得有东西直接等于length就可以了
	    		if(idx < 0 || idx == name.length()+1) return false;
	    		
	    		String fext = name.substring(idx+1);
	    		
	    		for(String ext : exts) {
	    			if(ext.equals(fext)) {
	    				fileExt = ext;
	    				return name.endsWith("."+ext);
	    			}
	    		}
	    		return false;
	    	 }
	    };
	}
	
	/**
	 * 通过扫描返回BufferedReader
	 * @return
	 */
    protected synchronized BufferedReader scan() {

    	File[] fs = this.workdir.listFiles(extFilter);
    	if(fs == null) return null;
    	
    	for(File file : fs) {
    		if(file.isFile() && file.canRead()) {
    			tempFile = new File(file.getAbsolutePath()+".lock");
    			file.renameTo(tempFile);
    			try {
    				System.out.println(file);
					return new BufferedReader(new FileReader(tempFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
    		}
    	}
    	return null;
    }
    
    /**
     * 把文件按照行的数组形式返回
     * @param max
     * @return
     */
    public synchronized String[] getLines(int max) {
    	if(reader == null) {
    		reader = scan();
    		if(reader == null) return null;
    	}

    	try {
    		boolean done = true;
        	String line;
        	ArrayList<String> lines = new ArrayList<String>();
        	
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty()) continue;
				
				lines.add(line);
				if(lines.size() == max) {
					done = false;
					break;
				}
			}
			if(done ) {
				reader.close();
				//tempFile.delete();
				reader = null;
				tempFile = null;
			}
			if(lines.isEmpty()) return getLines(max);
			
			String[] strs = lines.toArray(new String[lines.size()]);
			return strs;
		} catch (IOException e) {
			return null;
		}
		
    }
    public static void main(String[] args) {
		System.out.println("123.".indexOf('.'));
	}
}
