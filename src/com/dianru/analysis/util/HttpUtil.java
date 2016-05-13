package com.dianru.analysis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * @Porject lando
 * @author junwu.zhu
 * @date:Apr 17, 2014 3:16:47 PM
 * @version : 1.0
 * @email : icerivercomeon@gmail.com
 * @desciption : HTTP Util
 */
public class HttpUtil {
	// 设置HTTPS SSL验证数据
	// static {
	// System.setProperty("javax.net.ssl.trustStore", "");
	// System.setProperty("javax.net.ssl.trustStorePassword", "");
	// }
	public static final String POST = "post"; // post请求
	public static final String GET = "get"; // get请求

	/**
	 * 根据前缀的不同获取获取URLConnection
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static HttpURLConnection openHttpURLConnection(String url)
			throws IOException {
		URL getUrl = new URL(url);
		// 根据拼凑的URL，打开连接，URL.openConnection()函数会根据
		if (url.startsWith("https")) { // https
			HttpsURLConnection connection = (HttpsURLConnection) getUrl
					.openConnection();
			connection.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			return connection;
		} else {
			HttpURLConnection connection = (HttpURLConnection) getUrl
					.openConnection();
			return connection;
		}
	}

	/**
	 * 转换为完整url
	 * 
	 * @param protocol
	 * @param requestMethod
	 * @param url
	 * @param params
	 * @return
	 */
	public static String convert2fullUrl(String protocol, String url,
			String params) {
		StringBuffer fullUrl = new StringBuffer();
		boolean isHasHttp = url.startsWith("http://")
				|| url.startsWith("https://");
		fullUrl.append(ValidateUtils.isEmpty(protocol) || isHasHttp ? "http://"
				: protocol + "://");// 请求头
		fullUrl.append(url);// 请求url
		fullUrl.append(params != null && params.startsWith("?") ? params : "?"
				+ params); // 参数
		return fullUrl.toString();
	}

	/**
	 * 请求
	 * 
	 * @param url
	 * @return
	 */
	public static String request(String method, String url) {
		if (POST.equalsIgnoreCase(method)) {
			return post(url);
		} else {
			return get(url);
		}
	}

	/**
	 * get请求
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		StringBuffer result = new StringBuffer();
		try {
			HttpURLConnection connection = openHttpURLConnection(url);// 获取http连接
			connection.connect();// 建立与服务器的连接，并未发送数据
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));// 发送数据到服务器并使用Reader读取返回的数据
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			reader.close();
			connection.disconnect();// 断开连接
		} catch (Exception e) {
			return null;
		}
		return result.toString();
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @return
	 */
	public static String post(String url) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuffer result = new StringBuffer();
		try {
			HttpURLConnection connection = openHttpURLConnection(url);// 获取http连接

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod(POST);
			connection.connect();// 建立与服务器的连接，并未发送数据

			out = new PrintWriter(connection.getOutputStream());
			out.flush();

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}

			out.close();
			in.close();
			connection.disconnect();// 断开连接
		} catch (IOException e) {
			return null;
		}
		return result.toString();
	}

	/**
	 * 验证http请求校验码
	 * 
	 * @param url
	 * @return
	 */
	public static int verify(String url) {
		try {
			HttpURLConnection connection = openHttpURLConnection(url);// 获取链接
			connection.connect();// 建立与服务器的连接，并未发送数据
			int code = connection.getResponseCode();
			connection.disconnect();// 断开连接
			return code;
		} catch (IOException e) {
			return 0;
		}
	}
}