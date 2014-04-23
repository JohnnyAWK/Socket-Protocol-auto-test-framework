package com.dbStudio.gameTest.protocol.impl;

import java.io.IOException;
import java.util.Map;

import com.dbStudio.gameTest.packageContentParser.IPackageContenParser;
import com.dbStudio.gameTest.protocol.IProtocol;
import com.dbStudio.gameTest.protocol.ProtocolType;
import com.dbStudio.gameTest.utils.Amf3Util;
import com.dbStudio.gameTest.utils.Amf3Util.Amf3Decoder;
import com.dbStudio.gameTest.utils.BytesConvertUtil;
import com.dbStudio.gameTest.utils.EmptyContainer;
import com.dbStudio.gameTest.utils.PackageTools;
import com.dbStudio.gameTest.utils.TypeInfoParser;

public class SevenQProtocol implements IProtocol {
	
	static final int PACKAGE_HEAD_LENGTH = 4;
	
	static final int COMPRESS_FLAG_LENGTH = 1;
	
	static final int DATA_START_INDEX = PACKAGE_HEAD_LENGTH + COMPRESS_FLAG_LENGTH;

	public SevenQProtocol() {

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public byte[] encode(Object params){
		
		if (TypeInfoParser.isMap(params)) {
			@SuppressWarnings("rawtypes")
			Map param = (Map) params;
			
			return PackageCreater.createPackage((byte)0, param);

		} else {
			throw new IllegalArgumentException("param is not a map type");
		}
	}
	
	@Override
	public Map<Object, Object> decode(byte[] recBytes) {
		return decodeToMap(recBytes);
		
	}

	private final Map<Object, Object> decodeToMap(byte[] recBytes) {
		try {
			byte[] data = PackageTools.getSubBytes(recBytes, DATA_START_INDEX, 
						recBytes.length - PACKAGE_HEAD_LENGTH - COMPRESS_FLAG_LENGTH);

			Amf3Decoder amf3Decoder = new Amf3Decoder();

			return amf3Decoder.decodeToMap(data);
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}
	
	@Override
	public Integer getCmd(Object obj) {
		if (TypeInfoParser.isMap(obj)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> temp = (Map<String, Object>) obj;
			
			return (Integer) temp.get("cmd");
		}
		else {
			return 0;
		}
	}
	
	@Override
	public ProtocolType getProtocolType() {
		return ProtocolType.SEVENQ;
	}
	
	final ContentParser sevenQContentParser = new ContentParser();
	
	@Override
	public final IPackageContenParser getContentParser() {
		return sevenQContentParser;
	}

	private final class ContentParser implements IPackageContenParser{
		/***
		 * 根据从服务器返回的byte流中，解析出数据包体的长度
		 * 
		 * @param recBytes
		 *            从服务器接收的字节流数组
		 * @param headLength
		 *            数据包头长度
		 * @return 包体长度
		 */
		int getBodyLength(byte[] recBytes){
			byte[] head = getHead(recBytes);
			return BytesConvertUtil.byte2Int(head);
		}

		byte[] getHead(byte[] recBytes) {
			return PackageTools.getSubBytes(recBytes, 0, PACKAGE_HEAD_LENGTH);
		}

		@Override
		public int getProtocolHeadLength() {
			return PACKAGE_HEAD_LENGTH;
		}

		@Override
		public int getProtocolBodyLength(byte[] head) {
			return BytesConvertUtil.byte2Int(head);
		}

		@Override
		public int getCompletePackageLength(byte[] recvHandledPkg) {
		
			return PACKAGE_HEAD_LENGTH + getBodyLength(recvHandledPkg);
		}

		@Override
		public byte[] getCompletePackage(byte[] recvHandledPkg) {
			int pkgLen = getCompletePackageLength(recvHandledPkg);
			return PackageTools.getSubBytes(recvHandledPkg, 0, pkgLen);
		}
		
		/***
		 * 获取完整数据包中数据段（即完整数据包中除掉包头4字节、1字节压缩为的剩余部分）
		 */
		@Override
		public byte[] getDataSegment(byte[] handledPkg) {
			
			return PackageTools.getSubBytes(handledPkg, DATA_START_INDEX, 
					handledPkg.length - PACKAGE_HEAD_LENGTH - COMPRESS_FLAG_LENGTH);
		}
	}

	public static class PackageCreater {

		/***
		 * construct data header
		 * 
		 * @param bodyLength(body包含了压缩标志位)
		 * @return
		 */
		private static final byte[] createHead(int bodyLength) {
			assert bodyLength > 0;
			
			return BytesConvertUtil.int2Byte(bodyLength );
		}
		
		/***
		 * 
		 * @param compressFlag 压缩标志位
		 * @param params 需要传递给接口的参数
		 * @return 编码好的body
		 */
		private static final byte[] createBody(byte compressFlag, Map<String, Object> params) {
			byte[] encodedSendData = encodeMapToBytes(params);
			
			byte[] body = new byte[COMPRESS_FLAG_LENGTH + encodedSendData.length];
			
			body[0] = compressFlag;
			System.arraycopy(encodedSendData, 0, body, COMPRESS_FLAG_LENGTH, encodedSendData.length);
			
			return body;
		}
		
		public static byte[] createPackage(byte compressFlag,Map<String, Object> sendData) {
			byte[] body = createBody(compressFlag, sendData);
			
			byte[] head = createHead(body.length);
			
			byte[] sendPackage = PackageTools.combinePackage(head, body);
			
			return sendPackage;
		}
		
		
		/***
		 * 把map转换为已编码的byte数组,数据会经过amf3编码
		 * 
		 * @param params
		 * @return
		 */
		private final static byte[] encodeMapToBytes(Map<String, Object> params) {
			if (params == null || params.isEmpty())
				throw new NullPointerException("Map param is null or empty");

			Amf3Util.Amf3Encoder amf3Encoder = new Amf3Util.Amf3Encoder();

			try {
				/** 使用amf3编码传递的参数 */
				return amf3Encoder.encode(params);
			} catch (IOException e) {
				e.printStackTrace();
				return EmptyContainer.EMPTY_BYTE_ARRAY;
			}
		}

	}

}
