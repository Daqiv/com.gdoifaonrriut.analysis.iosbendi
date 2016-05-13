package com.dianru.analysis.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Porject lando
 * @author JunWu.zhu
 * @date:Apr 17, 2014 3:16:47 PM
 * @version : 1.0
 * @email : icerivercomeon@gmail.com
 * @desciption :访问在当前类声明的private/protected成员变量及private/protected函数的BeanUtils.
 *             注意,因为必须为当前类声明的变量,通过继承获得的protected变量将不能访问, 需要转型到声明该变量的类才能访问.
 */
public class BeanUtils {

	/**
	 * 取得对象中自定义get方法名和值
	 * 
	 * @param obj
	 */
	static public StringBuffer getPropsInfo(Object obj) {
		StringBuffer info = new StringBuffer(obj + "{\n");
		if (null != obj) {
			Method[] methods = obj.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().startsWith("get")
						&& method.getParameterTypes().length == 0) {
					try {
						info.append(method.getName()).append(
								"=" + method.invoke(obj) + "\n");
					} catch (Exception e) {

					}
				}
			}
			info.append("}");
		}
		return info;
	}

	/**
	 * 对象反射成map
	 * 
	 * @param obj
	 */
	public static Map<String, Object> obj2Map(Object obj) {
		Class<?> clazz = obj.getClass();
		Map<String, Object> params = null;
		try {
			PropertyDescriptor[] pdArr = Introspector.getBeanInfo(clazz)
					.getPropertyDescriptors();
			if (null != pdArr) {
				params = new LinkedHashMap<String, Object>();
				Object mVal = null;
				for (PropertyDescriptor pd : pdArr) {
					if (!pd.getName().equals("class")
							&& (mVal = pd.getReadMethod().invoke(obj)) != null) {
						params.put(pd.getName(), mVal);
					}
				}
				mVal = null;
			}
			pdArr = null;
			clazz = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}
}
