package com.dianru.analysis.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Porject lando
 * @author JunWu.zhu
 * @date:Apr 17, 2014 3:16:47 PM
 * @version : 1.0
 * @email : icerivercomeon@gmail.com
 * @desciption : 文件工具
 */
public class FileUtils {
	/**
	 * 默认缓冲区大小
	 */
	public static final int BUFFER_1024_SIZE = 1024;

	private static Logger LOG = LogManager.getLogger(FileUtils.class);

	/**
	 * 读取文件内容:行形式读取
	 * 
	 * @param String
	 *            filePathAndName 完整绝对路径文件名
	 * @return String 返回文本文件的内容
	 */
	public static String readFile(String filePathAndName) throws IOException {
		return readFile(filePathAndName, "utf-8", null, BUFFER_1024_SIZE);
	}

	/**
	 * 读取文件内容
	 * 
	 * @param String
	 *            filePathAndName 完整绝对路径文件名
	 * @param String
	 *            encoding 文件打开编码方式 例如 GBK,UTF-8
	 * @param String
	 *            sep 返回内容分隔符 例如：#，默认为\n;
	 * @param int bufLen 缓冲区大小
	 * @return String 返回文件内容
	 */
	public static String readFile(String absPath, String encoding, String sep,
			int bufLen) throws IOException {

		// 文件路径是否为空 , 是否存在该文件
		if (null == absPath || absPath.trim().equals("")) {
			return "";
		}
		File file = new File(absPath);

		if (!file.exists()) {
			return "";
		}

		// 内容分隔符
		if (sep == null || sep.equals("")) {
			sep = "\n"; // 默认以行分隔
		}

		StringBuffer rtnFileContent = new StringBuffer(); // 返回内容对象
		FileInputStream fs = null;
		InputStreamReader isr = null;
		LineNumberReader br = null;
		try {
			fs = new FileInputStream(file); // 构造文件流
			if (encoding == null || encoding.trim().equals("")) { // 文件流编码
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding.trim());
			}
			br = new LineNumberReader(isr, bufLen); // 读取文件

			String data = "";
			while ((data = br.readLine()) != null) {
				rtnFileContent.append(data).append(sep); // 拼接
			}
		} catch (IOException e) {
			System.err
					.print(".readFile(String filePathAndName,String encoding, String sep, int bufLen) read file content error !");
			throw e;
		} finally {
			try {
				if (null != br) {
					br.close();
				}
				if (null != isr) {
					isr.close();
				}
				if (null != fs) {
					fs.close();
				}
			} catch (IOException e) {
				System.err
						.print(".readFile(String filePathAndName,String encoding, String sep, int bufLen) Close file stream error !");
				throw e;
			}
		}
		return rtnFileContent.toString();
	}

	/**
	 * 新建一个文件并写入内容
	 * 
	 * @param String
	 *            filePath 文件所属文件夹路径
	 * @param String
	 *            fileName 文件名
	 * @param String
	 *            fileContent 内容
	 * @return boolean 写入文件是否success
	 * @throws IOException
	 */
	public static boolean createFile(String filePath, String fileName,
			String fileContent) throws IOException {
		return createFile(filePath, fileName, fileContent, BUFFER_1024_SIZE,
				false);
	}

	/**
	 * 新建一个文件并写入内容
	 * 
	 * @param String
	 *            filePath 文件所属文件夹路径
	 * @param String
	 *            fileName 文件名
	 * @param String
	 *            fileContent 内容
	 * @param int bufLen 设置缓冲区大小
	 * @param boolean isWrite 是否追加写入文件
	 * @return boolean 写入文件是否success
	 * @throws IOException
	 */
	public static boolean createFile(String filePath, String fileName,
			String fileContent, int bufLen, boolean isWrite) throws IOException {

		// 该文件是否存在，如doesn't exist，则创建该文件
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		boolean flag = false; // 写入文件是否success
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(filePath + File.separator + fileName, isWrite); // 构建文件写入流
			bw = new BufferedWriter(fw, bufLen); // 构建写入流
			bw.write(fileContent); // 写入文件内容
			flag = true;
		} catch (IOException e) {
			System.err.println(".createFile() is error !");
			flag = false;
			throw e;
		} finally {
			if (bw != null) {
				bw.flush();
				bw.close();
			}
			if (fw != null)
				fw.close();
		}

		return flag;
	}

	public static void createLockFile(String fileName, String content)
			throws IOException {

		// 该文件是否存在，如doesn't exist，则创建该文件
		File file = new File(fileName);

		File dir = file.getParentFile();

		if (!dir.exists())
			dir.mkdirs();

		if (!file.exists()) {
			file.createNewFile();
		}

		// 对该文件加锁
		FileOutputStream out = null;
		FileChannel fcout = null;
		FileLock flout = null;
		try {
			out = new FileOutputStream(file);
			fcout = out.getChannel();
			flout = fcout.tryLock();

			out.write(content.getBytes("utf-8"));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (flout != null) {
				flout.release();
			}
			if (fcout != null) {
				fcout.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 将内容写入文件
	 * 
	 * @param filePath
	 *            : 文件路径
	 * @param fileContent
	 *            : 文件内容
	 * @return
	 * @throws Exception
	 */
	public static void writeFile(String filePath, String fileContent)
			throws Exception {
		// 源文件和新文件路径不可为空
		FileOutputStream fos = new FileOutputStream(filePath); // 构造文件输出流
		if (null != fos) {
			fos.write(fileContent.getBytes());
			fos.flush();
			fos.close();
		}
	}

	/***
	 * 追加文件内容
	 * */
	public static void appendFile(String fileName, String content)
			throws IOException {
		FileWriter writer = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(fileName, true);
			writer.write(content);
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
	}

	/**
	 * delete file ，可以是文件或文件夹
	 * 
	 * @param fileName
	 *            要删除的文件名
	 * @return 删除success返回true，否则返回false
	 */
	public static boolean delete(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			LOG.debug("delete file fail :" + fileName + " doesn't exist!");
			return false;
		} else {
			if (file.isFile())
				return deleteFile(fileName);
			else
				return deleteDirectory(fileName);
		}
	}

	/**
	 * delete single file
	 * 
	 * @param fileName
	 *            要删除的文件的文件名
	 * @return 单个文件删除success返回true，否则返回false
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				LOG.debug("delete single file " + fileName + " success!");
				return true;
			} else {
				LOG.debug("delete single file " + fileName + " fail !");
				return false;
			}
		} else {
			LOG.debug("delete single file fail ：" + fileName
					+ " doesn't exist!");
			return false;
		}
	}

	/**
	 * delete directory 及目录下的文件
	 * 
	 * @param dir
	 *            要删除的目录的文件路径
	 * @return 目录删除success返回true，否则返回false
	 */
	public static boolean deleteDirectory(String dir) {
		// 如果dir不以文件分隔符结尾，自动添加文件分隔符
		if (!dir.endsWith(File.separator))
			dir = dir + File.separator;
		File dirFile = new File(dir);
		// 如果dir对应的文件doesn't exist，或者不是一个目录，则退出
		if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
			LOG.debug("delete directory fail ：" + dir + " doesn't exist!");
			return false;
		}
		boolean flag = true;
		// delete file 夹中的所有文件包括子目录
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = FileUtils.deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
			// 删除子目录
			else if (files[i].isDirectory()) {
				flag = FileUtils.deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag) {
			LOG.debug("delete directory fail !");
			return false;
		}
		// 删除当前目录
		if (dirFile.delete()) {
			LOG.debug("delete directory " + dir + " success!");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 文件重命名
	 * 
	 * @param path
	 *            文件目录
	 * @param oldname
	 *            原来的文件名
	 * @param newname
	 *            新文件名
	 */
	public static void renameFile(String path, String oldname, String newname) {
		
		if(StringUtils.isNull(path)||StringUtils.isNull(oldname)||StringUtils.isNull(newname)){
			LOG.error("path:" + path + " or oldname:" + oldname + " or newname:" + newname + " is null!");
			return;
		}
		
		if (!path.endsWith(File.separator))
			path = path + File.separator;
		
		File fileDir = new File(path);
		if(!fileDir.isDirectory()){
			LOG.error(path + " is not directory!");
			return;
		}
		
		if (!oldname.equals(newname)) {// 新的文件名和以前文件名不同时,才有必要进行重命名
			File oldfile = new File(path + oldname);
			File newfile = new File(path + newname);
			if (!oldfile.exists()) {
				LOG.error(path +  oldname + " is not exist(old file)!");
				return;
			}
			if (newfile.exists())// 若在该目录下已经有一个文件和新文件名相同，则不允许重命名
				LOG.error(path + newname + " is exist(new file)!");
			else {
				oldfile.renameTo(newfile);
				LOG.debug(path + oldname + " rename "+ path + oldfile +" success!");
			}
		} else {
			LOG.error("two file name is same!");
		}
	}

	public static void main(String[] args) {
//		try {
//			createLockFile("/Users/goforit/Documents/data/test.txt",
//					"tewqfeafaf3vew");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		delete("/Users/goforit/Documents/data");
		renameFile("/Users/goforit/Documents/data", "test3.txt", "test1.txt");
	}
}
