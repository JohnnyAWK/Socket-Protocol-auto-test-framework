package com.dbStudio.gameTest.runner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.dbStudio.gameTest.packageContentParser.IPackageContenParser;
import com.dbStudio.gameTest.protocol.IProtocol;
import com.dbStudio.gameTest.protocol.impl.NvyaoProtocol;
import com.dbStudio.gameTest.protocol.impl.SevenQProtocol;
import com.dbStudio.gameTest.socket.PackageTransfer;
import com.dbStudio.gameTest.utils.ConfigManager;
import com.dbStudio.gameTest.utils.Sleeper;

public class DefaultRunner {
	
	private final static Map<String, ? super IProtocol> protocolDict = new HashMap<String, IProtocol>();
	
	static{
		protocolDict.put("nvyao", new NvyaoProtocol());
		protocolDict.put("7q", new SevenQProtocol());
	}
	
	private String ip;
	private int port;
	
	private IProtocol mProtocol;
	
	private PackageTransfer mSocket;
	
	private IPackageContenParser mContentParser;
	
	public Map<Integer, Object> pkgsCache;
	
	private DefaultRunner(){
		pkgsCache = new HashMap<>();
	} 
	
	/***
	 * 读取配置文件，获取server的IP、port，获取协议类型
	 * @param config 配置文件全路径
	 */
	public final void loadConfig(String config) {
		ConfigManager cfg = ConfigManager.getCongfig(new File(config));
		
		ip = cfg.get("ip");
		port = Integer.parseInt(cfg.get("port"));
		
		mProtocol = (IProtocol) protocolDict.get(cfg.get("protocol"));

	}
	
	public static DefaultRunner createRunner() {
		return new DefaultRunner();
	}
	
	public void setContentParser(IPackageContenParser parser) {
		mContentParser = parser;
	}
	
	public void setServerIP(String ip) {
		this.ip = ip;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	/***
	 * 设置协议
	 * @param className 协议的类名，一定要包含全路径，如:com.xxx.xxx.protocolA
	 * 调用成功后，加载指定的协议
	 */
	public void setProtocol(String className){
		try {
			Class<?> klass = Class.forName(className);
			//判断IProtocol是不是klass的父类或接口
			//方法isAssignableFrom是用来判断子类是否继承或父接口的，调用方法为：父类.isAssignableFrom(子类)
			if(IProtocol.class.isAssignableFrom(klass)) {
				mProtocol = (IProtocol) klass.newInstance();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public IProtocol getProtocol(){
		return mProtocol;
	}
	
	/***
	 * 初始化runner，根据loadConfig获取的配置，获取协议的parser，连接socket；如果连接成功，那么启动一个线程来接收从server返回的包
	 * @throws IOException 连接server失败，抛出这个异常
	 */
	public void init() throws IOException{
		mContentParser = mProtocol.getContentParser();
		mSocket = PackageTransfer.createTransfer(ip, port);
		
		mSocket.connectServer();
		if(mSocket.isConnected()){
			new Thread(new ReceiveThread(), "Socket Reveive Thread").start();
		}
		else {
			throw new IOException("connect server failed");
		}
	}
	
	/***
	 * 发送数据，这个API会根据所选的协议调用协议编码方法来编码数据，然后通过socket发送，发送后，会sleep50毫秒
	 * @param data
	 */
	public <T> void sendDataAndSleep(T data) {
		byte[] encodedData = mProtocol.encode(data);
		mSocket.send(encodedData);
		Sleeper.sleepSomeMilliseconds(50);
	}
	
	private final Object receiveDataOrBlocking(Integer cmd){
		while(true){
			/**先获取一个包*/
			byte[] pkg = mSocket.getHandledPackage();
			if(pkg == null){
				Sleeper.sleepSomeMilliseconds(50);
				continue;
			}
			else {
				/**获取cmd，并把数据解码*/
				Integer cmdTemp = mProtocol.getCmd(pkg);
				/**如果解析不出CMD，说明数据包已损坏*/
				if(cmdTemp == null)
					continue;
				
				Object data = mProtocol.decode(pkg);
				/**添加到缓存*/
				addCache(cmdTemp, data);
					
				if(cmd.equals(cmdTemp)) {
					return data;
				}

				Sleeper.sleepSomeMilliseconds(50);
				continue;	
				
			}
		}
	}
	
	private final Object receiveDataOrBlockingUtilTimeout(Integer cmd, long timeout){
		final long time = System.currentTimeMillis() + timeout;
		
		while(System.currentTimeMillis() < time){
			byte[] pkg = mSocket.getHandledPackage();
			
			if(pkg == null){
				Sleeper.sleepSomeMilliseconds(50);
				continue;
			}
			else {
				
				Integer cmdTemp = mProtocol.getCmd(pkg);
				Object data = mProtocol.decode(pkg);
					
				addCache(cmdTemp, data);
					
				if(cmd.equals(cmdTemp)) {
					return data;
				}

				Sleeper.sleepSomeMilliseconds(50);
				continue;				
			}
		}
		
		return null;
	}
	
	/***
	 * 根据指定的cmd获取解码后的数据，如果没有则一直阻塞
	 * @param cmd
	 * @return 解码后的数据
	 */
	public Object getDataOrBlocking(Integer cmd) {
		if(isCached(cmd)) {
			return getFromCache(cmd);
		}
		else {
			return receiveDataOrBlocking(cmd);
		}
	}
	
	/***
	 * 根据指定的cmd获取解码后的数据，如果没有则一直阻塞到超时，
	 * @param cmd
	 * @param timeoutInMills 超时时间
	 * @return 获取到数据后返回，超时获取返回null
	 */
	public Object getDataOrBlocking(Integer cmd, long timeoutInMills) {
		if(isCached(cmd)) {
			return getFromCache(cmd);
		}
		else {
			return receiveDataOrBlockingUtilTimeout(cmd, timeoutInMills);
		}
	}
	
	/***
	 * 关闭runner，并关闭socket，以及释放线程
	 */
	public void shutDown() {
		mSocket.close();
	}
	
	private final void addCache(Integer cmd, Object data) {
		if((cmd != null) && !isCached(cmd) ){
			pkgsCache.put(cmd, data);
		}
	}
	
	private final boolean isCached(Integer cmd) {
		return pkgsCache.containsKey(cmd);
	}
	
	private Object getFromCache(Integer cmd) {
		return pkgsCache.get(cmd);
	}
	
	private final class ReceiveThread implements Runnable {

		@Override
		public void run() {
			try {
				mSocket.doReceiveLoop(mContentParser);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
