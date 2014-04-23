package com.dbStudio.gameTest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class CommonsTool {
	/**
	 * 工具方法，将Map转换为List
	 * 
	 * @param params
	 * @return
	 */
	public static List<NameValuePair> convertMap2List(Map<String, String> params) {
		if (params.isEmpty())
			throw new NullPointerException("params is empty");

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			paramList.add(new BasicNameValuePair(entry.getKey().trim(), entry
					.getValue().trim()));
		}

		return paramList;
	}

}
