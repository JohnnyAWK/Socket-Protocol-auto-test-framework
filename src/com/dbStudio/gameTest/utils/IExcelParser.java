package com.dbStudio.gameTest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IExcelParser {
	public static final class ExcelBean {
		private String cmd;
		private List<HashMap<String, String>> argsList;
		private List<HashMap<String, String>> expectList;
		
		public ExcelBean() {
			cmd = "";
			argsList = new ArrayList<HashMap<String,String>>();
			expectList = new ArrayList<HashMap<String,String>>();
		}

		public String getCmd() {
			return cmd;
		}

		public void setCmd(String cmd) {
			this.cmd = cmd;
		}

		public List<HashMap<String, String>> getArgsList() {
			return argsList;
		}

		public void setArgsList(List<HashMap<String, String>> argsList) {
			this.argsList = argsList;
		}

		public List<HashMap<String, String>> getExpectList() {
			return expectList;
		}

		public void setExpectList(List<HashMap<String, String>> expectList) {
			this.expectList = expectList;
		}
	}
	/***
	 * 获取excel数据，并转换成ExcelBean格式
	 * @param sheetName 需要获取的sheet名字
	 * @return excel数据转换成ExcelBean格式
	 */
	public ExcelBean getExcelDataBean(String sheetName);
}
