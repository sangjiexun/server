package com.weipai.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.weipai.mapper.ContactWayMapper;
import com.weipai.model.ContactWay;
import com.weipai.service.ContactWayService;

@Service
@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
public class ContactWayServiceImp implements ContactWayService {
	@Resource
	ContactWayMapper contactWayMapper;
	
	@Override
	public int deleteByPrimaryKey(Integer id) {
		return 0;
	}

	@Override
	public int insert(ContactWay record) {
		return 0;
	}

	@Override
	public int insertSelective(ContactWay record) {
		return 0;
	}

	@Override
	public ContactWay selectByPrimaryKey(Integer id) {
		return contactWayMapper.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(ContactWay record) {
		return contactWayMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(ContactWay record) {
		return contactWayMapper.updateByPrimaryKey(record);
	}

}
