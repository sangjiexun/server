package com.weipai.service;

import com.weipai.model.NoticeTable;

/**
 * 公告
 * @author luck
 *
 */
public interface NoticeTableService {
	
	
	int deleteByPrimaryKey(Integer id);

    int insert(NoticeTable record);

    int insertSelective(NoticeTable record);

    NoticeTable selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NoticeTable record);

    int updateByPrimaryKey(NoticeTable record);
    
    NoticeTable selectRecentlyObject();

}
