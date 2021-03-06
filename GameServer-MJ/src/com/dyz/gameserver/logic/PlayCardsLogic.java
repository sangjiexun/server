package com.dyz.gameserver.logic;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.context.Rule;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ResponseMsg;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.chupai.ChuPaiResponse;
import com.dyz.gameserver.msg.response.common.ReturnInfoResponse;
import com.dyz.gameserver.msg.response.common.ReturnOnLineResponse;
import com.dyz.gameserver.msg.response.followBanker.FollowBankerResponse;
import com.dyz.gameserver.msg.response.gang.GangResponse;
import com.dyz.gameserver.msg.response.gang.OtherGangResponse;
import com.dyz.gameserver.msg.response.hu.HuPaiAllResponse;
import com.dyz.gameserver.msg.response.hu.HuPaiResponse;
import com.dyz.gameserver.msg.response.login.BackLoginResponse;
import com.dyz.gameserver.msg.response.login.OtherBackLoginResonse;
import com.dyz.gameserver.msg.response.peng.PengResponse;
import com.dyz.gameserver.msg.response.pickcard.OtherPickCardResponse;
import com.dyz.gameserver.msg.response.pickcard.PickCardResponse;
import com.dyz.gameserver.msg.response.roomcard.RoomCardChangerResponse;
import com.dyz.gameserver.msg.response.xiazui.XiaZuiResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.FinalGameEndItemVo;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.PlayBehaviedVO;
import com.dyz.gameserver.pojo.PlayRecordGameVO;
import com.dyz.gameserver.pojo.PlayRecordItemVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.model.PlayRecord;
import com.dyz.myBatis.model.Standings;
import com.dyz.myBatis.model.StandingsAccountRelation;
import com.dyz.myBatis.model.StandingsDetail;
import com.dyz.myBatis.model.StandingsRelation;
import com.dyz.myBatis.services.AccountService;
import com.dyz.myBatis.services.PlayRecordService;
import com.dyz.myBatis.services.StandingsAccountRelationService;
import com.dyz.myBatis.services.StandingsDetailService;
import com.dyz.myBatis.services.StandingsRelationService;
import com.dyz.myBatis.services.StandingsService;
import com.dyz.persist.util.DateUtil;
import com.dyz.persist.util.HuPaiType;
import com.dyz.persist.util.Naizi;
import com.dyz.persist.util.Naizi_gd2;
import com.dyz.persist.util.NormalHuPai;
import com.dyz.persist.util.StringUtil;

/**
 * Created by kevin on 2016/6/18. 玩牌逻辑
 */
public class PlayCardsLogic {
	/**
	 * 当前出牌人的索引
	 */
	private int curAvatarIndex;
	/**
	 * 当前摸牌人的索引(初始值为庄家索引)
	 */
	private int pickAvatarIndex;
	/**
	 * 整张桌子上所有牌的数组
	 */
	private List<Integer> listCard = null;
	
	/**
	 * 本局鬼牌
	 */
	public int perGui = -1;
	/**
	 * 骰子点数
	 */
	public int perTouzi = -1;
	/**
	 * 有人要胡的數組
	 */
	private List<Avatar> huAvatar = new ArrayList<>();
	/**
	 * 有人要碰的數組
	 */
	private List<Avatar> penAvatar = new ArrayList<>();
	/**
	 * 有人要杠的數組
	 */
	private List<Avatar> gangAvatar = new ArrayList<>();
	/**
	 * 有人要咋吃的數組
	 */
	private List<Avatar> chiAvatar = new ArrayList<>();
	/**
	 * 起手胡
	 */
	private List<Avatar> qishouHuAvatar = new ArrayList<>();
	/**
	 * 存抓的码
	 */
	List<Integer> mas = new ArrayList<Integer>();
	/**
	 * 存抓的有效码
	 */
	List<Integer> validMa = new ArrayList<Integer>();
	/**
	 * 下张牌的索引
	 */
	private int nextCardindex = 0;
	/**
	 * 上一家出的牌的点数
	 */
	private int putOffCardPoint;
	/**
	 * 判断是否是抢胡
	 */
	private boolean qianghu = false;
	/**
	 * 当前玩家摸的牌的点数
	 */
	private int currentCardPoint = -2;
	/**
	 * 4家玩家信息集合
	 */
	private List<Avatar> playerList;
	/**
	 * 判断是否可以同时几个人胡牌
	 */
	private int huCount = 0;
	/**
	 * 庄家
	 */
	public Avatar bankerAvatar = null;
	/**
	 * 房间信息
	 */
	private RoomVO roomVO;
	/**
	 * 记录本次游戏是否已经胡了，控制摸牌
	 */
	private boolean hasHu;
	/**
	 * 记录某个玩家断线时最后一条消息
	 */
	// private ResponseMsg responseMsg;
	/**
	 * 记录某个玩家断线时发送最后一条消息的玩家
	 */
	// private Avatar lastAvtar;

	private NormalHuPai normalHuPai;
	/**
	 * String有胡家uuid:码牌1:码牌2 组成
	 */
	private String allMas;
	/**
	 * 控制胡牌返回次数
	 */
	int numb = 1;
	// 跟庄牌
	int followPoint = -1;
	// 是否跟庄
	boolean followBanke = true;
	// 跟庄的次数
	int followNumber = 0;
	// 记录抢杠胡 多响情况
	boolean hasPull = true;
	// 单局是否结束，判断能否调用准备接口 10-11新增
	boolean singleOver = false;

	// 游戏回放，
	PlayRecordGameVO playRecordGame;
	/**
	 * 和前段握手，判断是否丢包的情况，丢包则继续发送信息 Integer为用户uuid
	 */
	// private List<Integer> shakeHandsInfo = new ArrayList<Integer>();
	private Map<Integer, ResponseMsg> shakeHandsInfo = new HashMap<Integer, ResponseMsg>();

	public void setPickAvatarIndex(int pickAvatarIndex) {
		this.pickAvatarIndex = pickAvatarIndex;
	}

	public Map<Integer, ResponseMsg> getShakeHandsInf() {
		return shakeHandsInfo;
	}

	public void updateShakeHandsInfo(Integer uuid, ResponseMsg msg) {
		shakeHandsInfo.put(uuid, msg);
	}

	public String getAllMas() {
		return allMas;
	}

	public List<Avatar> getPlayerList() {
		return playerList;
	}

	/**
	 * 房主ID
	 */
	private int theOwner;

	public void setCreateRoomRoleId(int value) {
		theOwner = value;
	}

	public void setPlayerList(List<Avatar> playerList) {
		this.playerList = playerList;
	}

	public PlayCardsLogic() {
		normalHuPai = new NormalHuPai();
	}

	/**
	 * 初始化牌
	 */
	public void initCard(RoomVO value) {
		roomVO = value;
		listCard = new ArrayList<Integer>();
		
		for(int i = 0; i < 27; i++) {//标准牌。
			for(int num = 0; num < 4; num++) {
				listCard.add(i);
			}
		}

		int extraPai = 0;
		int rtype = roomVO.getRoomType();
		if (rtype == 1 && roomVO.getGui() == 3) { //红中麻将额外加红中。
			extraPai = 31;
		} else if (rtype == 4) {//广东麻将
			if(roomVO.isAddWordCard()) {
				extraPai = 100;
			} else if (roomVO.getGui() == 1) { //额外加白板鬼
				extraPai = 33;
			}
		} else if (rtype == 5 || rtype == 7) { //默认全套。亳州麻将、甩九幺
			extraPai = 100;
		} else if (rtype == 2 || rtype == 6 || rtype == 8) { //开关控制全套。划水、金昌、推倒胡
			if(roomVO.isAddWordCard()) {
				extraPai = 100;
			}
		} 

		int paiCount = 27;
		if(extraPai != 0) {
			paiCount = 34;
			if(extraPai == 100) { //加全套风牌、字牌
				for(int i = 27; i < 34; i++) {
					for(int num = 0; num < 4; num++) {
						listCard.add(i);
					}
				}
			} else {
				for(int num = 0; num < 4; num++) {
					listCard.add(extraPai);
				}
			}
		}

		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).avatarVO.setPaiArray(new int[2][paiCount]);
		}
		// 洗牌
		shuffleTheCards();
		// 发牌
		dealingTheCards(roomVO);
	}
	
	/**
	 * 产生鬼牌
	 */
	public void initGui(RoomVO value) {
		if(value.getRoomType() == 1){
			if(value.getGui() == 3)
				perGui = 31; //红中做鬼
		} else if(value.getRoomType() == 4){
			if(value.getGui() == 1)//白板做鬼
				perGui = 33;
			else if(value.getGui()==2){//翻鬼
				int ran = listCard.size() - nextCardindex + 1;
				int ran2 = (int) (Math.random() * ran);
				perGui = listCard.get(ran2);
				int r = ran2 % 13;
				int touzi1=1;
				int touzi2=1;
				if(r<3){
					touzi1=1;
					touzi2=1;
				}else if(r<7){
					int r2 = (int) (Math.random() * r);
					touzi1= r2<1 ? 1 : r2;
					touzi2 = r-touzi1;
				}else{
					int r2 = (int) (Math.random() * 6) + 1;
					touzi1= r2<r-6 ? 6 : r2;
					touzi2 = r-touzi1;
				}
				perTouzi = touzi1*10 + touzi2;
			}
		}
		
		roomVO.setGuiPai(perGui);
	}

	/**
	 * 随机洗牌
	 */
	public void shuffleTheCards() {
		Collections.shuffle(listCard);
		Collections.shuffle(listCard);
	}

	/**
	 * 检测玩家是否胡牌了
	 * 
	 * @param avatar
	 * @param cardIndex 点炮胡的牌编号。如果是100表示自摸的。
	 * @param type mo-自摸; chu-点炮; ganghu-杠上花
	 */
	public boolean checkAvatarIsHuPai(Avatar avatar, int cardIndex, String type) {
		System.out.println("   wxd>>> add&check   "+ cardIndex + " type " + type);
		
		boolean flag = false;
		if (type.equals("chu")) { //别人的点炮需要临时加到牌组里判断。
			avatar.putCardInList(cardIndex);
			flag = checkHu(avatar, cardIndex);
			avatar.pullCardFormList(cardIndex);
		} else {
			flag = checkHu(avatar, cardIndex);
		}
		
		if (flag && type.equals("ganghu")) { //杠上花
			if(roomVO.getRoomType() == 2) { //划水麻将杠上花 ，大胡
				avatar.avatarVO.setHuType(2);
			}
		}
		return flag;
	}

	/**
	 * 摸牌
	 */
	public void pickCard() {
		clearAvatar();
		// 摸牌
		pickAvatarIndex = getNextAvatarIndex();
		// pickAvatarIndex = nextIndex;
		// 本次摸得牌点数，下一张牌的点数，及本次摸的牌点数
		int tempPoint = getNextCardPoint();
		// System.out.println("摸牌："+tempPoint+"----上一家出牌"+putOffCardPoint+"--摸牌人索引:"+pickAvatarIndex);
		if (tempPoint != -1) {
			// 回放记录
			PlayRecordOperation(pickAvatarIndex, tempPoint, 2, -1, null, null);

			currentCardPoint = tempPoint;
			final Avatar avatar = playerList.get(pickAvatarIndex);
			avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
			avatar.qiangHu = true;
			avatar.canHu = true;
			avatar.avatarVO.setHuType(0);// 重置划水麻将胡牌格式
			// 记录摸牌信息
			// avatar.canHu = true;
			avatar.getSession().sendMsg(new PickCardResponse(1, tempPoint));
			for (int i = 0; i < playerList.size(); i++) {
				if (i != pickAvatarIndex) {
					playerList.get(i).getSession().sendMsg(
						new OtherPickCardResponse(1, pickAvatarIndex));
				} else {
					playerList.get(i).gangIndex.clear();// 每次摸牌就先清除缓存里面的可以杠的牌下标
				}
			}
			// 判断自己摸上来的牌自己是否可以胡
			StringBuffer sb = new StringBuffer();
			// 摸起来也要判断是否可以杠，胡
			avatar.putCardInList(tempPoint);
			if (avatar.checkSelfGang()) {
				gangAvatar.add(avatar);
				sb.append("gang");
				for (int i : avatar.gangIndex) {
					sb.append(":" + i);
				}
				sb.append(",");
				// avatar.gangIndex.clear();//9-18出牌了才清楚(在杠时断线重连后需要这里面的数据)
			}
			if (checkAvatarIsHuPai(avatar, 100, "mo")) {
				huAvatar.add(avatar);
				sb.append("hu,");
			}
			if (sb.length() > 2) {
				// System.out.println(sb);
				/*
				 * try { Thread.sleep(300); }catch (InterruptedException e) {
				 * e.printStackTrace(); }
				 */
				avatar.getSession().sendMsg(
						new ReturnInfoResponse(1, sb.toString()));
			}
			
			//出牌倒计时
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					System.out.println("----------- Time Up -----------");
			        //putOffCard(avatar, 1);
				}
			};
			
			System.out.println("----------- Start Count  -----------");
			Timer timer = new Timer();
			long delay = 3000;
			timer.schedule(task, delay);
			//TODO cd Start Counting

		} else {
			// System.out.println("流局");
			PlayRecordOperation(pickAvatarIndex, -1, 9, -1, null, null);
			// 流局处理，直接算分
			settlementData("1");
		}
	}

	/**
     *
     */
	/**
	 * 杠了别人(type)/自己摸杠了自后摸牌起来 然后再检测是否可以胡 可以杠等情况
	 * 
	 * @param avatar
	 */
	public void pickCardAfterGang(Avatar avatar) {

		// 本次摸得牌点数，下一张牌的点数，及本次摸的牌点数
		int tempPoint = getGangCardPoint();
		currentCardPoint = tempPoint;
		// System.out.println("摸牌!--"+tempPoint);
		if (tempPoint != -1) {
			// int avatarIndex = playerList.indexOf(avatar); // 2016-8-2注释
			pickAvatarIndex = playerList.indexOf(avatar);
			// Avatar avatar = playerList.get(pickAvatarIndex);
			// 记录摸牌信息
			for (int i = 0; i < playerList.size(); i++) {
				if (i != pickAvatarIndex) {
					playerList.get(i).getSession().sendMsg(
						new OtherPickCardResponse(1, pickAvatarIndex));
				} else {
					playerList.get(i).gangIndex.clear();// 每次出牌就先清除缓存里面的可以杠的牌下标
					playerList.get(i).getSession()
							.sendMsg(new PickCardResponse(1, tempPoint));
					// 摸牌之后就重置可否胡别人牌的标签
					playerList.get(i).canHu = true;
					// System.out.println("摸牌玩家------index"+pickAvatarIndex+"名字"+playerList.get(i).avatarVO.getAccount().getNickname());
				}
			}
			// 记录摸牌信息
			PlayRecordOperation(pickAvatarIndex, currentCardPoint, 2, -1, null,
					null);

			// 判断自己摸上来的牌自己是否可以胡
			StringBuffer sb = new StringBuffer();
			// 摸起来也要判断是否可以杠，胡
			avatar.putCardInList(tempPoint);
			if (avatar.checkSelfGang()) {
				gangAvatar.add(avatar);
				sb.append("gang");
				for (int i : avatar.gangIndex) {
					sb.append(":" + i);
				}
				sb.append(",");
				// avatar.gangIndex.clear();
			}
			if (checkAvatarIsHuPai(avatar, 100, "ganghu")) {
				// 检测完之后不需要移除
				huAvatar.add(avatar);
				sb.append("hu,");
			}
			if (sb.length() > 2) {
				// System.out.println(sb);
				avatar.getSession().sendMsg(
						new ReturnInfoResponse(1, sb.toString()));
			}

		} else {
			// 流局
			// system.out.println("流局");
			// 记录摸牌信息
			PlayRecordOperation(pickAvatarIndex, -1, 9, -1, null, null);
			settlementData("1");
		}
	}

	/**
	 * 获取下一位摸牌人的索引
	 * 
	 * @return
	 */
	public int getNextAvatarIndex() {
		int nextIndex = curAvatarIndex + 1;
		if (nextIndex >= 4) {
			nextIndex = 0;
		}
		return nextIndex;
	}

	/**
	 * 玩家选择放弃操作
	 * 
	 * @param avatar
	 * @param
	 *
	 */
	public void gaveUpAction(Avatar avatar) {
		// 放弃的时候，至少一个数组不为空才行
		if (validateStatus()) {
			avatar.huAvatarDetailInfo.clear();
			// System.out.println(JsonUtilTool.toJson(avatar.getRoomVO()));
			avatar.avatarVO.setHuType(0);// 重置划水麻将胡牌格式
			if (pickAvatarIndex == playerList.indexOf(avatar)) {
				// 如果是自己摸的过，则 canHu = true；
				avatar.canHu = true;
				// system.out.println("自己摸的过");
				clearAvatar();
			} else {
				// system.out.println("别人打的过");
				// 如果别人打的牌过，
				// 放弃胡，则检测有没人杠
				if (huAvatar.contains(avatar)) {
					huAvatar.remove(avatar);
					avatar.canHu = false;
					avatar.qiangHu = false;
				}
				if (gangAvatar.contains(avatar)) {
					gangAvatar.remove(avatar);
					avatar.gangIndex.clear();
				}
				if (penAvatar.contains(avatar)) {
					penAvatar.remove(avatar);
				}
				if (chiAvatar.contains(avatar)) {
					chiAvatar.remove(avatar);
				}
				if (huAvatar.size() == 0) {
					for (Avatar item : gangAvatar) {
						if (item.gangQuest) {
							avatar.qiangHu = false;
							// 进行这个玩家的杠操作，并且把后面的碰，吃数组置为0;
							gangCard(item, putOffCardPoint);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人gang");
							return;
						}
					}
					for (Avatar item : penAvatar) {
						if (item.pengQuest) {
							// 进行这个玩家的碰操作，并且把后面的吃数组置为0;
							pengCard(item, putOffCardPoint);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人pen");
							return;
						}
					}
					for (Avatar item : chiAvatar) {
						if (item.chiQuest) {
							// 进行这个玩家的吃操作
							CardVO cardVo = new CardVO();
							cardVo.setCardPoint(putOffCardPoint);
							chiCard(item, cardVo);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人吃");
							return;
						}
					}
				}
				// 如果都没有人胡，没有人杠，没有人碰，没有人吃的情况下。则下一玩家摸牌
				chuPaiCallBack();
			}
		}
	}

	/**
	 * 清理胡杠碰吃数组，并把玩家的请求状态全部设置为false;
	 */
	public void clearArrayAndSetQuest() {
		while (gangAvatar.size() > 0) {
			gangAvatar.remove(0).setQuestToFalse();
		}
		while (penAvatar.size() > 0) {
			penAvatar.remove(0).setQuestToFalse();
		}
		while (chiAvatar.size() > 0) {
			chiAvatar.remove(0).setQuestToFalse();
		}
	}

	private boolean checkFollow(int cardPoint) { //检查第一轮是否全出一样的牌。
		if(!followBanke) {
			return false;
		}
		
		if(followPoint == -1){
			followPoint = cardPoint;
			followNumber++;
		} else {
			if(followPoint != cardPoint) {
				followBanke = false;
				return false;
			} else {
				followNumber++;
			}
		}
		
		if(followNumber == 4) {
			followBanke = false;
			followPoint = -1;
		}
		
		return (followNumber == 4);
	}
	
	/**
	 * 出牌
	 * 
	 * @param avatar
	 * @param cardPoint
	 */
	public void putOffCard(Avatar avatar, int cardPoint) {
		System.out.println("   wxd>>>  put off card 11 " + cardPoint);
		boolean isFollow = checkFollow(cardPoint);
		if(isFollow) {
			System.out.println("被跟庄");
		}
		
		if (roomVO.getRoomType() == 2 && isFollow) {
			for (Avatar ava : playerList) {
				// 被跟庄 需要处理分数*********
				ava.getSession().sendMsg(new FollowBankerResponse());
				ava.getHuVO().updateTotalInfo("genzhuang", "1");
				
				int score = ava.avatarVO.isMain()?-3:1; //庄家-3，闲家+1
				ava.getHuVO().updateGangAndHuInfos("1", score);
			}
		}

		System.out.println("   wxd>>>  put off card 22 " + cardPoint);
		// avatar.gangIndex.clear();//每次出牌就清除 缓存里面的可以杠的牌下标
		// System.err.println("出牌："+cardPoint);
		avatar.avatarVO.setHuType(0);// 重置划水麻将胡牌格式
		// 出牌信息放入到缓存中，掉线重连的时候，返回房间信息需要
		avatar.avatarVO.updateChupais(cardPoint);
		avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
		// 已经出牌就清除所有的吃，碰，杠，胡的数组
		clearAvatar();

		putOffCardPoint = cardPoint;
		// system.out.println("出牌点数"+putOffCardPoint+"---出牌人索引:"+playerList.indexOf(avatar));
		curAvatarIndex = playerList.indexOf(avatar);
		PlayRecordOperation(curAvatarIndex, cardPoint, 1, -1, null, null);
		avatar.pullCardFormList(putOffCardPoint);
		for (int i = 0; i < playerList.size(); i++) {
			// 不能返回给自己
			if (i != curAvatarIndex) {
				playerList.get(i).getSession().sendMsg(
					new ChuPaiResponse(1, putOffCardPoint, curAvatarIndex));
				// system.out.println("发送打牌消息----"+playerList.get(i).avatarVO.getAccount().getNickname());
			} else {
				playerList.get(i).gangIndex.clear();// 每次出牌就先清除缓存里面的可以杠的牌下标
			}
		}
		System.out.println("   wxd>>>  start  play card  " + putOffCardPoint);
		// 房间为可点炮。房间为可抢杠胡并且有癞子时才检测其他玩家有没胡的情况。
		boolean canDianPao = (avatar.getRoomVO().getHuXianZhi() == 0 && avatar.getRoomVO().getGui() != 3 && avatar.getRoomVO().getRoomType()!=4);
		canDianPao = true; //TODEL
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < playerList.size(); i++) {
			sb.setLength(0);//sb.clear();
			Avatar ava = playerList.get(i);
			if (ava.getUuId() != avatar.getUuId()) {
				if(canDianPao)
				{
					if (ava.canHu && checkAvatarIsHuPai(ava, putOffCardPoint, "chu")) {
						// 胡牌状态为可胡的状态时才行
						huAvatar.add(ava);
						sb.append("hu,");
					}
				}
				
				if (ava.checkGang(putOffCardPoint)) {
					gangAvatar.add(ava);
					// 同时传会杠的牌的点数
					sb.append("gang:" + putOffCardPoint + ",");
				}
				System.out.println("   wxd>>>   check peng" + ava.checkPeng(putOffCardPoint));
				if (ava.checkPeng(putOffCardPoint)) {
					penAvatar.add(ava);
					sb.append("peng:" + curAvatarIndex + ":"
							+ putOffCardPoint + ",");
				}
				if (ava.checkChi(putOffCardPoint) && getNextAvatarIndex() == i){
					chiAvatar.add(ava);
					sb.append("chi");
				}
				if (sb.length() > 1) {
					System.out.println("   wxd>>>    send msg1   " + sb);
					ava.getSession().sendMsg(new ReturnInfoResponse(
							1, sb.toString()));
				}
			}
		}
		// 如果没有吃，碰，杠，胡的情况，则下家自动摸牌
		chuPaiCallBack();
	}

	/**
	 * 吃牌
	 * 
	 * @param avatar
	 * @param
	 * @return
	 */
	public boolean chiCard(Avatar avatar, CardVO cardVo) {
		// 碰，杠都比吃优先
		boolean flag = false;
		// int avatarIndex = playerList.indexOf(avatar);
		if (huAvatar.size() == 0 && penAvatar.size() == 0
				&& gangAvatar.size() == 0 && chiAvatar.size() > 0) {
			if (chiAvatar.contains(avatar)) {
				// 回放记录
				// PlayRecordOperation(playerList.indexOf(avatar),cardVo.getCardPoint(),3,-1);
				// 更新牌组
				avatar.putCardInList(cardVo.getCardPoint());
				avatar.setCardListStatus(cardVo.getCardPoint(), 4);
				clearArrayAndSetQuest();
				flag = true;
				// *****吃牌后面弄，需要修改传入的参数 CardVO
				// String str = "";.getClass()
				// avatar.getHuVO().updateTotalInfo("chi", str);
				// 标记吃了的牌的下标//碰 1 杠2 胡3 吃4
				// avatar.setCardListStatus(cardVo.getCardPoint(), 4);
				// avatar.setCardListStatus(cardVo.getCardPoint(), 4);
				// avatar.setCardListStatus(cardVo.getCardPoint(), 4);
				
				// curAvatarIndex = avatarIndex;// 2016-8-1注释掉
				// 更新用户的正常牌组(不算上碰，杠，胡，吃)吃牌这里还需要修改****
				// playerList.get(avatarIndex).avatarVO.updateCurrentCardList(cardVo.getCardPoint());
			}
		} else {
			if (chiAvatar.size() > 0) {
				for (Avatar ava : chiAvatar) {
					ava.chiQuest = true;
					ava.cardVO = cardVo;// 存储前段发送过来的吃对象
				}
			}
		}
		return flag;
	}

	/**
	 * 碰牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean pengCard(Avatar avatar, int cardIndex) {
		boolean flag = false;
		// 这里可能是自己能胡能碰能杠 但是选择碰
		if (cardIndex != putOffCardPoint) {
			System.out.println("传入错误的牌:传入的牌" + cardIndex + "---上一把出牌："
					+ putOffCardPoint);
			return false;
		}
		if (followBanke) {
			followBanke = false;
		}
		if (cardIndex < 0) {
			try {
				avatar.getSession().sendMsg(
						new ErrorResponse(ErrorCode.Error_000019));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		if ((penAvatar.size() >= 1 && huAvatar.size() == 0)
				|| (huAvatar.contains(avatar) && huAvatar.size() == 1 && penAvatar
						.size() == 1)) {
			avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
			if (huAvatar.contains(avatar)) {
				huAvatar.remove(avatar);
			}
			if (gangAvatar.contains(avatar)) {
				gangAvatar.remove(avatar);
			}
			if (penAvatar.contains(avatar)) {
				// 回放记录
				PlayRecordOperation(playerList.indexOf(avatar), cardIndex, 4,
						-1, null, null);
				// 把出的牌从出牌玩家的chupais中移除掉
				playerList.get(curAvatarIndex).avatarVO.removeLastChupais();
				penAvatar.remove(avatar);
				// 更新牌组
				flag = avatar.putCardInList(cardIndex);
				avatar.setCardListStatus(cardIndex, 1);

				// 把各个玩家碰的牌记录到缓存中去,牌的index
				avatar.getHuVO().updateTotalInfo("peng", cardIndex + "");
				// avatar.getResultRelation().put(key, value);
				clearArrayAndSetQuest();
				for (int i = 0; i < playerList.size(); i++) {
					if (playerList.get(i).getUuId() == avatar.getUuId()) {
						// 碰了的牌放入到avatar的resultRelation Map中
						avatar.putResultRelation(1, cardIndex + "");
						avatar.setCardListStatus(cardIndex, 1);
					}
					playerList.get(i).getSession().sendMsg(
						new PengResponse(1, cardIndex, playerList.indexOf(avatar)));
				}
				// responseMsg = new
				// PengResponse(1,cardIndex,playerList.indexOf(avatar));
				// lastAvtar = avatar;
				// 更新摸牌人信息 2016-8-3
				pickAvatarIndex = playerList.indexOf(avatar);
				curAvatarIndex = playerList.indexOf(avatar);
				currentCardPoint = -2;// 断线重连判断该自己出牌
			}
		} else {
			if (penAvatar.size() > 0) {
				for (Avatar ava : penAvatar) {
					ava.pengQuest = true;
				}
			}
		}
		return flag;
	}

	/**
	 * 杠牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean gangCard(Avatar avatar, int cardPoint) {
		boolean flag = false;
		if (followBanke) {
			followBanke = false;
		}
		int avatarIndex = playerList.indexOf(avatar);
		
		if (gangAvatar.size() > 0) {
			if ((huAvatar.contains(avatar) && huAvatar.size() == 1)
					|| huAvatar.size() == 0) {
				avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
				if (huAvatar.contains(avatar)) {
					huAvatar.remove(avatar);
				}
				if (penAvatar.contains(avatar)) {
					penAvatar.remove(avatar);
				}
				if (chiAvatar.contains(avatar)) {
					chiAvatar.remove(avatar);
				}
				if (gangAvatar.contains(avatar)) {
					gangAvatar.remove(avatar);
					String str = "";
					int type = -1; //判断杠的类型，0-明杠; 1-暗杠
					int score = 0; //杠牌分数(转转麻将) 杠牌番数(划水麻将)
					String endStatisticstype = "";
					int playRecordType;// 游戏回放 记录杠的类型
					if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {
						// 自杠(明杠或暗杠)，这里的明杠时需要判断本房间是否是抢杠胡的情况
						String strs = avatar.getResultRelation().get(1);
						if (strs != null && strs.contains(cardPoint + "")) { //碰牌自摸明杠（划水麻将里面的过路杠）
							// 抢杠提前退出
							if ((avatar.getRoomVO().getRoomType() != 4 && avatar.getRoomVO().getHuXianZhi() == 0 && checkQiangHu(avatar, cardPoint))
									|| (avatar.getRoomVO().getRoomType() == 4 && avatar.getRoomVO().getHuXianZhi() == 2 && checkQiangHu(avatar, cardPoint))
									) {
								// 如果是抢杠胡，则判断其他玩家有胡牌的情况，有则给予提示 //判断其他三家是否能抢杠胡。
								// 如果抢胡了，则更新上家出牌的点数为 杠的牌
								putOffCardPoint = cardPoint;
								gangAvatar.add(avatar);
								avatar.gangQuest = true;
								// 回放记录
								PlayRecordOperation(avatarIndex, cardPoint, 5,
										4, null, null);
								return false;
							}

							playRecordType = 3;
							str = "0:" + cardPoint + ":" + Rule.Gang_ming;
							type = 0;
							score = 1;
							endStatisticstype = "minggang";
						} else {
							playRecordType = 2;
							str = "0:" + cardPoint + ":" + Rule.Gang_an;
							type = 1;
							score = 2;
							endStatisticstype = "angang";
						}
						avatar.putResultRelation(2, cardPoint + "");
						avatar.setCardListStatus(cardPoint, 2);// 杠牌标记2

						// 分数变动
						String record = type == 0?"5":"4";
						avatar.getHuVO().updateGangAndHuInfos(record, score * 3);
						for (Avatar ava : playerList) {
							if (ava.getUuId() != avatar.getUuId()) {
								ava.getHuVO().updateGangAndHuInfos(record, -1 * score);
							}
						}
						flag = true;
					} else { // 点杠(分在明杠里面)
						playRecordType = 1;
						str = playerList.get(curAvatarIndex).getUuId()
								+ ":" + cardPoint + ":" + Rule.Gang_dian;
						type = 0;
						score = 3;
						endStatisticstype = "minggang";
						if (roomVO.getRoomType() == 2) {// 划水麻将里叫放杠
							endStatisticstype = "fanggang";
						}

						avatar.putResultRelation(2, cardPoint + "");
						avatar.setCardListStatus(cardPoint, 2);// 杠牌标记2
						
						// 把出的牌从出牌玩家的chupais中移除掉
						playerList.get(curAvatarIndex).
							avatarVO.removeLastChupais();

						// 更新牌组(点杠时才需要更新) 自摸时不需要更新
						flag = avatar.putCardInList(cardPoint);

						// 分数变动
						playerList.get(curAvatarIndex).getHuVO()
							.updateGangAndHuInfos("5", -1 * score);
						avatar.getHuVO().updateGangAndHuInfos("5", score);
					}
					
					// 整个房间统计每一局游戏 杠，胡的总次数
					roomVO.updateEndStatistics(avatar.getUuId() + "",
							endStatisticstype, 1);

					avatar.getHuVO().updateTotalInfo("gang", str);
					// 回放记录
					PlayRecordOperation(avatarIndex, cardPoint, 5,
							playRecordType, null, null);

					clearArrayAndSetQuest();
					// 可以换牌的情况只补一张牌
					// 摸牌并判断自己摸上来的牌自己是否可以胡/可以杠****
					for (int i = 0; i < playerList.size(); i++) {
						if (avatar.getUuId() != playerList.get(i).getUuId()) {
							// 杠牌返回给其他人只返回杠的类型和杠牌的玩家位置
							playerList.get(i).getSession().sendMsg(
								new OtherGangResponse(1, cardPoint, avatarIndex, type));
						} else {
							playerList.get(i).getSession().sendMsg(
								new GangResponse(1, 1, 1, type));
						}
					}
					pickCardAfterGang(avatar);
				}
			} else {
				for (Avatar ava : gangAvatar) {
					ava.gangQuest = true;
				}
			}
		} else {
			try {
				playerList.get(avatarIndex).getSession()
						.sendMsg(new ErrorResponse(ErrorCode.Error_000016));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return flag;
	}

	/**
	 * 胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean huPai(Avatar avatar, int cardIndex, String type) {
		boolean flag = false;
		// 胡牌就清除掉存的其他人所有的可以碰 杠 吃的信息
		if (huCount == 0) {
			huCount = huAvatar.size();
		}
		StringBuffer sb = new StringBuffer();
		avatar.setCardListStatus(cardIndex, 3);
		
		for (Avatar itemAva : playerList) {
			itemAva.getSession().sendMsg(new XiaZuiResponse(1, avatar.xiazuiVO));
		}
		
		int playRecordType = 6;// 胡牌的分类
		int maCount = avatar.getRoomVO().getMa();
		if (maCount >= 1 && !(huCount > 1 && !StringUtil.isEmpty(allMas))) { //需要抓码 && 防止多响的重复抓码
			if (huCount == 1) { //单响 胡家抓码
				if (roomVO.getGui() == 3 && avatar.getPaiArray()[0][31] == 0) { //当单响胡家手上没有红中，则多抓一个码
					maCount++;
				}
				sb.append(avatar.getUuId());
			} else { //多响 点炮玩家抓码 出牌人的索引 curAvatarIndex
				sb.append(playerList.get(pickAvatarIndex).getUuId());
			}
			
			//抓码
			int ma;
			for (int i = 0; i < maCount; i++) {
				ma = getNextCardPoint();
				if(ma == -1)  break;
				
				mas.add(ma);
				sb.append(":" + ma);
			}
			allMas = sb.toString();
		}
		if (huAvatar.size() > 0) {
			if (huAvatar.contains(avatar)) {
				// if(playerList.get(pickAvatarIndex).getUuId() !=
				// avatar.getUuId()){
				if (StringUtil.isNotEmpty(type) && type.equals("qianghu")) {
					// 是抢胡，则各更新 出牌人等于摸牌人 , 然后冲被抢玩家的牌里移除此牌
					playRecordType = 7;
					if (hasPull) {// 两个人都抢胡，只能移除一次牌
						curAvatarIndex = pickAvatarIndex;
						playerList.get(curAvatarIndex).pullCardFormList(
								cardIndex);// 2016-8-9 22:38
						hasPull = false;
					}
				}
				if (pickAvatarIndex == curAvatarIndex) {
					// 把胡了的牌索引放入到对应赢家的牌组中
					avatar.putCardInList(cardIndex);
					// system.out.println("点炮");
					// 当摸牌人的索引等于出牌人的索引时，表示点炮了
					// 点炮 别人点炮的时候查看是否可以胡
					if (avatar.canHu) {
						// 胡牌数组中移除掉胡了的人
						huAvatar.remove(avatar);
						gangAvatar.clear();
						penAvatar.clear();
						chiAvatar.clear();
						;
						// 两个人之间建立关联，游戏结束算账用
						if (!validMa.isEmpty()) {
							List<Integer> newValidMa = HuPaiType.getInstance().getHuType(
										playerList, avatar, playerList.get(curAvatarIndex),
										roomVO.getRoomType(), cardIndex, mas, huCount, type, 
										roomVO.getGui() == 3, bankerAvatar);
							for (Integer j : newValidMa) {
								validMa.add(j);
							}
						} else {
							validMa = HuPaiType.getInstance().getHuType(
									playerList, avatar, playerList.get(curAvatarIndex), 
									roomVO.getRoomType(), cardIndex, mas, huCount, type,
									roomVO.getGui() == 3, bankerAvatar);
						}
						// 整个房间统计每一局游戏 杠，胡的总次数
						roomVO.updateEndStatistics(avatar.getUuId() + "",
								"jiepao", 1);
						roomVO.updateEndStatistics(
								playerList.get(curAvatarIndex).getUuId() + "",
								"dianpao", 1);
						flag = true;
					} else {
						// system.out.println("放过一个人就要等自己摸牌之后才能胡其他人的牌");
						huAvatar.remove(avatar);
					}
				} else {
					// 自摸,
					// system.out.println("自摸");
					// 胡牌数组中移除掉胡了的人
					huAvatar.remove(avatar);
					gangAvatar.clear();
					penAvatar.clear();
					chiAvatar.clear();
					;
					// 两个人之间建立关联，游戏结束算账用 自摸不会出现抢胡的情况
					if (!validMa.isEmpty()) {
						List<Integer> newValidMa = HuPaiType.getInstance().getHuType(
									playerList, avatar, playerList.get(curAvatarIndex),
									roomVO.getRoomType(), cardIndex, mas, huCount, type, 
									roomVO.getGui() == 3, bankerAvatar);
						for (Integer j : newValidMa) {
							validMa.add(j);
						}
					} else {
						validMa = HuPaiType.getInstance().getHuType(
								playerList, avatar, avatar, roomVO.getRoomType(), cardIndex,
								mas, huCount, "", roomVO.getGui() == 3, bankerAvatar);
					}
					roomVO.updateEndStatistics(avatar.getUuId() + "", "zimo", 1);
					flag = true;
				}
				// 本次游戏已经胡，不进行摸牌
				hasHu = true;
				// 游戏回放
				PlayRecordOperation(playerList.indexOf(avatar), cardIndex,
						playRecordType, -1, null, null);
			}
		}
		if (huAvatar.size() == 0 && numb == 1) {
			numb++;
			// 所有人胡完
			if (huCount >= 2) {
				// 重新分配庄家，下一局点炮的玩家坐庄
				for (Avatar itemAva : playerList) {
					if (playerList.get(pickAvatarIndex).getUuId() == itemAva.getUuId()) {
						bankerAvatar = itemAva;
						itemAva.avatarVO.setMain(true);
					} else {
						itemAva.avatarVO.setMain(false);
					}
				}
			} else {
				// 重新分配庄家，下一局胡家坐庄
				for (Avatar itemAva : playerList) {
					if (avatar.getUuId() == itemAva.getUuId()) {
						bankerAvatar = itemAva;
						itemAva.avatarVO.setMain(true);
					} else {
						itemAva.avatarVO.setMain(false);
					}
				}
			}
			// 更新roomlogic的PlayerList信息
			RoomManager.getInstance()
					.getRoom(playerList.get(0).getRoomVO().getRoomId())
					.setPlayerList(playerList);
			// 一局牌胡了，返回这一局的所有数据吃，碰， 杠，胡，分数、码等信息
			settlementData("0");
		}
		return flag;
	}

	/**
	 * 胡牌/流局/解散房间后返回结算数据信息 不能多次调用，多次调用，总分会多增加出最近一局的分数 第一局结束扣房卡
	 */
	public void settlementData(String type) {

		int totalCount = roomVO.getRoundNumber();
		int useCount = RoomManager.getInstance().getRoom(roomVO.getRoomId())
				.getCount();
		if (totalCount == (useCount + 1) && !type.equals("2")) {
			// 第一局结束扣房卡
			deductRoomCard();
		}
		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		if (!type.equals("0")) {
			allMas = null;
		}
		StandingsDetail standingsDetail = new StandingsDetail();
		StringBuffer content = new StringBuffer();
		StringBuffer score = new StringBuffer();
		for (Avatar avatar : playerList) {
			// 生成战绩内容
			content.append(avatar.avatarVO.getAccount().getNickname() + ":"
					+ avatar.getHuVO().getTotalScore() + ",");

			// 统计本局分数
			avatar.getHuVO().setNickname(avatar.avatarVO.getAccount()
					.getNickname());
			avatar.getHuVO().setPaiArray(avatar.getPaiArray()[0]);
			avatar.getHuVO().setUuid(avatar.getUuId());
			array.add(avatar.getHuVO());
			// 在整个房间信息中修改总分数(房间次数用完之后的总分数)
			roomVO.updateEndStatistics(avatar.getUuId() + "", "scores",
					avatar.getHuVO().getTotalScore());
			score.append(avatar.getUuId()
					+ ":"
					+ roomVO.getEndStatistics().get(avatar.getUuId() + "")
							.get("scores") + ",");
			// 修改存储的分数(断线重连需要)
			avatar.avatarVO.supdateScores(avatar.getHuVO().getTotalScore());
			// 游戏回放 中码消息
			if (avatar.avatarVO.isMain()) {
				if (!type.equals("0")) {
					PlayRecordOperation(playerList.indexOf(avatar), -1, 8, -1,
							null, null);
				} else {
					PlayRecordOperation(playerList.indexOf(avatar), -1, 8, -1,
							allMas, HuPaiType.getInstance().getValidMa());
				}
			}
		}
		json.put("avatarList", array);
		json.put("allMas", allMas);
		json.put("type", type);
		if (!type.equals("0")) {
			json.put("validMas", new ArrayList<>());
		} else {
			json.put("validMas", validMa);
		}
		json.put("currentScore", score.toString());
		// 生成战绩content
		standingsDetail.setContent(content.toString());
		try {
			standingsDetail.setCreatetime(DateUtil.toChangeDate(new Date(),
					DateUtil.maskC));
			int id = StandingsDetailService.getInstance().saveSelective(
					standingsDetail);
			if (id > 0) {
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(
						roomVO.getRoomId());
				roomLogic.getStandingsDetailsIds().add(standingsDetail.getId());
				// 更新游戏回放中的玩家分数
				PlayRecordInitUpdateScore(standingsDetail.getId());
			} else {
				System.out.println("分局战绩录入失败：" + new Date());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		int count = 10;
		for (Avatar avatar : playerList) {
			// 发送消息
			avatar.getSession().sendMsg(new HuPaiResponse(1, json.toString()));

			avatar.overOff = true;
			avatar.oneSettlementInfo = json.toString();

			// 清除一些存储数据
			avatar.getResultRelation().clear();
			avatar.avatarVO.getChupais().clear();
			avatar.avatarVO.setCommonCards(0);
			avatar.avatarVO.setHasMopaiChupai(false);
			// 清除 hu ReturnObjectVO 信息
			avatar.avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());

			count = RoomManager.getInstance()
					.getRoom(avatar.getRoomVO().getRoomId()).getCount();
			// 没有经过算分 不能开始下一局游戏
			singleOver = true;// 10-11新增
		}
		// 房间局数用完，返回本局胡牌信息的同时返回整个房间这几局的胡，杠等统计信息
		if (count <= 0) {
			// 总房间战绩
			Standings standings = new Standings();
			StringBuffer sb = new StringBuffer();
			// standings.setContent(content);
			Map<String, Map<String, Integer>> endStatistics = roomVO
					.getEndStatistics();
			Map<String, Integer> map = new HashMap<String, Integer>();
			Set<Entry<String, Map<String, Integer>>> set = endStatistics
					.entrySet();
			JSONObject js = new JSONObject();
			List<FinalGameEndItemVo> list = new ArrayList<FinalGameEndItemVo>();
			FinalGameEndItemVo obj;
			for (Entry<String, Map<String, Integer>> param : set) {
				obj = new FinalGameEndItemVo();
				obj.setUuid(Integer.parseInt(param.getKey()));
				sb.append(AccountService.getInstance()
						.selectByUUid(Integer.parseInt(param.getKey()))
						.getNickname());
				map = param.getValue();
				for (Entry<String, Integer> entry : map.entrySet()) {
					switch (entry.getKey()) {
					case "zimo":
						obj.setZimo(entry.getValue());
						break;
					case "jiepao":
						obj.setJiepao(entry.getValue());
						break;
					case "dianpao":
						obj.setDianpao(entry.getValue());
						break;
					case "minggang":
						obj.setMinggang(entry.getValue());
						break;
					case "angang":
						obj.setAngang(entry.getValue());
						break;
					case "scores":
						obj.setScores(entry.getValue());
						sb.append(":" + entry.getValue() + ",");
						break;
					default:
						break;
					}
				}
				list.add(obj);
			}
			js.put("totalInfo", list);
			js.put("theowner", theOwner);
			// system.out.println("这个房间次数用完：返回数据=="+js.toJSONString());
			// 战绩记录存储
			standings.setContent(sb.toString());
			try {
				standings.setCreatetime(DateUtil.toChangeDate(new Date(),
						DateUtil.maskC));
				standings.setRoomid(roomVO.getId());
				int i = StandingsService.getInstance().saveSelective(standings);
				if (i > 0) {
					// 存储 房间战绩和每局战绩关联信息
					StandingsRelation standingsRelation;
					List<Integer> standingsDetailsIds = RoomManager
							.getInstance().getRoom(roomVO.getRoomId())
							.getStandingsDetailsIds();
					for (Integer standingsDetailsId : standingsDetailsIds) {
						standingsRelation = new StandingsRelation();
						standingsRelation.setStandingsId(standings.getId());
						standingsRelation
								.setStandingsdetailId(standingsDetailsId);
						StandingsRelationService.getInstance().saveSelective(
								standingsRelation);
					}
					// 存储 房间战绩和每个玩家关联信息
					StandingsAccountRelation standingsAccountRelation;
					for (Avatar avatar : playerList) {
						standingsAccountRelation = new StandingsAccountRelation();
						standingsAccountRelation.setStandingsId(standings
								.getId());
						standingsAccountRelation.setAccountId(avatar.avatarVO
								.getAccount().getId());
						StandingsAccountRelationService.getInstance()
								.saveSelective(standingsAccountRelation);
					}
				}
				System.out.println("整个房间战绩" + i);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// 发送消息
			for (Avatar avatar : playerList) {
				avatar.getSession().sendMsg(
						new HuPaiAllResponse(1, js.toString()));
			}
			// 4局完成之后解散房间//销毁
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(
					roomVO.getRoomId());
			roomLogic.destoryRoomLogic();
			roomLogic = null;
		}
	}

	/**
	 * 出牌返回出牌点数和下一家玩家信息
	 */
	private void chuPaiCallBack() {
		if (!hasHu && checkMsgAndSend()) {
			// 如果没有吃，碰，杠，胡的情况，则下家自动摸牌
			pickCard();
		}
	}

	/**
	 * 發送吃，碰，杠，胡牌信息
	 * 
	 * @return
	 */
	private boolean checkMsgAndSend() {
		if (huAvatar.size() > 0) {
			return false;
		}
		if (gangAvatar.size() > 0) {
			return false;
		}
		if (penAvatar.size() > 0) {
			return false;
		}
		if (chiAvatar.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 发牌
	 */
	private void dealingTheCards(RoomVO value) {
		nextCardindex = 0;
		bankerAvatar = null;
		for (int i = 0; i < 13; i++) {
			for (int k = 0; k < playerList.size(); k++) {
				Avatar avatar = playerList.get(k);
				if (bankerAvatar == null) {
					if (avatar.avatarVO.isMain()) {
						bankerAvatar = avatar;
					}
				}
				avatar.putCardInList(listCard.get(nextCardindex));
				avatar.oneSettlementInfo = "";
				avatar.overOff = false;
				nextCardindex++;
			}
		}
		//翻鬼
		initGui(value);
		
		bankerAvatar.putCardInList(listCard.get(nextCardindex));
		// nextCardindex++;
		singleOver = false;
		// 检测一下庄家有没有天胡
		if (checkHu(bankerAvatar, listCard.get(nextCardindex))) {
			// 检查有没有天胡/有则把相关联的信息放入缓存中
			huAvatar.add(bankerAvatar);
			// //二期优化注释 pickAvatarIndex =
			// playerList.indexOf(bankerAvatar);//第一个摸牌人就是庄家
			// 发送消息
			bankerAvatar.getSession().sendMsg(new HuPaiResponse(1, "hu,"));
			bankerAvatar.huAvatarDetailInfo.add(listCard.get(nextCardindex)
					+ ":" + 0);
		}
		// 检测庄家起手有没的杠 长沙麻将叫做大四喜
		if (bankerAvatar.checkSelfGang()) {
			gangAvatar.add(bankerAvatar);
			// 发送消息
			StringBuffer sb = new StringBuffer();
			sb.append("gang");
			for (int i : bankerAvatar.gangIndex) {
				sb.append(":" + i);
			}
			sb.append(",");

			bankerAvatar.getSession().sendMsg(
					new ReturnInfoResponse(1, sb.toString()));
		}
		// 游戏回放
		PlayRecordInit();
	}

	/**
	 * 游戏回放，记录 房间信息和初始牌组，玩家信息
	 */
	public void PlayRecordInit() {
		playRecordGame = new PlayRecordGameVO();
		RoomVO roomVo = roomVO.clone();
		roomVo.setEndStatistics(new HashMap<String, Map<String, Integer>>());
		roomVo.setPlayerList(new ArrayList<AvatarVO>());
		playRecordGame.roomvo = roomVo;
		PlayRecordItemVO playRecordItemVO;
		Account account;
		StringBuffer sb;
		for (int i = 0; i < playerList.size(); i++) {
			playRecordItemVO = new PlayRecordItemVO();
			account = playerList.get(i).avatarVO.getAccount();
			playRecordItemVO.setAccountIndex(i);
			playRecordItemVO.setAccountName(account.getNickname());
			sb = new StringBuffer();
			int[] str = playerList.get(i).getPaiArray()[0];
			for (int j = 0; j < str.length; j++) {
				sb.append(str[j] + ",");
			}
			playRecordItemVO.setCardList(sb.substring(0, sb.length() - 1));
			playRecordItemVO.setHeadIcon(account.getHeadicon());
			playRecordItemVO.setSex(account.getSex());
			playRecordItemVO.setGameRound(roomVO.getCurrentRound());
			playRecordItemVO.setUuid(account.getUuid());
			playRecordGame.playerItems.add(playRecordItemVO);
		}
	}

	/**
	 * 游戏回放，记录打牌操作信息
	 * 
	 * @param curAvatarIndex
	 *            操作玩家索引
	 * @param cardIndex
	 *            操作相关牌索引
	 * @param type
	 *            操作相关步骤 1出牌，2摸牌，3吃，4碰，5杠，6胡(自摸/点炮),7抢胡,8抓码,9:流局.....
	 * @param gangType
	 *            type不为杠时 传入 -1
	 * @param ma
	 *            不是抓码操作时 为null
	 */
	public void PlayRecordOperation(Integer curAvatarIndex, Integer cardIndex,
			Integer type, Integer gangType, String ma, List<Integer> valideMa) {

		// System.out.println("记录操作"+type);
		PlayBehaviedVO behaviedvo = new PlayBehaviedVO();
		behaviedvo.setAccountindex_id(curAvatarIndex);
		behaviedvo.setCardIndex(cardIndex + "");
		behaviedvo.setRecordindex(playRecordGame.behavieList.size());
		behaviedvo.setType(type);
		behaviedvo.setGangType(gangType);
		if (StringUtil.isNotEmpty(ma)) {
			behaviedvo.setMa(ma);
			behaviedvo.setValideMa(valideMa);
		}
		playRecordGame.behavieList.add(behaviedvo);

	}

	/**
	 * 游戏回放，记录 房间信息和初始牌组，玩家信息 中添加分数
	 * 
	 * @param standingsDetailId
	 *            本局游戏的id
	 */
	public void PlayRecordInitUpdateScore(int standingsDetailId) {

		if (!playRecordGame.playerItems.isEmpty()) {
			// 没有发牌就解散房间
			for (int i = 0; i < playerList.size(); i++) {
				playRecordGame.playerItems.get(i).setSocre(
						playerList.get(i).avatarVO.getScores());
			}
			// playRecordGame.standingsDetailId = standingsDetailId;
			// 信息录入数据库表中
			// String playRecordContent = JsonUtilTool.toJson(playRecordGame);
			String playRecordContent = JSONObject.toJSONString(playRecordGame);
			// System.out.println(playRecordContent);
			PlayRecord playRecord = new PlayRecord();
			playRecord.setPlayrecord(playRecordContent);
			playRecord.setStandingsdetailId(standingsDetailId);
			PlayRecordService.getInstance().saveSelective(playRecord);
			// 录入表之后重置 记录
			playRecordGame = new PlayRecordGameVO();
		}

	}

	/**
	 * 获取下一张牌的点数,如果返回为-1 ，则没有牌了
	 * 
	 * @return
	 */
	public int getNextCardPoint() {
		nextCardindex++;
		if (nextCardindex < listCard.size()) {
			return listCard.get(nextCardindex);
		}
		return -1;
	}

	/**
	 * 获取杠后拿的下一张牌的点数,如果返回为-1 ，则没有牌了
	 * 
	 * @return
	 */
	public int getGangCardPoint() {
		if (nextCardindex < listCard.size()) {
			int card = listCard.get(listCard.size() - 1);
			listCard.remove(listCard.size() - 1);
			return card;
		}
		return -1;
	}

	/**
	 * 是否是起手胡
	 * 
	 * @return
	 */
	public boolean qiShouFu(Avatar avatar) {
		/**
		 * 起手胡： 1 、大四喜：起完牌后，玩家手上已有四张一样的牌，即可胡牌。（四喜计分等同小胡自摸）pai[i] == 4 2
		 * 、板板胡：起完牌后，玩家手上没有一张 2 、 5 、 8 （将牌），即可胡牌。（等同小胡自摸） 3
		 * 、缺一色：起完牌后，玩家手上筒、索、万任缺一门，即可胡牌。（等同小胡自摸） 4 、六六顺：起完牌后，玩家手上已有 2
		 * 个刻子（刻子：三个一样的牌），即可胡牌。（等同小胡自摸）
		 */
		// 1:大四喜
		boolean flag = false;
		int[] pai = avatar.getPaiArray()[0];
		boolean flagWan = true;
		boolean flagTiao = true;
		boolean flagTong = true;
		int threeNum = 0;
		boolean dasixi = false;
		boolean banbanhu = false;
		boolean quyise = false;
		boolean liuliushun = false;
		for (int i = 0; i < pai.length; i++) {
			if (pai[i] == 4) {
				// 大四喜
				dasixi = true;
				// 胡牌信息放入缓存中****
			}
			if (pai[i] == 3) {
				// 六六顺
				threeNum++;
				if (threeNum == 2) {
					liuliushun = true;
				}
			}
			if (i >= 0 && i <= 8) {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条万子
					flagWan = false;
				}
			} else if (i > 9 && i <= 18) {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条条子
					flagTiao = false;
				}
			} else {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条筒子
					flagTong = false;
				}
			}
		}
		if (pai[1] == 0 && pai[4] == 0 && pai[7] == 0 && pai[10] == 0
				&& pai[13] == 0 && pai[16] == 0 && pai[19] == 0 && pai[22] == 0
				&& pai[25] == 0) {
			// 板板胡
			banbanhu = true;
		}
		if ((flagWan || flagTiao || flagTong)) {
			// 缺一色
			quyise = true;
		}
		return flag;
	}

	private List<AvatarVO> getAvatarVoList() {
		List<AvatarVO> result = new ArrayList<>();
		for (int m = 0; m < playerList.size(); m++) {
			result.add(playerList.get(m).avatarVO);
		}
		return result;
	}

	/**
	 * 检测胡牌算法，其中包含七小对，普通胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	private boolean checkHu(Avatar avatar, Integer cardIndex) {
		System.out.println("   wxd>>>   checkhu  roomtype " + roomVO.getRoomType());
		// 根据不同的游戏类型进行不用的判断
		if (roomVO.getRoomType() == 1) {
			// 转转麻将
			return checkHuZZhuan(avatar);
		} else if (roomVO.getRoomType() == 2) {
			// 划水麻将
			return checkHuHShui(avatar, cardIndex);
		} else if (roomVO.getRoomType() == 3){
			// 长沙麻将
			return false;
		}else if (roomVO.getRoomType() == 4) {
			// 广东麻将
			return checkHuGuangDong(avatar);
		}else if (roomVO.getRoomType() == 5) {
			// 亳州麻将
			return checkHuBoZhou(avatar);
		}else if (roomVO.getRoomType() == 6) {
			// 金昌麻将
			return checkHuBoZhou(avatar);
		}else if (roomVO.getRoomType() == 7) {
			// 甩九幺
			return checkHuBoZhou(avatar);
		}else if (roomVO.getRoomType() == 8) {
			// 推倒胡
			return checkHuBoZhou(avatar);
		}
		
		return false;
	}
	
	/**
	 * 判断广东麻将是否胡牌
	 * 
	 * @param avatar
	 * @return 1:是否自摸胡 /1- 自摸胡，2- 可以抢杠胡 2:是否抢杠胡 /1- 自摸胡，2- 可以抢杠胡 3:是否红中赖子 4:是否抓码
	 *         5:是否可胡七小对
	 */
	public boolean checkHuGuangDong(Avatar avatar) {
		int[][] paiList = avatar.getPaiArray();
		// 不需要移除掉碰，杠了的牌组，在判断是否胡的时候就应判断了
		boolean flag = false;
		
		if (roomVO.getSevenDouble() && !flag) {
			// 可七小对
			int isSeven = HuPaiType.CheckHuTypeSevenDouble(paiList, roomVO.getGui()==0?-1:roomVO.getGuiPai());
			if (isSeven == 0) {
				// System.out.println("没有七小对");
			} else {
				avatar.avatarVO.setHuType(2);
				flag = true;
			}
		}
		if (!flag) {
			if (roomVO.getGui()>0) {  // 有癞子
				flag = Naizi_gd2.testHuPai(paiList.clone(), roomVO.getGuiPai());
			} else {
				flag = normalHuPai.checkGDHu(paiList.clone());
			}
			
			if(flag)
				avatar.avatarVO.setHuType(1);
		}
		return flag;
	}

	/**
	 * 判断转转麻将是否胡牌
	 * 
	 * @param avatar
	 * @return 1:是否自摸胡 /1- 自摸胡，2- 可以抢杠胡 2:是否抢杠胡 /1- 自摸胡，2- 可以抢杠胡 3:是否红中赖子 4:是否抓码
	 *         5:是否可胡七小对
	 */
	public boolean checkHuZZhuan(Avatar avatar) {
		int[][] paiList = avatar.getPaiArray();
		boolean flag = false;
		if (roomVO.getSevenDouble() && !flag) {
			// 可七小队
			int isSeven = HuPaiType.CheckHuTypeSevenDouble(paiList, roomVO.getGui() != 0?roomVO.getGuiPai():-1);
			if (isSeven != 0) {
				//if (isSeven == 1) {
				//	System.out.println("七对");
				//} else {
				//	System.out.println("龙七对");
				//}
				flag = true;
			}
		}
		if (!flag) {
			if (roomVO.getGui() != 0) {  // 有癞子
				flag = Naizi.testHuiPai(paiList.clone());
			} else {
				flag = normalHuPai.checkZZHu(paiList.clone());
			}
		}
		return flag;
	}

	/**
	 * 判断划水麻将是否胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean checkHuHShui(Avatar avatar, Integer cardIndex) {
		int[][] paiList = avatar.getPaiArray();
		boolean flag = false;
		if (roomVO.getSevenDouble() && !flag) {
			// 可七小队
			int isSeven = HuPaiType.CheckHuTypeSevenDouble(paiList, -1);
			if (isSeven != 0) {
				//if (isSeven == 1) {
				//	System.out.println("七对");
				//} else {
				//	System.out.println("龙七对");
				//}
				if (pickAvatarIndex == playerList.indexOf(avatar)) {
					// 自摸七小队
					avatar.huAvatarDetailInfo.add(currentCardPoint + ":" + 7);
				} else {
					// 点炮七小队
					avatar.huAvatarDetailInfo.add(cardIndex + ":" + 6);
				}
				avatar.avatarVO.setHuType(2);// 划水麻将大胡
				flag = true;
			}
		}
		// 判断是否可以普通胡的时候，需要检测 风牌是否都是成对或成三
		if (!flag) {
			flag = normalHuPai.checkHSHu(paiList.clone(), roomVO.isAddWordCard());
			if (flag) {
				avatar.avatarVO.setHuType(1);// 划水麻将小胡
			}
		}
		return flag;

	}

	public boolean checkHuBoZhou(Avatar avatar) {
		int[][] paiList = avatar.getPaiArray();
		
		if (roomVO.getBozhouHu() == 2) {
			boolean tmpcheckQueYiMen = HuPaiType.CheckHuTypeQueYiMen(paiList);
			if (!tmpcheckQueYiMen) // 缺一门
			{
				return false;
			}
		}
		
		if (roomVO.getSevenDouble()) { // 可七小对
			int isSeven = HuPaiType.CheckHuTypeSevenDouble(paiList, -1);
			if (isSeven != 0) {
				return true;
			}
		}
		
		boolean flag = normalHuPai.checkBZHu(paiList.clone());
		return flag;
	}
	
	/**
	 * 前后端握手消息处理，前段接收到消息则会访问整个握手接口，说明接收到信息了 然后后台从list里面移除这个用户对应的uuid，
	 * 到最后list里面剩下的就表示前段还没有接收到消息， 则重新发送消息
	 * 
	 * @param avatar
	 */
	public void shakeHandsMsg(Avatar avatar) {
		shakeHandsInfo.remove(avatar.getUuId());
	}

	/**
	 * 在可以抢杠胡的情况下，判断其他人有没胡的情况
	 * 
	 * @return boolean
	 */
	public boolean checkQiangHu(Avatar avatar, int cardPoint) {
		boolean flag = false;

		for (Avatar ava : playerList) {
			if (ava.getUuId() != avatar.getUuId() && ava.qiangHu) {
				// 判断其他三家有没抢胡的情况
				ava.putCardInList(cardPoint);

				// 存抢胡信息（划水麻将才有，转转麻将当做普通点炮）(转转麻将被抢胡了 减6分)
				if(checkHu(ava, cardPoint)) {
					huAvatar.add(ava);
					ava.getSession().sendMsg(new ReturnInfoResponse(
							1, "qianghu:" + cardPoint));
					flag = true;
					
					if (roomVO.getRoomType() == 2) { //划水麻将抢杠胡为大胡
						ava.avatarVO.setHuType(2);
					}
				}
				ava.pullCardFormList(cardPoint);
			}
		}
		if (flag) {
			qianghu = true;
		}
		return flag;
	}

	/**
	 * 玩家玩游戏时断线重连
	 * 
	 * @param avatar
	 */
	public void returnBackAction(Avatar avatar) {
		RoomVO room = roomVO.clone();
		List<AvatarVO> lists = new ArrayList<AvatarVO>();
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).getUuId() != avatar.getUuId()) {
				// 给其他三个玩家返回重连用户信息
				playerList.get(i).getSession().sendMsg(
					new OtherBackLoginResonse(1, avatar.getUuId() + ""));
			}
			lists.add(playerList.get(i).avatarVO);
		}
		// 给自己返回整个房间信息
		AvatarVO avatarVo = null;
		List<AvatarVO> playerLists = new ArrayList<AvatarVO>();
		for (int j = 0; j < lists.size(); j++) {
			int paiCount = 0;// 有多少张普通牌
			avatarVo = lists.get(j);
			if (avatarVo.getAccount().getUuid() != avatar.getUuId()) {
				// 其他三家的牌组需要处理，不能让重连的玩家知道详细的牌组
				for (int k = 0; k < avatarVo.getPaiArray()[0].length; k++) {
					if (avatarVo.getPaiArray()[0][k] != 0
							&& avatarVo.getPaiArray()[1][k] == 0) {
						paiCount = paiCount + avatarVo.getPaiArray()[0][k];
						// avatarVo.getPaiArray()[0][k] = 0;
					}
				}
				avatarVo.setCommonCards(paiCount);
				playerLists.add(avatarVo);

			} else {
				// 不需要处理自己的牌组
				playerLists.add(avatarVo);
			}
		}
		if (playerList.size() == 3) {
			playerList.add(avatar);
		}
		if (playerLists.size() == 3) {
			playerLists.add(avatar.avatarVO);
		}
		room.setPlayerList(playerLists);
		avatar.getSession().sendMsg(new BackLoginResponse(1, room));
		// lastAvtar.getSession().sendMsg(responseMsg);

	}

	/**
	 * 断线重连返回最后操作信息
	 * 
	 * @param avatar
	 */
	public void LoginReturnInfo(Avatar avatar) {
		// 断线重连之后，该进行的下一步操作，json存储下一步操作指引
		JSONObject json = new JSONObject();//
		StringBuffer sb = new StringBuffer();
		if (huAvatar.contains(avatar)) {
			// 这里需要判断是自摸胡，还是别人点炮胡
			if (pickAvatarIndex != curAvatarIndex) {
				// 自摸
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			} else {
				// 点炮
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			}
			if (qianghu) {
				sb.append("qianghu:" + putOffCardPoint + ",");
				// system.out.println("抢胡");
			} else {
				sb.append("hu,");
				// system.out.println("胡");
			}
		}
		if (penAvatar.contains(avatar)) {
			sb.append("peng,");
			json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
			json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			// system.out.println("碰");
		}
		if (gangAvatar.contains(avatar)) {
			// 这里需要判断是别人打牌杠，还是自己摸牌杠
			StringBuffer gangCardIndex = new StringBuffer();
			List<Integer> gangIndexs = avatar.gangIndex;
			for (int i = 0; i < gangIndexs.size(); i++) {
				gangCardIndex.append(":" + gangIndexs.get(i));
			}
			if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {
				// 自摸杠
				sb.append("gang" + gangCardIndex.toString() + ",");
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				// system.out.println("自杠");
			} else {
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				sb.append("gang" + gangCardIndex.toString() + ",");
				// system.out.println("点杠");
			}
		}
		if (sb.length() > 1) {
			// //system.out.println(sb);
			// 该自己杠/胡/碰
			// 游戏轮数
			int roundNum = RoomManager.getInstance()
					.getRoom(avatar.getRoomVO().getRoomId()).getCount();
			json.put("gameRound", roundNum);// 游戏轮数
			// 桌面剩余牌数
			json.put("surplusCards", listCard.size() - nextCardindex);
			// System.out.println(json.toString());
			avatar.getSession().sendMsg(
					new ReturnOnLineResponse(1, json.toString()));
			// System.out.println(sb);
			avatar.getSession().sendMsg(
					new ReturnInfoResponse(1, sb.toString()));
		} else {
			if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {
				// 该自己出牌
				// system.out.println("自己出牌");
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数，当currentCardPoint
																// = -2时
																// 表示是碰了之后出牌
				json.put("pickAvatarIndex", pickAvatarIndex);// 当前摸牌人的索引
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			} else {
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("pickAvatarIndex", pickAvatarIndex);// 当前摸牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			}
			// 游戏局数
			int roundNum = RoomManager.getInstance()
					.getRoom(avatar.getRoomVO().getRoomId()).getCount();
			json.put("gameRound", roundNum);
			// 桌面剩余牌数
			json.put("surplusCards", listCard.size() - nextCardindex);
			// System.out.println(json.toString());
			avatar.getSession().sendMsg(
					new ReturnOnLineResponse(1, json.toString()));
		}

	}

	/*
	 * 清空所有数组
	 */
	public void clearAvatar() {
		huAvatar.clear();
		penAvatar.clear();
		gangAvatar.clear();
		chiAvatar.clear();
		qishouHuAvatar.clear();
	}

	/**
	 * 清空除胡之外的数组
	 */
	public void clearAvatarExceptHu() {
		penAvatar.clear();
		gangAvatar.clear();
		chiAvatar.clear();
		qishouHuAvatar.clear();
	}

	/**
	 * 检测当，缓存数组里全部为空时，放弃操作，则不起作用
	 */
	public boolean validateStatus() {
		if (huAvatar.size() > 0 || penAvatar.size() > 0
				|| gangAvatar.size() > 0 || chiAvatar.size() > 0
				|| qishouHuAvatar.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 第一局结束扣房卡
	 */
	public void deductRoomCard() {
		int currentCard = 0;
		if (roomVO.getRoundNumber() == 4) {
			currentCard = -1;
		} else {
			currentCard = 0 - roomVO.getRoundNumber() / 8;
		}
		Avatar zhuangAvatar = playerList.get(0);
		zhuangAvatar.updateRoomCard(currentCard);// 开始游戏，减去房主的房卡,同时更新缓存里面对象的房卡(已经在此方法中修改)
		int roomCard = zhuangAvatar.avatarVO.getAccount().getRoomcard();
		zhuangAvatar.getSession().sendMsg(
				new RoomCardChangerResponse(1, roomCard));
	}
	
	/**
	 * 返回牌
	 */
	public List<Integer> getListCard(){
		List<Integer> list = new ArrayList<Integer>();
		for (int i=nextCardindex;i<listCard.size();i++) {
			list.add(listCard.get(i));
		}
		return list;
	}
	
	/**
	 * 返回下张牌的索引
	 */
	public int getListCardIndex(){
		return nextCardindex;
	}
	
	/**
	 * 替换牌
	 */
	public String setListCard(int index, int card){
		if(index < nextCardindex)
			return "只能更换未打出的牌，此索引指向已经打出的牌";
		if(index > listCard.size()-1)
			return "此索引超出牌堆";
		//为什么要nextCardindex+1？ 因为nextCardindex是已经摸在手中的牌了，
		//如果nextCardindex位置的牌刚好是要查找的替换的牌，被替换了，包括场上的这张牌就跑出5张来了
		for (int i = nextCardindex + 1; i<listCard.size(); i++) {
			if(listCard.get(i) == card){
				int value = listCard.get(index);
				listCard.set(index, card);
				listCard.set(i, value);
				return "换牌成功";
			}
		}
		return "剩余牌中不存在替换的牌，替换失败";
	}
}
