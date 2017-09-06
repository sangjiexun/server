package com.weipai.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weipai.common.Params;
import com.weipai.controller.base.BaseController;
import com.weipai.model.Account;
import com.weipai.model.ContactWay;
import com.weipai.model.Manager;
import com.weipai.model.NoticeTable;
import com.weipai.model.PrizeRule;
import com.weipai.service.AccountService;
import com.weipai.service.ContactWayService;
import com.weipai.service.ManagerService;
import com.weipai.service.NoticeTableService;
import com.weipai.service.PrizeRuleService;
import com.weipai.utils.StringUtil;

@Controller
@RequestMapping("/controller/manager")
public class ManagerController extends BaseController {

	@Autowired
	private ManagerService managerService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private NoticeTableService noticeTableService;
	@Autowired
	private PrizeRuleService prizeRuleService;
	@Autowired
	private ContactWayService contactWayService;

	/**
	 * 后台登录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		JSONObject json = new JSONObject();
		if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
			Manager manager = managerService.selectManagerByUsername(username);
			
			System.out.println(StringUtil.MD5(password));
			
			if (manager != null
					&& StringUtil.MD5(password).equals(manager.getPassword())) {
				json.put("mess", "0");
				//登录成功 用户信息放入缓存
				session.setAttribute("manager", manager);
				session.setAttribute("actualCard", manager.getActualcard());
				session.setAttribute("totalCards", manager.getTotalcards());
				session.setAttribute("tel", manager.getTelephone());
				session.setAttribute("name", manager.getName());
				session.setAttribute("type", manager.getPowerId());
				session.setAttribute("id", manager.getId());
				if(manager.getPowerId() == 1){
					//如果是超管登录  则需要返回最新公告
					NoticeTable notice = noticeTableService.selectRecentlyObject();
					ContactWay contactWay = contactWayService.selectByPrimaryKey(1);
					if(notice != null){
						session.setAttribute("notice", notice.getContent()+"");
						session.setAttribute("contactway", contactWay.getContent()+"");
					}
				}
			} else {
				json.put("mess", "1");
			}
		} else {
			json.put("mess", "2");
		}
		returnMessage(response, json);
	}
	
	/**
	 * 超管修改代理商密码
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/updateManagerPwd")
	public void updateManagerPwd(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		String managerId = request.getParameter("managerId");
		String newPwd = request.getParameter("newPwd");
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager != null && manager.getPowerId() == 1){
			//只有超管才能找回代理商密码
			if(StringUtil.isInteger(managerId, 1000, 2) && StringUtil.isNotEmpty(newPwd)){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("id",Integer.parseInt(managerId));
				map.put("password", StringUtil.MD5(newPwd));
				try {
					int i = managerService.updateByMap(map);
					if(i == 1){
						json.put("status_code","0");
					}
					else{
						json.put("status_code", "1");
						json.put("error", "传入参数有误!");
					}
				} catch (Exception e) {
					e.printStackTrace();
					json.put("status_code", "1");
					json.put("error", "信息修改异常!");
				}
			}
			else{
				json.put("status_code", "1");
				json.put("error", "传入参数有误!");
			}
		}
		else{
			json.put("status_code", "1");
			json.put("error", "只能通过超级管理员才能找回密码!");
		}
		returnMessage(response, json);
	}
	
	/**
	 *后台管理员修改个人信息(密码/手机号码)
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updateManagerInfo ")
	public void updateManagerInfo(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String newPwd = request.getParameter("newPwd");
		String newTel = request.getParameter("oneTel");
		String oldPwd = request.getParameter("oldPwd");
		String oldTel = request.getParameter("oldTel");
		boolean pwd =false;
		boolean tel = false;
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				Map<String,Object> map = new HashMap<String, Object>();
				if(StringUtil.isNotEmpty(newPwd)){
					if(StringUtil.isNotEmpty(oldPwd)){
						if(manager.getPassword().equals(StringUtil.MD5(oldPwd))){
							map.put("password", StringUtil.MD5(newPwd));
							pwd = true;
						}
						else{
							json.put("status", "1");
							json.put("error", "旧密码输入错误!");
						}
					}
					else{
						json.put("status", "1");
						json.put("error", "修改密码时，必须输入旧密码!");
					}
				}
				
				if(StringUtil.isPhone(newTel)){
					if(StringUtil.isNotEmpty(oldTel)){
						if(manager.getTelephone().equals(oldTel)){
							map.put("telephone", newTel);
							tel = true;
						}
						else{
							json.put("status", "1");
							json.put("error", "旧手机号码输入错误!");
						}
					}
					else{
						json.put("status", "1");
						json.put("error", "修改手机号码时，必须输入旧手机号!");
					}
				}
				if(!map.isEmpty()){
					map.put("id", manager.getId());
					managerService.updateByMap(map);
					//更新session里面的manager信息
					if(pwd){
						manager.setPassword(StringUtil.MD5(newPwd));
					}
					if(tel){
						manager.setTelephone(newTel);
						json.put("tel",newTel);
					}
					session.setAttribute("manager",manager);
					json.put("status", "0");
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
					
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "请先登录");
				
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 * 获取游戏服务器相关信息  开放数量，在线人数，总用户等信息
	 * 在本地取的参数有： 1:会员()总数  2：售出房卡总数   3：房间总数  4:玩家总数 5：总房卡数(玩家房卡总数额/代理商房卡总数)
	 * //6:今天创建房间总数 
	 * //7:今日售出(今天充值-今天退卡)
	 * 	//8:本周售出(本周充值-本周退卡)
	 * //9:今日新增用户
	 * 
	 * 
	 * 到游戏服务器取的参数有： 1：当前在线房间总数   2：当前在线人数
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/getAllGameInfos")
	public void getAllGameInfos(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager != null && manager.getPowerId() == 1 ){
			json = managerService.getAllGameInfos();
		}
		returnMessage(response, json);
	}
	
	
	/**
	 * 获取所有玩家
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/getAccounts")
	public void getAccounts(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager != null && manager.getPowerId() == 1 ){
			Map<String , Integer> map = new HashMap<String, Integer>();
			map.put("startNum", 0);
			map.put("pageNumber", Params.pageNumber);
			//获取所有玩家   
			List<Account> accounts = accountService.selectAllAccount(map);
			for (Account account : accounts) {
				array.add(account);
			}
		}
		returnMessage(response, array);
	}
	
	/**
	 * 获取所有经销商 代理商
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/getManagers")
	public void getManagers(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager != null){
			Map<String , Integer> map = new HashMap<String, Integer>();
			map.put("pageNumber", Params.pageNumber);
			if(manager.getPowerId() != 1){
				map.put("startNum", 0);
				//普通代理商只能获取自己下面的
				map.put("managerUpId", manager.getId());
			}else{
				//超级管理员可以获取所有
				map.put("startNum", 0);
			}
			//获取所有玩家   
			List<Manager> managers = managerService.selectObjectsByMap(map);
			for (Manager m : managers) {
				array.add(m);
			}
			returnMessage(response, array);
		}
	}
	
	
	/**
	 * 添加代理商
	 * @param request
	 * @param response
	 */
	@RequestMapping("/addProxyAccount")
	public void addProxyAccount(HttpServletRequest request, HttpServletResponse response){
		String  newManagerName = request.getParameter("newManagerName");
		String  newManagerPwd = request.getParameter("newManagerPwd");
		String  newManagerTel = request.getParameter("newManagerTel");
		String  newManagerType = request.getParameter("newManagerType");
		JSONObject json = new JSONObject();
		HttpSession session = request.getSession();
		//后期考虑  代理商是否能添加代理商，零售商是否能添加零售商
		if(session != null ){
			if(StringUtil.isPhone(newManagerTel) && StringUtil.isNotEmpty(newManagerName) && StringUtil.isNotEmpty(newManagerPwd)){
				Manager managerOld = managerService.selectManagerByUsername(newManagerName);
				if(managerOld == null){
					//判断账户是否已经被注册
					managerOld = new Manager();
					managerOld. setName(newManagerName);
					managerOld. setPowerId(Integer.parseInt(newManagerType)); 
					managerOld.setTelephone(newManagerTel);
					managerOld.setPassword(StringUtil.MD5(newManagerPwd));
					managerOld. setManagerUpId(((Manager)session.getAttribute("manager")).getId());
					try {
						int result = managerService.saveSelective(managerOld);
						if(result > 0){
							json.put("status", "0");
						}else{
							json.put("status", "1");
							json.put("error", "用户添加失败");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "用户添加异常");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "账户已经被注册");
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "传入参数有误");
			}
		}
		else{
			json.put("status", "1");
			json.put("error", "账户未登录");
		}
		returnMessage(response,json);
	}
	
	
	/**
	 * 查询自己下面所有的用户
	 * @param request
	 * @param response
	 */
	@RequestMapping("/selectAllProxyByMyself")
	public void selectAllProxyByMyself(HttpServletRequest request, HttpServletResponse response){
		
	}
	
	/**
	 * 查询自己下面所有的充值记录
	 * @param request
	 * @param response
	 */
	@RequestMapping("/selectAllLogByMyself")
	public void selectAllLogByMyself(HttpServletRequest request, HttpServletResponse response){
		
	}
	/**
	 *超级管理员可以为所有用户充值房卡
	 *零售商只为玩家充值
	 *代理商只为零售商充值
	 * @param request
	 * @param response
	 */
	@RequestMapping("/addActualCardToAccount")
	public void addActualCardToAccount(HttpServletRequest request, HttpServletResponse response){
		int userid = Integer.parseInt(request.getParameter("userid"));
		int managerid = Integer.parseInt(request.getParameter("managerid"));
		String paycardnum = request.getParameter("payCardNum");
		JSONObject json = new JSONObject();
		if(StringUtil.isInteger(paycardnum, 0, 0)){
			int payCardNum = Integer.parseInt(paycardnum);
			HttpSession session = request.getSession();
			if(session != null){
				Manager manager = (Manager) session.getAttribute("manager");
				if(manager != null && payCardNum != 0){
					manager = managerService.selectByPrimaryKey(manager.getId());
					//检查mananger的房卡是否够充值
					if(userid != 0 && managerid == 0){
						//说明是为玩家充值
						json = managerService.updateAccountRoomCard(userid, manager, payCardNum);
					}
					else if(managerid != 0 && userid ==0){
						//说明是为代理商或者经销商充值
						json = managerService.updateManagerRoomCard(managerid, manager, payCardNum);
					}
					else{
						System.out.println("传入参数有误");
						json.put("status", "1");
						json.put("error", "传入参数有误");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
				}
			}
			else{
				System.out.println("未建立链接就进行充值的非法操作");
				json.put("status", "1");
				json.put("error", "未建立链接就进行充值的非法操作");	
			}
		}
		else{
			json.put("status", "1");
			json.put("error", "请输入正确的充值房卡数量");	
		}
		returnMessage(response,json);
	}
	
	/**
	 * 为代理商或者零售商用户充值房卡
	 * @param request
	 * @param response
	 */
	/*@RequestMapping("/addActualCardToProxy")
	public void addActualCardToProxy(HttpServletRequest request, HttpServletResponse response){
		int id = Integer.parseInt(request.getParameter("id"));
		int addCardNum = Integer.parseInt(request.getParameter("addCardNum"));
	}*/
	/**
	 * 设置用户状态
	 * @param request
	 * @param response
	 */
	/*@RequestMapping("/setAccountStatus")
	public void setAccountStatus(HttpServletRequest request, HttpServletResponse response){
		int uuid = Integer.parseInt(request.getParameter("uuid"));
		int status = Integer.parseInt(request.getParameter("status"));
	}*/
	
	/**
	 * 删除代理商用户
	 * @param request
	 * @param response
	 */
	@RequestMapping("/deleteProxyAccount")
	public void deleteProxyAccount(HttpServletRequest request, HttpServletResponse response){
		int id = Integer.parseInt(request.getParameter("id"));
		int result = managerService.deleteByPrimaryKey(id);
		
		JSONObject json = new JSONObject();
		if(result > 0){
			json.put("mess", "删除代理商用户成功");
			System.out.println("删除代理商用户成功");
		}else{
			json.put("mess", "删除代理商用户失败");
		}
		returnMessage(response,json);
	}
	
	/**
	 * 获取所有奖品信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/prizesInfo")
	public void prizesInfo(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				try {
					array = managerService.getPrizesInfo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		returnMessage(response,array);
	}	
	
	/**
	 * 抽奖状态信息，
	 * @param request
	 * @param response
	 */
	@RequestMapping("/prizesStatus")
	public void prizesStatus(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String type = request.getParameter("type");//0 表示得到状态  1表示修改状态
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				PrizeRule prizeRule = prizeRuleService.selectByPrimaryKey(1);
				if(type.equals("0")){
					String status  = prizeRule.getStatus();
					json.put("status", status);
				}
				else if(type.equals("1")){
					String status = request.getParameter("status");
					prizeRule.setStatus(status);
					try {
						int i = prizeRuleService.updateByPrimaryKey(prizeRule);
						if(i > 0){
							json.put("status", "0");
						}
						else{
							json.put("status", "1");
							json.put("error", "开启/关闭失败");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "修改状态异常");
					}
				}
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 * 修改单个奖品信息概率，等信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updatePrizeInfo")
	public void updatePrizeInfo(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		String probability = request.getParameter("probability");
		String oneName = request.getParameter("oneName");
		String afterUpImg = request.getParameter("afterUpImg");
		String count = request.getParameter("count");
		String id = request.getParameter("id");
		String type = request.getParameter("type");
		JSONObject json = new JSONObject();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && StringUtil.isInteger(count, 0, 0)){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("id",Integer.parseInt(id));
				map.put("probability", Integer.parseInt(probability));
				map.put("prizeName", oneName);
				map.put("prizecount", Integer.parseInt(count));
				map.put("imageUrl", afterUpImg);
				map.put("type", type);
				try {
					json = managerService.updatePrizeInfo(map);
				} catch (Exception e) {
					e.printStackTrace();
					json.put("status_code", "1");
					json.put("error", "异常情况");
				}
			}
			else{
				json.put("status_code", "1");
				json.put("error", "请传入正确参数");
			}
		}
		returnMessage(response,json);
	}	
	
	/**
	 * 获取抽奖信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/winnersInfo")
	public void winnersInfo(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				String status = request.getParameter("status");
				try {
					array = managerService.getWinnersInfo(status);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			/*else{
				System.out.println("未登录就进行充值的非法操作");
				json.put("status", "1");
				json.put("error", "未登录就进行充值的非法操作");
			}*/
		}
		/*else{
			System.out.println("未建立链接就进行充值的非法操作");
			json.put("status", "1");
			json.put("error", "未建立链接就进行充值的非法操作");	
		}*/
		returnMessage(response,array);
	}
	
	/**
	 * 发送公告，通知游戏后台，发送公告
	 * @param request
	 * @param response
	 */
	@RequestMapping("/sendNotice")
	public void sendNotice(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				String notice = request.getParameter("notice");
				try {
					json = managerService.saveNewNotice(notice);
				} catch (Exception e) {
					e.printStackTrace();
					json.put("status_code", "1");
			        json.put("error", "异常情况");
				}
			}
		}
		returnMessage(response,json);
	}
	/**
	 * 修改奖品规则
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updatePrizeRule")
	public void updatePrizeRule(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String prizeRule = request.getParameter("prizeRule");
		String prizePerDay = request.getParameter("prizePerDay");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && manager.getPowerId() == 1){
				if(StringUtil.isNotEmpty(prizeRule) && StringUtil.isInteger(prizePerDay, 100, 0)){
					PrizeRule prizerule = new PrizeRule();
					prizerule.setId(1);
					prizerule.setContent(prizeRule);
					prizerule.setPrecount(Integer.parseInt(prizePerDay));
					try {
						prizeRuleService.updateByPrimaryKeySelective(prizerule);
						json.put("status_code", "0");
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status_code", "1");
						json.put("error", "信息修改异常");
					}
				}
				else{
					json.put("status_code", "1");
					json.put("error", "抽奖次数和规则不能为空");
				}
			}
			else{
				json.put("status_code", "1");
				json.put("error", "只有超级管理员才能发布信息");
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 * 修改充卡联系信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updateContactway ")
	public void updateContactway(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String newContact = request.getParameter("newContact");
		System.out.println(newContact);
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && manager.getPowerId() == 1){
				if(StringUtil.isNotEmpty(newContact)){
					ContactWay contactWay  = new ContactWay();
					contactWay.setId(1);
					contactWay.setContent(newContact);
					try {
						contactWayService.updateByPrimaryKeySelective(contactWay);
						json.put("status_code", "0");
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status_code", "1");
						json.put("error", "修改出现异常");
					}
				}
				else{
					json.put("status_code", "1");
					json.put("error", "保存信息不能为空");
				}
			}
			else{
				System.out.println("只有超级管理员才能修改");
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 *只能根据准确的uuid搜索到玩家，
	 * @param request
	 * @param response
	 */
	@RequestMapping("/searchAccountByUuid ")
	public void searchAccountByUuid(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String accountUuid = request.getParameter("accountUuid");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				if(StringUtil.isNotEmpty(accountUuid) && StringUtil.isInteger(accountUuid, 0, 0)){
					try {
						Account account = accountService.selectByUuid(Integer.parseInt(accountUuid));
						if(account != null){
							json.put("status_code", "0");
							json.put("data", account);
							json.put("type", manager.getPowerId());
						}
						else{
							json.put("status_code", "1");
							json.put("error", "用户不存在");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status_code", "1");
						json.put("error", "异常情况");
					}
				}
				else{
					json.put("status_code", "1");
					json.put("error", "错误的玩家id");
				}
			}
			else{
				System.out.println("管理员未登录，请登录");
			}
		}
		returnMessage(response,json);
	}
	/**
	 *只能根据准确的手机号码搜索到代理商
	 * @param request
	 * @param response
	 */
	@RequestMapping("/searchManagerByTel ")
	public void searchManagerByTel(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		String managerTel = request.getParameter("managerTel");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				if(StringUtil.isNotEmpty(managerTel) && StringUtil.isPhone(managerTel)){
					try {
						Map<String,Object> map = new HashMap<String, Object>();
						map.put("telephone", managerTel);
						if(manager.getId() != 1){
							map.put("managerUpId", manager.getId());
						}
						List<Manager> searchManager = managerService.selectManagerByTel(map);
						if(!searchManager.isEmpty()){
							for (Manager m : searchManager) {
								array.add(m);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		returnMessage(response,array);
	}
	
	/**
	 *超管给代理商减房卡
	 * @param request
	 * @param response
	 */
	@RequestMapping("/reduceManagerRoomCard ")
	public void reduceManagerRoomCard(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String reducedManagerId = request.getParameter("managerId");
		String cardNumb = request.getParameter("cardNumb");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && manager.getId() == 1){
				if(StringUtil.isNotEmpty(reducedManagerId) && StringUtil.isNotEmpty(cardNumb)
						&& StringUtil.isInteger(cardNumb, 10000, 0) && StringUtil.isInteger(reducedManagerId, 0, 0) ){
					try {
						//判断代理商的房卡是否够减
						int reducedmanagerId = Integer.parseInt(reducedManagerId);
						Manager  mana = managerService.selectByPrimaryKey(reducedmanagerId);
						int cardnumb  =  0 - Integer.parseInt(cardNumb);
						int actualCard = mana.getActualcard();
						if(actualCard >= Math.abs(cardnumb)){
							json = managerService.updateManagerRoomCard(reducedmanagerId, manager, cardnumb);
							Manager searchManager = managerService.selectByPrimaryKey(reducedmanagerId);
							json.put("data", searchManager);
						}
						else{
							json.put("status", "1");
							json.put("error", "该代理商房卡不够减");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "异常情况");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "只有超管才能减房卡");
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 *超管给玩家减房卡
	 * @param request
	 * @param response
	 */
	@RequestMapping("/reduceAccountRoomCard ")
	public void reduceAccountRoomCard(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String reducedAccountId = request.getParameter("accountId");
		String cardNumb = request.getParameter("cardNumb");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && manager.getId() == 1){
				if(StringUtil.isNotEmpty(reducedAccountId) && StringUtil.isNotEmpty(cardNumb)
						&& StringUtil.isInteger(cardNumb, 10000, 0) && StringUtil.isInteger(reducedAccountId, 0, 0) ){
					try {
						//判断玩家的房卡是否够减
						int reducedaccountId = Integer.parseInt(reducedAccountId);
						Account  acc = accountService.selectByPrimaryKey(reducedaccountId);
						int cardnumb  =  0 - Integer.parseInt(cardNumb);
						int roomCard = acc.getRoomcard();
						if(roomCard >= Math.abs(cardnumb)){
							json = managerService.updateAccountRoomCard(reducedaccountId, manager, cardnumb);
							if(json.get("status").equals("0")){
								acc.setTotalcard(acc.getTotalcard()+cardnumb);
								acc.setRoomcard(acc.getRoomcard() + cardnumb);
								json.put("data", acc);
							}
						}
						else{
							json.put("status", "1");
							json.put("error", "该玩家房卡不够减");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "异常情况");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "只有超管才能减房卡");
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 *修改玩家状态(删除玩家)
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updateAccountStatus ")
	public void updateAccountStatus(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String id = request.getParameter("accountId");
		String status = request.getParameter("status");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null && manager.getId() == 1){
				if(StringUtil.isNotEmpty(id) && StringUtil.isNotEmpty(status)){
					try {
						Map<String,Object> map = new HashMap<String, Object>();
						map.put("id", id);
						map.put("status", status);
						
						int i = accountService.updateAccountStatus(map);
						if(i >= 1){
							json.put("status", "0");
						}
						else{
							json.put("status", "1");
							json.put("error", "信息修改有误");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "信息修改有误");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "只有超管才能删除玩家");
			}
		}
		returnMessage(response,json);
	}
	
	/**
	 *超管给玩家减房卡
	 * @param request
	 * @param response
	 */
	@RequestMapping("/updateManagerStatus ")
	public void updateManagerStatus(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		String manangerId = request.getParameter("manangerId");
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				if(StringUtil.isNotEmpty(manangerId)){
					int managerid = Integer.parseInt(manangerId);
					try {
						Manager updatedManager = managerService.selectByPrimaryKey(managerid);
						if(manager.getId() == 1 || updatedManager.getManagerUpId() == manager.getId()){
							//超级管理员不用判断被删除的代理是否在其下面
							int i = managerService.updateManagerStatus(updatedManager,manager.getId());
							//管理员操作日志记录
							
							if(i >= 1){
								json.put("status", "0");
							}
							else{
								json.put("status", "1");
								json.put("error", "删除失败");
							}
						}
						else{
							json.put("status", "1");
							json.put("error", "自己删除自己下面的代理商");
						}
					} catch (Exception e) {
						e.printStackTrace();
						json.put("status", "1");
						json.put("error", "信息修改出现异常");
					}
				}
				else{
					json.put("status", "1");
					json.put("error", "传入参数有误");
				}
			}
			else{
				json.put("status", "1");
				json.put("error", "请先登录");
			}
		}
		returnMessage(response,json);
	}
	
	
	/**
	 *后台管理员获取自己的房卡流水信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/roomCardWaterCourse ")
	public void roomCardWaterCourse(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				array  = managerService.roomCardWaterCourse(manager.getId());
			}
		}
		returnMessage(response,array);
	}
	
	/**
	 *后台管理员获取自己的操作流水信息
	 * @param request
	 * @param response
	 */
	@RequestMapping("/operationWaterCourse ")
	public void operationWaterCourse(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray array = new JSONArray();
		if(session != null){
			Manager manager = (Manager) session.getAttribute("manager");
			if(manager != null){
				array = managerService.operationWaterCourse(manager.getId());
			}
		}
		returnMessage(response,array);
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 退出登录
	 * @param request
	 * @param response
	 */
	@RequestMapping("/logout ")
	public void logout(HttpServletRequest request, HttpServletResponse response){
		
		HttpSession session = request.getSession();
		JSONObject json = new JSONObject();
		if(session != null){
			session.invalidate();
		}
		
	}
}
