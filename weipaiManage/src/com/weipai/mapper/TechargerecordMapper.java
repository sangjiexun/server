package com.weipai.mapper;

import java.util.Date;
import java.util.List;

import com.weipai.model.Techargerecord;

public interface TechargerecordMapper {
    int deleteByPrimaryKey(Integer id);

    int save(Techargerecord record);

    int saveSelective(Techargerecord record);

    Techargerecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Techargerecord record);

    int updateByPrimaryKey(Techargerecord record);
    /**
     * 超管卖给代理商的历史房卡总数
     * @return
     */
    int selectTotalRoomCards();
    /**
     *超管当天卖给代理商的房卡总数 
     */
    int selectSaleRoomCardsToday(Date date);
    /**
     * 超管本周卖给代理商房卡总数
     * @param date
     * @return
     */
    int saleRoomCardsThisWeek(Date date);
    /**
     * 得到该用户下面的房卡记录(充值和被充值)
     * @param id
     * @return
     */
    List<Techargerecord> selectByRelateId(int id);
}