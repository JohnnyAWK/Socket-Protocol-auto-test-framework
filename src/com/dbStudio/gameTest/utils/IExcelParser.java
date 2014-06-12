package com.dbStudio.gameTest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IExcelParser {
	public static final class ExcelBean {
		private String cmd;
		private List<HashMap<String, String>> argsList;
		private List<HashMap<String, String>> expectResultList;
		
		public ExcelBean() {
			cmd = "";
			argsList = new ArrayList<HashMap<String,String>>();
			expectResultList = new ArrayList<HashMap<String,String>>();
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

		public List<HashMap<String, String>> getExpectResultList() {
			return expectResultList;
		}

		public void setExpectResultList(List<HashMap<String, String>> expectList) {
			this.expectResultList = expectList;
		}
	}
	/***
	 * 获取excel数据，并转换成ExcelBean格式
	 * @param sheetName 需要获取的sheet名字
	 * @return excel数据转换成ExcelBean格式
	 */
	public ExcelBean getExcelDataBean(String sheetName);
	
	/***
	 * 根据行号获取该行的单元格值，如果单元格与单元格之间有空白单元格，会跳过这些空白单元格
	 * @param sheetName sheet
	 * @param rowIndex 行号（从1开始）
	 * @return 单元格值的字符数组
	 * @throws Exception 当rowIndex非法或者sheet不存在时，抛出这个异常
	 */
	public String[] getRowValues(String sheetName, int rowIndex) throws Exception;
	
	/***
	 * 获取Sheet所有数据，每一行保存到一个String数组里面，如果某一行单元格与单元格之间有空白单元格，会跳过这些空白单元格
	 * @param sheetName sheet的名字
	 * @return sheet的所有数据
	 * @throws Exception
	 */
	public List<String[]> getSheetDatas(String sheetName) throws Exception;
}
