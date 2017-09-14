package com.dyz.gameserver.msg.response.xiazui;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.XiaZuiVO;

import net.sf.json.JSONObject;

import java.io.IOException;

/**
 * 
 * @author luck
 *
 */
public class XiaZuiResponse extends ServerResponse {
    /**
     * 必须调用此方法设置消息号
     *
     * @param status
     * @param
     */
    public XiaZuiResponse(int status, XiaZuiVO xiazuiVO) {
        super(status, ConnectAPI.XIAZUI_RESPONSE);
        try {
            output.writeUTF(xiazuiVO.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
