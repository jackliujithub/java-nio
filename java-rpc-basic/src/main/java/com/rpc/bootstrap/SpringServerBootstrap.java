package com.rpc.bootstrap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rpc.server.BasicServer;

public class SpringServerBootstrap {
	private static ApplicationContext context;

	public static void main(String[] args) {
		//启动spring ，加载相应的服务
		context = new ClassPathXmlApplicationContext("classpath:spring-config.xml");
		
		//启动服务端
		new BasicServer().start();
	}
}
