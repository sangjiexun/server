package com.weipai.common;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;


public  abstract class AppCf {
	public static Integer startNum = 0;
	public static final Integer number = 1;//每次加载广告条数
		
	final static  String config_path = "config/total.properties";
	final static  Properties properties = new Properties();
	
	public static Properties getProperties() {
		 InputStream stream=Thread.currentThread().getContextClassLoader().getResourceAsStream(config_path);  
         try {
			properties.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties; 
	}
	public static void main(String[] args) throws IOException {
		Properties p  = getProperties();
		for (Entry<Object, Object> string : p.entrySet()) {
			System.out.println(string.getKey()+":");
			System.out.println(string.getValue());
		}
	}
}
