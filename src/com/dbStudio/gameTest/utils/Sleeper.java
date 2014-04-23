package com.dbStudio.gameTest.utils;

import java.util.concurrent.TimeUnit;

public class Sleeper {
	/***
	 * sleep指定的秒
	 * @param seconds
	 */
	public static void sleepSomeSeconds(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * sleep指定的分钟
	 * @param minutes
	 */
	public static void sleepSomeMinutes(int minutes) {
		try {
			TimeUnit.MINUTES.sleep(minutes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * sleep指定毫秒数
	 * @param milliseconds
	 */
	public static void sleepSomeMilliseconds(long milliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	
