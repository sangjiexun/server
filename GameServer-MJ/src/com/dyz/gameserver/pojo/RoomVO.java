package com.dyz.gameserver.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 2016/6/22.
 */
public class RoomVO {
    /**
     * 数据库表ID
     */
    private int id;
    /**
     * 房间当前轮数
     */
    private int currentRound = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
    
    public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}
	
	// ----------------- 统计所有玩家的杠，胡次数的成员变量 ----------------- 
    /**
     * 开一个房间几局游戏完后，统计所有玩家的杠，胡次数
     * 第一个key：用户uuid
     * 
     * 转转麻将
     * 第二个key：1:自摸(zimo) value次数，2:接炮(jiepao) value次数,3:点炮(dianpao)value次数,
     * 4:明杠(minggang)value次数，5:暗杠(angang) value次数 , 6: 总成绩(scores)  value分数
     *  
     *  
     *  划水麻将
     *  第二个key 不同  value 为番数
     *  跟庄("跟庄")	1番（庄家出3番，其他每人得1番）
		过路杠("glgang")	1番
		暗杠("angang")	2番
		放杠("fanggang")	3番（谁放牌谁出番）
		自摸("zimo")	4番（其他三家每家出4番）
		普通点炮(pudian)	5番（谁放炮谁出番）
		七对点炮(qidian)	5*3番（谁放炮谁出番）
		七对自摸(qizimo)	4*3番（其他三家每家出12番）
		杠开(gangkaihu)	4*3番（其他三家每家出12番）      （杠上花）
		抢杠(qiangganghu)	5*3番（谁要杠牌谁出番）         （抢杠胡）
     */
    private Map<String , Map<String,Integer>> endStatistics = new HashMap<String, Map<String,Integer>>();
    
	public Map<String, Map<String, Integer>> updateEndStatistics(String uuid , String type ,int roundScore) {
		if(endStatistics.get(uuid) == null){
			Map<String,Integer > map = new HashMap<String , Integer>();
    		map.put(type,roundScore);
    		endStatistics.put(uuid, map);
		}
		else{
			if(endStatistics.get(uuid).get(type) != null){
				endStatistics.get(uuid).put(type, endStatistics.get(uuid).get(type)+roundScore);
			}
			else{
				endStatistics.get(uuid).put(type, roundScore);
			}
		}
		return endStatistics;
	}
	
    public Map<String, Map<String, Integer>> getEndStatistics() {
		return endStatistics;
	}
    
	public void setEndStatistics(Map<String, Map<String, Integer>> endStatistics) {
		this.endStatistics = endStatistics;
	}

	// ----------------- 客户端包结构 ----------------- 
    /**
     * 房间ID
     */
    private int roomId;
	/**
     * 房间名
     */
    public String name;
    /**
     * 房间模式，1-转转麻将。2-划水麻将。3-长沙麻将。4-广东麻将。5-亳州麻将
     */
    private int roomType;
    /**
     * 房间的使用总次数
     */
    private int roundNumber;

    /**
     * 是否要字牌
     */
    private boolean addWordCard;
    /**
     * 胡法的限制
     * 0：可以点炮；1：只能自摸；2：自摸 + 抢杠胡
     */
    private int huXianzhi;
    /**
     * 听牌规则
     * 0：不特别限制，不显示文字；1：必须听牌；2：允许听牌行为；3：不允许听牌
     */
    private int listenType;
    /**
     * 七小对
     */
    private boolean sevenDouble;
    /**
     * 鬼牌 0无鬼 1白板 2翻鬼 3红中
     */
    private int gui;
    /**
     * 本局鬼牌 
     */
    private int guiPai;

    //划水麻将专属规则
    /**
     * 下鱼(漂)(0--10)
     */
    private int xiaYu;
    
    //广东麻将专属规则
    /**
     *抓码的个数
     */
    private int ma;
    
    //亳州麻将专属规则
    /**
     * 1.推倒胡 2.断一门
     */
    private int bozhouHu;
	/**
     * 1.不下嘴 2.下嘴
     */
    private int xiazui;
    /**
     * 不报听可胡
     */
    private boolean nolisterToBeard;
    /**
     * 暗杠亮
     */
    private boolean angangLiang;
    /**
     * 亳州麻将自摸倍数
     */
    private int bozhouZimoMagnification;
    
    //金昌麻将专属规则
    /**
     * 0x1：清一色  0x10：一条龙
     */
    private int OneAndOneColorTrain;
    /**
     * 报听
     */
    private boolean ReadyHand;
    /**
     * 底分
     * 1：1分   2：2分   3：5分    4：10分
     */
    private int BottomScore;
    /**
     * 甩九幺
     * 1：自摸   2：收炮（不算胡）
     */
    private int SJYHu;
    
    /**
     * 整个房间对应的所有人的牌组
     */
    private List<AvatarVO> playerList;
    
    
	public int getRoomId() {
        return roomId;
    }
	public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

	public int getRoomType() {
        return roomType;
    }
    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public boolean isAddWordCard() {
        return addWordCard;
    }
    public void setAddWordCard(boolean addWordCard) {
        this.addWordCard = addWordCard;
    }
    
    public int getHuXianZhi() {
        return huXianzhi;
    }
    public void setHuXianZhi(int huXianzhi) {
        this.huXianzhi = huXianzhi;
    }
    
	public int getListenType() {
		return listenType;
	}
	public void setListenType(int listenType) {
		this.listenType = listenType;
	}
    
    public boolean getSevenDouble() {
        return sevenDouble;
    }
    public void setSevenDouble(boolean sevenDouble) {
        this.sevenDouble = sevenDouble;
    }

    public int getGui() {
        return gui;
    }
    public void setGui(int gui) {
        this.gui = gui;
    }
    
    public int getGuiPai() {
        return guiPai;
    }
    public void setGuiPai(int guiPai) {
        this.guiPai = guiPai;
    }

    public int getXiaYu() {
        return xiaYu;
    }
    public void setXiaYu(int xiaYu) {
        this.xiaYu = xiaYu;
    }

    public int getMa() {
        return ma;
    }
    public void setMa(int ma) {
        this.ma = ma;
    }
    
    public int getHuXianzhi() {
		return huXianzhi;
	}
	public void setHuXianzhi(int huXianzhi) {
		this.huXianzhi = huXianzhi;
	}

	public int getBozhouHu() {
		return bozhouHu;
	}
	public void setBozhouHu(int bozhouHu) {
		this.bozhouHu = bozhouHu;
	}

	public int getXiazui() {
		return xiazui;
	}
	public void setXiazui(int xiazui) {
		this.xiazui = xiazui;
	}

	public boolean isNolisterToBeard() {
		return nolisterToBeard;
	}
	public void setNolisterToBeard(boolean nolisterToBeard) {
		this.nolisterToBeard = nolisterToBeard;
	}

	public boolean isAngangLiang() {
		return angangLiang;
	}
	public void setAngangLiang(boolean angangLiang) {
		this.angangLiang = angangLiang;
	}

	public int getBozhouZimoMagnification() {
		return bozhouZimoMagnification;
	}
	public void setBozhouZimoMagnification(int bozhouZimoMagnification) {
		this.bozhouZimoMagnification = bozhouZimoMagnification;
	}
	
	public int getOneAndOneColorTrain() {
		return OneAndOneColorTrain;
	}
	public void setOneAndOneColorTrain(int oneAndOneColorTrain) {
		OneAndOneColorTrain = oneAndOneColorTrain;
	}

	public boolean isReadyHand() {
		return ReadyHand;
	}
	public void setReadyHand(boolean readyHand) {
		ReadyHand = readyHand;
	}

	public int getBottomScore() {
		return BottomScore;
	}
	public void setBottomScore(int bottomScore) {
		BottomScore = bottomScore;
	}

	public int getSJYHu() {
		return SJYHu;
	}
	public void setSJYHu(int sJYHu) {
		SJYHu = sJYHu;
	}

    public List<AvatarVO> getPlayerList() {
        return playerList;
    }
    public void setPlayerList(List<AvatarVO> playerList) {
        this.playerList = playerList;
    }

	// ----------------- 拷贝构造函数 ----------------- 
    public RoomVO clone(){
    	RoomVO result = new RoomVO();
        result.id = id;
        result.currentRound = currentRound;
        result.endStatistics = endStatistics;
        
    	result.roomId = roomId;
        result.name = name;
        result.roomType = roomType;
        result.roundNumber = roundNumber;

        result.addWordCard = addWordCard;
        result.huXianzhi = huXianzhi;
        result.listenType = listenType;
        result.sevenDouble = sevenDouble;
        result.gui = gui;
        result.guiPai = guiPai;
        
        result.xiaYu = xiaYu;
        
        result.ma = ma;
        
        result.bozhouHu = bozhouHu;
        result.xiazui = xiazui;
        result.nolisterToBeard = nolisterToBeard;
        result.angangLiang = angangLiang;
        result.bozhouZimoMagnification = bozhouZimoMagnification;
        
        result.OneAndOneColorTrain = OneAndOneColorTrain;
        result.ReadyHand = ReadyHand;
        result.BottomScore = BottomScore;
        result.SJYHu = SJYHu;
        
        result.playerList = playerList;
        return result;
    }
}
