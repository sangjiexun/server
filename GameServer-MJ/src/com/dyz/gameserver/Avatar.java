package com.dyz.gameserver;

import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.gameserver.pojo.XiaZuiVO;
import com.dyz.gameserver.sprite.Character;
import com.dyz.gameserver.sprite.base.GameObj;
import com.dyz.gameserver.sprite.tool.AsyncTaskQueue;
import com.dyz.myBatis.services.AccountService;
import com.dyz.persist.util.GlobalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 2016/6/18.
 */
public class Avatar implements GameObj {
    public AvatarVO avatarVO;
    //请求吃
    public boolean chiQuest = false;
    //存储吃牌的信息
    public CardVO cardVO = new CardVO();
    //请求碰
    public boolean pengQuest = false;
    //请求杠
    public boolean gangQuest = false;
    //请求胡
    public boolean huQuest = false;
    
    public boolean canHu = true;
    
    public boolean qiangHu = true;
    
    /**
     * 额外加分的牌
     */
    public int[] extraScoreCardIndexs;
    /**
     * 额外加分的倍数
     */
    public int extraScoreMultiple;
    
    public XiaZuiVO xiazuiVO;
    
    /**
     * 是否是一局结算时掉线
     */
    public boolean overOff = false;
    /**
     * 存储某一句游戏断线时 结算信息
     */
    public String oneSettlementInfo;
    
    //当自己摸牌时检测自己是否有杠的牌。把杠的牌放入到整个list里面，然后在转入给前端
    public List<Integer> gangIndex = new  ArrayList<Integer>();
    /**
     * 检测到有人胡牌时存储胡牌的详细消息(划水麻将和长沙麻将用)
     *      0: 天胡
     *             1：明杠 	 1*3番
					2：暗杠	 2*3番
					3：放杠/点杠 	   1*3番（谁放牌谁出番）
					4：自摸 	6番（其他三家每家出2番）
					5：普通点炮	3番（谁放炮谁出番）
					6：七对点炮	3*3番（谁放炮谁出番）
					7：七对自摸	18番（其他三家每家出6番）
					8：杠开/杠上花	18番（其他三家每家出6番）
					9：抢杠	3*3番（谁要杠牌谁出番）
					10：一炮双响	根据胡牌者的牌型来计算放炮者出的番数（胡牌两方所胡牌型的番数相加）
					11：一炮三响	根据胡牌者的牌型来计算放炮者出的番数（同上）
					
				数组格式   牌索引:类型
				放弃的时候需要清空
		*
		*小胡点炮：3
		*大胡点炮：3*3
		*
		*小胡自摸： 2*3   （每家2分）
		*大胡自摸： 2*3*3  (每家6分)
		*
		*
		*
		* 
		* 
		* 		
     */
    public List<String> huAvatarDetailInfo = new ArrayList<String>();
    
	public CardVO getCardVO() {
		return cardVO;
	}

	public void setCardVO(CardVO cardVO) {
		this.cardVO = cardVO;
	}

	public RoomVO getRoomVO() {
        return roomVO;
    }

    public void setRoomVO(RoomVO roomVO) {
        this.roomVO = roomVO;
        if(avatarVO != null){
            avatarVO.setRoomId(roomVO.getRoomId());
        }
    }

    private RoomVO roomVO;
    /**
     * session
     */
    private GameSession session;
    /**
     * 日志记录
     */
    private static final Logger logger = LoggerFactory.getLogger(Character.class);
    /**
     * 异步操作队列
     */
    private AsyncTaskQueue asyncTaskQueue = new AsyncTaskQueue();
    /**
     * list里面字符串规则 
     * 杠：uuid(出牌家),介绍(明杠，暗杠)  （123，明杠）
     * 自己摸来杠：介绍(明杠，暗杠)
     * 
     * 点炮：uuid(出牌家),介绍(胡的类型) （123，qishouhu）
     * 自摸：介绍(胡的类型)
     * 
     * 碰：
     * 
     * eg:  碰： key:1     value: 碰的牌的下标
     * 			杠：key:2    value: 杠的牌的下标
     * 			胡：key:3    value: 胡的牌的下标
     *         吃：key:4    value:  吃的牌的下标(1:2:3)
     * 
     * key:1:碰    2:杠    3:胡   4:吃   5:抢胡
     * value:信息，分条信息之间用","隔开
     */
    private Map<Integer,String> resultRelation = new HashMap<Integer,String>(); 

    public Map<Integer,String> getResultRelation() {
		return resultRelation;
	}
    //存储杠和胡的信息
	public  void putResultRelation(Integer i , String str) {
		synchronized(resultRelation){
			if(resultRelation.get(i) == null){
				resultRelation.put(i, str);
			}
			else{
				resultRelation.put(i, resultRelation.get(i)+","+str);
			}
		}
	}
	
	public Avatar(){
    }


    public void setQuestToFalse(){
        huQuest = false;
        gangQuest = false;
        pengQuest = false;
        chiQuest = false;
    }
    /**
     * 更新用户数据表信息
     * @param value
     */
    public void updateRoomCard(int value){
        int number = avatarVO.getAccount().getRoomcard();
        number += value;
        avatarVO.getAccount().setRoomcard(number);
        AccountService.getInstance().updateByPrimaryKeySelective(avatarVO.getAccount());
    }
    /**
     * 获取玩家session
     * @return
     */
    public GameSession getSession() {
        return session;
    }
    /**
     * 获取玩家session
     * @return
     */
    public void setSession(GameSession gameSession) {
         this.session = gameSession;
    }
    /**
     * 添加异步任务，针对异步操作数据库
     * @param tasks
     */
    public void addAsyncTask(Runnable... tasks){
        asyncTaskQueue.addTask(tasks);
    }

    /**
     * 获取用户uuid
     * @return
     */
    public int getUuId(){
        return avatarVO.getAccount().getUuid();
    }

    /**
     * 檢測是否可以碰 大于等于2张且对应的下标不为1都可以碰
     * @param cardIndex
     * @return
     */
    public boolean checkPeng(int cardIndex){
    	System.out.println("杠了的牌="+cardIndex+"====="+resultRelation.get(2));
    	System.out.println("碰了的牌="+cardIndex+"====="+resultRelation.get(1));
    	
    	boolean flag = false;
        if(avatarVO.getPaiArray()[0][cardIndex] >= 2 ){
        	if(resultRelation.get(1) == null ){
        		flag = true;
        	}
        	else{
        		String strs [] = resultRelation.get(1).split(",");
        		for (int i = 0; i < strs.length; i++) {
        			if(strs[i].equals(cardIndex+"")){
						flag  =  false;
						 i = strs.length;
					}
					else{
						flag  =  true;
					}
				}
        	}
        }
        return flag;
    }

    /**
     * 檢測是否可以杠别人出的牌/此牌对应的下标不为1（碰过了的牌）
     * @param cardIndex
     * @return
     */
    public boolean checkGang(int cardIndex){
    	boolean flag = false;
    	gangIndex.clear();//每次出牌就先清除缓存里面的可以杠的牌下标
        if(avatarVO.getPaiArray()[0][cardIndex] == 3 && avatarVO.getPaiArray()[1][cardIndex] == 0){
    		gangIndex.add(cardIndex);
    		flag = true;
        }
        return flag;
    }
    /**
     * 检测当前自己的牌是否可杠
     * @param  
     * @return
     */
    public boolean checkSelfGang(){
    	gangIndex.clear();//先清除缓存里面的可以杠的牌下标
    	//剔除掉当前以前吃，碰，杠的牌组 再进行比较
    	boolean flag = false;
		for (int i = 0; i < avatarVO.getPaiArray()[0].length; i++) {
			if (avatarVO.getPaiArray()[0][i] == 4 && avatarVO.getPaiArray()[1][i] != 2) {
				gangIndex.add(i);
				flag = true;
				break;
			}
		}
        return flag;
    }
    /**
     * 檢測是否可以吃
     * @param cardIndex
     * @return
     */
    public boolean checkChi(int cardIndex){
    	if (cardIndex < 10000) //绝对true;
    		return false;
    	
    	boolean flag = false;
    	//system.out.println("判断吃否可以吃牌-----cardIndex:"+cardIndex);
    	
    	//TODO 这里检测吃的时候需要踢出掉碰 杠了的牌
    	int []  cardList = avatarVO.getPaiArray()[0];
    	if((cardIndex % 9 == 0) && (cardList[cardIndex + 1] >= 1) && (cardList[cardIndex + 2] >= 1)){
    		flag = true;
    	} else if(cardIndex % 9 == 1 && ((cardList[cardIndex - 1] >=1 && cardList[cardIndex + 1] >=1) 
				|| (cardList[cardIndex + 1] >=1 && cardList[cardIndex + 2] >=1))){
			flag = true;
		} else if(cardIndex % 9 == 8 && cardList[cardIndex - 1] >=1 && cardList[cardIndex - 2] >=1){
			flag = true;
		} else if(cardIndex % 9 == 7 && ((cardList[cardIndex + 1] >=1 && cardList[cardIndex - 1] >=1)
				|| (cardList[cardIndex - 1] >=1 && cardList[cardIndex - 2] >=1))){
			flag = true;
		} else if((cardList[cardIndex - 1] >= 1 && cardList[cardIndex + 1] >= 1)
				|| (cardList[cardIndex - 1] >= 1 && cardList[cardIndex - 2] >= 1) 
				|| (cardList[cardIndex + 1] >= 1 && cardList[cardIndex + 2] >= 1)){
			flag = true;
		}
        return flag;
    }

    /**
     * 为自己的牌组里加入新牌
     * /碰 1  杠2  胡3  吃4
     * @param cardIndex
     */
    public boolean putCardInList(int cardIndex){
        if(avatarVO.getPaiArray()[0][cardIndex]<4) {
            avatarVO.getPaiArray()[0][cardIndex]++;
        }
        return true;
    }

    /**
     * 设置牌的状态
     * @param cardIndex
     * @param type;//碰 1  杠2  胡3  吃4
     */
    public void setCardListStatus(int cardIndex,int type){
        avatarVO.getPaiArray()[1][cardIndex] = type;
    }

    /**
     * 把牌从自己的牌列中去除
     * @param cardIndex
     */
    public void pullCardFormList(int cardIndex){
        if(avatarVO.getPaiArray()[0][cardIndex]>0) {
            avatarVO.getPaiArray()[0][cardIndex]--;
        }
    }
    
    public void pullCardFormList(int[] cardList) {
    	for(int i = 0; i < cardList.length; i++) {
    		pullCardFormList(cardList[i]);
    	}
    }

    /**
     * 得到牌组的一维数组。用来判断是否胡牌和听牌用
     * @return
     */
    public int[][] getPaiArray(){
        return avatarVO.getPaiArray();
    }
    
    public boolean HasExtraInPaiArray() {
    	if(extraScoreCardIndexs == null) {
    		return false;
    	}
    	
    	for(int i = 0; i < extraScoreCardIndexs.length; i++) {
    		if(avatarVO.getPaiArray()[0][i] > 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public int CountExtraInPaiArray() {
    	if(extraScoreCardIndexs == null) {
    		return 0;
    	}
    	
    	int cnt = 0;
    	for(int i = 0; i < extraScoreCardIndexs.length; i++) {
    		if(avatarVO.getPaiArray()[0][i] > 0) {
    			cnt += 1;
    		}
    	}
    	return cnt;
    }

    public String printPaiString(){
        String sb = "";
        for(int a=0;a<2;a++) {
             sb += "[";
            for (int i = 0; i < avatarVO.getPaiArray()[a].length; i++) {
                sb += avatarVO.getPaiArray()[a][i]+",";
            }
            sb += "]";
        }
        return sb;
    }
    @Override
    public void destroyObj() {
    	//统计在线用户****
        logger.info("Avatar用户{}断开服务器链接",avatarVO.getAccount().getNickname());
        if(session != null){
        	//session.clearAllInfo();
        }
    }
    /**
     * kevinTest
     * 测试胡牌用的数据
     * @return
     */
    public int[] getTestPaiArray(){
        return new int[]{0, 2, 2, 2, 4, 1, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }
    
    public HuReturnObjectVO getHuVO()
    {
        return avatarVO.getHuReturnObjectVO();
    }

}
