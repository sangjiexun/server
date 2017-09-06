package com.weipai.mapper;

import java.util.List;
import java.util.Map;

import com.weipai.model.Resources;

public interface ResourcesMapper {
    int deleteByPrimaryKey(Integer id);

    int save(Resources record);

    int saveSelective(Resources record);

    Resources selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Resources record);

    int updateByPrimaryKey(Resources record);
    
    List<Resources> selectMenus( Map<String, Integer> map);
}