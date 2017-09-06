package com.weipai.model;

import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("Operatelog")
public class Operatelog {
    private Integer id;

    private Integer managerId;

    private Integer managerDownId;

    private Integer accountId;

    private Date createtime;

    private String mark;

    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getManagerDownId() {
        return managerDownId;
    }

    public void setManagerDownId(Integer managerDownId) {
        this.managerDownId = managerDownId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark == null ? null : mark.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }
}