package com.weipai.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weipai.model.Manager;

public interface ManagerService {
	int deleteByPrimaryKey(Integer id);

    int save(Manager record);

    int saveSelective(Manager record);

    Manager selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Manager record);

    int updateByPrimaryKey(Manager record);
	/**
	 * 根据用户名得到密码
	 * @param username
	 * @return
	 */
	Manager selectManagerByUsername(String username);
	  /**
     * 根据不同条件得到代理商/零售商列表
     * @param map
     * @return
     */
    List<Manager> selectObjectsByMap(Map<String ,Integer> map);
    /**
     * @param id  被充值玩家的id
     * @param manager  充值管理员
     * @param payCardNum  充值房卡的数量
     * @return
     */
    JSONObject  updateAccountRoomCard(Integer id,Manager manager,Integer payCardNum);
    /**
     * @param id  被充值代理的id
     * @param manager  充值管理员
     * @param payCardNum  充值房卡的数量
     * @return
     */
    JSONObject  updateManagerRoomCard(Integer id,Manager manager,Integer payCardNum);
	/**
	 * 获取中奖人信息
	 * @param status
	 * @return
	 */
    JSONArray getWinnersInfo(String status);
    /**
     * 获取所有精品信息
     * @return
     */
    JSONArray getPrizesInfo();
    /**
     * 修改单个奖品信息
     * @param map
     * @return
     */
    JSONObject updatePrizeInfo(Map<String,Object> map);
    /**
     * 增加公告
     * @param notice
     * @return
     */
    JSONObject saveNewNotice(String notice);
    /**
     * 根据手机号码和manager_up_id查找对象
     * @param map
     * @return
     */
    List<Manager> selectManagerByTel(Map<String, Object> map);
    
    /**
     *  修改代理商的状态(删除代理商)
     * @param manager  被删除代理
     * @param managerId  操作人id
     * @return
     */
    int updateManagerStatus(Manager manager , Integer managerId);
    /**
     * 修改密码和手机号码
     * @param map
     * @return
     */
    int updateByMap(Map<String,Object> map);

	JSONObject getAllGameInfos();
	
	//管理员获取个人房卡流水信息
	JSONArray roomCardWaterCourse(int managerId);

	//管理员获取个人操作流水信息
	JSONArray operationWaterCourse(int managerId);

}
