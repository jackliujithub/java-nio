
package com.rpc.util;


public class RpcException extends RuntimeException {

	public RpcException(String errorMsg) {
		super();
		this.errorMsg = errorMsg;
	}


	public String getErrorMsg() {
		return errorMsg;
	}


	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}


	public int getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}


	private String errorMsg;
	private int errorCode = -1;
	
	
	public RpcException(String errorMsg, int errorCode) {
		super();
		this.errorMsg = errorMsg;
		this.errorCode = errorCode;
	}
	

	public RpcException(){
		
	}
	
	
}
