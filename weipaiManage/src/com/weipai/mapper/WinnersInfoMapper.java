package com.weipai.mapper;

import java.util.List;

import com.weipai.model.WinnersInfo;

public interface WinnersInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(WinnersInfo record);

    int insertSelective(WinnersInfo record);

    WinnersInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WinnersInfo record);

    int updateByPrimaryKey(WinnersInfo record);
    /**
     * 有状态则根据状态赛选出中奖人信息，status为空则选出全部中奖信息
     * @param status
     * @return
     */
    List<WinnersInfo> selectWinnersInfoByMap(String status);
}