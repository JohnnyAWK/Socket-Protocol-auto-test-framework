package com.dbStudio.gameTest.utils;

public class PackageTools {
	
	private PackageTools(){}
	
	public static final void assertNotNull(byte[] bytes) {
		if(null == bytes)
			throw new NullPointerException("param is null");
	}
	
	public static final void assertNotEmpty(byte[] bytes) {
		if (bytes.length == 0)
			throw new NullPointerException("param is empty");
	}
	
	public static boolean isEmpty(byte[] bytes){
		return bytes.length == 0;
	}
	
	public static final void assertOffsetIsVaild(byte[] pkg, int offset) {
		if(offset < 0 || offset > pkg.length)
			throw new IllegalArgumentException("invaild offset: " + offset);
	}
	
	public static final void asserIntegerGreaterThanZero(int arg) {
		if(arg <= 0)
			throw new IllegalArgumentException("param is less than 0");
	}
	
	/***
	 * 获取包头
	 * @param src 源字节数组
	 * @param startSrcPos 起始位置
	 * @param count 拷贝的个数
	 * 
	 * 
	 * @return 截取的协议头
	 * @throws IllegalArgumentException 数组的剩余字节数，不等于需要拷贝的字节数
	 */
	public static final byte[] getSubBytes(byte[] src, int startSrcPos, int count) {
		
		if((src.length - startSrcPos) < count)
			throw new IllegalArgumentException("copy bytes count: " + count 
					+ " bigger than source length: " + src.length);
		
		byte[] bytes = new byte[count];
		
		System.arraycopy(src, startSrcPos, bytes, 0, count);
		
		return bytes;
	}
	
	/***
	 * 获取一份拷贝
	 * @param src 需要获取拷贝的源
	 * @return 拷贝后的数组
	 */
	public static final byte[] copyPackage(final byte[] src) {
		assertNotNull(src);
		
		byte[] newOne = new byte[src.length];
		
		System.arraycopy(src, 0, newOne, 0, src.length);
		
		return newOne;
	}
	
	/***
	 * 从大包中，获取截取了一个完整包后的剩余部分
	 * @param src 大包
	 * @param firstCompletePkgLength 第一个完整包的长度
	 * @return 返回截取了完整包后，大包的剩余部分
	 */
	public static final byte[] getRemainPkg(byte[] src, int firstCompletePkgLength) {
		int remainPkgLength = src.length - firstCompletePkgLength;
		
		asserIntegerGreaterThanZero(remainPkgLength);
		
		byte[] remainPkg = new byte[remainPkgLength];
		System.arraycopy(src, firstCompletePkgLength, remainPkg, 0, remainPkgLength);
		
		return remainPkg;
	}
	
	/***
	 * 把2个数据包合并成一个包，一定要注意先后顺序
	 * @param head 新包的包头
	 * @param last 
	 * @return
	 */
	public static byte[] combinePackage(byte[] pkgHead, byte[] pkgTail) {
		assertNotNull(pkgHead);
		assertNotNull(pkgTail);
		assertNotEmpty(pkgHead);
		assertNotEmpty(pkgTail);		
		
		byte[] newPkg = new byte[pkgHead.length + pkgTail.length];
		
		/**copy head*/
		System.arraycopy(pkgHead, 0, newPkg, 0, pkgHead.length);
		
		/**copy tail*/
		System.arraycopy(pkgTail, 0, newPkg, pkgHead.length, pkgTail.length);
		
		return newPkg;
	}
	
}
