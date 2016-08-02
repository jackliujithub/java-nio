package com.rpc.client;

import com.rpc.protocol.Constans;
import com.rpc.protocol.Request;

public class Result {

	/**结果是否ok*/
	private volatile boolean resultOk = false;
	/**是否请求超时*/
	private volatile boolean isTimeOut = false;
	/**执行结果*/
	private Object result;
	
	/**请求包*/
	private Request request;
	
	public boolean isResultOk() {
		return resultOk;
	}

	public void setResultOk(boolean resultOk) {
		this.resultOk = resultOk;
	}

	public Object getResult() {
		try {
			synchronized (this) {
				while(!isResultOk()){
					this.wait(Constans.RESULT_WAIT_TIMEOUT);
					if(!isResultOk()){//由于超时执行到此处
						setTimeOut(true);
						return new ReadTimeOut();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/***
	 * 
	 *TODO 超时处理,业务包超时，关闭当前连接，并重试其它服务，重试三次不行则直接返回错误。
	 */
	private void timeOutHandler() {
		
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public boolean isTimeOut() {
		return isTimeOut;
	}

	public void setTimeOut(boolean isTimeOut) {
		this.isTimeOut = isTimeOut;
	}
	
	
}
