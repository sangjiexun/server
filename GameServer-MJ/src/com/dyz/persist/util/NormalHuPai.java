package com.dyz.persist.util;

/**
 * Created by kevin on 2016/7/30.
 */
public class NormalHuPai {

    /**
     *   将牌标志，即牌型“三三三三二”中的“二”
     */
    private boolean hasJiang = false;
    
    public static void main(String[] args){
    	int[] pai = new int[]{0,0,0,3,1,0,1,1,1, 0, 0, 0,0,0,0,0,0,0,0,0,1,2,0,1,1,1,1};
    	//int [] pai = new int[]{0,0,0,0,0,0,1,1,1,     0,0,2,0,3,1,1,1,0,     0,0,1,1,1,0,0,0,0,   0,0,0,0,0,0,0};
    	NormalHuPai normalHuPai = new NormalHuPai();
    	boolean flag = normalHuPai.checkHuPai(pai, 0);
    	System.out.println(flag);
    }
    
    private int[] CleanPenGang(int[][] paiList) {
        int[] pai = GlobalUtil.CloneIntList(paiList[0]);
        for(int i = 0; i < paiList[0].length; i++){
            if(paiList[1][i] == 1 && pai[i] >= 3) {
                pai[i] -= 3;
            } else if(paiList[1][i] == 2 && pai[i] == 4){
                pai[i] -= 4;
            }
        }
        return pai;
    }
    
    /**
     * 判断广东麻将普通胡牌
     * @param paiList
     * @return
     */
    public boolean checkGDHu(int[][] paiList){
        hasJiang = false;
        int[] pai = CleanPenGang(paiList);
        return checkHuPai(pai, 0);
    }
    /**
     * 判断转转麻将普通胡牌
     * @param paiList
     * @return
     */
    public boolean checkZZHu(int[][] paiList){
        hasJiang = false;
        int[] pai = CleanPenGang(paiList);
        return checkHuPai(pai, 0);
    }
    /**
     * 判断划水麻将普通胡牌方法
     * @param paiList
     * @return
     */
    public boolean checkHSHu(int[][] paiList , boolean feng){
        hasJiang = false;
        int[] pai = CleanPenGang(paiList);
        if(feng) {
			for (int i = 27; i < paiList[0].length; i++) { //判断风牌规则。
				if(paiList[0][i] == 1){
					return false;
				}
				
				if(paiList[0][i] == 2){ //如果风牌成对
					if(!hasJiang) {
						hasJiang = true;
						pai[i] -= 2;
					}
					else { //风牌有对牌个数超过2个时，不能胡
						return false;
					}
				}
			}
		}
		return checkHuPai(pai, 0);
    }
    
    /**
     * 判断亳州麻将普通胡牌
     * @param paiList
     * @return
     */
    public boolean checkBZHu(int[][] paiList){
        hasJiang = false;
        int[] pai = CleanPenGang(paiList);
        return checkHuPai(pai, 0);
    }
    
    /**
     * 普通胡牌算法
     * @param paiList
     * @param index  查找起始的下标。
     * @return
     */
    private boolean checkHuPai(int[] paiList, int index) {
        if (!Remain(paiList)) {
            return hasJiang;           //   递归退出条件：如果没有剩牌，则胡牌返回。
        }
        
        for (int i = index; i < paiList.length; i++) {        //找到有牌的地方，i就是当前牌，PAI[i]是个数
            if (paiList[i] == 4) {                            //4张组合(杠子)
                paiList[i] = 0;
                if (checkHuPai(paiList, index + 1)) {
                    return true;
                }
                paiList[i] = 4;
            }
            if (paiList[i] >= 3) {                             //3张组合(刻子)
                paiList[i] -= 3;
                if (checkHuPai(paiList, index)) {
                    return true;
                }
                paiList[i] += 3;
            }
            if (!hasJiang && paiList[i] >= 2)                  //  2张组合(将牌)，如果之前没有将牌，
            {
                hasJiang = true;
                paiList[i] -= 2;
                if (checkHuPai(paiList, index)) { 
                	return true;
                }
                paiList[i] += 2;
                hasJiang = false;
            }
            
            //顺牌组合，注意是从前往后组合！排除数值为8和9的牌
            if (i < 27 && i % 9 < 7 && paiList[i] > 0 && paiList[i + 1] > 0 && paiList[i + 2] > 0)
            {
                paiList[i]--;
                paiList[i + 1]--;
                paiList[i + 2]--;
                if (checkHuPai(paiList, index)) {
                    return true;
                }
                paiList[i]++;
                paiList[i + 1]++;
                paiList[i + 2]++;
            }
        }
        return false;
    }

    //   检查是否有剩余牌
    boolean Remain(int[] paiList) {
        for (int i = 0; i < paiList.length; i++) {
            if(paiList[i] > 0) {
            	return true;
            }
        }
        return false;
    }
}
