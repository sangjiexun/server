package com.dyz.gameserver.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.context.GameServerContext;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomNoice;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomResponse;
import com.dyz.gameserver.msg.response.login.BackLoginResponse;
import com.dyz.gameserver.msg.response.login.OtherBackLoginResonse;
import com.dyz.gameserver.msg.response.outroom.DissolveRoomResponse;
import com.dyz.gameserver.msg.response.outroom.OutRoomResponse;
import com.dyz.gameserver.msg.response.shuaijiuyao.ShuaiJiuYaoResponse;
import com.dyz.gameserver.msg.response.shuaijiuyao.StartShuaiJiuYaoResponse;
import com.dyz.gameserver.msg.response.startgame.PrepareGameResponse;
import com.dyz.gameserver.msg.response.startgame.StartGameResponse;
import com.dyz.gameserver.msg.response.xiazui.StartXiaZuiResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.ReadyVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.gameserver.pojo.CardListVO;
import com.dyz.gameserver.pojo.XiaZuiVO;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.services.AccountService;

/**
 * Created by kevin on 2016/6/18.
 * 房间逻辑
 */
public class RoomLogic {
    private List<Avatar> playerList;
    private  Avatar createAvator;
    private RoomVO roomVO;
    private PlayCardsLogic playCardsLogic;
    /**
     * //同意解散房间的人数
     */
    private int dissolveCount = 1;
    /**
     *记录是否已经有人申请解散房间
     */
    private boolean dissolve = true;
    /**
     * 是否已经解散房间
     */
    private  boolean hasDissolve = false; 
    /**
     *记录拒绝解散房间的人数，两个人及以上就不解散房间
     */
    private int refuse = 0 ;
    /**
     * 房间属性 1-为普通房间
     */
    private int roomType = 1;
    /**
     * 准备阶段
     */
    private int readyPhase = 0;
    /**
     * 最终的阶段号
     */
    private int finalPhase = 2;
  //战绩存取每一局的id
  	List<Integer> standingsDetailsIds = new ArrayList<Integer>();
    /**
     * 房间使用次数
     */
    private int count=0;
    public RoomLogic(RoomVO roomVO){
        this.roomVO = roomVO;
        if(roomVO != null){
        	count = roomVO.getRoundNumber();
        }
    }

    /**
     * 创建房间
     * @param avatar
     */
    public void CreateRoom(Avatar avatar){
        createAvator = avatar;
        roomVO.setPlayerList(new ArrayList<AvatarVO>());
        playerList = new ArrayList<Avatar>();
        avatar.avatarVO.setMain(true);
        avatar.setRoomVO(roomVO);
        playerList.add(avatar);
        roomVO.getPlayerList().add(avatar.avatarVO);
    }

    /**
     * 进入房间,
     * @param avatar
     */
    public boolean intoRoom(Avatar avatar){
    	synchronized(roomVO){
    		if(playerList.size() == 4){
    			try {
    				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000011));
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return false;
    		} else {
    			avatar.avatarVO.setMain(false);
    			avatar.avatarVO.setRoomId(roomVO.getRoomId());//房间号也放入avatarvo中
    			avatar.setRoomVO(roomVO);
    			noticJoinMess(avatar);//通知房间里面的其他几个玩家
    			playerList.add(avatar);
    			roomVO.getPlayerList().add(avatar.avatarVO);
    			RoomManager.getInstance().addUuidAndRoomId(avatar.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
    			avatar.getSession().sendMsg(new JoinRoomResponse(1, roomVO));
    			try {
    				Thread.sleep(500);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			return true;
    		}
    	}
    }
    /**
     * 当有人加入房间且总人数不够4个时，对其他玩家进行通知
     */
    private void noticJoinMess(Avatar avatar){
    	AvatarVO avatarVo = avatar.avatarVO;
    	for (int i = 0; i < playerList.size(); i++) {
            playerList.get(i).getSession().sendMsg(new JoinRoomNoice(1,avatarVo));
		}
    }

    /**
     * 退出房间
     * @param avatar
     */
    public void exitRoom(Avatar avatar){
        JSONObject json = new JSONObject();
//		accountName:”名字”//退出房间玩家的名字(为空则表示是通知的自己)
//		status_code:”0”//”0”退出成功，”1” 退出失败
//		mess：”消息”
//      type："0"  0退出房间    1解散房间
        json.put("accountName", avatar.avatarVO.getAccount().getNickname());
        json.put("status_code", "0");
        json.put("uuid", avatar.getUuId());
        
        if(avatar.avatarVO.isMain()){ //群主退出房间就是解散房间
        	json.put("type", "1");
        	exitRoomDetail(json);
        } else {
        	json.put("type", "0");
        	exitRoomDetail(avatar, json);
        }
    }

    /**
     * 申请解散房间
     */
    public void dissolveRoom(Avatar avatar , int roomId , String type){
    	//向其他几个玩家发送解散房间信息  
    	JSONObject json;
    	//为0时表示是申请解散房间，1表示同意解散房间  2表示不同意解散房间  3表示解散房间(大部分人同意解散房间)
    	//dissolveCount  = playerList.size();
    	if(type.equals("0")){
    		dissolve = false;
    		dissolveCount = 1;
    		json = new JSONObject();
    		json.put("type", "0");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//申请解散房间
    		if(playerList.size() == 1){
    			//如果只有房主一个人时，点申请解散,直接调用退出房间
    			 json = new JSONObject();
    			 json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		     json.put("status_code", "0");
    		     json.put("uuid", avatar.getUuId());
    		 	json.put("type", "1");
    			exitRoomDetail(json);
    		}else{
    			for (Avatar ava : playerList) {
    				ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    			}
    		}
    	}
    	else if(type.equals("2")){
    		json = new JSONObject();
    		json.put("type", "2");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//拒绝解散房间，向其他玩家发送消息
    		for (Avatar ava : playerList) {
    			ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    		}
    		refuse = refuse+1;
    		if(refuse == 2){
    			//重置申请状态， 
    			refuse = 0;
    			dissolve = true;
    			dissolveCount = 1;
    		}
    	}
    	else if(type.equals("1")){
    		//同意解散房间
    		dissolveCount = dissolveCount+1;
    		json = new JSONObject();
    		json.put("type", "1");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//同意解散房间，向其他玩家发送消息
    		for (Avatar ava : playerList) {
    			ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    		}
    		//下面是判断是否所有人都同意解散房间
    		int onlineCount = 0;
    		for (Avatar avat : playerList) {
    			if(avat.avatarVO.getIsOnLine()){
    				onlineCount++;
    			}
    		}
    		
    		if(onlineCount <= dissolveCount+1 && !hasDissolve ){
    			RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).count = 0;
    			hasDissolve = true;
    			//先结算信息，里面同时调用了解散房间的信息
    			playCardsLogic.settlementData("2");
    		}
    	}
    }
    /**
     * 玩家选择放弃操作
     * @param avatar
     * @param  //1-胡，2-杠，3-碰，4-吃
     */
    public void gaveUpAction(Avatar avatar){
        playCardsLogic.gaveUpAction(avatar);
    }

    /**
     * 出牌
     * @return
     */
    public void chuCard(Avatar avatar, int cardIndex){
        playCardsLogic.putOffCard(avatar,cardIndex);
    }

    /**
     * 摸牌
     */
    public void pickCard(){   	
        playCardsLogic.pickCard();
    }
    /**
     * 吃牌
     * @param avatar
     * @return
     */
    public boolean chiCard(Avatar avatar,CardVO cardVo){
    	return playCardsLogic.chiCard(avatar,cardVo);
    }
    /**
     * 碰牌
     * @param avatar
     * @return
     */
    public boolean pengCard(Avatar avatar,int cardIndex){
    	return playCardsLogic.pengCard( avatar, cardIndex);
    }
    /**
     * 杠牌
     * @param avatar
     * @return
     */
    public boolean gangCard(Avatar avatar,int cardPoint){
    	return playCardsLogic.gangCard(avatar, cardPoint);
    }
    /**
     * 胡牌
     * @param avatar
     * @return
     */
    public boolean huPai(Avatar avatar,int cardIndex,String type){
    	return playCardsLogic.huPai( avatar, cardIndex,type);
    	
    }

    /**
     * 检测是否可以开始游戏
     * @throws IOException 
     */
    private boolean CheckPhaseAllReady(int phase){
    	if(playerList.size() != 4){ //房间里面4个人且都准备好了则开始游戏
    		return false;
    	}
    	
		for (Avatar avatar : playerList) {
			if(!avatar.avatarVO.checkSingleIsReady(phase)){
				return false;
			}
		}
		return true;
    }
    
    private void EnterNextPhase() {
		startGameRound(readyPhase);
		readyPhase += 1;
    	System.out.println("    wxd>>>  EnterNextPhase" + readyPhase + " roomtype " + roomVO.getRoomType());
		if (readyPhase == 1) {
			if (roomVO.getXiazui() == 0 && roomVO.getRoomType() == 5) {//下嘴阶段
				for (Avatar ava: playerList) {
					ava.getSession().sendMsg(new StartXiaZuiResponse(1));
				}
			} else {//跳过下嘴阶段
		    	System.out.println("    wxd>>>  skip " + readyPhase);
				EnterNextPhase();
				return;
			}
		} else if (readyPhase == 2) {
			if (roomVO.getRoomType() == 7) {//甩九幺阶段
				for (Avatar ava: playerList) {
					ava.getSession().sendMsg(new StartShuaiJiuYaoResponse(1));
				}
			} else {//跳过甩九幺阶段
		    	System.out.println("    wxd>>>  skip " + readyPhase);
				EnterNextPhase();
				return;
			}
		}
		
		if(readyPhase > finalPhase) { //阶段结束后重置
	        readyPhase = 0; 
	        for(int i = 0;i < playerList.size(); i++){
	        	playerList.get(i).avatarVO.coverAllIsReady(false);
	        }
		}
    }
    /**
     * 游戏准备
     * @param avatar
     * @throws IOException 
     */
    public void readyGame(Avatar avatar, ReadyVO readyVO) throws IOException{
		if(count <= 0) { //房间次数已经为0 //正常不会出现这种情况
			for (Avatar ava: playerList) {
				ava.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000010));
			}
			return;
		}
		System.out.println("  check phase " + readyVO.getPhase() + "!=" + readyPhase);
		if(readyVO.getPhase() != readyPhase) { //接到错误的阶段准备包。
			return;
		}
		
		avatar.avatarVO.SingleIsReady(true, readyPhase);
		int avatarIndex = playerList.indexOf(avatar);
		//成功则返回
		for (Avatar ava : playerList) {
			ava.getSession().sendMsg(new PrepareGameResponse(1, avatarIndex, readyPhase));
		}
		System.out.println("   wxd>>>  readygame  p " + readyPhase +
				" ? " + CheckPhaseAllReady(readyPhase) + " vop? " + readyVO.getPhase());
		if(CheckPhaseAllReady(readyPhase)) {
			EnterNextPhase();
		}
    }
    /**
     * 开始一回合新的游戏
     * @param phase 1-全体准备，开始游戏 2-发牌前一刻
     */
    private void startGameRound(int phase){
    	System.out.println("    wxd>>>  start Game Round" + phase);
    	if(phase == 0) { //0-全体准备，开始游戏
	        assert(count > 0);
	        count--;
	        roomVO.setCurrentRound(roomVO.getCurrentRound() +1);
	        if((count +1) != roomVO.getRoundNumber()){
	    	    //说明不是第一局
	    	    Avatar avatar = playCardsLogic.bankerAvatar;
	    	    playCardsLogic = new PlayCardsLogic();
	    	    playCardsLogic.bankerAvatar = avatar;
	    	    //摸牌玩家索引初始值为庄家索引
	    	    playCardsLogic.setPickAvatarIndex(playerList.indexOf(avatar));
	        } else {
	    	    playCardsLogic = new PlayCardsLogic();
	    	    playCardsLogic.setPickAvatarIndex(0);
	        }
	        
	        playCardsLogic.setCreateRoomRoleId(createAvator.getUuId());
	        playCardsLogic.setPlayerList(playerList);
	    
    	} else if(phase == 1) { //1-发牌前一刻
	        playCardsLogic.initCard(roomVO);
	       
	        Account account;
	        for(int i = 0;i < playerList.size(); i++){
	    	    //清除各种数据  1：本局胡牌时返回信息组成对象 ，
	        	Avatar avatar = playerList.get(i);
	    	    avatar.avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
	            avatar.getSession().sendMsg(new StartGameResponse(1,avatar.getPaiArray(),playerList.indexOf(playCardsLogic.bankerAvatar),playCardsLogic.perGui,playCardsLogic.perTouzi));
	            //修改玩家是否玩一局游戏的状态
	            account = AccountService.getInstance().selectByPrimaryKey(avatar.avatarVO.getAccount().getId());
	            if(account.getIsGame().equals("0")){
	        	    account.setIsGame("1");
	        	    AccountService.getInstance().updateByPrimaryKeySelective(account);
	        	    avatar.avatarVO.getAccount().setIsGame("1");
	            }
	        }
    	} else if(phase == 2) {
    		
    	}
    }
    
    public void SetXiaZui(Avatar avatar, XiaZuiVO xiazuiVO) {
    	if(xiazuiVO.getXiazuiList() == 0) {
        	avatar.extraScoreCardIndexs = null;
    	} else {
	    	int[] zuiList = new int[9];
	    	for (int i = 0; i < 9; i++) {
	    		zuiList[i] = i * 3 + xiazuiVO.getXiazuiList() % 3;
	    	}
	    	avatar.extraScoreCardIndexs = zuiList;
    	}
    	avatar.extraScoreMultiple = xiazuiVO.getXiazuiMultiple();
    	
    	avatar.xiazuiVO = xiazuiVO;
    }
    
    public void SetShuaiJiuYao(Avatar avatar, CardListVO cardlistVO) {
    	System.out.println("  SetShuaiJiuYao  " + cardlistVO.getCardList().length + " from " + playerList.indexOf(avatar));
    	avatar.pullCardFormList(cardlistVO.getCardList());
    	cardlistVO.setAvatarIndex(playerList.indexOf(avatar));
		for (Avatar itemAva : playerList) {
			itemAva.getSession().sendMsg(new ShuaiJiuYaoResponse(1, cardlistVO));
		}
    }
    
    /**
     * 前后端握手消息处理
     * @param avatar
     */
    public void shakeHandsMsg(Avatar avatar){
    	playCardsLogic.shakeHandsMsg(avatar);
    }
    
    /**
     * 断线重连，如果房间还未被解散的时候，则返回整个房间信息
     * @param avatar
     */
    public void returnBackAction(Avatar avatar){
    	if(playCardsLogic == null){
        		//只是在房间，游戏尚未开始,打牌逻辑为空
        	for (int i = 0; i < playerList.size(); i++) {
        		if(playerList.get(i).getUuId() != avatar.getUuId()){
        			//给其他三个玩家返回重连用户信息
        			playerList.get(i).getSession().sendMsg(new OtherBackLoginResonse(1, avatar.getUuId()+""));
        		}
        	}
        	avatar.getSession().sendMsg(new BackLoginResponse(1, roomVO));
    	}
    	else{
    		playCardsLogic.returnBackAction(avatar);
    	}
    }
    

    public RoomVO getRoomVO() {
        return roomVO;
    }

	public List<Avatar> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<Avatar> playerList) {
		this.playerList = playerList;
	}

	public int getCount() {
		return count;
	}

	public boolean isDissolve() {
		return dissolve;
	}

	public void setDissolve(boolean dissolve) {
		this.dissolve = dissolve;
	}

	public void setDissolveCount(int dissolveCount) {
		this.dissolveCount = dissolveCount;
	}
	/**
	 * 断线重连返回最后操作信息
	 * @param avatar
	 */
	public void LoginReturnInfo(Avatar avatar){
		playCardsLogic.LoginReturnInfo(avatar);
	}

	
	/**
	 * 解散房间，销毁房间逻辑,打牌逻辑
	 */
	public void destoryRoomLogic(){
		AvatarVO avatarVO;
		GameSession gamesession;
		JSONObject json  = new JSONObject();
		json.put("type","3");
		for (Avatar avat : playerList) {
			avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			avatarVO.setIP(avat.avatarVO.getIP());
			avatarVO.setIsOnLine(avat.avatarVO.getIsOnLine());
			gamesession = avat.getSession();
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			avat.setSession(gamesession);
			if(avat.avatarVO.getIsOnLine()){
				gamesession.setLogin(true);
				avat.getSession().sendMsg(new DissolveRoomResponse(1, json.toString()));
				GameServerContext.add_onLine_Character(avat);
			} else{ //不在线则 更新
				GameServerContext.add_offLine_Character(avat);
			}
			try{
				RoomManager.getInstance().removeUuidAndRoomId(avat.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		hasDissolve = true;
		playCardsLogic = null;//9-22新增
		RoomManager.getInstance().destroyRoom(roomVO);
	}
	/**
	 * 房主退出房间，及解散房间，详细清除数据,销毁房间逻辑
	 * @param
	 */
	public void exitRoomDetail(JSONObject json){
		AvatarVO avatarVO;
		GameSession gamesession;
		for (Avatar avat : playerList) {
			avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			avatarVO.setIP(avat.avatarVO.getIP());
			avatarVO.setIsOnLine(avat.avatarVO.getIsOnLine());
			gamesession = avat.getSession();
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			avat.setSession(gamesession);
			if(avat.avatarVO.getIsOnLine()){
				gamesession.setLogin(true);
				avat.getSession().sendMsg(new OutRoomResponse(1, json.toString()));
				GameServerContext.add_onLine_Character(avat);
			}
			else{
			   //不在线则 更新
				GameServerContext.add_offLine_Character(avat);
			}
			RoomManager.getInstance().removeUuidAndRoomId(avat.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
		}
		hasDissolve = true;
		playCardsLogic = null;//9-22新增
		RoomManager.getInstance().destroyRoom(roomVO);
	}
	/**
	 * 房主外的玩家退出房间，详细清除单个数据
	 * @param avatar
	 */
	public void exitRoomDetail(Avatar avatar ,JSONObject json){
		
		for (int i= 0 ; i < playerList.size(); i++) {
    		//通知房间里面的其他玩家
			playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
    	}
		roomVO.getPlayerList().remove(avatar.avatarVO);
		playerList.remove(avatar);
		AvatarVO avatarVO;
		GameSession gamesession;
		avatarVO = new AvatarVO();
		avatarVO.setIP(avatar.avatarVO.getIP());
		avatarVO.setAccount(avatar.avatarVO.getAccount());
		gamesession = avatar.getSession();
		avatar = new Avatar();
		avatar.avatarVO = avatarVO;
		gamesession.setRole(avatar);
		gamesession.setLogin(true);
		avatar.setSession(gamesession);
		avatar.avatarVO.setIsOnLine(true);
		GameServerContext.add_onLine_Character(avatar);
		RoomManager.getInstance().removeUuidAndRoomId(avatar.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
		//
		
		
	}

	public List<Integer> getStandingsDetailsIds() {
		return standingsDetailsIds;
	}

	public void setStandingsDetailsIds(List<Integer> standingsDetailsIds) {
		this.standingsDetailsIds = standingsDetailsIds;
	}
	
	//**********作弊********************
	public List<Integer> getListCard()
	{
		return playCardsLogic.getListCard();
	}
	public int getListCardIndex()
	{
		return playCardsLogic.getListCardIndex();
	}
	public String setListCard(int index, int card)
	{
		return playCardsLogic.setListCard(index, card);
	}
}
