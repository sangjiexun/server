package com.weipai.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerAspect {
	
	private final String pointcut = "execution(* com.ddl.controller..*(..))";
	
	@Before(pointcut)
	public void before(){
		System.out.println("before");
	}
	
	@After(pointcut)
	public void after(){
		System.out.println("after");
	}
	
	@Around(pointcut)
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
		Object target = joinPoint.getTarget().getClass().getName();
		System.out.println("调用者+"+target);
		  
        Object[] args = joinPoint.getArgs();//2.传参  
        System.out.println("2.传参:----"+args[0]);  
         for (Object object : args) {
			System.out.println(object instanceof HttpServletRequest);
		}
		
		return joinPoint.proceed();
		
	}
}
