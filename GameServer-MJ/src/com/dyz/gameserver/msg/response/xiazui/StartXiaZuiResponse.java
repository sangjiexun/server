package com.dyz.gameserver.msg.response.xiazui;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;

import java.io.IOException;

/**
 * 
 * @author luck
 *
 */
public class StartXiaZuiResponse extends ServerResponse {
    /**
     * 必须调用此方法设置消息号
     *
     * @param status
     * @param
     */
    public StartXiaZuiResponse(int status) {
        super(status, ConnectAPI.START_XIAZUI_RESPONSE);
        try {
            output.writeUTF("-");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
