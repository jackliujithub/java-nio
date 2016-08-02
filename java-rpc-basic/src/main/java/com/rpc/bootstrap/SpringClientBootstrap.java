package com.rpc.bootstrap;

import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.rpc.service.User;
import com.rpc.service.UserService;

public class SpringClientBootstrap {
	
	private static ApplicationContext context;
	private static final Logger LOGGER = Logger.getLogger(SpringClientBootstrap.class);
	
	public static void main(String[] args) {
		testThreads();
	}
	
	private static void testInOneThread(){
		try {
			context = new ClassPathXmlApplicationContext("classpath:spring-config-client.xml");
			UserService userService = (UserService)context.getBean("userService");
			User user = new User();
			user.setName("张思_"+Thread.currentThread().getName());
			user.setPassword("abc");
			User result = userService.addUser(user);
			LOGGER.info("=====response result======" + JSON.toJSONString(result));
			while(true){
				//每10秒发送一次
				try {
					Thread.sleep(10 * 1000);
					result = userService.addUser(user);
					LOGGER.info("=====response result======" + JSON.toJSONString(result));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void testThreads(){
		int threadNum = 50;
		context = new ClassPathXmlApplicationContext("classpath:spring-config-client.xml");
		for(;threadNum>0;threadNum--){
			new Thread(new Runnable() {
				@Override
				public void run() {
					UserService userService = (UserService)context.getBean("userService");
					User user = new User();
					user.setName("张思_"+Thread.currentThread().getName());
					user.setPassword("abc");
					User result = userService.addUser(user);
					LOGGER.info("=====response result======" + JSON.toJSONString(result));
					while(true){
						//每10秒发送一次
						try {
							Thread.sleep(new Random().nextInt(5) * 1000);
							result = userService.addUser(user);
							LOGGER.info("=====response result======" + JSON.toJSONString(result));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}
}
