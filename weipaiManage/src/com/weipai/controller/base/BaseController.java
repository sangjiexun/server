package com.weipai.controller.base;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class BaseController {

	/**
	 * 公用返回app段需要的json信息
	 * @param response
	 * @param json
	 */
	public  void returnMessage(HttpServletResponse response , Object str){
		PrintWriter write = null;
		try {
			write = response.getWriter();
			write.print(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			write.close();
		}
	}
}
