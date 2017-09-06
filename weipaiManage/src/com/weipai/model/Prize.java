package com.weipai.model;

import org.apache.ibatis.type.Alias;

@Alias("Prize")
public class Prize {
    private Integer id;

    private Integer indexId;

    private String prizeName;

    private String imageUrl;

    private Double probability;
    
    private String status;
    
    private Integer prizecount;
    
    private String type;
    
    
    

    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPrizecount() {
		return prizecount;
	}

	public void setPrizecount(Integer prizecount) {
		this.prizecount = prizecount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIndexId() {
        return indexId;
    }

    public void setIndexId(Integer indexId) {
        this.indexId = indexId;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName == null ? null : prizeName.trim();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? null : imageUrl.trim();
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}