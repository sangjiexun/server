package com.weipai.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weipai.client.ClientSendRequest;
import com.weipai.common.OperationWaterCourse;
import com.weipai.common.RoomCardWaterCourse;
import com.weipai.mapper.AccountMapper;
import com.weipai.mapper.ManagerMapper;
import com.weipai.mapper.NoticeTableMapper;
import com.weipai.mapper.OperatelogMapper;
import com.weipai.mapper.PrizeMapper;
import com.weipai.mapper.RoomInfoMapper;
import com.weipai.mapper.TechargerecordMapper;
import com.weipai.mapper.WinnersInfoMapper;
import com.weipai.model.Account;
import com.weipai.model.Manager;
import com.weipai.model.NoticeTable;
import com.weipai.model.Operatelog;
import com.weipai.model.Prize;
import com.weipai.model.Techargerecord;
import com.weipai.model.WinnersInfo;
import com.weipai.service.ManagerService;
import com.weipai.utils.DateUtil;

@Service
@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
public class ManagerServiceImpl implements ManagerService {

	//替换后台查询游戏在线人数 以及实时冲卡ip
	public static final String IP_ADDR = "139.224.197.92";//服务器地址 
	public static final int PORT = 10123;//服务器端口号  
	
	@Resource
	ManagerMapper managerMapper;
	@Resource
	AccountMapper accountMapper;
	@Resource
	WinnersInfoMapper winnersInfoMapper;
	@Resource
	PrizeMapper prizeMapper;
	@Resource
	NoticeTableMapper noticeTableMapper;
	@Resource
	TechargerecordMapper techargerecordMapper;
	@Resource
	OperatelogMapper operatelogMapper;
	@Resource
	RoomInfoMapper roomInfoMapper;
	
	
	public Manager selectManagerByUsername(String username) {
		
		return managerMapper.selectObjectByUsername(username);
	}
	
	public List<Manager> selectObjectsByMap(Map<String, Integer> map) {
		
		return managerMapper.selectObjectsByMap(map);
	}

	public int deleteByPrimaryKey(Integer id){
		return managerMapper.deleteByPrimaryKey(id);
	}

	public int save(Manager record){
    	return managerMapper.save(record);
    }

	public int saveSelective(Manager record){
		int i  = managerMapper.saveSelective(record);
		if(i >= 1){
			proxyRecord(record,"0",1);
		}
		
    	return i;
    }

	public Manager selectByPrimaryKey(Integer id){
    	return managerMapper.selectByPrimaryKey(id);
    }

	public int updateByPrimaryKeySelective(Manager record){
    	return managerMapper.updateByPrimaryKeySelective(record);
    }

	public int updateByPrimaryKey(Manager record){
    	return managerMapper.updateByPrimaryKey(record);
    }

	
	/**
     * @param id  被充值玩家
     * @param manager 充值管理员
     * @param payCardNum  充值/减房卡的数量
     * @return
     */
	@Override
	public JSONObject updateAccountRoomCard(Integer accountid,Manager manager,
			Integer payCardNum) {
		JSONObject json = new JSONObject();
		if(manager.getPowerId() != 1){
			//1：不是超级管理元的情况下，检查mananger的房卡是否够充值
				if(manager.getActualcard() >= Math.abs(payCardNum)){
					//修改玩家用户房卡，同时修改代理商或经销商房卡数量
					Account account = accountMapper.selectByPrimaryKey(accountid);
					Map<String, Integer> param = new HashMap<String, Integer>();
					int finishRoomCard = account.getRoomcard() + payCardNum;
					int finishTotalcard = account.getTotalcard() + payCardNum;
					param.put("roomcard", finishRoomCard);
					param.put("totalcard", finishTotalcard);
					param.put("id", account.getId());
					manager = managerMapper.selectByPrimaryKey(manager.getId());
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("actualcard", manager.getActualcard() - payCardNum);
					map.put("id", manager.getId());
					try {
						//修改玩家房卡
						accountMapper.updateRoomCard(param);
						//代理商/经销商房卡
						managerMapper.updateActualcard(map);
						//增加修改记录
						roomCardRecord(accountid, 0, manager.getId(), payCardNum);
						json.put("status", "0");
						json.put("actualcard", manager.getActualcard() - payCardNum);//返回给页面
						account = accountMapper.selectByPrimaryKey(accountid);
						json.put("data", account);
						//修改完玩家房卡之后通知游戏后台
						String sendInfo = account.getId()+","+finishRoomCard;
						//发送消息给游戏后台
						Socket socket = null;
			        	try {
			        		//创建一个流套接字并将其连接到指定主机上的指定端口号
			        		socket = new Socket(IP_ADDR, PORT);  
				            //读取服务器端数据  
				            DataInputStream input = new DataInputStream(socket.getInputStream());  
				            //向服务器端发送数据  
				            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				            Thread.sleep(1000);
							//不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
							ClientSendRequest loginSend = new ClientSendRequest(0x158888);
							
							loginSend.output.writeUTF(sendInfo);
							out.write(loginSend.entireMsg().array());
							
							out.flush();
							out.close();
				            input.close();
			        	} catch (Exception e) {
			        		System.out.println("客户端异常:" + e.getMessage()); 
				            json.put("status", "1");
				            json.put("error", "游戏端发送消息异常");
			        	} finally {
			        		if (socket != null) {
			        			try {
									socket.close();
								} catch (IOException e) {
									socket = null; 
									System.out.println("客户端 finally 异常:" + e.getMessage()); 
								}
			        		}
			        	}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "数据处理有误");
					}
				}
				else{
					System.out.println("代理商/经销商房卡不足!");
					json.put("status", "1");
					json.put("error", "你的房卡不足");
				}
		}
		else{
			//超级管理员修改玩家用户房卡,但不修改超级管理员房卡数量
			Account account = accountMapper.selectByPrimaryKey(accountid);
			if(payCardNum < 0){
				//减房卡
				if(account.getRoomcard() >= Math.abs(payCardNum)){
					Map<String, Integer> param = new HashMap<String, Integer>();
					int finishRoomCard = account.getRoomcard() + payCardNum;
					int finishTotalcard = account.getTotalcard() + payCardNum;
					param.put("roomcard", finishRoomCard);
					param.put("totalcard", finishTotalcard);
					
					param.put("id", account.getId());
					json.put("data", "1");
					try {
						accountMapper.updateRoomCard(param);
						//增加修改记录
						//roomCardRecord(accountid, 0, manager.getId(), payCardNum);
						roomCardRecord(accountid, 0, 1, payCardNum);
						
						json.put("status", "0");
						//修改完玩家房卡之后通知游戏后台
						String sendInfo = account.getId()+","+finishRoomCard;
						//发送消息给游戏后台
						Socket socket = null;
						try {
							//创建一个流套接字并将其连接到指定主机上的指定端口号
							socket = new Socket(IP_ADDR, PORT);  
							//读取服务器端数据  
							DataInputStream input = new DataInputStream(socket.getInputStream());  
							//向服务器端发送数据  
							DataOutputStream out = new DataOutputStream(socket.getOutputStream());
							Thread.sleep(1000);
							//不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
							ClientSendRequest loginSend = new ClientSendRequest(0x158888);
							
							loginSend.output.writeUTF(sendInfo);
							out.write(loginSend.entireMsg().array());
							
							out.close();
							input.close();
						} catch (Exception e) {
							System.out.println("客户端异常:" + e.getMessage()); 
							json.put("status", "1");
							json.put("error", "游戏端发送消息异常");
						} finally {
							if (socket != null) {
								try {
									socket.close();
								} catch (IOException e) {
									socket = null; 
									System.out.println("客户端 finally 异常:" + e.getMessage()); 
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "数据处理有误");
					}

				}
				else{
					 json.put("status", "1");
			         json.put("error", "玩家房卡不够减数");
				}
				
			}
			else{
				//增加房卡
				Map<String, Integer> param = new HashMap<String, Integer>();
				int finishRoomCard = account.getRoomcard() + payCardNum;
				int finishTotalcard = account.getTotalcard() + payCardNum;
				param.put("roomcard", finishRoomCard);
				param.put("totalcard", finishTotalcard);
				
				param.put("id", account.getId());
				json.put("data", "1");
				try {
					accountMapper.updateRoomCard(param);
					roomCardRecord(accountid, 0, manager.getId(), payCardNum);
					
					json.put("status", "0");
					//修改完玩家房卡之后通知游戏后台
					String sendInfo = account.getId()+","+finishRoomCard;
					//发送消息给游戏后台
					Socket socket = null;
					try {
						//创建一个流套接字并将其连接到指定主机上的指定端口号
						socket = new Socket(IP_ADDR, PORT);  
						//读取服务器端数据  
						DataInputStream input = new DataInputStream(socket.getInputStream());  
						//向服务器端发送数据  
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						Thread.sleep(1000);
						//不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
						ClientSendRequest loginSend = new ClientSendRequest(0x158888);
						
						loginSend.output.writeUTF(sendInfo);
						out.write(loginSend.entireMsg().array());
						
						out.close();
						input.close();
					} catch (Exception e) {
						System.out.println("客户端异常:" + e.getMessage()); 
						json.put("status", "1");
						json.put("error", "游戏端发送消息异常");
					} finally {
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								socket = null; 
								System.out.println("客户端 finally 异常:" + e.getMessage()); 
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					json.put("status", "1");
					json.put("error", "数据处理有误");
				}
			}
		}
		return json;
	}

	 /**
     * @param id  代理商/经销商的id
     * @param manager 充值管理员
     * @param payCardNum  充值/减少房卡的数量（为负数）
     * @return
     */
	@Override
	public JSONObject updateManagerRoomCard(Integer managerid,Manager manager,
			Integer payCardNum) {
		JSONObject json = new JSONObject();
		if(manager.getPowerId() != 1){
			//1：不是超级管理元的情况下，检查mananger的房卡是否够充值
				if(manager.getActualcard() >= Math.abs(payCardNum)){
					//修改两边的房卡
					Manager managered = managerMapper.selectByPrimaryKey(managerid);
					Map<String, Integer> param = new HashMap<String, Integer>();
					param.put("actualcard", managered.getActualcard() + payCardNum);
					param.put("totalcards", managered.getTotalcards() + payCardNum);
					param.put("id", managered.getId());
					manager = managerMapper.selectByPrimaryKey(manager.getId());
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("actualcard", manager.getActualcard() - payCardNum);
					map.put("id", manager.getId());
					try {
						managerMapper.updateActualcard(param);
						//修改管理员/代理商
						managerMapper.updateActualcard(map);
						//增加充值记录
						roomCardRecord(0,managerid, manager.getId(), payCardNum);
						managered = managerMapper.selectByPrimaryKey(managerid);
						json.put("status", "0");
						json.put("actualcard",manager.getActualcard() - payCardNum);
						json.put("data", managered);
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "数据处理有误");
					}
				}
				else{
					System.out.println("代理商/经销商房卡不足!");
					json.put("status", "1");
					json.put("error", "你的房卡不足");
				}
		}
		else{
			//超管操作
			Manager managered = managerMapper.selectByPrimaryKey(managerid);
			if(payCardNum < 0){
				if(managered.getActualcard() >= Math.abs(payCardNum)){
					Map<String, Integer> param = new HashMap<String, Integer>();
					param.put("actualcard", managered.getActualcard() + payCardNum);
					param.put("totalcards", managered.getTotalcards() + payCardNum);
					param.put("id", managered.getId());
					try {
						managerMapper.updateActualcard(param);
						//增加充值记录
						roomCardRecord(0,managerid, 1, payCardNum);
						json.put("status", "0");
						json.put("data", "1");
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "数据处理有误");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "该代理房卡不够减数");
				}
			}
			else{
				Map<String, Integer> param = new HashMap<String, Integer>();
				param.put("actualcard", managered.getActualcard() + payCardNum);
				param.put("totalcards", managered.getTotalcards() + payCardNum);
				param.put("id", managered.getId());
				try {
					managerMapper.updateActualcard(param);
					//增加充值记录
					roomCardRecord(0,managerid, 1, payCardNum);
					json.put("status", "0");
					json.put("data", "1");
				} catch (Exception e) {
					e.printStackTrace();
					json.put("status", "1");
					json.put("error", "数据处理有误");
				}
			}
		}
		return json;
	}
	/**
	 * 根据状态status获取中奖人信息
	 * @param status
	 * @return
	 */
	public JSONArray getWinnersInfo(String status){
		JSONArray array = new JSONArray();
		
		List<WinnersInfo> winnersinfo = winnersInfoMapper.selectWinnersInfoByMap(status);
		JSONObject json;
		Account account;
		Prize prize;
		for (WinnersInfo winnerinfo : winnersinfo) {
			//微信账号weixin  	微信昵称nickName 	 奖品id:prizeId 	奖品名称:prizeName 
			//奖品数量（不需要） 	获奖时间createTime  发奖时间awardTime 	状态status
			json = new JSONObject();
			account = accountMapper.selectByPrimaryKey(winnerinfo.getAccountId());
			prize = prizeMapper.selectByPrimaryKey(winnerinfo.getPrizeId());
			json.put("weixin", "");//获取不到微信账号
			json.put("nickName", account.getNickname());
			json.put("prizeId", winnerinfo.getPrizeId());
			json.put("prizeName", prize.getPrizeName());
			json.put("createTime", DateUtil.toDefineString(winnerinfo.getCreatetime(), DateUtil.maskC));
			json.put("awardTime", DateUtil.toDefineString(winnerinfo.getAwardtime(), DateUtil.maskC));
			json.put("status", winnerinfo.getStatus());
			array.add(json);
		}
		return array;
	}
	/**
	 * 获取所有奖品信息
	 */
	@Override
	public JSONArray getPrizesInfo() {
		JSONArray array = new JSONArray();
		List<Prize> prizes = prizeMapper.selectPrizes();
		for (Prize prize : prizes) {
			array.add(prize);
		}
		return array;
	}
	/**
	 * 修改单个奖品信息
	 */
	@Override
	public JSONObject updatePrizeInfo(Map<String, Object> map) {
		
		JSONObject json = new  JSONObject();
		try {
			
			int i = prizeMapper.updatePrizeByMap(map);
			json.put("status_code","0");
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status_code", "1");
			json.put("error", "异常情况");
		}
		return json;
	}

	@Override
	public JSONObject saveNewNotice(String notice) {
		JSONObject json = new JSONObject();
		NoticeTable noticeTable = new NoticeTable();
		noticeTable.setContent(notice);
		noticeTable.setType(0);
		try {
			int count  = noticeTableMapper.insertSelective(noticeTable);
			if(count >= 1){
				//发送消息给游戏后台
				Socket socket = null;
	        	try {
	        		//创建一个流套接字并将其连接到指定主机上的指定端口号
	        		socket = new Socket(IP_ADDR, PORT);  
		            //读取服务器端数据  
		            DataInputStream input = new DataInputStream(socket.getInputStream());  
		            //向服务器端发送数据  
		            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		            Thread.sleep(3000);
					//不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
					ClientSendRequest loginSend = new ClientSendRequest(0x158888);
					loginSend.output.writeUTF("notice");
					out.write(loginSend.entireMsg().array());
					
					out.close();
		            input.close();
		            json.put("status_code", "0");
	        	} catch (Exception e) {
	        		System.out.println("客户端异常:" + e.getMessage()); 
		            json.put("status_code", "1");
		            json.put("error", "游戏端发送消息异常");
	        	} finally {
	        		if (socket != null) {
	        			try {
							socket.close();
						} catch (IOException e) {
							socket = null; 
							System.out.println("客户端 finally 异常:" + e.getMessage()); 
						}
	        		}
	        	}
			}
			else{
				json.put("status_code", "1");
				json.put("error", "信息录入失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status_code", "1");
			json.put("error", "信息录入异常");
		}
		return json;
	}

	@Override
	public List<Manager> selectManagerByTel(Map<String, Object> map) {
		
		return managerMapper.selectManagerByTel(map);
	}

	@Override
	public int updateManagerStatus(Manager manager,Integer managerId) {
		
		int i = managerMapper.updateManagerStatus(manager.getId());
		if(i >= 1){
			proxyRecord(manager,"1",managerId);
		}
		return i;
	}
	/**
	 * 录入充值记录  
	 * @param accountId  被充值玩家id
	 * @param managerId 被充值代理商id
	 * @param manger_up_id  充值管理员id(超管id或代理商id)
	 * @param payCardNum  充卡(正数)/退卡(负数)数量
	 * @throws ParseException 
	 */
	public void roomCardRecord(int accountId,int managerId,int manger_up_id,int payCardNum) throws ParseException{
		Techargerecord techargerecord = new Techargerecord();
		if(accountId != 0){
			techargerecord.setAccountId(accountId);
		}
		else if(managerId != 0){
			techargerecord.setManagerId(managerId);
		}
		techargerecord.setCreatetime(DateUtil.toChangeDate(new Date(), DateUtil.maskC));
		techargerecord.setManagerUpId(manger_up_id);
		techargerecord.setStatus("0");
		techargerecord.setTechargemoney(payCardNum);
		if(payCardNum < 0){
			techargerecord.setMark("退卡");
		}
		else{
			techargerecord.setMark("充值");
		}
		int i = techargerecordMapper.saveSelective(techargerecord);
		if(i < 1){
			System.out.println("充值/退卡信息记录录入失败!");
		}
	}
	/**
	 * 
	 * @param record 被增加的代理/被删除的代理
	 * @param type  记录类型(0:添加代理    1：删除代理)
	 */
	public void proxyRecord(Manager record,String type,Integer managerId){
		//添加，删除代理的日志记录
		Operatelog operatelog = new Operatelog();
		try {
			operatelog.setCreatetime(DateUtil.toChangeDate(new Date(), DateUtil.maskC));
			operatelog.setManagerDownId(record.getId());
			if(type.equals("0")){
				operatelog.setMark("新增");
				operatelog.setManagerId(record.getManagerUpId());
			}
			else{
				operatelog.setMark("删除");
				operatelog.setManagerId(managerId);
			}
			operatelog.setType(type);
			int i = operatelogMapper.saveSelective(operatelog);
			if(i <= 0){
				System.out.println("代理操作，日志信息录入失败");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public int updateByMap(Map<String, Object> map) {
		
		return managerMapper.updateByMap(map);
	}
	

	/**
	 *获取游戏服务器相关信息  开放数量，在线人数，总用户等信息
	 * 在本地取的参数有： 1:会员()总数  2：售出房卡总数   3：房间总数   4：总房卡数(玩家房卡总数额/代理商房卡总数)
	 * //5:今天创建房间总数 
	 * //6:今日售出(今天充值-今天退卡)
	 * 	//7:本周售出(本周充值-本周退卡)
	 * //8:今日新增用户
	 * 
	 * 
	 * 到游戏服务器取的参数有： 1：当前在线房间总数   2：当前在线人数
	 * 修改单个奖品信息
	 */
	@Override
	public JSONObject getAllGameInfos() {
		JSONObject json = new  JSONObject();

		//发送消息给游戏后台
		Socket socket = null;
    	try {
    		//创建一个流套接字并将其连接到指定主机上的指定端口号
    		socket = new Socket(IP_ADDR, PORT);  
            //读取服务器端数据  
            DataInputStream input = new DataInputStream(socket.getInputStream());  
            //向服务器端发送数据  
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			//不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
			ClientSendRequest loginSend = new ClientSendRequest(0x154444);
			loginSend.output.writeUTF("notice");
			out.write(loginSend.entireMsg().array());
			String content = serverCallBack(input);//游戏服务器返回的信息
			//直接从数据库取的信息
			//一：概况   二：房间统计    三： 房卡统计   四：玩家统计
			
			//一：概况---------------------------
			//1:总会员数(玩家总数)
			int totoalAccountCount = accountMapper.selectAllCount();
			  json.put("totoalAccountCount", totoalAccountCount);
			//2:历史总房间个数
			int AllRoomsCount = roomInfoMapper.selectCount();
			  json.put("AllRoomsCount", AllRoomsCount);
			//3：消耗房卡总数(根据房间获取消耗房卡总数)
			int consumedRoomCardCount = roomInfoMapper.selectConsumedRoomCardCount();
			  json.put("consumedRoomCardCount", consumedRoomCardCount);
			//4：微信充值房卡统计
			
			
			//二：房间统计---------------------------
			//5：在线房间统计，游戏服务器返回
			int onlineRoomsCount = Integer.parseInt(content.split(",")[0]);
			  json.put("onlineRoomsCount",onlineRoomsCount);
			//6:今天新建房间个数
			String currentTime = DateUtil.toDefineString(new Date(), DateUtil.maskL);
			currentTime = currentTime+" 00:00:00";
			int todayRoomsCount = 0;
			Date date = null;
			try {
				date = DateUtil.toDefineMaskDate(currentTime, DateUtil.maskC);
				todayRoomsCount = roomInfoMapper.selectTodayCount(date);
				  json.put("todayRoomsCount",todayRoomsCount);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			//三： 房卡统计---------------------------
			//7:历史充值房卡总数(管理员卖给代理的历史房卡总数)
			int totalRoomCards = techargerecordMapper.selectTotalRoomCards();
			json.put("totalRoomCards",totalRoomCards);
			//8：当天充值房卡总数(管理员当天卖给代理的房卡总数)
			int saleRoomCardsToday = techargerecordMapper.selectSaleRoomCardsToday(date);
			 json.put("saleRoomCardsToday",saleRoomCardsToday);
			//int totalAccountRoomCards = accountMapper.selectTotalAccountRoomCards();
			//9：今日售出房卡总数(卖给代理商的还是卖给玩家的？还是一起算上？)
			//int saleRoomCardsToday = techargerecordMapper.selectSaleRoomCardsToday(date);
			//10：本周充值房卡总数
			Date firstDayOfWeek = DateUtil.getFirstDayOfWeek(date);
			int saleRoomCardsThisWeek = techargerecordMapper.saleRoomCardsThisWeek(date);
			  json.put("saleRoomCardsThisWeek",saleRoomCardsThisWeek);
			
			//四：玩家统计---------------------------
			//11:当前在线人数 
			int onlineAccountCount = Integer.parseInt(content.split(",")[1]);
			  json.put("onlineAccountCount",onlineAccountCount);
			//12:今日最高在线人数 (从游戏服务器取)
			int topOnlineAccountCount = Integer.parseInt(content.split(",")[2]);
			  json.put("topOnlineAccountCount",topOnlineAccountCount);
			//12：今日新增用户
			int newAccountCountToday = accountMapper.selectNewAccountCountToday(date); 
			  json.put("newAccountCountToday",newAccountCountToday);
			
			out.close();
            input.close();
            json.put("status_code", "0");
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("客户端异常:" + e.getMessage()); 
            json.put("status_code", "1");
            json.put("error", "游戏端发送消息异常");
    	} finally {
    		if (socket != null) {
    			try {
					socket.close();
				} catch (IOException e) {
					socket = null; 
					System.out.println("客户端 finally 异常:" + e.getMessage()); 
				}
    		}
    	}
		return json;
	}
	//返回游戏服务器返回的信息
	/**
	 *   获取游戏服务器相关信息  开放数量，在线人数，总用户等信息
	 * 在本地取的参数有： 1:会员()总数  2：售出房卡总数   3：房间总数  4:玩家总数 5：总房卡数(玩家房卡总数额/代理商房卡总数)
	 * //6:今天创建房间总数 
	 * //7:今日售出(今天充值-今天退卡)
	 * 	//8:本周售出(本周充值-本周退卡)
	 * //9:今日新增用户
	 * 
	 * 
	 * 到游戏服务器取的参数有： 1：当前在线房间总数   2：当前在线人数
	 * @param input
	 * @return
	 */
	public static String serverCallBack(DataInputStream input){
		StringBuffer sb = new StringBuffer();
		try {
			ByteArrayOutputStream outSteam = new ByteArrayOutputStream();  
			byte[] buffer = new byte[input.readInt()];
		    int len = -1;
			if((len = input.read(buffer)) != -1) { 
			        outSteam.write(buffer, 11, len-11);  
			        sb.append(outSteam.toString());
			  } 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
			
	}

	/**
	 * 房卡流水
	 */
	@Override
	public JSONArray roomCardWaterCourse(int managerId) {
		JSONArray array = new JSONArray();
		List<Techargerecord> list = techargerecordMapper.selectByRelateId(managerId);
		RoomCardWaterCourse waterCourse;
		for (Techargerecord t : list) {
			waterCourse = new RoomCardWaterCourse();
			waterCourse.setCreateTime(DateUtil.toDefineString(t.getCreatetime(), DateUtil.maskC) );
			waterCourse.setMark(t.getMark());
			//判断 1：该管理员为玩家充值，2：该管理员为其他管理员充值  3：其他管理官为该管理员充值
			String name = null;
			int numb = t.getTechargemoney();
			if(t.getManagerId() != null && t.getManagerId() == managerId){
				//别的管理员为自己充卡（或超级管理员退卡操作）
				name = managerMapper.selectByPrimaryKey(t.getManagerUpId()).getName();
			}
			else if(t.getManagerUpId() != null && t.getManagerUpId() == managerId){
				//自己为别人充卡
				if(t.getManagerId() != null && t.getManagerId() > 0){
					//为代理充卡
					name =managerMapper.selectByPrimaryKey(t.getManagerId()).getName();
					numb = 0 - numb;
				}
				else if(t.getAccountId() != null && t.getAccountId() > 0){
					//为玩家充卡
					name =accountMapper.selectByPrimaryKey(t.getAccountId()).getNickname();
					numb = 0 - numb;
				}
				
			}
			waterCourse.setName(name);
			waterCourse.setNumb(numb);
			array.add(waterCourse);
		}
		return array;
	}
	
	/**
	 * 操作流水
	 */
	@Override
	public JSONArray operationWaterCourse(int managerId) {
		JSONArray array = new JSONArray();
		List<Operatelog> list = operatelogMapper.selectByRelateId(managerId);
		OperationWaterCourse operationWaterCourse;
		for (Operatelog o : list) {
			operationWaterCourse = new OperationWaterCourse();
			operationWaterCourse.setCreateTime(DateUtil.toDefineString(o.getCreatetime(), DateUtil.maskC) );
			operationWaterCourse.setMark(o.getMark());
			operationWaterCourse.setName(managerMapper.selectByPrimaryKey(o.getManagerDownId()).getName());
			array.add(operationWaterCourse);
		}
		return array;
	}
}
