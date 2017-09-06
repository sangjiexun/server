package com.weipai.model;

import org.apache.ibatis.type.Alias;

@Alias("Manager")
public class Manager {
    private Integer id;

    private Integer powerId;//1 管理员   2经销商    3 零售商

    private String name;

    private String password;
    
    private String telephone;

	private Integer actualcard;

    private Integer totalcards;

    private Integer managerUpId;

    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPowerId() {
        return powerId;
    }

    public void setPowerId(Integer powerId) {
        this.powerId = powerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
    public Integer getActualcard() {
        return actualcard;
    }

    public void setActualcard(Integer actualcard) {
        this.actualcard = actualcard;
    }

    public Integer getTotalcards() {
        return totalcards;
    }

    public void setTotalcards(Integer totalcards) {
        this.totalcards = totalcards;
    }

    public Integer getManagerUpId() {
        return managerUpId;
    }

    public void setManagerUpId(Integer managerUpId) {
        this.managerUpId = managerUpId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}