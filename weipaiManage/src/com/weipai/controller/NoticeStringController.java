package com.weipai.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.weipai.controller.base.BaseController;

@Controller
@RequestMapping("/controller/StringSet")											
public class NoticeStringController extends BaseController {

	
	/**
	 * 增加公告字符串
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/addNoticeString")
	public void addNoticeString(HttpServletRequest request, HttpServletResponse response) {
		int id = Integer.parseInt(request.getParameter("id"));
		String content = request.getParameter("content");
		int type = Integer.parseInt(request.getParameter("type"));
		//
	}
	
	
	/**
	 * 修改公告字符串
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/updateNoticeString")
	public void updateNoticeString(HttpServletRequest request, HttpServletResponse response) {
		int id = Integer.parseInt(request.getParameter("id"));
		String content = request.getParameter("content");
		JSONObject json = new JSONObject();
		returnMessage(response, json);
	}
	
	/**
	 * 删除公告字符串
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/deleteNoticeString")
	public void deleteNoticeString(HttpServletRequest request, HttpServletResponse response) {
		int id = Integer.parseInt(request.getParameter("id"));
	}
}
