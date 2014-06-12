package com.dbStudio.gameTest.packageContentParser;


public interface IPackageContenParser {
	
	public int getProtocolHeadLength();
	
	public int getProtocolBodyLength(byte[] head);
	
	/**获取一个完整包的长度*/
	public int getCompletePackageLength(byte[] recvUnhandledPkg);
	
	/**从socket收到的数据包中截取一个完整包*/
	public byte[] getCompletePackage(byte[] recvUnhandledPkg);
	
	/**从已经过粘包处理的完整包中获取数据段*/
	public byte[] getDataSegment(byte[] handledPkg);
}
