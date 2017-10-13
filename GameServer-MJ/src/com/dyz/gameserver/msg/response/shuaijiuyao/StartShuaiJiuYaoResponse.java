package com.dyz.gameserver.msg.response.shuaijiuyao;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;

import java.io.IOException;

/**
 * 
 * @author wuislet
 *
 */
public class StartShuaiJiuYaoResponse extends ServerResponse {
    /**
     * 必须调用此方法设置消息号
     *
     * @param status
     * @param
     */
    public StartShuaiJiuYaoResponse(int status) {
        super(status, ConnectAPI.START_SHUAIJIUYAO_RESPONSE);
        try {
            output.writeUTF("-");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
