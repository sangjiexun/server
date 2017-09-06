package com.weipai.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * jsp拦截
 * @author luck
 *
 */
public class JspFilter implements Filter  {

	public void destroy() {
	}
	/**
	 * 对还没有登录就对内部页面进行访问的请求进行拦截
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		
		
		System.out.println("拦截跳转");
		
		
		HttpServletRequest request=(HttpServletRequest) req;
		HttpServletResponse response=(HttpServletResponse) resp;
		HttpSession session=request.getSession(false);
		if(session==null||session.getAttribute("manager")==null){
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		chain.doFilter(req, resp);
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		
	}
}
