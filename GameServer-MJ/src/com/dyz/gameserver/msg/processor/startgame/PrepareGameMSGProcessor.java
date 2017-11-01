package com.dyz.gameserver.msg.processor.startgame;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.pojo.ReadyVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.gameserver.pojo.CardListVO;
import com.dyz.persist.util.JsonUtilTool;

/**
 *准备/开始游戏，当所有人都准备好后直接开始游戏
 * @author luck
 *
 */
public class PrepareGameMSGProcessor extends MsgProcessor implements
		INotAuthProcessor {

	public PrepareGameMSGProcessor() {
	}

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String str = request.getString();
		System.out.println("  wxd>>>  get pahse " + str);
        
        ReadyVO readyVO = JsonUtilTool.fromJson(str, ReadyVO.class);
		
		RoomVO roomVo = gameSession.getRole(Avatar.class).getRoomVO();
		if(roomVo != null ){
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVo.getRoomId());
			if(roomLogic != null){
				Avatar avatar = gameSession.getRole(Avatar.class);
				roomLogic.readyGame(avatar, readyVO);
			}else{
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
			}
		}
	}
}
