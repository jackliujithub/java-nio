package com.rpc.test.memory.netty.business;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class BusinessTask implements Runnable{

	private ChannelHandlerContext ctx;
	private String msg;
	
	public BusinessTask(ChannelHandlerContext ctx, String msg) {
		this.ctx = ctx;
		this.msg = msg;
	}
	
	public void run(){
		msg +="\r\n";
		byte[] src = null;
		try {
			src = msg.getBytes("utf-8");
			//无池化的ByteBuf
			ByteBuf buf = Unpooled.buffer().writeBytes(src);
			//TODO池化的ByteBuf
			//ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer().writeBytes(src);
			ctx.channel().writeAndFlush(buf);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (Throwable e) {
			e.printStackTrace();
			throw new Error("Error cant handler");
		}
		
	}
}
