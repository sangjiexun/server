package com.weipai.mapper;

import com.weipai.model.PrizeRule;

public interface PrizeRuleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PrizeRule record);

    int insertSelective(PrizeRule record);

    PrizeRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PrizeRule record);

    int updateByPrimaryKey(PrizeRule record);
}