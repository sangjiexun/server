package com.weipai.mapper;

import java.util.List;

import com.weipai.model.Operatelog;

public interface OperatelogMapper {
    int deleteByPrimaryKey(Integer id);

    int save(Operatelog record);

    int saveSelective(Operatelog record);

    Operatelog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Operatelog record);

    int updateByPrimaryKey(Operatelog record);
    
    List<Operatelog> selectByRelateId(int managerId);
}