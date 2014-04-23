package com.dbStudio.gameTest.protocol.impl;

import java.io.UnsupportedEncodingException;

import com.dbStudio.gameTest.packageContentParser.IPackageContenParser;
import com.dbStudio.gameTest.protocol.IProtocol;
import com.dbStudio.gameTest.protocol.ProtocolType;
import com.dbStudio.gameTest.utils.BytesConvertUtil;
import com.dbStudio.gameTest.utils.PackageTools;
import com.dbStudio.gameTest.utils.TypeInfoParser;


public class NvyaoProtocol implements IProtocol{
	/**L的长度*/
	static final int L_LENGTH = 2;
	/**L在数据包字节数组中的起始索引*/
	static final int L_START_POS = 0;
	
	/**CMD的长度*/
	static final int CMD_LENGTH = 2;
	/**CMD在数据包字节数组中的起始索引*/
	static final int CMD_STATR_POS = 2;
	
	/**ZIP的长度*/
	static final int ZIP_LENGTH = 1;
	/**ZIP在数据包字节数组中的起始索引*/
	static final int ZIP_START_POS = 4;
	
	/**数据段在字节数组中的起始索引*/
	static final int DATA_START_POS = L_LENGTH + CMD_LENGTH + ZIP_LENGTH;
	
	/***
	 * 客户端发送的格式为:
	 * L:int16, Cmd:int16, Data
	 * 		2		2
	 * 
	 * 
	 * 
	 * 服务器返回的格式为:
	 * L:int16, Cmd:int16, Zip:int8, Data
	 * 		2		2			1
	 * 
	 * L	代表整条协议的长度(注意:不算入L本身的长度！这点和醉西游不同)
	 * Cmd	代表协议号
	 * Zip	代表协议是否压缩(1:代表压缩过, 0:代表未压缩)
	 * Data	代表协议的数据主体
	 * */

	@Override
	public byte[] encode(Object sendData) {
		if(TypeInfoParser.isByteArray(sendData)){
			return (byte[]) sendData;
		}
		else {
			throw new IllegalArgumentException("param must be byte array, but now it is" + 
					TypeInfoParser.getObjectClassName(sendData));
		}
	}

	@Override
	public Object decode(byte[] handledPackage) {
		return handledPackage;
	}
	
	@Override
	public Integer getCmd(Object obj){
		return FieldParser.getCmd((byte[]) obj);
	}
	
	@Override
	public ProtocolType getProtocolType() {
		return ProtocolType.NVYAO;
	}
	
	public static class FieldParser {
		/**ZIP字段在从Socket收到的数据包Byte数组中的index
		 * 
		 * 服务器返回的格式为:
		 * L:int16, Cmd:int16, Zip:int8, Data
		 * 		2		2			1
		 * */
		

		
		/***
		 * 解析数据包中的特定字段，根据服务器返回的数据类型，调用对应的解析方法
		 */
		
		
		/**u8 => int*/
		public static int parserU8(byte u8) {
			return (int) (u8 & 0XFF);
		}
		
		/**u16 => int*/
		public static int parserU16(byte[] u16) {
			if(u16.length != 2)
				throw new IllegalArgumentException("param length is not 2");
			
			return (int) BytesConvertUtil.byte2Short(u16);
		}
		
		/**u32 => int*/
		public static int parserU32(byte[] u32) {
			if(u32.length != 4)
				throw new IllegalArgumentException("param length is not 4");
			
			return BytesConvertUtil.byte2Int(u32);
		}
		
		/**u64 => long*/
		public static long parserU64(byte[] u64) {
			if(u64.length != 8)
				throw new IllegalArgumentException("param length is not 8");
			
			return BytesConvertUtil.bytes2Long(u64);
		}
		
		/**byte array => String
		 * 
		 * @param bytes 表示完整的String byte array，除去了前2字节所表示的string长度
		 * */
		public static String parserString(byte[] bytes) {
			try {
				return new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return new String(bytes);
			}		
		}
		
		/**获取byte中前2个字节所表示的String长度*/
		public static int getStringLength(byte[] b) {

			return parserU16(b);
		}
		
		/**获取data数据段中，array的个数
		 * 
		 * @param data 完整的数据包中，除开L、CMD后剩余的数据
		 * */
		public static int getArrayCount(byte[] data) {
			byte[] arrayCountBytes = PackageTools.getSubBytes(data, 0, 2);
			
			return parserU16(arrayCountBytes);
		}
		
		/***
		 * <br>获取协议长度，所谓的协议是指整个数据包除开L的部分(包含CMD、ZIP字段)</br>
		 * @param pkg  		从Server返回的并经过了粘包处理的数据包
		 * @return	<br>协议长度(这个长度不是数据长度，要计算数据长度，需要减去CMD、和ZIP)</br>
		 */
		public static int getProtocolLength(byte[] pkg) {
			byte[] protocolLength = PackageTools.getSubBytes(pkg, 0, L_LENGTH);
			
			return parserU16(protocolLength);
		}
		
		/***
		 * <br>获取Cmd</br>
		 * @param pkg  		从socket收到的完整数据包
		 * @return	<br>Cmd号</br>
		 */
		public static Integer getCmd(byte[] pkg) {
			byte[] cmd = PackageTools.getSubBytes(pkg, CMD_STATR_POS, CMD_LENGTH);
			
			return Integer.valueOf(parserU16(cmd));
		}
		
		/***
		 * <br>获取Cmd</br>
		 * @param pkg  		从socket收到的完整数据包
		 * @return	<br>Cmd号</br>
		 */
		public static Integer getZip(byte[] pkg) {
			
			return pkg[ZIP_START_POS] & 0xFF;
		}
		
		/***
		 * 判断数据包是否压缩
		 * @param pkg 从socket收到的完整数据包
		 * @return 已压缩返回true，否则返回false
		 */
		public static boolean isZip(byte[] pkg) {
			if(pkg.length < 5)
				throw new IllegalArgumentException("package length is invalid, must bigger than 5");
			
			return (int) (pkg[ZIP_START_POS] & 0xFF) == 1? true : false;
		}
		
		/***
		 * 从数据包中，获取data(即从完整数据包中，剔除L、CMD、ZIP)
		 * @param pkg 完整的数据包
		 * @return 数据包中，data数据
		 */
		public static byte[] getData(byte[] pkg) {
			int protocol = getProtocolLength(pkg);
			
			/**完整的数据包长度 = L的长度 + 协议长度*/
			if((L_LENGTH + protocol) != pkg.length)
							throw new RuntimeException("包有问题");
			
			int dataLength = protocol - CMD_LENGTH - ZIP_LENGTH;

			return PackageTools.getSubBytes(pkg, DATA_START_POS, dataLength);
		}
		
	}
	
	final ContentParser nvyaoContentparser = new ContentParser();
	
	@Override
	public final IPackageContenParser getContentParser() {
		return nvyaoContentparser;
	}
	
	private final class ContentParser implements IPackageContenParser{
		
		@Override
		public int getCompletePackageLength(byte[] receiveBytes) {
			/**取L*/
			byte[] L = PackageTools.getSubBytes(receiveBytes, 0, L_LENGTH);
			
			/**根据L计算出协议体长度*/
			int protocolLength = FieldParser.parserU16(L);
			
			/**完整包长度 = L长度，加协议长度*/
			int fullPkgLength = L_LENGTH + protocolLength;
			
			return fullPkgLength;
		}

		@Override
		public byte[] getCompletePackage(byte[] undecodeSrc) {
			/**获取完整包长度*/
			int oneFullPackageLen = getCompletePackageLength(undecodeSrc);
			
			byte[] firstPkg = new byte[oneFullPackageLen];
			System.arraycopy(undecodeSrc, 0, firstPkg, 0, oneFullPackageLen);
			
			return firstPkg;
		}

		@Override
		public int getProtocolHeadLength() {
			return L_LENGTH;
		}

		@Override
		public int getProtocolBodyLength(byte[] head) {

			return FieldParser.parserU16(head);
		}

		@Override
		public byte[] getDataSegment(byte[] handledPkg) {
			
			return PackageTools.getSubBytes(handledPkg, DATA_START_POS, 
					handledPkg.length - L_LENGTH - CMD_LENGTH - ZIP_LENGTH);
		}
		
	}
	
	public static class NvyaoPackageCreater {
		/***
		 * 这个类负责组装客户端发往Server的数据
		 * 
		 * 发送的完整数据包 = head(包头) + body(包体)
		 * 
		 * head(包头) = L(协议体长度) + Cmd
		 * 
		 * body(包体) = 要传送的数据
		 */
		
		
		/**
		 * 注：发送字符串时，需要在每一个字符串前插入2字节来表示该字符串的长度
		 * 
		 * @param sendStr  需要发送的字符串
		 * 
		 * @return byte[] 返回组装好的byte数组
		 * @throws UnsupportedEncodingException 找不到对应的编码，抛出这个异常
		 * */
		public static byte[] convertSendStringToBytes(String sendStr) throws UnsupportedEncodingException {	
			sendStr = sendStr.trim();
			
			//发送内容byte
			byte[] content = sendStr.getBytes("UTF-8");
			
			//表示长度的byte
			byte[] strLength = BytesConvertUtil.short2Byte( (short) content.length);
			
			//组装的byte，为表示字符串长度bye + 内容byte
			byte[] ret = new byte[strLength.length + content.length];
			
			/**把表示长度的2字节放在最前面*/
			System.arraycopy(strLength, 0, ret, 0, strLength.length);
			System.arraycopy(content, 0, ret, strLength.length, content.length);
			
			return ret;
		}
		
		/***
		 * 创建包头，包头 = L + cmd
		 * @param L 协议体的长度
		 * @param cmd Cmd号
		 * @return 返回组装好的长度
		 */
		public static byte[] createSendPkgHead(short L, short cmd) {
			byte[] lBytes = BytesConvertUtil.short2Byte(L);
			byte[] cmdBytes = BytesConvertUtil.short2Byte(cmd);
			
			byte[] head = new byte[lBytes.length + cmdBytes.length];
			
			System.arraycopy(lBytes, 0, head, 0, lBytes.length);
			System.arraycopy(cmdBytes, 0, head, lBytes.length, cmdBytes.length);
			
			return head;
		}
		
		/***
		 * 创建完整数据包，完整包 = 包头 + 包体
		 * @param head 已构造好的包头
		 * @param body 已构造好的包体
		 * @return 返回组装好的完整数据包
		 */
		public static byte[] createSendPackage(byte[] head, byte[] body) {
			byte[] pkg = new byte[head.length + body.length];
			
			System.arraycopy(head, 0, pkg, 0, head.length);
			System.arraycopy(body, 0, pkg, head.length, body.length);
			
			return pkg;
		}
		
		/***
		 * 计算协议长度
		 * 注意：传入的数据只能是转换成的byte或者byte数组
		 * 协议长度 = Cmd + 数据长度
		 * @param 
		 * @return
		 * @throws IllegalArgumentException 当传入的参数不是byte或者byte[] 时，抛出该异常
		 */
		public static short calcProcotolLength(Object...sendParams) {
			/**协议长度*/
			short protocolLength = 0;
			
			for (Object param : sendParams) {
				
				if(TypeInfoParser.isByteArray(param)) {
					protocolLength += ((byte[]) param).length;
				}
				else if(TypeInfoParser.isByte(param)) {
					protocolLength += (short)1;
				}
				else {
					throw new IllegalArgumentException("param must be byte or byte Array");
				}
				
			}
			
			return protocolLength;
		}
			
	}
	
	
}
