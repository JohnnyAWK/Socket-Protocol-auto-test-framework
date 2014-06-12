package com.dbStudio.gameTest.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {
	
	/**mysql*/
	public static final String DB_MYSQL = "mysql";
	/**oracle*/
	public static final String DB_ORACLE = "oracle";
	
	/**mysql driver*/
	private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	
	/**oracle driver*/
	private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	
	private String mUrl;
	
	private String username;
	
	private String password;
	
	private static DBUtils DB_instance;

	private Connection mConn;
	
	private DBUtils(File config, String dbType) {
		loadDriver(dbType);
		loadConfig(config);
	}
	
	private DBUtils(String ip, String port, String instance, String dbType) {
		loadDriver(dbType);
		mUrl = createUrl(dbType, ip, port, instance);
	}
	
	private final void loadConfig(File configFile) {
		ConfigManager config =  ConfigManager.getCongfigManager(configFile);
		mUrl = config.get("url");
		username = config.get("username");
		password = config.get("password");
	}
	
	public static DBUtils createInstance(File configFile, String dbType) {
		if(DB_instance == null) {
			synchronized (DBUtils.class) {
				if(DB_instance == null) {
					DB_instance = new DBUtils(configFile, dbType);
				}
			}
		}
		return DB_instance;
	}
	
	public static DBUtils createInstance(String ip, String port, String instance, String dbType) {
		if(DB_instance == null) {
			synchronized (DBUtils.class) {
				if(DB_instance == null) {
					DB_instance = new DBUtils(ip, port,instance, dbType);
				}
			}
		}
		return DB_instance;
	}
	
	private boolean mIsConnected = false;
	
	/***
	 * 连接数据库
	 */
	public void connectDB() {
		try {
			checkCondition();
			
			mConn = DriverManager.getConnection(mUrl, username, password);
			mIsConnected = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private final void checkCondition() {
		if(mUrl.equals(""))
			throw new IllegalStateException("db url is empty");
		
		if(mUrl == null)
			throw new IllegalStateException("db url is empty");
		
		if(username.equals(""))
			throw new IllegalStateException("db access username is empty");
		
		if(username == null)
			throw new IllegalStateException("db access username is empty");
		
		if(password.equals(""))
			throw new IllegalStateException("db access password is empty");
		
		if(password == null)
			throw new IllegalStateException("db access password is empty");
	}
	
	/***
	 * 判断数据库是否连接
	 * @return 
	 * 			<br>true表示连接成功</br>
	 * 			false表示连接失败
	 */
	public boolean isConnected(){
		return mIsConnected;
	}
	
	/***
	 * 执行查询
	 * @param sql
	 * @return 返回查询结果集,如果没有查询结果为空或者发生异常，返回null
	 */
	public ResultSet query(String sql) {
		PreparedStatement ps = null;
		try {
			ps = mConn.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			return rs.getFetchSize() > 0? rs : null;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * 执行删除，更新操作
	 * @param sql
	 * @return 执行成功返回true，否则为false
	 */
	public boolean executeUpdate(String sql) {
		PreparedStatement ps = null;
		try {
			ps = mConn.prepareStatement(sql);
			
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/***
	 * 关闭Resultset 
	 * @param rs
	 */
	public void closeResultSet(ResultSet rs) {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * 关闭数据库连接
	 */
	public void closeConnection() {
		if(mConn != null) {
			try {
				mConn.close();
				mIsConnected = false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * 加载Mysql driver
	 */
	private static final void loadMysqlDriver(){
		try {
			Class.forName(MYSQL_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/***
	 * 加载Oracle driver
	 */
	private static final void loadOracleDriver(){
		try {
			Class.forName(ORACLE_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/***
	 * 加载驱动
	 * @param dbType 需要加载的驱动类型，传入DBUtils.DB_MYSQL或者DBUtils.DB_ORACLE
	 * @throws 参数不正确时，抛出IllegalArgumentException
	 */
	private static final void loadDriver(String dbType) {
		if(DB_MYSQL.equalsIgnoreCase(dbType)) {
			loadMysqlDriver();
			return;
		}
		
		if(DB_ORACLE.equalsIgnoreCase(dbType)) {
			loadOracleDriver();
			return;
		}
		
		throw new IllegalArgumentException(dbType + "is not a valid db");
	}
	
	/***
	 * <br>创建数据库访问的URL</br>
	 * <br>Oracle的url格式为：jdbc:oracle:thin:@192.168.188.20:1521:tianyias</br>
	 * 	Mysql的url格式为:jdbc:mysql://192.168.188.20:1521/tianyias
	 * @param dbType DB_MYSQL 或 DB_ORACLE
	 * @param ip	数据库IP地址
	 * @param port	数据库端口
	 * @param dbInstance 数据库实例
	 * @return 对应的url地址
	 * @throws IllegalArgumentException 当传递的db类型不是DB_MYSQL 或 DB_ORACLE时，抛出这个异常
	 */
	private final static String createUrl(String dbType, String ip, String port, String dbInstance) {
		if(DB_MYSQL.equalsIgnoreCase(dbType)) {
			return createMysqlUrl(ip, port, dbInstance);
		}
		
		if(DB_ORACLE.equalsIgnoreCase(dbType)) {
			return createOracleUrl(ip, port, dbInstance);
		}
		
		throw new IllegalArgumentException(dbType + "is not a valid db"); 
	}
	
	/***
	 * 创建Mysql Url
	 * @param ip
	 * @param port
	 * @param dbInstance
	 * @return
	 */
	private final static String createMysqlUrl(String ip, String port, String dbInstance) {
		return "jdbc:mysql://" + ip + ":" + port + "/" + dbInstance;
	}
	
	/***
	 * 创建Oracle Url
	 * @param ip
	 * @param port
	 * @param dbInstance
	 * @return
	 */
	private final static String createOracleUrl(String ip, String port, String dbInstance) {
		return "jdbc:oracle:thin:@" + ip + ":" + port + ":" + dbInstance;
	}
}
