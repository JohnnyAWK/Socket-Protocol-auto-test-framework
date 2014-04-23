package com.dbStudio.gameTest.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;

public class Amf3Util {
	
	/***
	 * Amf3 encoder
	 * below type can encode to AMF3 type:
	 * 	1. Map
	 * 	2. int
	 * 	3. byte[]
	 * @author Administrator
	 *
	 */
	public static class Amf3Encoder{
		
		private final SerializationContext mContext;
		
		public Amf3Encoder(){
			mContext = SerializationContext.getSerializationContext();
		}
		
		/***
		 * encode Map to byte by AMF3 protocol
		 * @param data
		 * @return
		 * @throws IOException
		 */
		public byte[] encode(Map<?, ?> data) throws IOException{
			Amf3Output amf3Output = new Amf3Output(mContext);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			amf3Output.setOutputStream(baos);
			
			try {
				amf3Output.writeObject(data);
				amf3Output.flush();
				return baos.toByteArray();
			} finally {				
				try {
					amf3Output.close();
					baos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/***
		 * encode int to byte by AMF3 protocol
		 * @param i
		 * @return
		 * @throws IOException
		 */
		public byte[] encode(int i) throws IOException{
			Amf3Output amf3Output = new Amf3Output(mContext);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			amf3Output.setOutputStream(baos);
			
			try {
				amf3Output.write(i);
				amf3Output.flush();
				return baos.toByteArray();
			} finally {				
				try {
					amf3Output.close();
					baos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/***
		 * convert byte array to amf3 byte array
		 * @param b
		 * @return
		 * @throws IOException
		 */
		public byte[] encode(byte[] b) throws IOException{
			Amf3Output amf3Output = new Amf3Output(mContext);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			amf3Output.setOutputStream(baos);
			
			try {
				amf3Output.write(b);
				amf3Output.flush();
				return baos.toByteArray();
			} finally {
				try {
					amf3Output.close();
					baos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	
	}
	
	/***
	 * Amf3 decoder
	 * can convert amf3 data to below type
	 * 	1. Map
	 * 	2. int
	 * @author Administrator
	 *
	 */
	public static class Amf3Decoder{
		private final SerializationContext mContext;
		
		public Amf3Decoder(){
			mContext = SerializationContext.getSerializationContext();

		}
		
		/***
		 * decode to map
		 * @param buf buffer
		 * @param offset
		 * @param length
		 * @return
		 * @throws ClassNotFoundException
		 * @throws IOException
		 */
		@SuppressWarnings("unchecked")
		public Map<String,Object> decodeToMap(byte[] src, int srcStartPos, int count) throws 
							ClassNotFoundException, IOException{
			Amf3Input amf3Input = new Amf3Input(mContext);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(src, srcStartPos, count);
			amf3Input.setInputStream(bais);
			
			try {
				Object obj = amf3Input.readObject();
				return (HashMap<String,Object>) obj;
			} finally {
				try {
					bais.close();
					
					amf3Input.close();
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public Map<Object,Object> decodeToMap(byte[] body) throws ClassNotFoundException, IOException{
			Amf3Input amf3Input = new Amf3Input(mContext);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(body);
			amf3Input.setInputStream(bais);
			
			try {
				Object obj = amf3Input.readObject();
				
				if(TypeInfoParser.isMap(obj)){
					return (HashMap<Object, Object>) obj;
				}
				else {
					throw new IOException("decoded bytes is not a map type");
				}
		
			} finally {
				try {
					bais.close();
					amf3Input.close();
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}			
		}
		
		/***
		 * decode to int
		 * @param buf
		 * @param offset
		 * @param length
		 * @return
		 * @throws IOException
		 */
		public int decodeToInt(byte[] buf, int offset, int length) throws IOException{
			Amf3Input amf3Input = new Amf3Input(mContext);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(buf, offset, length);
			amf3Input.setInputStream(bais);
			
			try {
				int bodyLength = amf3Input.readInt();
				return bodyLength;
			} finally {
				try {
					bais.close();
					amf3Input.close();
				} catch (Exception ignore) {}				
			}					
		}
	
	}
		
}
