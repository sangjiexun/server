package com.dyz.persist.util;

import com.dyz.gameserver.pojo.CheckObjectVO;

import java.util.*;

/**
 * Created by kevin on 2016/6/29.
 */
public class Naizi_gd {
    private static HashMap<Integer,List<CheckObjectVO>> shengCard = new HashMap<>();
    
    
    public static  boolean testHuPai(int [][] paiList, int guiPai){
        int[] pai =GlobalUtil.CloneIntList(paiList[0]);
        int guiCount=0;
        for(int i=0;i<paiList[0].length;i++){
        	if(i==guiPai){
        		guiCount=pai[i];
        		pai[i]=0;
        	}
            if(paiList[1][i] == 1 && pai[i] >= 3) {
                pai[i] -= 3;
            }else if(paiList[1][i] == 2 && pai[i] == 4){
                pai[i]  -= 4;
            }
        }
       return getNeedHunNum(pai, guiCount);
    }

    /**
     * 得到胡牌需要的赖子数
     * type 将在哪里
     * @return
     */
    private static boolean getNeedHunNum(int[] paiList, int guiCount){
    	int zhong = guiCount;
        int[] wan_arr = new int[9];
        int[] tiao_arr = new int[9];
        int[] tong_arr = new int[9];
        int[] feng_arr = new int[7];
        
        int needNum = 0;
        int index = 0;
        for(int i=0;i< paiList.length;i++){
            if(i<9){
                wan_arr[index] = paiList[i];
                index++;
            }
            if(i>=9 && i<18){
                if(i == 9){
                    index = 0;
                }
                tiao_arr[index] = paiList[i];
                index++;
            }
            if(i>=18 && i<27){
                if(i == 18){
                    index = 0;
                }
                tong_arr[index] = paiList[i];
                index++;
            }
            if(i>=27){
            	if(i == 27){
                    index = 0;
                }
                feng_arr[index] = paiList[i];
                index++;              
            }
        }
        //如果将在万中
        needNum = getNumWithJiang(wan_arr.clone())+ getNorNumber(tiao_arr.clone()) + getNorNumber(tong_arr.clone()) + getNorNumber(feng_arr.clone());
        if(needNum <= zhong){
        	return true;
        }
        else {
        	//如果将在条中
        	needNum = getNorNumber(wan_arr.clone()) +getNumWithJiang(tiao_arr.clone()) + getNorNumber(tong_arr.clone()) + getNorNumber(feng_arr.clone());
        	if(needNum <= zhong){
        		return true;
        	}
        	else{
        		//如果将在筒中
        		needNum = getNorNumber(wan_arr.clone()) + getNorNumber(tiao_arr.clone())+getNumWithJiang(tong_arr.clone()) + getNorNumber(feng_arr.clone());
        		if(needNum <= zhong){
        			return true;
        		}
        		else{
        			//如果将在风字中
        			needNum = getNorNumber(wan_arr.clone()) + getNorNumber(tiao_arr.clone())+getNorNumber(tong_arr.clone()) + getNumWithJiang(feng_arr.clone());
            		if(needNum <= zhong){
            			return true;
            		}
            		else{
            			return false;
            		}
        		}
        	}
        }
    }

    //带将时，判断杠、顺子和三同需要多少个癞子
    private static int getNumWithJiang(int[] temp_arr){
    	int result = 0;
    	if(temp_arr.length==7) { //风牌
    		int intShuang=0; //双牌个数
    		int intSignal=0; //单牌个数
    		for (int i = 0;  i < temp_arr.length; i++) {//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
                //   跟踪信息
                //   4张组合(杠子)
                if(temp_arr[i] != 0){
                    if (temp_arr[i] == 4)                               //   如果当前牌数等于4张
                    {
                                                            //   除开全部4张牌
                    }
                    //   3张组合(大对)
                    if (temp_arr[i] >= 3)                               //   如果当前牌不少于3张
                    {
                                                          //   减去3张牌                                                         //   取消3张组合
                    }
                    if (temp_arr[i] == 2)                               //   如果当前牌不少于3张
                    {
                    	intShuang ++;                                   //   减去3张牌                                                         //   取消3张组合
                    } 
                    if (temp_arr[i] == 1)                               //   如果当前牌不少于3张
                    {
                    	intSignal ++;                                   //   减去3张牌                                                         //   取消3张组合
                    }
                }
            }
    		
    		//分析还要多少个癞子才能胡牌
    		if(intShuang==0 && intSignal==0){
    			result=2;
    		}else if(intShuang>0 && intSignal==0){
    			result=intShuang-1;
    		}else if(intShuang==0 && intSignal>0){
    			result=(intSignal-1)*2+1;
    		}
    	}else if(temp_arr.length==9){ //非风牌
    		int intShuang=0; //双牌个数
    		int intSignal=0; //单牌个数
    		int intLian=0; //连单有多少对
    		//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
    		for (int i = 0;  i < temp_arr.length; i++) {
                //   跟踪信息
                //   4张组合(杠子)
                if(temp_arr[i] != 0){
                    if (temp_arr[i] == 4)                               //   如果当前牌数等于4张
                    {
                        temp_arr[i] = 0;                                     //   除开全部4张牌
                    }
                    //   3张组合(大对)
                    if (temp_arr[i] >= 3)                               //   如果当前牌不少于3张
                    {
                        temp_arr[i] -= 3;                                   //   减去3张牌                                                         //   取消3张组合
                    }
                    
                    //   顺牌组合，注意是从前往后组合！
                    //   排除数值为8和9的牌
                    if (i<7 && temp_arr[i+1]!=0 && temp_arr[i+2]!=0)             //   如果后面有连续两张牌
                    {
                        temp_arr[i]--;
                        temp_arr[i + 1]--;
                        temp_arr[i + 2]--;                                     //   各牌数减1
                    }
                    
                    if (temp_arr[i] == 2)                               
                    {
                		temp_arr[i] -= 2;
                		intShuang ++;                                   
                    } 
                }
            }
    		
    		//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
			for (int i = 0; i < temp_arr.length; i++) {
				if (temp_arr[i] == 1) {
					if (i<8 && temp_arr[i + 1] == 1) {
						temp_arr[i]--;
						temp_arr[i + 1]--;
						intLian++;
					} else if (i<7 && temp_arr[i + 2] == 1) {
						temp_arr[i]--;
						temp_arr[i + 2]--;
						intLian++;
					} else {
						intSignal++;
					}
				}
			}
			
			//分析还要多少个癞子才能胡牌==》开始
			if(intShuang==0){ //双牌为0
				if(intLian==0){//连单为0
					if(intSignal==0){//单为0
						result=2;
					}else{
						result= (intSignal-1)*2+1;
					}
				}else{
					if(intSignal==0){
						result= intLian + 2; //如果单为0，那么需要2个鬼做将，并且需要intLian个鬼和连单组成顺子
					}else{
						result= intLian + (intSignal-1)*2+1;//需要intLian个鬼和连单组成顺子，且需要1个鬼和一个单组成将，剩下的每个单需要2个鬼一起组成顺子或三同
					}
				}
			}else{ //双牌不为0
				if(intLian==0){//连单为0
					if(intSignal==0){//单为0
						result=intShuang-1;
					}else{
						result= (intShuang-1) + (intSignal*2);
					}
				}else{
					if(intSignal==0){
						result= (intShuang-1) + intLian; //如果双不为0，单为0，需要intShuang-1个鬼和其他双组成三同或三顺，并且需要intLian个鬼和连单组成顺子
					}else{
						//需要intShuang-1个鬼和其他双组成三同或三顺，且需要intLian个鬼和连单组成顺子，且每个单需要2个鬼一起组成顺子或三同
						result= (intShuang-1) + intLian + (intSignal*2);
					}
				}
			}
    		//分析还要多少个癞子才能胡牌==》结束
    	}
    	
    	return result;
    }
    
    
    //判断杠、顺子和三同需要多少个癞子
    private static int getNorNumber(int[] temp_arr){
    	int result = 0;
    	if(temp_arr.length==7) { //风牌
    		for (int i = 0;  i < temp_arr.length; i++) {//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
                //   跟踪信息
                //   4张组合(杠子)
                if(temp_arr[i] != 0){
                    if (temp_arr[i] == 4)                               //   如果当前牌数等于4张
                    {
                                                            //   除开全部4张牌
                    }
                    //   3张组合(大对)
                    if (temp_arr[i] >= 3)                               //   如果当前牌不少于3张
                    {
                                                          //   减去3张牌                                                         //   取消3张组合
                    }
                    if (temp_arr[i] == 2)                               //   如果当前牌不少于3张
                    {
                        result ++;                                   //   减去3张牌                                                         //   取消3张组合
                    } 
                    if (temp_arr[i] == 1)                               //   如果当前牌不少于3张
                    {
                        result += 2;                                   //   减去3张牌                                                         //   取消3张组合
                    }
                }
            }
    	}else if(temp_arr.length==9){ //非风牌
    		for (int i = 0;  i < temp_arr.length; i++) {//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
                //   跟踪信息
                //   4张组合(杠子)
                if(temp_arr[i] != 0){
                    if (temp_arr[i] == 4)                               //   如果当前牌数等于4张
                    {
                        temp_arr[i] = 0;                                     //   除开全部4张牌
                    }
                    //   3张组合(大对)
                    if (temp_arr[i] >= 3)                               //   如果当前牌不少于3张
                    {
                        temp_arr[i] -= 3;                                   //   减去3张牌                                                         //   取消3张组合
                    }
                    
                    //   顺牌组合，注意是从前往后组合！
                    //   排除数值为8和9的牌
                    if (i<7 && temp_arr[i+1]!=0 && temp_arr[i+2]!=0)             //   如果后面有连续两张牌
                    {
                        temp_arr[i]--;
                        temp_arr[i + 1]--;
                        temp_arr[i + 2]--;                                     //   各牌数减1
                    }
                }
            }
    		
    		for (int i = 0;  i < temp_arr.length; i++) {//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
                //   跟踪信息
                //   4张组合(杠子)
                if(temp_arr[i] != 0){
                	if (temp_arr[i] == 2)                               
                    {
                		temp_arr[i] -= 2;
                        result ++;                                   
                    } 
                    if (temp_arr[i] == 1)                              
                    {
                    	if(i<8 && temp_arr[i+1] == 1){
                    		temp_arr[i]--;
                    		temp_arr[i+1]--;
                    		result++;
                    	}else if(i<7 && temp_arr[i+2] == 1){
                    		temp_arr[i]--;
                    		temp_arr[i+2]--;
                    		result++;
                    	}else {
                    		result += 2;
                    	}                                   
                    }                    
                }
                
                //
            }
    	}
    	
    	return result;
    }
    
}
