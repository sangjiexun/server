package com.weipai.common;

import java.util.HashMap;
import java.util.Map;

public class MoneyTest {
	final static  String url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	public static void main(String[] args) {
		String orderNNo =  MoneyUtils.getOrderNo() ; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nonce_str", MoneyUtils.buildRandom());//随机字符串
		map.put("mch_billno", orderNNo);//商户订单
		map.put("mch_id", "1246528101");//商户号
		map.put("wxappid", "wx647495375e5f5a2c");//商户appid
		map.put("nick_name", "哒哒乐科技");//提供方名称
		map.put("send_name", "哒哒乐科技");//用户名
		//根据用户在商家公众账号上的openid，获取用户在红包公众账号上的openid 
		map.put("re_openid", "-boF9Cvsv3-Hb4ASc");//用户在openid  从前段传入
		map.put("total_amount", 100);//付款金额(单位是分)
		map.put("min_value", 100);//最小红包
		map.put("max_value", 2000);//最大红包
		map.put("total_num", 1);//红包发送总人数
		map.put("wishing", "新年快乐");//红包祝福语
		map.put("client_ip", "192.168.0.120");//ip地址
		map.put("act_name", "过年红包");//活动名称
		map.put("remark", "新年新气象");//备注
		map.put("sign", MoneyUtils.createSign(map));//签名
		
		String result = "";
		try {
			System.out.println(MoneyUtils.createXML(map));
			result = MoneyUtils.doSendMoney(url, MoneyUtils.createXML(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:"+result);
	}
}
