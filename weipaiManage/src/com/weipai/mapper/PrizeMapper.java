package com.weipai.mapper;

import java.util.List;
import java.util.Map;

import com.weipai.model.Prize;

public interface PrizeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Prize record);

    int insertSelective(Prize record);

    Prize selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Prize record);

    int updateByPrimaryKey(Prize record);
    /**
     * 获取所有奖品信息
     * @return
     */
    List<Prize> selectPrizes();
    /**
     * 修改单个的奖品信息
     * @return
     */
    int updatePrizeByMap(Map<String,Object>map);
}