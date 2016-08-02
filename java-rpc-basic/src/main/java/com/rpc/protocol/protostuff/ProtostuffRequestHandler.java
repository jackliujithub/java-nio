package com.rpc.protocol.protostuff;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.jca.cci.core.InteractionCallback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpc.protocol.Constans;
import com.rpc.protocol.Request;
import com.rpc.protocol.Response;
import com.rpc.protocol.concurrent.RpcBusinessTask;
import com.rpc.protocol.concurrent.ThreadPoolUtil;
import com.rpc.protocol.heart.ServerHeartBeat;
import com.rpc.spring.SpringObjectFactoryUtil;

public class ProtostuffRequestHandler extends SimpleChannelInboundHandler<Request>{

	private static final Logger logger = Logger.getLogger(ProtostuffRequestHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request request)
			throws Exception {
		handlerMethod(request,ctx);
	}

	public Object handlerMethod(Request msg, ChannelHandlerContext ctx){
		switch (msg.getPackageType()) {
		case Constans.HEADRT_PACKAGE:
			return heartBeatHander(msg,ctx);
		case Constans.BUSINESS_PACKAGE:	
			return invokeMethod(msg,ctx);
		default:
			break;
		}
		return null;
	}
	
	/**
	 *处理心跳包 
	 * @param ctx 
	 */
	private Object heartBeatHander(Request request, ChannelHandlerContext ctx){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("time", new Date().getTime());
		InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		String clientIpInfo = inetSocketAddress.toString();
		ServerHeartBeat heartBeat = ServerHeartBeat.getHeartBeat(getHeartBeatKey(inetSocketAddress));
		if(null == heartBeat){
			logger.error("server no save such channal:"+clientIpInfo);
			heartBeat = new ServerHeartBeat();
			heartBeat.addHeartBeat(getHeartBeatKey(inetSocketAddress));
			logger.info("add heartBeat:"+clientIpInfo);
			return jsonObject;
		}
		//更新心跳时间
		heartBeat.updateHeaderBeatTime();
		
		//发送心跳应答
		Response response = new Response();
		response.setSeq(request.getSeq());
		response.setPackageType(request.getPackageType());
		response.setResult(jsonObject);
		logger.info("======send result:======" + JSON.toJSONString(response));
		ctx.writeAndFlush(response);
		
		return jsonObject;
	}
	
	
	/**
	 * 调用业务方法
	 * @param msg
	 * @return
	 */
	private Object invokeMethod(Request msg,ChannelHandlerContext ctx){
		try {
			ThreadPoolUtil.submit(new RpcBusinessTask(msg, ctx));
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
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("server caught exception", cause);
		//发生异常，去掉保存的连接
		InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		String clientIpInfo = inetSocketAddress.toString();
		ServerHeartBeat.removeHearBeat(getHeartBeatKey(inetSocketAddress));
		logger.warn("===========client connecte closed===========" + clientIpInfo);
    }


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		String clientIpInfo = inetSocketAddress.toString();
		ServerHeartBeat heartBeat = new ServerHeartBeat();
		heartBeat.addHeartBeat(getHeartBeatKey(inetSocketAddress));
		logger.info("===========client connected===========" + clientIpInfo);
	}
	
	private String getHeartBeatKey(InetSocketAddress inetSocketAddress){
		String address = inetSocketAddress.getAddress().getHostAddress();
		if(address.startsWith("/")){
			address = address.substring(1);
		}
		return address+"," + inetSocketAddress.getPort();
	}
	
	
}
