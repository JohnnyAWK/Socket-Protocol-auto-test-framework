package com.dbStudio.gameTest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
	/***
	 * 验证是否是一个合法的文件
	 * @param file
	 * @return
	 */
	public static boolean isValidFile(String file) {

		return isValidFile(new File(file));
	}
	
	/***
	 * 验证是否是一个合法的文件
	 * @param file
	 * @return
	 */
	public static boolean isValidFile(File file) {
		return file.exists()? file.isFile() : false;
	}
	
	/***
	 * 写文件
	 * @param file 文件名
	 * @param data 写的数据，如果是复合对象，最好覆盖对象的toString方法
	 * @param append 是否追加写
	 */
	public static <T> void writeFile(String file, T data, boolean append){
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(file, append);
			
			writer.write(data.toString());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * 获取文件后缀名
	 * @param fileFullName 文件名
	 * @return 成功，返回文件后缀名， 失败返回null
	 */
	public static String getFileExt(String fileFullName){
		int dot = fileFullName.lastIndexOf(".");
		
		if((dot > -1) && (dot < fileFullName.length() -1)) {
			return fileFullName.substring(dot + 1);
		}
		else {
			return null;
		}
	}
	
	/***
	 * 拷贝文件
	 * @param source
	 * @param dest
	 */
	public static void copyFile(File source, File dest){
		if(!isValidFile(source)) return;
		
		FileInputStream in = null;
		FileOutputStream out = null;
		
		byte[] buffer = new byte[1024 * 64]; //64K
		int bytesRead = -1;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			
			while ( (bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
