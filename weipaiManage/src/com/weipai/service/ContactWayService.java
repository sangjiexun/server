package com.weipai.service;

import com.weipai.model.ContactWay;

public interface ContactWayService {
	 int deleteByPrimaryKey(Integer id);

	    int insert(ContactWay record);

	    int insertSelective(ContactWay record);

	    ContactWay selectByPrimaryKey(Integer id);

	    int updateByPrimaryKeySelective(ContactWay record);

	    int updateByPrimaryKey(ContactWay record);
}
