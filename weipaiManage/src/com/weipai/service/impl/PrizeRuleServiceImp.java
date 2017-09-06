package com.weipai.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.weipai.mapper.PrizeRuleMapper;
import com.weipai.model.PrizeRule;
import com.weipai.service.PrizeRuleService;


@Service
@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
public class PrizeRuleServiceImp implements PrizeRuleService {
	@Resource
	PrizeRuleMapper prizeRuleMapper;

	@Override
	public int deleteByPrimaryKey(Integer id) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(PrizeRule record) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.insert(record);
	}

	@Override
	public int insertSelective(PrizeRule record) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.insertSelective(record);
	}

	@Override
	public PrizeRule selectByPrimaryKey(Integer id) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(PrizeRule record) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(PrizeRule record) {
		// TODO Auto-generated method stub
		return prizeRuleMapper.updateByPrimaryKey(record);
	}

}
