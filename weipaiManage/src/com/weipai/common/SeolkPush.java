package com.weipai.common;

import java.util.ArrayList;
import java.util.List;

import com.gexin.rp.sdk.base.impl.ListMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;

/**
 * 个推类
 * @author luck
 *
 */
public class SeolkPush {
	// appId
	private String appId;
	// appKey
	private String appKey;
	// master
	private String master;
	// host
	private static String host = "http://sdk.open.api.igexin.com/apiex.htm";
	// 注册个推
	private IGtPush iGtpush;

	/**
	 * 无参构造，默认配置
	 */
	public SeolkPush() {

		this("YnaXHfkDSK6Hl6yPReUuZ2", "B6MLxsUIwh88GWoCq2GqX5",
				"vbrg9KFK556p28Zwf9GXL4");
		iGtpush = new IGtPush(host, appKey, master);
	}

	/**
	 * 带参构造，更改配置
	 * 
	 * @param appId
	 * @param appKey
	 */
	public SeolkPush(String appId, String appKey, String master) {

		this.appId = appId;
		this.appKey = appKey;
		this.master = master;
		iGtpush = new IGtPush(host, appKey, master);
	} 

	/**
	 * 
	 * @param title
	 * @param message
	 * @param clientID
	 */
	public void pushTo(String title, String text, String clientId) {

		// 创建消息体 单推消息
		SingleMessage singleMessage = getSingleMessage(title, text);
		// 获取推送对象
		Target target = getTarget(clientId);
		try {
			
			iGtpush.pushMessageToSingle(singleMessage, target);
		} catch (RequestException e) {
			
			e.printStackTrace();
			iGtpush.pushMessageToSingle(singleMessage, target, e.getRequestId());
		}
	}

	public void pushTo(String title, String text, String... clientIds) {

		ListMessage listMessage = getListMessage(title, text);

		List<Target> targets = new ArrayList<Target>();

		for (String clientId : clientIds) {

			Target target = getTarget(clientId);

			targets.add(target);
		}
		// 获取taskId
		String taskId = iGtpush.getContentId(listMessage);
		// 使用taskId对目标进行推送
		iGtpush.pushMessageToList(taskId, targets);
	}

	/**
	 * 消息推送模板
	 */
	private NotificationTemplate getTemplate(String title, String text) {

		NotificationTemplate template = new NotificationTemplate();

		// 设置APPID与APPKEY
		template.setAppId(appId);
		template.setAppkey(appKey);
		// 设置通知栏标题与内容
		template.setTitle(title);
		template.setText(text);
		// 设置通知是否响铃，震动，或者可清除
		template.setIsRing(true);
		template.setIsVibrate(true);
		template.setIsClearable(true);
		// 点击推送消息，立即启动app
		template.setTransmissionType(1);

		return template;
	}

	/**
	 * 获取消息体
	 * @param title
	 * @param text
	 * @return SingleMessage
	 */
	private SingleMessage getSingleMessage(String title, String text) {

		SingleMessage singleMessage = new SingleMessage();
		// 设置推送消息内容
		singleMessage.setData(getTemplate(title, text));
		// 离线有效时间，单位为毫秒，可选
		singleMessage.setOfflineExpireTime(24 * 3600 * 1000);

		return singleMessage;
	}
	
	/**
	 * 获取消息体
	 * @param title
	 * @param text
	 * @return ListMessage
	 */
	private ListMessage getListMessage(String title, String text) {

		ListMessage listMessage = new ListMessage();
		// 设置推送消息内容
		listMessage.setData(getTemplate(title, text));
		// 离线有效时间，单位为毫秒，可选
		listMessage.setOfflineExpireTime(24 * 3600 * 1000);

		return listMessage;
	}

	/**
	 * 获取推送对象
	 */
	private Target getTarget(String clientId) {

		Target target = new Target();
		// 设置客户端所属应用唯一ID
		target.setAppId(appId);
		// 设置客户端身份ID
		target.setClientId(clientId);

		return target;
	}
}