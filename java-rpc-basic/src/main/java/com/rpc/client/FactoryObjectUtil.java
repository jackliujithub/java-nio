package com.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.alibaba.fastjson.JSON;
import com.rpc.client.route.IRouteUtil;
import com.rpc.protocol.Request;
import com.rpc.util.RpcException;
import com.rpc.util.TimeOutException;

public class FactoryObjectUtil implements FactoryBean{

	private String serviceId;
	
	private Class serviceClass;
	
	private static Logger Logger = LoggerFactory.getLogger(FactoryObjectUtil.class);
	@Override
	public Object getObject() throws Exception {
		Object userService = Proxy.newProxyInstance(getServiceClass().getClassLoader(), new Class[]{getServiceClass()}, 
				new InvocationHandler(){
					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						String seq = SequenceUtil.getSeq();
						Request request = new Request();
						request.setSeq(seq);
						request.setClassName(getServiceClass().getName());
						request.setServiceId(getServiceId());
						request.setMethodName(method.getName());
						request.setParams(args);
						if(null != args){
							Class[] paraTypes = new Class[args.length];
							for(int i=0;i<args.length;i++){
								paraTypes[i] = args[i].getClass();
							}
							request.setParameterTypes(paraTypes);
						}
						
						RpcClientBasic rpcClientBasic = IRouteUtil.getRoute(request);
						
						if(null == rpcClientBasic){
							throw new TimeOutException("无远程连接信息," + JSON.toJSONString(request));
						}
						
						Result  result = rpcClientBasic.sendMsg(request, seq);
						Object businessReulst = result.getResult();
						//读超时，需要重新拿其它服务连接，进行服务调用
						if(businessReulst instanceof ReadTimeOut ){
							Logger.error("第一次调用超时：" + JSON.toJSONString(request));
							RetryInvoker retryInvoker = new RetryInvoker();
							//添加超时的连接信息
							request.getTimeOutRpcClientBasics().add(rpcClientBasic);
							retryInvoker.setRequest(request);
							businessReulst = retryInvoker.retry();
						}
						if(businessReulst instanceof ReadTimeOut){
							throw new TimeOutException("超时，重试后无远程连接信息," + JSON.toJSONString(request));
						}
						request = null;
						return businessReulst;
					}
		});
		return userService;
	}

	
	class RetryInvoker {
		//重试次数,默认两次
		private int retryCount = 2;
		//当前重试次数
		private int currentRetryCount = 0;
		//请求信息
		private Request request;
		//调用结果
		private Object resultObject;
		
		public Object retry(){
			if(currentRetryCount >= retryCount){
				return resultObject;
			}
			currentRetryCount ++;
			String seq = SequenceUtil.getSeq();
			request.setSeq(seq);
			RpcClientBasic rpcClientBasic = IRouteUtil.getRoute(request);
			if(null == rpcClientBasic){
				throw new TimeOutException("超时，重试后无远程连接信息," + JSON.toJSONString(request));
			}
			Result  result = rpcClientBasic.sendMsg(request, seq);
			resultObject = result.getResult();
			if(resultObject instanceof ReadTimeOut){
				Logger.error("第"+(currentRetryCount+1)+"次调用超时：" + JSON.toJSONString(request));
				//添加超时信息
				request.getTimeOutRpcClientBasics().add(rpcClientBasic);
				return this.retry();
			}
			return resultObject;
		}

		public Request getRequest() {
			return request;
		}

		public void setRequest(Request request) {
			this.request = request;
		}
	}
	
	@Override
	public Class<?> getObjectType() {
		// TODO Auto-generated method stub
		return getServiceClass().getClass();
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return true;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public Class getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(Class serviceClass) {
		this.serviceClass = serviceClass;
	}

}
