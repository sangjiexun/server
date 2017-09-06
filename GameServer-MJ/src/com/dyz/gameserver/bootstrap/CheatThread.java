package com.dyz.gameserver.bootstrap;

import java.util.Scanner;

import java.util.List;

import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;

public class CheatThread extends Thread {
	public void run(){
		RoomLogic roomlogic = null;
		System.out.println("作弊器线程开始");
		while(true){
			try {				
				System.out.println("请输入操作：\n 1：进入房间\n 2：显示当前牌\n 3：替换牌");
				Scanner sc = new Scanner(System.in);
				String s = sc.nextLine();
				
				int key = Integer.parseInt(s);
				switch (key) {
				case 1:
				{
					roomlogic = null;
					System.out.println("请输入房间号：");
					s = sc.nextLine();
					int roomId= Integer.parseInt(s);
					roomlogic = RoomManager.getInstance().getRoom(roomId);
					if(roomlogic!=null){
						System.out.println("进入房间成功");
					}else {
						System.out.println("房间号不存在");
					}
					
					break;
				}
				case 2:
				{
					if(roomlogic!=null)
					{
						int index = roomlogic.getListCardIndex();
						System.out.println("当前牌的索引："+ index);
						List<Integer> listCard = roomlogic.getListCard();
						int count = listCard.size();
						System.out.println("共有剩余牌"+ count + "张：");
						for (Integer card : listCard) {
							System.out.print(card);
							System.out.print(",");
						}
						System.out.print("\n");
					}else {
						System.out.println("还没有进入房间，请先进入房间");
					}
					break;
				}
				case 3:
				{
					if(roomlogic!=null)
					{
						System.out.println("\n请输入需要被替换的牌的索引：");
						sc = new Scanner(System.in);
						s = sc.nextLine();
						int index = Integer.parseInt(s);
						System.out.println("\n请输入需要被替换的牌的牌值：");
						sc = new Scanner(System.in);
						s = sc.nextLine();
						int cardInt = Integer.parseInt(s);
						String result = roomlogic.setListCard(index, cardInt);
						System.out.println(result);
					}else {
						System.out.println("还没有进入房间，请先进入房间");
					}
					break;
				}
				default:
					break;
				}
								
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.toString());
			}
			
			
		}		
	}
}
