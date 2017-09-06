package com.weipai.mapper;

import com.weipai.model.Power;

public interface PowerMapper {
    int deleteByPrimaryKey(Integer id);

    int save(Power record);

    int saveSelective(Power record);

    Power selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Power record);

    int updateByPrimaryKey(Power record);
}