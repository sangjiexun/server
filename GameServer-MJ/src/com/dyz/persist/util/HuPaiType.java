package com.dyz.persist.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.context.Rule;
import com.dyz.gameserver.Avatar;

/**
 * 判断胡牌类型
 * @author luck
 *
 */
public class HuPaiType {
     
	/**
	 * 有效码 key：原始码   value：处理之后的码
	 */
	private  Map<Integer , Integer> map = new HashMap<Integer , Integer>();
	/**
	 * 有效码
	 */
	private   List<Integer> validMa;
	
	public List<Integer> getValidMa() {
		return validMa;
	}
	public void setValidMa(List<Integer> validMa) {
		this.validMa = validMa;
	}
	
	/**
	 * 包含所有的有效码(处理成0-3之间数的码)
	 */
	private static StringBuffer sb;
	
	
	private static HuPaiType huPaiType ;
	
	
	private HuPaiType() {
		
	}
	public  static HuPaiType getInstance(){
		if(huPaiType == null){
			huPaiType = new HuPaiType();
		}
		return huPaiType;
	}
	/**
	 * 
	 * 	 //区分转转麻将，划水麻将，长沙麻将
	 * 
	 * 返回String的规格
     * 存储本局 杠，胡关系
     * list里面字符串规则 
     * 杠：uuid(出牌家),介绍(明杠，暗杠)  （123，明杠）
     * 自己摸来杠：介绍(明杠，暗杠)
     * 点炮：uuid(出牌家),介绍(胡的类型) （123，qishouhu）
     * 自摸：介绍(胡的类型)
     * Map：key-->1：表示信息    2:表示次数
     * count 为1表示单胡  2表示多响
     */
	public List<Integer> getHuType(Avatar avatarShu , Avatar avatar , int roomType ,int cardIndex,
			List<Avatar> playerList,List<Integer> mas,int count,String type,boolean hongzhong, Avatar bankerAvatar){
		//区分转转麻将，划水麻将，长沙麻将
		if(roomType == 1){
			//转转麻将没有大小胡之分
			return zhuanZhuan(avatarShu, avatar, cardIndex, playerList, mas, count, type, hongzhong);
		}
		else if(roomType == 2){
			//划水麻将
			huaShui(avatarShu, avatar, cardIndex, playerList, count);
			return new ArrayList<>();
		}
		else if(roomType == 3){
			//长沙麻将
			changSha(avatarShu, avatar, cardIndex);
			return new ArrayList<>();
		}else if(roomType == 4){
			//广东麻将
			return guangDong(avatarShu, avatar, cardIndex, playerList, mas, count, bankerAvatar);
		}else if(roomType == 5){
			//亳州麻将
			boZhou(avatarShu, avatar, cardIndex, playerList, count, bankerAvatar);
			return new ArrayList<>();
		}
		
		return new ArrayList<>();
	}
	
	/**
	 * 广东麻将
	 * @param avatarShu  输家
	 * @param avatar  自己
	 * @param cardIndex
	 * @param playerList
	 * @param huCount 是否是一炮多响
	 */
	private List<Integer> guangDong(Avatar avatarShu, Avatar avatar, int cardIndex, 
			List<Avatar> playerList, List<Integer> mas, int huCount, Avatar bankerAvatar) {
		sb = new StringBuffer();
		int zmCount = 0;
		validMa = new ArrayList<Integer>();
		
		//计算中码
		if(mas != null){
			int selfIndex = playerList.indexOf(avatar);
			int bankerIndex = playerList.indexOf(bankerAvatar);
			
			int orderdiff = (selfIndex - bankerIndex + 4) % 4; //庄家 跟 胡牌者的位置差距。e.g. 1表示胡家在庄家下家。
			sb.append(orderdiff + ",");
			
			for (Integer cardPoint : mas) {
				if(orderdiff == returnMa(cardPoint)) {
					zmCount += 1;
					if(!validMa.contains(cardPoint)) {
						validMa.add(cardPoint);
					}
				}
			}
			System.out.println("有效码：" + validMa);
		}
		
		String str;
		int score = CalGuangDongHuScore(avatar.getPaiArray(), zmCount);
		if(avatarShu.getUuId() == avatar.getUuId()) { //自摸类型
			for (int i = 0; i < playerList.size(); i++) {
				if(playerList.get(i).getUuId() == avatar.getUuId()) {
					if(avatar.avatarVO.getHuType() == 2){
						str = "0:" + cardIndex + ":" + Rule.Hu_self_qixiaodui;
					}else {
						str = "0:" + cardIndex + ":" + Rule.Hu_zi_common;
					}
					avatar.getHuVO().updateTotalInfo("hu", str);
					avatar.getHuVO().updateGangAndHuInfos("1", score*3);
				} else{
					playerList.get(i).getHuVO().updateGangAndHuInfos("1", -1*score);
				}
			}
		} else {
			score *= 3; //点炮分数全包
			
			if(huCount == 1){
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
				avatarShu.getHuVO().updateTotalInfo("hu", str);
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				//杠的分数全部由被抢杠胡的人出
				for (Avatar ava :playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){ //被抢杠的人自己
						int score2 = avatarShu.getHuVO().getGangScore();
						if(score2>0){
							avatarShu.getHuVO().updateGangAndHuInfos("4", -score2);
						}
					}else{
						int score2 = ava.getHuVO().getGangScore();
						if(score2<0){
							ava.getHuVO().updateGangAndHuInfos("4", -score2);
							avatarShu.getHuVO().updateGangAndHuInfos("4", score2);
						}
					}
				}
			}
			//广东麻将没有点炮多响？
			else{ //点炮  多响  
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
				avatarShu.getHuVO().updateTotalInfo("hu", str);
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				//杠的分数全部由被抢杠胡的人出
				for (Avatar ava :playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){ //被抢杠的人自己
						int score2 = avatarShu.getHuVO().getGangScore();
						if(score2>0){
							avatarShu.getHuVO().updateGangAndHuInfos("4", -score2);
						}
					}else{
						int score2 = ava.getHuVO().getGangScore();
						if(score2<0){
							ava.getHuVO().updateGangAndHuInfos("4", -score2);
							avatarShu.getHuVO().updateGangAndHuInfos("4", score2);
						}
					}
				}
			}
		}
		
		return validMa;
	}
	/**
	 * 划水麻将
	 * @param avatarShu  输家
	 * @param avatar  自己
	 * @param cardIndex
	 * @param playerList
	 * @param huCount 是否是一炮多响
	 */
	private static void huaShui(Avatar avatarShu , Avatar avatar,  int cardIndex , 
			List<Avatar> playerList , int huCount){
		String str;
		int score = 0;
		int xiayu = avatar.getRoomVO().getXiaYu();
		if(avatarShu.getUuId() == avatar.getUuId() ){
			//自摸类型
			for (int i = 0; i < playerList.size(); i++) {
				if(avatar.avatarVO.getHuType() == 1){
					//小胡 2分
					score = 2;
				}
				else if(avatar.avatarVO.getHuType() == 2){
					//大胡  6 分
					score = 6;
				}
				if(xiayu >= 0){
					score = score + 2*xiayu;
				}
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  
					avatar.getHuVO().updateTotalInfo("hu", str);
					avatar.getHuVO().updateGangAndHuInfos("1", score*3);
				}
				else{
					playerList.get(i).getHuVO().updateGangAndHuInfos("1", -1*score);
				}
			}
		}
		else{
			//点炮   单响
			if(avatar.avatarVO.getHuType() == 1){
				//小胡 2分
				score = 3;
			}
			else if(avatar.avatarVO.getHuType() == 2){
				//大胡  6 分
				score = 9;
			}
			if(xiayu >= 0){
				score = score + 2*xiayu;
			}
			if(huCount == 1){
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				//修改胡家自己的番数
				avatar.getHuVO().updateTotalInfo("hu", str);
				
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				//修改点炮玩家的番
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
				//点炮信息放入放炮玩家信息中
				avatarShu.getHuVO().updateTotalInfo("hu", str);
			}
			else{
				//点炮  多响  
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
				//修改胡家自己的番数
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				//修改点炮玩家的番数
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
				//点炮信息放入放炮玩家信息中
				avatarShu.getHuVO().updateTotalInfo("hu", str);
			}
		}
	}
	/**
	 *  转转麻将 算分
	 * @param avatarShu 输家 自摸时也表示赢家
	 * @param avatar 赢家   
	 * @param cardIndex
	 * @param playerList
	 * @param mas
	 * @param count
	 *  type  qiangganghu
	 */
	private  List<Integer> zhuanZhuan(Avatar  avatarShu , Avatar avatar , int cardIndex, List<Avatar> playerList,
			List<Integer> mas , int count,String type,boolean hongzhong){
		map = new HashMap<Integer,Integer>();
		sb = new StringBuffer();
		int score = 0;
		String str; 
		int selfCount = 0;
		List<Integer> maPoint = new ArrayList<Integer>();
		//有效的码   sb = "1,2,3"样式
		sb.append("0,");
		if(mas != null){
			int ma;
			for (Integer cardPoint : mas) {
				ma = returnMa(cardPoint);
				maPoint.add(ma);
				//system.out.println("处理过的码----"+cardPoint);
				map.put(cardPoint, ma);
			}
		}
		//抓的码里面有多少个指向对应的各个玩家
		selfCount  = Collections.frequency(maPoint, 0);//自己 
		int downCount  = Collections.frequency(maPoint, 1);//下家
		int towardCount  = Collections.frequency(maPoint, 2);//对家
		int upCount  = Collections.frequency(maPoint, 3);//上家
		
		
		//int selfIndex = 0;//胡家在数组中的位置 （0-3）//2016-8-3
		int selfIndex = playerList.indexOf(avatar);
		/*for (int i = 0; i < playerList.size(); i++) {
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					selfIndex = i;
				}
			}*///2016-8-3
		//其他三家在playerList中的下标，同上面的selfCount，downCount对应
		int downIndex = otherIndex(selfIndex,1);
		int towardIndex = otherIndex(selfIndex,2);
		int upIndex = otherIndex(selfIndex,3);
		if(avatarShu.getUuId() == avatar.getUuId() ){
			//自摸
			score = 2;
			for (int i = 0; i < playerList.size(); i++) {
				str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					// avatar.avatarVO.updateScoreRecord(1, 2*3);//记录分数
					//:游戏自摸1，接炮2，点炮3，暗杠4，明杠5 ，胡6记录(key), 码7
					//修改自己的分数
					//avatar.getHuVO().updateTotalInfo("hu", str);
					playerList.get(i).getHuVO().updateTotalInfo("hu", str);
					avatar.getHuVO().updateGangAndHuInfos("1", score*3);
					for (int j = 0; j < selfCount; j++) {
						//抓码 抓到自己，再加分
						avatar.getHuVO().updateGangAndHuInfos("7", score*3);
					}
				}
				else{
					//修改其他三家分数
					//ava.avatarVO.updateScoreRecord(1, -1*2);//记录分数（负分表示别人自摸扣的分）
					playerList.get(i).getHuVO().updateGangAndHuInfos("1", -1*score);
					for (int j = 0; j < selfCount; j++) {
						//胡家抓码抓到自己，所有这里还要再减分
						playerList.get(i).getHuVO().updateGangAndHuInfos("7", -1*score);
					}
				}
			}
			if(hongzhong){
				//抢胡抓的码 只有 1 5 9 或红中有效
			}
			else{
				//自摸没选红中癞子的情况下所有码都有效
				sb.append("1,2,3");
				//抓码加减分
				for (int j = 0; j < downCount; j++) {
					//抓码 抓到下家，胡家加分，下家减分
					playerList.get(selfIndex).getHuVO().updateGangAndHuInfos("7", score);
					playerList.get(downIndex).getHuVO().updateGangAndHuInfos("7", -1*score);
				}
				for (int j = 0; j < towardCount; j++) {
					//抓码 抓到对家，胡家加分，对家减分
					playerList.get(selfIndex).getHuVO().updateGangAndHuInfos("7", score);
					playerList.get(towardIndex).getHuVO().updateGangAndHuInfos("7", -1*score);
				}
				for (int j = 0; j < upCount; j++) {
					//抓码 抓到上家，胡家加分，上家减分
					playerList.get(selfIndex).getHuVO().updateGangAndHuInfos("7", score);
					playerList.get(upIndex).getHuVO().updateGangAndHuInfos("7",-1*score);
				}
			}
		}
		else{
			//点炮   单响  
			score = 1;
			//抢杠胡加上红中癞子时 抢杠胡 6分
			if(StringUtil.isNotEmpty(type) && type.equals("qianghu") && hongzhong){
				score = 6;
			}
			if(count == 1){
				int dPaoIndex = playerList.indexOf(avatarShu);//点炮人在数组中的下标 2016-8-1
				/* for (Avatar ava : playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){
						 //得到点炮玩家的下标
						 dPaoIndex = playerList.indexOf(ava);
					 }
				 }*///2016-8-1
				//点炮玩家被抓到码的次数
				int dPaoCount = 0;
				if(dPaoIndex == downIndex){
					dPaoCount = downCount;
					if(!hongzhong){
						sb.append("1,");
					}
				}
				else if(dPaoIndex == towardIndex){
					dPaoCount = towardCount;
					if(!hongzhong){
						sb.append("2,");
					}
				}
				else if(dPaoIndex == upIndex){
					dPaoCount = upCount;
					if(!hongzhong){
						sb.append("3");
					}
				}
				
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				//修改胡家自己的分数
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",score);
				//修改点炮玩家的分数
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				for (int j = 0; j < selfCount; j++) {
					//抓码 抓到胡家，胡家加分
					avatar.getHuVO().updateGangAndHuInfos("7", score);
					//抓码 抓到胡家，点家再减分
					avatarShu.getHuVO().updateGangAndHuInfos("7", -1*score);
				}
				if(StringUtil.isNotEmpty(type) && type.equals("qianghu") && hongzhong){
					//有红中癞子的时候抢胡抓的码 只有 1 5 9 或红中有效
				}
				else{
					for (int j = 0; j < dPaoCount; j++) {
						//抓码 抓到点炮玩家，胡家加分
						avatar.getHuVO().updateGangAndHuInfos("7", score);
						//抓码 抓到输家，点家再减分
						avatarShu.getHuVO().updateGangAndHuInfos("7", -1*score);
					}
				}
				
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
				//点炮信息放入放炮玩家信息中
				avatarShu.getHuVO().updateTotalInfo("hu", str);
			}
			else{
				//点炮  多响   (抓码人为点炮玩家)
				//点炮玩家被抓到码的次数   selfCount
				//胡牌玩家被抓到码的次数
				selfIndex = playerList.indexOf(avatarShu);//点家的索引，及摸码玩家的索引
				
				downIndex = otherIndex(selfIndex,1);
				towardIndex = otherIndex(selfIndex,2);
				upIndex = otherIndex(selfIndex,3);
				
				int huCount = playerList.indexOf(avatar);
				if(huCount == downIndex){
					huCount = downCount;
					sb.append("1,");
				}
				else if(huCount == towardIndex){
					huCount = towardCount;
					sb.append("2,");
				}
				else if(huCount == upIndex){
					huCount = upCount;
					sb.append("3");
				}
				
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
				//修改胡家自己的分数
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",score);
				//修改点炮玩家的分数
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				for (int j = 0; j < selfCount; j++) {
					//抓码 抓到自己，赢家加分
					avatar.getHuVO().updateGangAndHuInfos("7", score);
					//抓码 抓到胡家，输家再减分
					avatarShu.getHuVO().updateGangAndHuInfos("7", -1*score);
				}
				if(StringUtil.isNotEmpty(type) && type.equals("qianghu")){
					//抢胡抓的码 只有 1 5 9 或红中有效
				}
				else{
					for (int j = 0; j < huCount; j++) {
						//抓码 抓到点炮玩家，赢家加分
						avatar.getHuVO().updateGangAndHuInfos("7", score);
						//抓码 抓到自己，输家再减分
						avatarShu.getHuVO().updateGangAndHuInfos("7", -1*score);
					}
				}
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
				//点炮信息放入放炮玩家信息中
				avatarShu.getHuVO().updateTotalInfo("hu", str);
				
			}
		}
		validMa = new ArrayList<Integer>();
		Set<Entry<Integer, Integer>>  set= map.entrySet();
		for (Entry<Integer, Integer> entry : set) {
			if(sb.toString().contains(entry.getValue()+"")){
				validMa.add(entry.getKey());
			}
		}
		System.out.println("有效码："+validMa);
		return validMa;
	}
	/**
	 * 处理抓到的码点数，成0-3之间的数
	 * @param cardPoint
	 * @return
	 */
	public static int returnMa(int cardPoint){
		if (cardPoint >= 31) { //中发白
			cardPoint = cardPoint - 31;
			return cardPoint;
		} else if(cardPoint >= 27) { //东南西北
			cardPoint = cardPoint - 27;
			return cardPoint;
		} else {// 普通牌
			return (cardPoint % 9) % 4;
		}
	}
	
	
	/**
	 * 长沙麻将
	 * @param uuid
	 * @param avatar
	 * @param str
	 * @return
	 */
	private static void changSha(Avatar  avatarShu , Avatar avatar , int cardIndex){
	}
	

	/**
	 * 亳州麻将
	 * @param avatarShu  输家
	 * @param avatar  自己
	 * @param cardIndex
	 * @param playerList
	 * @param huCount 是否是一炮多响
	 * @param bankerAvatar 庄家
	 */
	private void boZhou(Avatar avatarShu, Avatar avatar, int cardIndex, 
			List<Avatar> playerList, int huCount, Avatar bankerAvatar) {
		sb = new StringBuffer();
		
		String str;
		int score = CalBoZhouHuScore(avatar.getPaiArray());
		if(avatarShu.getUuId() == avatar.getUuId()) { //自摸类型
			for (int i = 0; i < playerList.size(); i++) {
				if(playerList.get(i).getUuId() == avatar.getUuId()) {
					if(avatar.avatarVO.getHuType() == 2){
						str = "0:" + cardIndex + ":" + Rule.Hu_self_qixiaodui;
					}else {
						str = "0:" + cardIndex + ":" + Rule.Hu_zi_common;
					}
					avatar.getHuVO().updateTotalInfo("hu", str);
					avatar.getHuVO().updateGangAndHuInfos("1", score*3);
				} else{
					playerList.get(i).getHuVO().updateGangAndHuInfos("1", -1*score);
				}
			}
		} else {
			score *= 3; //点炮分数全包
			
			if(huCount == 1){
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
				avatarShu.getHuVO().updateTotalInfo("hu", str);
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				//杠的分数全部由被抢杠胡的人出
				for (Avatar ava :playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){ //被抢杠的人自己
						int score2 = avatarShu.getHuVO().getGangScore();
						if(score2>0){
							avatarShu.getHuVO().updateGangAndHuInfos("4", -score2);
						}
					}else{
						int score2 = ava.getHuVO().getGangScore();
						if(score2<0){
							ava.getHuVO().updateGangAndHuInfos("4", -score2);
							avatarShu.getHuVO().updateGangAndHuInfos("4", score2);
						}
					}
				}
			} else { //点炮  多响  
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;
				avatar.getHuVO().updateTotalInfo("hu", str);
				avatar.getHuVO().updateGangAndHuInfos("2",1*score);
				
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
				avatarShu.getHuVO().updateTotalInfo("hu", str);
				avatarShu.getHuVO().updateGangAndHuInfos("3",-1*score);
				
				//杠的分数全部由被抢杠胡的人出
				for (Avatar ava :playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){ //被抢杠的人自己
						int score2 = avatarShu.getHuVO().getGangScore();
						if(score2>0){
							avatarShu.getHuVO().updateGangAndHuInfos("4", -score2);
						}
					}else{
						int score2 = ava.getHuVO().getGangScore();
						if(score2<0){
							ava.getHuVO().updateGangAndHuInfos("4", -score2);
							avatarShu.getHuVO().updateGangAndHuInfos("4", score2);
						}
					}
				}
			}
		}
	}
	
	private static int otherIndex(int selfindex,int count){
		int thisIndex = selfindex + count;
		if(thisIndex>= 4){
			thisIndex = thisIndex -4;
		}
		return thisIndex;
	}
	
	/**
	 * 检查是否七小对胡牌
	 * 
	 * @param paiList
	 * @param laizi -1:无赖子，!-1:赖子的牌点数。
	 * @return 0-没有胡牌。1-普通七小对，2-龙七对
	 */
	public static int CheckSevenDouble(int[][] paiList, int laizi) {
		int count = 0; // 单牌个数
		int result = 1;
		for (int i = 0; i < paiList[0].length; i++) {
			if(paiList[1][i] != 0) { //七小对必须门清。
				return 0;
			}
			
			if (paiList[0][i] != 0 && i != laizi) {
				if (paiList[0][i] != 2 && paiList[0][i] != 4) {
					count++;
				} else if (paiList[0][i] == 4) {
					result = 2;
				}
			}
		}
		
		if(laizi == -1) {
			if(count != 0) {
				result = 0;
			}
		} else {
			int diff = paiList[0][laizi] - count;
			//if (diff < 0 || diff % 2 != 0) { //赖子张数不够 || 赖子凑不成对 （不知道赖子成对算不算七小对）
			if(diff != 0) {
				result = 0;
			}
		}
		return result;
	}
	
	public static int GetMenCnt(int[][] paiList) { //获取门数。
		int flag = 0;
		for (int i = 0; i < 27; i++) { //不查字牌
			if (paiList[0][i] != 0)
			{
				flag |= (1 << (i / 9));
			}
		}
		System.out.println("   wxd>>> 获取门数  " + flag);
		return flag;
	}
	
	public static boolean CheckQueYiMen(int[][] paiList) { //缺一门
		return GetMenCnt(paiList) != 7;
	}
	
	public static boolean CheckQingYiSe(int[][] paiList) { //清一色
		int cnt = GetMenCnt(paiList);
		return (cnt == 1 || cnt == 2 || cnt == 4);
	}
	
	/**
	 * 广东麻将算分
	 * @param paiList
	 * @return
	 */
	public static int CalGuangDongHuScore(int[][] paiList, int zmCount) { //TODO 返回胡法
		int score = 2;
		if(CheckSevenDouble(paiList, -1) != 0) {
			score = 4;
		}
		if(zmCount > 0){
			score = score + 2 * zmCount;
		}
		return score;
	}
	
	/**
	 * 亳州麻将算分
	 * @param paiList
	 * @return
	 */
	public static int CalBoZhouHuScore(int[][] paiList) { //TODO 返回胡法
		StringBuilder sb = new StringBuilder();
		int score = 2;
		if(CheckSevenDouble(paiList, -1) != 0) {
			score = 4;
			sb.append(Rule.Hu_self_qixiaodui);
		}
		return score; // + ":" + sb.toString();
	}
}
