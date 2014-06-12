package com.dbStudio.gameTest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

public final class ConfigManager {
	/**女妖测试服IP*/
	public final static String NVYAO_TEST_SERVER_IP_ADD = ;
	/**女妖测试服端口*/
	public final static int NVYAO_TEST_SERVER_PORT = ;
	
	/**七雄线上服地址*/
	public final static String SEVEN_Q_ONLINE_SERVER = ;
	/**七雄线上服端口*/
	public final static int SEVEN_Q_ONLINE_SERVER_PORT = ;
	
	private final File configFile;
	
	private Properties p;
	
	private ConfigManager(File file) {
		configFile = file;
		if(!FileUtil.isValidFile(file))
			throw new IllegalArgumentException(file.getAbsolutePath() + " not exist or is a dir");
		
		p  = new Properties();
		loadConfig();
	}
	
	public static ConfigManager getCongfigManager(File file) {
		return new ConfigManager(file);
	}
	
	private boolean isOpen = false;
	
	private final void loadConfig() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
			p.load(fis);
			isOpen = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String get(String key) {
		if(!isOpen) {
			loadConfig();
		}
		
		return p.getProperty(key, "null");
	}
	
	public void put(String key, String value) {
		if(!isOpen) {
			loadConfig();
		}
		
		p.put(key, value);
	}
	
	public void put(Map<String, String> params) {
		if(!isOpen) {
			loadConfig();
		}
		
		p.putAll(params);
	}
	
}
