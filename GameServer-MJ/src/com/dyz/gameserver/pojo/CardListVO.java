package com.dyz.gameserver.pojo;

/**
 * Created by wuislet on 2017/9/14.
 */
public class CardListVO {
    /**
     * 甩九幺的牌号
     */
    private int[] cardList;
    
    private int avatarIndex;
	
	public int[] getCardList() {
		return cardList;
	}

	public void setCardList(int[] cardList) {
		this.cardList = cardList;
	}

	public int getAvatarIndex() {
		return avatarIndex;
	}

	public void setAvatarIndex(int avatarIndex) {
		this.avatarIndex = avatarIndex;
	}
}
