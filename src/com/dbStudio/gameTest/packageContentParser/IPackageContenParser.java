package com.dbStudio.gameTest.packageContentParser;

/**
 *<br> 协议 = 协议头 + 协议体</br>
 * <br>一般协议头为2或4字节，用以表示协议体的长度，</br>
 * 
 * <br>女妖的发送协议包为：协议头(L) + 协议体(cmd、data)</br>
 * <br>女妖的接收协议包为：协议头(L) + 协议体(cmd、Zip、data)</br>
 * 
 *<br> 七雄的发送包和接收包格式一致： 协议头(4字节表示协议体长度) + 协议体(1字节是否压缩标志位、data)</br>
 * 
 * */
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
