package com.dbStudio.gameTest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * <br>通常excel的格式为:</br>
 * <br>cmd 1000</br>
 * <br>arg</br>
 * <br>参数名1    参数名2....N</br>
 * <br>第一组实参</br>
 * <br>第二组实参</br>
 * 	...第N组实参
 * <br>expect result</br>
 * <br>（第一组预期结果）预期结果1....预期结果N</br>
 * 	（第N组预期结果）预期结果1....预期结果N
 * 
 * <p></p>
 * 
 * <br>每组预期结果的格式:</br>
 * 	返回字段名=字段值
 * 
 * */

public class DefaultExcelParser {
	
	/**excel 07*/
	public static final String EXCEL_2007 = "excel 2007";
	
	/**excel 03*/
	public static final String EXCEL_2003 = "excel 2003";
	
	/**CMD所在的行号*/
	private static final int CMD_ROW_NUMBER = 0;
	/**CMD所在行的单元格号*/
	private static final int CMD_CELL_NUMBER = 0;
	
	/**入参值的起始行号,注意行号从0开始*/
	private static final int ARGS_VALUE_START_ROW_INDEX = 3;
	
	/**cmd标识*/
	private static final String CMD = "cmd";
	
	/**expect result标识*/
	private static final String EXPECT_RESULT = "expect result";
	
	private DefaultExcelParser() {}
	
	/***
	 * 创建excel解析器
	 * @param excelFileName 	excel文件名字
	 * @param excelVersion		excel版本，excel03还是07，通常传入"DefaultExcelParser.EXCEL_07"或
	 * 	"DefaultExcelParser.EXCEL_03"
	 * @return 根据传递的excel版本，返回excel03或者excel07解析器
	 * @throws IllegalArgumentException 当excel文件不存在或指定的excel版本不正确时，抛出这个异常
	 */
	public static IExcelParser createExcelParser(String excelFileName, String excelVersion) {
		//先校验文件是否合法
		if(!FileUtil.isValidFile(excelFileName))
			throw new IllegalArgumentException(excelFileName + " is not a valid excel file or not exist");
		
		if(EXCEL_2007.equals(excelVersion))
			return new Excel07Parser(excelFileName);
		
		if(EXCEL_2003.equals(excelVersion))
			return new Excel03Parser(excelFileName);
		
		throw new IllegalArgumentException("excelType is not valid");
	}
	
	/***
	 * excel 2007解析器
	 * @author 杨如耀
	 *
	 */
	private final static class Excel07Parser implements IExcelParser{
		private final File f;
		private Workbook wb;
		
		Excel07Parser(String file) {
			f = new File(file);
			wb = openWorkbook(f);
		}
		
		@Override
		public ExcelBean getExcelDataBean(String sheetName) {
			try {
				return getData(sheetName);
			} catch (Exception e) {				
				e.printStackTrace();
				return null;
			}
		}
		
		/***
		 * 获取excel数据
		 * @param sheetName sheet
		 * @return excel数据转成ExcelBean
		 * @throws Exception  如果数据的格式未按照预定格式，则抛出该异常
		 */
		private final ExcelBean getData(String sheetName) throws Exception {
			ExcelBean bean = new ExcelBean();
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(sheetName);
			
			//获取cmd
			String cmd = getCmd(sheet.getRow(0));
			bean.setCmd(cmd);
			
			//获取"expect result"字段的所在行位置，具体的期望结果值，在下一个位置
			int expectResultIndex = getExpectResultStartIndex(sheet);
			
			//获取arg
			bean.setArgsList(getArgs(sheet, expectResultIndex));
			
			//获取期望结果
			bean.setExpectList(getExpectValue(sheet, expectResultIndex + 1));
			
			return bean;
		}
		
		/***
		 * 获取sheet中"except result"字段的起始行号
		 * @param sheet
		 * @return 如果解析正确，返回"except result"字段的起始行号；否则返回0
		 */
		private final int getExpectResultStartIndex(XSSFSheet sheet) {
			//遍历每一行，取每一行的第一个单元格，判断单元格内容
			for (int i = 0, j = sheet.getLastRowNum(); i < j; i++) {
				XSSFRow row = sheet.getRow(i);
				if(getCellValueAndConvertToString(row.getCell(0)).equals(EXPECT_RESULT))
					return row.getRowNum();
			}
			
			return 0;
		}
		
		/***
		 * 获取cmd
		 * @param row cmd所在的行，一般是第一行（程序中，行号从0开始，所以cmd所在行为0行）
		 * @return 行号正确，返回cmd值，否则返回字符串0
		 */
		private final String getCmd(Row row) {
			if (row.getRowNum() != CMD_ROW_NUMBER)
				return "0";
			
			Cell cmd = row.getCell(CMD_CELL_NUMBER);
			
			if(getCellValueAndConvertToString(cmd).equals(CMD)) {
				return getCellValueAndConvertToString(row.getCell(1));
			}
			else {
				return "0";
			}
		}
		
		/***
		 * 获取入参名
		 * @param row 参数名所在行
		 * @return  参数名构成的数组
		 */
		private final String[] getArgsName(Row row) {
			
			String[] args = new String[row.getPhysicalNumberOfCells()];
			for(int i=0, j=row.getLastCellNum(); i<j; i++){
				args[i] = getCellValueAndConvertToString(row.getCell(i));
			}
				
			return args;
		}
		
		/***
		 * 获取参数值
		 * @param sheet
		 * @param index "expect result"字段起始位置，根据这个值，就可以确定实参的组数
		 * @return 参数名和参数值组成的List， List中每个Map表示一组参数
		 * @throws Exception
		 */
		private final List<HashMap<String, String>> getArgs(XSSFSheet sheet, int index) throws Exception {
			List<HashMap<String, String>> argsList = new ArrayList<>();
			
			//获取参数名
			String[] argName = getArgsName(sheet.getRow(2));
			
			if(argName == null)
				throw new Exception("get args name error");
			
			for (int rowIndex = ARGS_VALUE_START_ROW_INDEX; rowIndex < index; rowIndex++) {
				XSSFRow row = sheet.getRow(rowIndex);
				
				HashMap<String, String> oneArgGroup = new HashMap<>();
				for(int cellIndex = 0, cellCount = row.getLastCellNum(); cellIndex<cellCount; cellIndex++){
					//参数名的位置肯定和实参一一对应
					oneArgGroup.put(argName[cellIndex], 
							getCellValueAndConvertToString(row.getCell(cellIndex)));
				}
				
				argsList.add(oneArgGroup);
			}
			
			return argsList;
		}
		
		/***
		 * 获取期望值
		 * @param sheet sheet
		 * @param expectResultIndex "expect result"字段起始位置
		 * @return 预期结果组成的List， List中每个Map表示一组预期结果
		 */
		private final List<HashMap<String, String>> getExpectValue(XSSFSheet sheet, 
																		int expectResultIndex) {
			List<HashMap<String, String>> expectResultList = new ArrayList<>();
			
			for(int startIndex = expectResultIndex, rowCount = sheet.getLastRowNum(); 
					startIndex <= rowCount; startIndex++) {
				XSSFRow row = sheet.getRow(startIndex);
				
				HashMap<String, String> expectResult = new HashMap<>();
				for(int i=0, j=row.getLastCellNum(); i<j; i++) {
					String value = getCellValueAndConvertToString(row.getCell(i));
					String[] temp = value.split("=");
					expectResult.put(temp[0], temp[1]);
				}
				
				expectResultList.add(expectResult);
			}
			
			return expectResultList;
		}
		
		/**
		 * 获取单元格的值并转换成String类型
		 * @param cell 单元格
		 * @return 返回单元格的String类型值，否则返回""
		 */
		private final String getCellValueAndConvertToString(Cell cell) {
			switch (cell.getCellType()) {
			
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			
			case Cell.CELL_TYPE_NUMERIC:
				return String.valueOf((int)cell.getNumericCellValue());
				
			default:
				return "";
			}
		}
		
		/***
		 * 打开工作薄
		 * @param f
		 * @return 返回工作薄的实例
		 */
		private final Workbook openWorkbook(File f) {
			try {
				return new XSSFWorkbook(new FileInputStream(f));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
	/***
	 * excel03解析器与07解析器一样，区别在于一些类只能解析07，具体的类对应关系如下:
	 * <br>excel07				&nbsp;&nbsp;&nbsp;			excel03</br>
	 * 
	 *<br>XSSFWorkbook			&nbsp;&nbsp;&nbsp;			HSSFWorkbook</br>
	 *<br>XSSFSheet				&nbsp;&nbsp;&nbsp;			HSSFSheet</br>
	 *<br>XSSFRow				&nbsp;&nbsp;&nbsp;			HSSFRow</br>
	 *<br>XSSFCell				&nbsp;&nbsp;&nbsp;			HSSFCell</br>
	 * 
	 * 
	 * @author 杨如耀
	 *
	 */
	private final static class Excel03Parser implements IExcelParser{
		
		private final File f;
		private Workbook wb;
		
		Excel03Parser(String file) {
			f = new File(file);
			wb = openWorkbook(f);
		}
		
		@Override
		public ExcelBean getExcelDataBean(String sheetName) {
			try {
				return getData(sheetName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		private final ExcelBean getData(String sheetName) throws Exception {
			ExcelBean bean = new ExcelBean();
			HSSFSheet sheet = (HSSFSheet) wb.getSheet(sheetName);
			
			String cmd = getCmd(sheet.getRow(0));
			bean.setCmd(cmd);
			
			int expectResultIndex = getExpectResultStartIndex(sheet);
			
			bean.setArgsList(getArgs(sheet, expectResultIndex));
			
			bean.setExpectList(getExpectValue(sheet, expectResultIndex + 1));
			
			return bean;
		}
		
		private final int getExpectResultStartIndex(HSSFSheet sheet) {
			
			for (int i = 0, j = sheet.getLastRowNum(); i < j; i++) {
				HSSFRow row = sheet.getRow(i);
				if(getCellValueAndConvertToString(row.getCell(0)).equals(EXPECT_RESULT))
					return row.getRowNum();
			}
			
			return 0;
		}
		
		private final String getCmd(Row row) {
			if (row.getRowNum() != CMD_ROW_NUMBER)
				return "0";
			
			Cell cmd = row.getCell(CMD_CELL_NUMBER);
			
			if(getCellValueAndConvertToString(cmd).equals(CMD)) {
				return getCellValueAndConvertToString(row.getCell(1));
			}
			else {
				return "0";
			}
		}
		
		private final String[] getArgsName(Row row) {

			String[] args = new String[row.getPhysicalNumberOfCells()];
			for(int i=0, j=row.getLastCellNum(); i<j; i++){
				args[i] = getCellValueAndConvertToString(row.getCell(i));
			}
				
			return args;
		}
		
		private final List<HashMap<String, String>> getArgs(HSSFSheet sheet, int index) throws Exception {
			List<HashMap<String, String>> argsList = new ArrayList<>();
			
			String[] argName = getArgsName(sheet.getRow(2));
			
			if(argName == null)
				throw new Exception("get args name error");
			
			for (int rowIndex = ARGS_VALUE_START_ROW_INDEX; rowIndex < index; rowIndex++) {
				HSSFRow row = sheet.getRow(rowIndex);
				
				HashMap<String, String> oneArgGroup = new HashMap<>();
				for(int cellIndex = 0, cellCount = row.getLastCellNum(); cellIndex<cellCount; cellIndex++){
					oneArgGroup.put(argName[cellIndex], 
							getCellValueAndConvertToString(row.getCell(cellIndex)));
				}
				
				argsList.add(oneArgGroup);
			}
			
			return argsList;
		}
		
		private final List<HashMap<String, String>> getExpectValue(HSSFSheet sheet, 
																		int expectResultIndex) {
			List<HashMap<String, String>> expectResultList = new ArrayList<>();
			
			for(int startIndex = expectResultIndex, rowCount = sheet.getLastRowNum(); 
					startIndex <= rowCount; startIndex++) {
				HSSFRow row = sheet.getRow(startIndex);
				
				HashMap<String, String> expectResult = new HashMap<>();
				for(int i=0, j=row.getLastCellNum(); i<j; i++) {
					String value = getCellValueAndConvertToString(row.getCell(i));
					String[] temp = value.split("=");
					expectResult.put(temp[0], temp[1]);
				}
				
				expectResultList.add(expectResult);
			}
			
			return expectResultList;
		}
		
		private final String getCellValueAndConvertToString(Cell cell) {
			switch (cell.getCellType()) {
			
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			
			case Cell.CELL_TYPE_NUMERIC:
				return String.valueOf((int)cell.getNumericCellValue());
				
			default:
				return "";
			}
		}
		
		private final Workbook openWorkbook(File f) {
			try {
				return new HSSFWorkbook(new FileInputStream(f));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
}
