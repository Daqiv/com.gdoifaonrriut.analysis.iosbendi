package com.dianru.analysis.util;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Porject lando
 * @author JunWu.zhu
 * @date:Apr 17, 2014 3:16:47 PM
 * @version : 1.0
 * @email : icerivercomeon@gmail.com
 * @desciption :字符串操作工具
 */
public class StringUtils {

	private StringUtils() {
	}

	/**
	 * Converts <code>null</code> to empty string, otherwise returns it
	 * directly.
	 * 
	 * @param string
	 *            The nullable string
	 * @return empty string if passed in string is null, or original string
	 *         without any change
	 */
	public static String null2String(String string) {
		return string == null ? "" : string;
	}

	/**
	 * Converts <code>null</code> to empty string, otherwise returns it
	 * directly.
	 * 
	 * @param string
	 *            The nullable string
	 * @return empty string if passed in string is null, or original string
	 *         without any change
	 */
	public static String nvl(Object object, String value) {
		return object == null ? value : object.toString();
	}

	/**
	 * Replace blank
	 */
	public static String replaceBlank(String str) {
		if (ValidateUtils.isEmpty(str)) {
			return "";
		}
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(str);
		return m.replaceAll("");
	}

	/**
	 * trim
	 */
	public static String trim(String value) {
		return ValidateUtils.isEmpty(value) ? "" : value.replace(" ", "");
	}

	/**
	 * Encoding to UTF-8
	 */
	public static String enc2UTF_8(String str) {
		if (ValidateUtils.isEmpty(str)) {
			return "";
		}
		try {
			// return URLDecoder.decode(str, "UTF-8");
			return URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
			return str;
		}
	}

	public static void main(String[] args) {
		System.out.println(enc2UTF_8("%E4%B8%81%E5%B8%85"));
	}

	/**
	 * 替换字符串中{0},{1}...对应params数组中对应索引值
	 */
	public static String replaceBrace(String str, Object... params) {
		if (params == null || params.length == 0) {
			return str;
		}
		for (int i = 0, l = params.length; i < l; i++) {
			str = str.replaceAll("\\{" + i + "\\}", String.valueOf(params[i]));
		}
		return str;
	}

	/**
	 * XSS攻击
	 * 
	 * @param str
	 * @return
	 */
	public static String xss(String str) {
		return str.replaceAll("<", "&lt;");
	}

	public static String decodeUnicode(String theString) {

		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	
	/**
	 * 导入java工具包
	 * @param val
	 * @return
	 */
	public static boolean isNull(String val){
		if (val == null){
			return true;
		}
		return false;
	}
}
