package com.rpc.bootstrap;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.rpc.client.Result;

public class TimeOutTest {

	public static void main(String[] args) {
		Result result = new Result();
		new Thread(new TaskBusi(result)).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		new Thread(new Task(result)).start();
	}
}

	class TaskBusi implements Runnable{
		
		private Result result;
		public TaskBusi(Result result){
			this.result = result;
		}
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			Object reslutObject = result.getResult();
			System.out.println("cast:" + (System.currentTimeMillis() - start));
			System.out.println("reslutObject=====" + reslutObject);
		}
		
	}

	class Task implements Runnable{

		private Result result;
		
		public Task(Result result) {
			this.result = result;
		}
		
		@Override
		public void run() {
			synchronized (result) {
				if(result.isTimeOut()){
					System.out.println("=========invoke timeout.ip=======");
					return;
				}
				result.setResult("123");
				result.setResultOk(true);
				result.notifyAll();
			}
		}
		
	}
