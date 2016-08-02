
package com.rpc.protocol.concurrent;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

import com.alibaba.fastjson.JSON;
import com.rpc.protocol.Request;
import com.rpc.protocol.Response;
import com.rpc.spring.SpringObjectFactoryUtil;


public class RpcBusinessTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(RpcBusinessTask.class);
	/**请求包*/
	private Request msg;
	
	/**连接句柄*/
	private ChannelHandlerContext ctx;
	
	public RpcBusinessTask(Request msg,ChannelHandlerContext ctx){
		this.setMsg(msg);
		this.setCtx(ctx);
	}
	@Override
	public void run() {
		Response response = new Response();
		response.setSeq(getMsg().getSeq());
		response.setPackageType(getMsg().getPackageType());
		response.setResult(handlerMethod());
		logger.info("======send result:======" + JSON.toJSONString(response));
		getCtx().writeAndFlush(response);
		//may ben friendly
		ctx = null;
	}
	
	
	/**
	 * 调用业务方法
	 * @param msg
	 * @return
	 */
	private Object handlerMethod(){
		try {
			String className = getMsg().getClassName();
			String methodName = getMsg().getMethodName();
			Class<?>[] parameterTypes = getMsg().getParameterTypes();
			Object[] paraValues = getMsg().getParams();
			
			Object serviceBean = getServiceBeanObject(getMsg().getServiceId());
		    Class<?> serviceClass = serviceBean.getClass();
		    Method method = serviceClass.getMethod(methodName, parameterTypes);
	        method.setAccessible(true);
	        return method.invoke(serviceBean, paraValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * TODO 获得serviceBean
	 * @param className
	 * @return
	 */
	public Object getServiceBeanObject(String className){
		return SpringObjectFactoryUtil.getObject(className);
	}
	public Request getMsg() {
		return msg;
	}
	public void setMsg(Request msg) {
		this.msg = msg;
	}
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
}
