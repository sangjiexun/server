package com.weipai.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSONObject;
import com.weipai.controller.base.BaseController;
/**
 * 上传下载类
 * @author luck
 */

@Controller
@RequestMapping("/uploadAndDown")
public class UploadAndDownController extends BaseController {

	
	/**
	 * 上传奖品图片
	 * @param request
	 * @param response
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping("/uploadImg")
	public void uploadImg(HttpServletRequest request , HttpServletResponse response) throws IllegalStateException, IOException{
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
		//type表示上传图片的属性(头像？广告？图标？)
		JSONObject json = new JSONObject();
		if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile((String) iter.next());
				if (file != null){
						//上传奖品图片
						String nameSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
						String pathHead = request.getSession().getServletContext().getRealPath("/")+"images/";
						File fileHead = new File(pathHead);
						if(!fileHead .exists() && !fileHead .isDirectory()) { 
							try {
								fileHead .mkdir();    
							} catch (Exception e) {
								e.getStackTrace();
								json.put("status_code", "1");
								json.put("error", "保存路径文件夹创建异常!");
							}
						} 
						//  下面的加的日期是为了防止上传的名字一样
						String endPath  = "prize"+new SimpleDateFormat("yyyyMMdd").format(new Date()) + System.currentTimeMillis()+nameSuffix;
						String path = pathHead+ endPath;
						File localFile = new File(path);
						file.transferTo(localFile);
						try {
							json.put("status_code", "0");
							json.put("imgurl", "images/"+endPath);
						} catch (Exception e) {
							json.put("status_code", "1");
							json.put("error", "头像修改失败");
						}
				}
				else{
					json.put("status_code", "1");
					json.put("error", "上传不能为空");
				}
			}
		}
		else{
			json.put("status_code", "1");
			json.put("error", "文件上传方式有误");
		}
		response.setContentType("text/html");
		returnMessage(response, json.toJSONString());
	}
}
