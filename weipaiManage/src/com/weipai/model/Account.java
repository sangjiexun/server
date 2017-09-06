package com.weipai.model;

import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("Account")
public class Account {
    private Integer id;

    private Integer uuid;

    private String openid;

    private String nickname;

    private String headicon;

    private Integer roomcard;

    private String unionid;

    private String province;

    private String city;

    private Integer sex;

    private Integer managerUpId;

    private Integer prizecount;

    private Integer actualcard;

    private Integer totalcard;

    private Date createtime;

    private Date lastlogintime;

    private String status;
    
    private String isGame;

    
    
    public String getIsGame() {
		return isGame;
	}

	public void setIsGame(String isGame) {
		this.isGame = isGame;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUuid() {
        return uuid;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getHeadicon() {
        return headicon;
    }

    public void setHeadicon(String headicon) {
        this.headicon = headicon == null ? null : headicon.trim();
    }

    public Integer getRoomcard() {
        return roomcard;
    }

    public void setRoomcard(Integer roomcard) {
        this.roomcard = roomcard;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid == null ? null : unionid.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getManagerUpId() {
        return managerUpId;
    }

    public void setManagerUpId(Integer managerUpId) {
        this.managerUpId = managerUpId;
    }

   

    public Integer getPrizecount() {
		return prizecount;
	}

	public void setPrizecount(Integer prizecount) {
		this.prizecount = prizecount;
	}

	public Integer getActualcard() {
        return actualcard;
    }

    public void setActualcard(Integer actualcard) {
        this.actualcard = actualcard;
    }

    public Integer getTotalcard() {
        return totalcard;
    }

    public void setTotalcard(Integer totalcard) {
        this.totalcard = totalcard;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getLastlogintime() {
        return lastlogintime;
    }

    public void setLastlogintime(Date lastlogintime) {
        this.lastlogintime = lastlogintime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}