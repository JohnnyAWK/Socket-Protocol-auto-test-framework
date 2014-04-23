package com.dbStudio.gameTest.utils;

import java.nio.ByteBuffer;

public class BytesConvertUtil {
	/***
	 * convert byte to int
	 * @param b
	 * @return
	 */
	public static int byte2Int(byte[] b) {
        int mask = 0xFF;  
        int temp = 0;  
        int n = 0;  
        
        for(int i = 0; i < 4; i++){
            n <<= 8;  
            temp = b[i] & mask;  
            n |= temp;  
        }
        return n;
	}
	
	public static byte[] int2Byte(int i){
		if(i > Integer.MAX_VALUE)
			return null;
		
		if(i < Integer.MIN_VALUE)
			return null;
		
		byte[] b = new byte[4];
		
		b[0] = (byte) ((i >> 24) & 0xFF);
		b[1] = (byte) ((i >> 16) & 0xFF);
		b[2] = (byte) ((i >> 8) & 0xFF);
		b[3] = (byte) (i & 0xFF);
		
		return b;
	}
	
	public static byte[] short2Byte(short s) {
		byte[] ret = new byte[2];
		
		ret[0] = (byte)((s >> 8)& 0xff);
		ret[1] = (byte)(s & 0xff);
		
		return ret;
	}
	
    public static short byte2Short(byte[] b) { 
        int mask = 0xFF;  
        int temp = 0;  
        short n = 0;  
        
        for(int i = 0; i < 2; i++){
            n <<= 8;  
            temp = b[i] & mask;  
            n |= temp;  
        }
        return n;
    }
    
    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    
    public static byte[] long2Bytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
    
    public static long bytes2Long(byte[] bytes) {
    	buffer.clear();
        buffer.put(bytes, 0, 8);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
}
