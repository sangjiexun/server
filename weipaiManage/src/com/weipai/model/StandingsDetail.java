package com.weipai.model;

import java.util.Date;

public class StandingsDetail {
    private Integer id;

    private Integer standingsId;

    private String content;

    private Date createtime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStandingsId() {
        return standingsId;
    }

    public void setStandingsId(Integer standingsId) {
        this.standingsId = standingsId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }
}