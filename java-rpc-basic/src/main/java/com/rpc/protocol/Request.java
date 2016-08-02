package com.rpc.protocol;

import io.netty.util.internal.StringUtil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.rpc.client.RpcClientBasic;

public class Request implements Serializable{

	/**序列化*/
	private String seq;
	
	/**接口名*/
	private String className;
	
	/**方法名*/
	private String methodName;
	
	/**方法信息*/
	private Class<?>[] parameterTypes;
	
	/**参数信息*/
	private Object[] params;

	/**serviceId*/
	private String serviceId;
	/**包类型，-1为心跳包，1为业务包*/
	private int packageType = 1;
	/**协议版本*/
	private static String version = "0.1";
	
	/**超时的timeOutRpcClientBasics*/
	private transient List<RpcClientBasic>  timeOutRpcClientBasics = new LinkedList<RpcClientBasic>(); 
	
	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public static String getVersion() {
		return version;
	}

	public static void setVersion(String version) {
		Request.version = version;
	}

	public int getPackageType() {
		return packageType;
	}

	public void setPackageType(int packageType) {
		this.packageType = packageType;
	}

	public List<RpcClientBasic> getTimeOutRpcClientBasics() {
		return timeOutRpcClientBasics;
	}

	public void setTimeOutRpcClientBasics(List<RpcClientBasic> timeOutRpcClientBasics) {
		this.timeOutRpcClientBasics = timeOutRpcClientBasics;
	}

	
	public boolean isTimeOutConnect(String ipInfo) {
		for(RpcClientBasic rpcClientBasic:timeOutRpcClientBasics){
			if(ipInfo.equals(rpcClientBasic.getHost()+","+rpcClientBasic.getPort())){
				return true;
			}
			
		}
		return false;
	}

	
}
