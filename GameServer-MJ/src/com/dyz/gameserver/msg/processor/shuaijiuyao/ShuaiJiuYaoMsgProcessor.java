package com.dyz.gameserver.msg.processor.shuaijiuyao;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.gameserver.pojo.CardListVO;
import com.dyz.persist.util.JsonUtilTool;

/**
 * 
 * @author wuislet
 *
 */
public class ShuaiJiuYaoMsgProcessor extends MsgProcessor implements
		INotAuthProcessor {

	public ShuaiJiuYaoMsgProcessor() {
	}

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String str = request.getString();
		CardListVO cardlistVO = JsonUtilTool.fromJson(str, CardListVO.class);
		RoomVO roomVo = gameSession.getRole(Avatar.class).getRoomVO();
		if(roomVo != null ){
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVo.getRoomId());
			if(roomLogic != null){
				Avatar avatar = gameSession.getRole(Avatar.class);
				roomLogic.SetShuaiJiuYao(avatar, cardlistVO);
			}else{
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000023));
			}
		}
	}
}
