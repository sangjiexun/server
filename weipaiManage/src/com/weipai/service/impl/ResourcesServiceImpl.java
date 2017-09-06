package com.weipai.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.weipai.service.ResourcesSevice;

@Service
@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
public class ResourcesServiceImpl implements ResourcesSevice {

	
	
	
	
}
