package com.weipai.service;

/**
 * 公用方法接口，各种操作记录接口
 * @author luck
 *
 */
public interface CommonService {
	/**
	 * 充卡/退卡操作记录
	 */
	 void roomCardRecord();
	
	/**
	 * 添加/删除代理记录
	 */
	 void operateProxyRecord();
}
