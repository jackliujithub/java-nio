package com.rpc.protocol;

import java.io.Serializable;

public class Response implements Serializable{

	private String seq;
	
	private int code;
	
	private String errorMsg;
	
	private Object result;

	/**包类型，-1为心跳包，1为业务包*/
	private int packageType = Constans.BUSINESS_PACKAGE;
	
	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public int getPackageType() {
		return packageType;
	}

	public void setPackageType(int packageType) {
		this.packageType = packageType;
	}
	
}
