package com.rpc.test.memory.netty.business;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessHandler.class);
	
	private static ThreadPoolExecutor threadPoolExecutor = 
			new ThreadPoolExecutor(15, 15, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(200),
					new BusinessThreadFactory(),
					new RejectedExecutionHandler(){
				@Override
				public void rejectedExecution(Runnable r,ThreadPoolExecutor executor) {
					//System.out.println("task rejected");
					LOGGER.error("task rejected");
				}
				
			});
	
	public static void submit(Runnable task){
		threadPoolExecutor.execute(task);
	}
	
	static class BusinessThreadFactory implements ThreadFactory {
		private static AtomicLong threadNumAtomicLong = new AtomicLong(1);
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("businessThread-" + threadNumAtomicLong.getAndIncrement());
			return t;
		}
	}
}
