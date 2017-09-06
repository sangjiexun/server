package com.weipai.mapper;

import com.weipai.model.NoticeTable;

public interface NoticeTableMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(NoticeTable record);

    int insertSelective(NoticeTable record);

    NoticeTable selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NoticeTable record);

    int updateByPrimaryKey(NoticeTable record);
    
    
    NoticeTable selectRecentlyObject();
}