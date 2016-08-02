package com.rpc.test.memory.netty.client;


import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

public class ClientChannelInit extends ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new LineBasedFrameDecoder(8192*10));
		ch.pipeline().addLast(new StringDecoder(Charset.forName("utf-8")));

		ch.pipeline().addLast(new MessageToByteEncoder<String>() {

			@Override
			protected void encode(ChannelHandlerContext ctx, String msg,
					ByteBuf out) throws Exception {
				//System.out.println("======encode======");
				out.writeBytes(msg.getBytes("utf-8"));
			}
			
		});
		//ch.pipeline().addLast(handlers);
		//流量整形
		//ch.pipeline().addLast(new GlobalTrafficShapingHandler(ch.eventLoop(), 1024, 1*1024*1024, 60 * 1000));
		ch.pipeline().addLast(new clientChannelInboundHandler());
	}

	class clientChannelInboundHandler implements ChannelInboundHandler {
		
		@Override
		public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			System.out.println("client receive msg:" + Thread.currentThread().getName());
			
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			JSONObject jsonObject = new JSONObject();
			//50K的数据
			for(int i=0;i<50;i++){
				jsonObject.put("hellohellohellohellohellohellohellohellohellohello"+i, "nettynettynettynettynettynettynettynettynettynetty"+i);
			}
			
			String msg = jsonObject.toJSONString()+"\r\n";
			//byte[] src = msg.getBytes("utf-8");
			//ctx.channel().writeAndFlush(Unpooled.buffer().writeBytes(src));
			ctx.writeAndFlush(msg);
		}
	}
}
