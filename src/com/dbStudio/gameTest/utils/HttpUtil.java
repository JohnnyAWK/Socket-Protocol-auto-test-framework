package com.dbStudio.gameTest.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	
	private static final int BUF_SIZE = 1024;
	
	/**
	 * Post方法调用接口
	 * @param url				待测试接口的地址
	 * @param paramList			传递的参数
	 * @return					调用成功返回字符串
	 * 							调用失败返回null
	 * @throws IOException
	 */
    public static String doPost(String url, List<NameValuePair> paramList) throws IOException{
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpPost httpPost = new HttpPost(url);
    	
    	httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
    	HttpResponse httpResponse = null;
    	
    	InputStream is = null;
    	
    	ByteArrayOutputStream baos = null;
    	
    	try {
    		httpResponse = httpClient.execute(httpPost);
        	if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
    			HttpEntity entity = httpResponse.getEntity();
    			if (entity != null) {
    				is = entity.getContent();
    				
    				int bytesRead = -1;
    				byte[] buffer = new byte[BUF_SIZE];   				
    				baos = new ByteArrayOutputStream();
    				
    				while ((bytesRead = is.read(buffer, 0, BUF_SIZE)) != -1) {
    					baos.write(buffer, 0, bytesRead);
    				}
    						
    				return baos.toString();
    			}
    		}
		} finally {
			try {
				if(is != null) is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(baos != null) baos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			httpClient.getConnectionManager().shutdown();
		}
    	
    	return null;
	}
    
    /**
     * 参数为Map形式的方法，方法同doPost(String url, List<NameValuePair> paramList)
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> params) throws IOException{
    	List<NameValuePair> paramList = CommonsTool.convertMap2List(params);
    	return doPost(url, paramList);
    }
     
    /**
     * Get方法调用接口
     * @param baseUrl				不含参数的Url
     * @param params				传递给接口的参数
     * @return						调用成功返回字符串
     * @throws IOException
     */
    public static String doGet(String baseUrl, List<NameValuePair> params) throws IOException{
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpGet httpGet = new HttpGet(getCompletedUrl(baseUrl, params));
    	
    	ResponseHandler<String> handler = new BasicResponseHandler();
    	try {
    		String response = httpClient.execute(httpGet, handler);
    		return response;
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	} 	
    }
    
    /**
     * 重载方法
     * @param baseUrl
     * @param params
     * @return
     * @throws IOException
     */
    public static String doGet(String baseUrl, Map<String, String> params) throws IOException{
    	List<NameValuePair> paramList = CommonsTool.convertMap2List(params);
    	return doGet(baseUrl, paramList);
    }
    
    /**
	 * @param url			upload url
	 * @param params		post params
	 * @param file			absolute file path
	 * @return				if success, return String; else return null
	 * */
	public static String uploadFile(String url, String fieldName, String file) throws IOException{

		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		HttpPost post = new HttpPost(url);
		File uploadFile = new File(file);
		
		InputStream is = null;
		
		// <input type="file" name="userfile" />
		MultipartEntity entity = new MultipartEntity();
		ContentBody cbFile = new FileBody(uploadFile);
		entity.addPart(fieldName, cbFile);
		
		post.setEntity(entity);
		try {
			HttpResponse response = httpClient.execute(post);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				
				HttpEntity httpEntity = response.getEntity();
				
				if (httpEntity != null) {
					
					is = httpEntity.getContent();
    				@SuppressWarnings("unused")
					int bytesRead = -1;
    				byte[] buffer = new byte[1024];
    				StringBuffer stringBuffer = new StringBuffer();
    				while ((bytesRead = is.read(buffer)) != -1) {
    					stringBuffer.append(new String(buffer).toCharArray());
    				}
    				
    				buffer = null;				
    				return stringBuffer.toString();
				}
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}
    
    /**
     * 该方法实用与get方法，组装url和参数，拼接成最终调用的url
     * @param baseUrl				不含参数的url地址
     * @param params				传给接口的参数	
     * @return						返回调用接口的完整url地址
     * @throws IOException
     */
    private static final String getCompletedUrl(String baseUrl, List<NameValuePair> params) throws IOException{
    	if (!baseUrl.endsWith("?")) {
			baseUrl = baseUrl + "?";
		}
    	return baseUrl + EntityUtils.toString(new UrlEncodedFormEntity(params));
    }
    
}
