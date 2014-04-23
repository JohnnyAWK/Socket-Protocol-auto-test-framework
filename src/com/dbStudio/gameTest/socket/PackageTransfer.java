package com.dbStudio.gameTest.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dbStudio.gameTest.packageContentParser.IPackageContenParser;
import com.dbStudio.gameTest.utils.EmptyContainer;
import com.dbStudio.gameTest.utils.PackageTools;
import com.dbStudio.gameTest.utils.Sleeper;

public class PackageTransfer{

	private final String host;
	private final int port;

	private Socket client;

	private InputStream in;

	private OutputStream out;

	private byte[] buffer;
	
	private static final int BUF_SIZE = 1024 * 8;
	
	private final ConcurrentLinkedQueue<byte[]> handledQueue;
	
	public PackageTransfer(String host, int port) {
		checkPort(port);
		
		handledQueue = new ConcurrentLinkedQueue<byte[]>();

		this.host = host;
		this.port = port;
		
		client = new Socket();

		buffer = new byte[BUF_SIZE];
	}
	
	public static PackageTransfer createTransfer(String host, int port) {
		return new PackageTransfer(host, port);
	}
	
	private final void checkPort(int port) {
		if (port < 1024 || port > 65535)
			throw new IllegalArgumentException(
					"port must between 1024 and 65535");
	}
	
	private int timeout = DEFAULT_TIMEOUT;

	private static final int DEFAULT_TIMEOUT = 1000 * 30; // 30秒

	public void setConnectTimeOut(int milliSeconds) {
		timeout = milliSeconds;
	}
	
	private boolean isConnected = false;
	
	public final void connectServer() {
		try {
			client.connect(new InetSocketAddress(host, port), timeout);

			in = client.getInputStream();
			out = client.getOutputStream();

			isConnected = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final boolean isConnected() {
		return isConnected;
	}
	
	public void send(byte[] sendData) {
		if (isConnected) {
			try {
				out.write(sendData);
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doReceiveLoop(final IPackageContenParser parser) throws IOException {
		loopAndHandlePackage(parser);
	}
	
	public byte[] getHandledPackage() {
		return handledQueue.poll();
	}
	
	public boolean isQueueEmpty() {
		return handledQueue.isEmpty();
	}
	
	public int getReceivedPkgCount() {
		return handledQueue.size();
	}
	
	private volatile boolean stopReceive = false;
	
	private final void loopAndHandlePackage(final IPackageContenParser parser) throws IOException {
		int bytesRead = -1;
		/**remains保存解析出完整包后剩余的部分*/
		byte[] remains = EmptyContainer.EMPTY_BYTE_ARRAY;
		
		while (!stopReceive) {
			
			try {
				while ((bytesRead = in.read(buffer)) != -1) {

					byte[] temp = new byte[bytesRead];
					System.arraycopy(buffer, 0, temp, 0, bytesRead);
						
					if(remains.length > 0) {
						/**剩余部分的长度大于0，表明有上一次处理后，还有剩余部分未处理，那么把收到的包和这个半包合并成一个新包（上一次包的数据在前）
						 * 第一次执行时，因为remains长度为0，所以跳过了这个判断内的语句，然后解析第一个包，如果有剩余就组合
						 * */
						temp = PackageTools.combinePackage(remains, temp);
					}
							
					remains = handlePackage(temp, parser);			
				}
			} catch (IOException e) {
				if("socket closed".equalsIgnoreCase(e.getMessage())){
					//关闭时，因为read方法还在阻塞中，所以要抛出这个异常，这是正常的，所以不处理
				}
				else {
					e.printStackTrace();
				}
			}
			
			Sleeper.sleepSomeMilliseconds(50);
		}
	}
	
	/***
	 * 解析从服务器收到的包，切割完整包，并返回半包
	 * @param unHandledPkg 从socket收到的数据包
	 * @param parser 不同的协议，传递不同的包内容解析器
	 * @return 如果收到的包是一个或者多个完整包，那么返回0长度字节数组
	 * 			如果收到的包是1个半或者N个半数据包，那么截取一个或N个完整包，并把剩余的部分返回
	 */
	private final byte[] handlePackage(byte[] unHandledPkg, IPackageContenParser parser) {
		/**调用一次read，从Server收到的数据包(可能是半包、1个包、1.x、2.x....)*/
		int pkgLen = unHandledPkg.length;
		
		/**一个完整数据包的长度*/
		int completePkgLen = parser.getCompletePackageLength(unHandledPkg);
		
		if(completePkgLen > pkgLen) {
			/**当前收到的数据不到一个完整包，则直接返回，等待下一个包*/
			return unHandledPkg;
		} 
		else if (completePkgLen == pkgLen) {
			/**一个完整包，则直接丢到已处理队列*/
			handledQueue.offer(unHandledPkg);
			
			return EmptyContainer.EMPTY_BYTE_ARRAY;
		} 
		else {
			/**有多个包，那么就递归解析，*/
			byte[] onePkg = parser.getCompletePackage(unHandledPkg);
			handledQueue.offer(onePkg);
			
			/**截取除完整包后的剩余部分*/
			byte[] remain = PackageTools.getSubBytes(unHandledPkg, onePkg.length, pkgLen - onePkg.length);
			
			return handlePackage(remain, parser);
		}
	}
	
	public void close() {
		stopReceiveLoop();
		closeInputStream();
		closeOutputStream();
		closeSocket();
	}
	
	private void stopReceiveLoop() {
		stopReceive = true;
	}

	private void closeSocket() {
		if (isConnected) {
			if (client != null) {
				try {
					client.close();
					isConnected = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void closeInputStream() {
		if(in != null) {
			try {
				client.shutdownInput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void closeOutputStream() {
		if(out != null) {
			try {
				client.shutdownOutput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
}
