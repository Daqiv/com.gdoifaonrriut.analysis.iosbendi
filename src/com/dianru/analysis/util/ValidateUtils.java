package com.dianru.analysis.util;

import java.util.regex.Pattern;

/**
 * @Porject lando
 * @author JunWu.zhu
 * @date:Apr 17, 2014 3:16:47 PM
 * @version : 1.0
 * @email : icerivercomeon@gmail.com
 * @desciption : 验证工具
 */
public final class ValidateUtils {

	public static final String

	/** 邮箱正则 */
	REGX_EMAIL = "\\w+([-+.]\\w+)*@([a-z0-9A-Z]+)([-.]\\w+)*\\.\\w+([-.]\\w+)*",
			/** 带两位小数数字格式 */
			REGX_NUMBER = "[0-9]+(.[0-9]{1,}+)?",
			/** 整数格式 (不可输入0X开头格式:[0]|[1-9][0-9]+) */
			REGX_NUMBER_INT = "[-]?[0-9]+",
			/** 昵称正则 */
			REGX_NICKNAME = "^[a-zA-Z0-9\u0391-\uFFE5]{2,18}$";

	/**
	 * 验证是否为Null
	 * 
	 * @param object:为字符串则调用isEmpty
	 * @return
	 */
	public static boolean isNull(Object obj) {
		return obj instanceof String ? isEmpty(obj.toString()) : obj == null;
	}

	/**
	 * 验证是否Empty
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}

	/**
	 * 验证是否是Email. true 有效 false 无效
	 */
	public static boolean isEmail(final String str) {
		return isEmpty(str) || str.getBytes().length > 90 ? false : Pattern
				.matches(REGX_EMAIL, str);
	}

	/**
	 * 验证数字合法性，小数位不限制
	 * 
	 * @param numStr
	 * @return 验证通过返回 true
	 */
	public static boolean isNumber(Object str) {
		return isNull(str) ? false : Pattern.matches(REGX_NUMBER, str
				.toString());
	}

	/**
	 * 验证是否为整形数字
	 * 
	 * @return 验证通过返回 true
	 */
	public static boolean isNumber4Int(Object str) {
		return isNull(str) ? false : Pattern.matches(REGX_NUMBER_INT, str
				.toString());
	}

	/**
	 * 验证是否是昵称（昵称不能包含特殊字符，可以是汉字，数字和字母，字符在1-30个）
	 * 
	 * @param str
	 */
	public static boolean isNickname(String str) {
		return isEmpty(str) ? false : Pattern.matches(REGX_NICKNAME, str);
	}

}
