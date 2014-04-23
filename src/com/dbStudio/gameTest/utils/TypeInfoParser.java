package com.dbStudio.gameTest.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeInfoParser {
	static final Logger logger = Logger.getLogger(TypeInfoParser.class.getName());
	
	/**basic type class info*/
	static final Class<?>[] basicTypesArray = {
		byte.class,
		boolean.class,
		short.class,
		char.class,
		int.class,
		float.class,
		double.class,
		long.class
	};
	
	/**boxing type class info*/
	static final Class<?>[] boxingTypesArray = {
		Byte.class,
		Boolean.class,
		Short.class,
		Character.class,
		Integer.class,
		Float.class,
		Double.class,
		Long.class
	};
	
	/**常用类型*/
	static final Collection<Class<?>> commonTypes = new HashSet<Class<?>>();
	
	/**基本类型*/
	static final Collection<Class<?>> basicTypes = new HashSet<Class<?>>();
	
	/**装箱类型*/
	static final Collection<Class<?>> boxingTypes = new HashSet<Class<?>>();
	
	static {
		basicTypes.addAll(Arrays.asList(basicTypesArray));
		
		boxingTypes.addAll(Arrays.asList(boxingTypesArray));
		
		commonTypes.addAll(Arrays.asList(basicTypesArray));
		commonTypes.addAll(Arrays.asList(boxingTypesArray));	
		commonTypes.add(String.class);
		
		logger.setLevel(Level.INFO);
	}
	
	private static final void assertNotNullOrThrowException(Object obj) {
		if (obj == null)
			throw new NullPointerException("obj is null");
	}
	
	/***
	 * 判断是否是基本类型(byte、shot、boolean、char、 int、 float、double、long)
	 * @param obj
	 * @return 如果是基本类型，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isBasicType(Object obj) {
		assertNotNullOrThrowException(obj);
		
		Class<?> klass = obj.getClass();
		
		return basicTypes.contains(klass);
	}
	
	/***
	 * 判断是否是装箱类型(Byte、Shot、Boolean、Character、 Integer、 Float、Double、Long)
	 * @param obj
	 * @return 如果是装箱类型，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isBoxingType(Object obj) {
		assertNotNullOrThrowException(obj);
		
		Class<?> klass = obj.getClass();
		
		return boxingTypes.contains(klass);
	}
	
	/***
	 * 判断是否是常用类型(基本类型、装箱类型、String)
	 * @param obj
	 * @return 如果是常用类型，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isCommonType(Object obj) {
		assertNotNullOrThrowException(obj);
		
		Class<?> klass = obj.getClass();
		
		return commonTypes.contains(klass);
	}
	
	/***
	 * 添加常用类型(基本类型、装箱类型、String)
	 * @param obj
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static void addCommonType(Object obj) {
		assertNotNullOrThrowException(obj);
		
		Class<?> klass = obj.getClass();
		
		commonTypes.add(klass);
	}
	
	/***
	 * 获取常用类型数量
	 * @return 常用类型的数量
	 */
	public static int getCommonTypesCount() {
		return commonTypes.size();
	}
	
	/***
	 * 判断是否是byte数组
	 * @param obj
	 * @return 如果是byte数组，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isByte(Object obj) {
		assertNotNullOrThrowException(obj);
		
		return obj.getClass().getSimpleName().equalsIgnoreCase("byte");
	}
	
	/***
	 * 判断是否是byte数组
	 * @param obj
	 * @return 如果是byte数组，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isByteArray(Object obj) {
		assertNotNullOrThrowException(obj);
		
		return obj instanceof byte[];
	}
	
	/***
	 * 判断是否是数组
	 * @param obj
	 * @return 如果是数组，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isArray(Object obj) {
		assertNotNullOrThrowException(obj);
		
		Class<?> klass = obj.getClass();
		
		return klass.isArray();
	}
	
	/***
	 * 获取array长度
	 * @param array
	 * @return
	 */
	public static final int getArrayLength(Object array) {
		return Array.getLength(array);
	}
	
	/***
	 * 获取array里面的每一项，并添加到list
	 * @param array
	 * @return
	 */
	public static List<Object> getArraysItem(Object array) {
		if(!isArray(array))
			throw new IllegalArgumentException("param is not a array type");
		
		final int arrayLength = getArrayLength(array);
		
		if(arrayLength > 0) {
			List<Object> temp = new ArrayList<Object>();
			for (int i = 0; i < arrayLength; i++) {
				Object item = Array.get(array, i);
				
				temp.add(item != null? item : "null");
			}
			
			return temp;
		}
		else {
			return null;
		}
	}
	
	/***
	 * 判断是否是Map
	 * @param obj
	 * @return 如果是Map，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isMap(Object obj) {
		assertNotNullOrThrowException(obj);
		
		return obj instanceof Map;
	}
	
	/***
	 * 判断是否是List
	 * @param obj
	 * @return 如果是List，返回true， 否则返回false
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 */
	public static boolean isList(Object obj) {
		assertNotNullOrThrowException(obj);
		
		return obj instanceof List;
	}
	
	/***
	 * 获取obj的类名
	 * @param obj
	 * @return
	 */
	public final static String getObjectClassName(Object obj){
		return obj.getClass().getSimpleName();
	}
	
	/***
	 * 遍历Map，获取Map内元素的key和value
	 * @param obj
	 * @return Map内元素的key和value
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 * 			IllegalArgumentException 传递的参数不是Map或者Map的子类型，抛出该异常
	 */
	public static Map<Object, Object>  traversingMap(Object obj) {
		assertNotNullOrThrowException(obj);
		
		if (!isMap(obj)) {
			throw new IllegalArgumentException("obj is not a Map");
		}
		
		Map<Object, Object> map = new HashMap<Object, Object>();
		@SuppressWarnings("rawtypes")
		Map<?, ?> src = (Map) obj;
		
		for(Map.Entry<?, ?> entry : src.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		
		return map;
	}
	
	/***
	 * 遍历List，获取List内元素的字段名和字段值
	 * @param obj
	 * @return 由List内元素的字段名和字段值组成的Map
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 * 			IllegalArgumentException 传递的参数不是List或者List的子类型，抛出该异常
	 */
	public static Map<Object, Object>  traversingList(Object obj) {
		assertNotNullOrThrowException(obj);
		
		if (!isList(obj)) {
			throw new IllegalArgumentException("obj is not a List");
		}
		
		Map<Object, Object> map = new HashMap<Object, Object>();
		@SuppressWarnings("rawtypes")
		List src = (List) obj;
		
		for (int i = 0, length = src.size(); i < length; i++) {
			try {
				Map<Object, Object> element = getObjectFileds(src.get(i));
				
				map.putAll(element);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
				continue;
			}
		}
		
		return map;
	}
	
	/***
	 * 获取对象字段名和值
	 * @param obj
	 * @return 对象字段名和值组成的Map
	 * 			如果对象没有字段，则返回空的map
	 * 			如果传递的参数是常用类型，则返回空map0
	 * @Throws NullPointerException 传递参数为空，抛出该异常
	 * 			Exception 发生其它错误，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getObjectFileds(Object obj) throws Exception {
		assertNotNullOrThrowException(obj);
		
		if (isBasicType(obj) || isBoxingType(obj) || isCommonType(obj)) {
			return EmptyContainer.EMPTY_MAP;
		}
		
		final Map<Object, Object> filedMap = new HashMap<Object, Object>();
		
		Class<?> klass = obj.getClass();
		Field[] fields = klass.getDeclaredFields();
		
		if(fields.length > 0) {
			for (Field field : fields) {	
				field.setAccessible(true);
				
				Object value = field.get(obj);				
				
				filedMap.put(field.getName(), value != null? value : "null");
			}
			
			return filedMap;
		} 
		else {
			return EmptyContainer.EMPTY_MAP;
		}
		
	}
	
}
