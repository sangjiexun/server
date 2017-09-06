package com.weipai.service;

import com.weipai.model.PrizeRule;

public interface PrizeRuleService {
	 int deleteByPrimaryKey(Integer id);

	    int insert(PrizeRule record);

	    int insertSelective(PrizeRule record);

	    PrizeRule selectByPrimaryKey(Integer id);

	    int updateByPrimaryKeySelective(PrizeRule record);

	    int updateByPrimaryKey(PrizeRule record);
}
