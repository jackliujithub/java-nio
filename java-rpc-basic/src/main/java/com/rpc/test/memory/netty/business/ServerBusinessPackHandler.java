package com.rpc.test.memory.netty.business;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;

import org.apache.log4j.Logger;


public class ServerBusinessPackHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger LOGGER = Logger.getLogger(ServerBusinessPackHandler.class);
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		//System.out.println("=======server receive msg=====:"+Thread.currentThread().getName());
		//业务处理
		BusinessTask buninessHandler = new BusinessTask(ctx,msg);
		BusinessHandler.submit(buninessHandler);
//		msg +="\r\n";
//		byte[] src = msg.getBytes("utf-8");
//		ctx.channel().writeAndFlush(Unpooled.buffer().writeBytes(src));
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketAddress address = ctx.channel().remoteAddress();
		//System.out.println("server connec:" + address.toString());
		LOGGER.info("server connec:" + address.toString());
	}

	
}
