package com.weipai.common;

import java.util.Properties;



public  abstract class Params {
	
	static Properties properties = AppCf.getProperties();
	//获取红包提现申请分页条数
	public static final  Integer pageNumber = Integer.valueOf(properties.get("pageNumber").toString());
	
}
