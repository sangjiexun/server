package com.weipai.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.weipai.mapper.AccountMapper;
import com.weipai.model.Account;
import com.weipai.service.AccountService;

@Service
@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
public class AccountServiceImpl implements AccountService {

	@Resource
	AccountMapper accountMapper;
	
	public List<Account> selectObjectsByMap(Map<String, Integer> map) {
		
		return accountMapper.selectObjectsByMap(map);
	}
	
	public Integer selectObjectCountByMap(Map<String, Integer> map){
		
		return accountMapper.selectObjectCountByMap(map);
	}

	public List<Account> selectAllAccount(Map<String , Integer> map){
		
		return accountMapper.selectAllAccount(map);
	}

	@Override
	public Account selectByPrimaryKey(Integer id) {
		return   accountMapper.selectByPrimaryKey(id);
	}

	@Override
	public Account selectByUuid(Integer uuid) {
		return accountMapper.selectByUuid(uuid);
	}

	@Override
	public int updateAccountStatus(Map<String, Object> map) {
		return accountMapper.updateAccountStatus(map);
	}
}
