package com.dbStudio.gameTest.protocol;

import com.dbStudio.gameTest.packageContentParser.IPackageContenParser;

public interface IProtocol{
	
	/***
	 * 数据编码
	 * @param params
	 * @return
	 */
	public byte[] encode(Object data);
	
	/***
	 * 数据解码
	 */
	public Object decode(byte[] handledPackage);
	
	/***
	 * 获取协议内容解析器
	 * @return
	 */
	public IPackageContenParser getContentParser();
	
	/***
	 * 获取CMD
	 * @param obj 经过decode的数据
	 * @return
	 */
	public Integer getCmd(Object obj);
	
	/***
	 * 获取协议类型
	 * @return
	 */
	public ProtocolType getProtocolType();
}
