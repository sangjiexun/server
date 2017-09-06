package com.weipai.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.weipai.model.Account;

public interface AccountMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Account record);

    int insertSelective(Account record);

    Account selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Account record);
    
    Account selectByUuid(Integer uuid);

    int updateByPrimaryKey(Account record);
    /**
     * 代理商/零售商得到其下面的所有普通用户
     * @param map
     * @return
     */
    List<Account> selectObjectsByMap(Map<String ,Integer> map);
    /**
     * <!-- 对得到条件代理商/零售商下面所有的用户个数 -->
     * @param map
     * @return
     */
    Integer selectObjectCountByMap(Map<String , Integer> map);
    /**
     * 得到所有的游戏玩家
     * @return
     */
    List<Account> selectAllAccount(Map<String , Integer> map);
    /**
     * 修改房卡数量
     * @param map
     * @return
     */
    int updateRoomCard(Map<String,Integer> map);
    /**
     * 修改玩家状态(删除玩家)
     * @param map
     * @return
     */
    int updateAccountStatus(Map<String,Object> map);
    /**
     * 得到玩家总数
     * @return
     */
    int selectAllCount();
    /**
     * 获取所有玩家的当前房卡总和
     * @return
     */
    int selectTotalAccountRoomCards();
    /**
     * 获取当天新增的玩家
     * @param date
     * @return
     */
    int selectNewAccountCountToday(Date date);
}