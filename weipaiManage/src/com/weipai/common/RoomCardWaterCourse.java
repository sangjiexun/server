package com.weipai.common;


/**
 * 房卡流水
 * @author luck
 *
 */
public class RoomCardWaterCourse {

	private String createTime;//时间
	
	private Integer numb;//变动数量
	
	private String name;
	
	private String mark;

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Integer getNumb() {
		return numb;
	}

	public void setNumb(Integer numb) {
		this.numb = numb;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
	
	
}
