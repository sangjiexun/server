package com.weipai.mapper;

import java.util.Date;

import com.weipai.model.RoomInfo;


public interface RoomInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RoomInfo record);

    int insertSelective(RoomInfo record);

    RoomInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RoomInfo record);

    int updateByPrimaryKey(RoomInfo record);
    
    int selectCount();
    
    int selectTodayCount(Date date);
    //获取所有消耗的房卡总和
    int selectConsumedRoomCardCount();
}