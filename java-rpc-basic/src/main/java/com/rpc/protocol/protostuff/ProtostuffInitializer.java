package com.rpc.protocol.protostuff;

import com.rpc.protocol.Request;
import com.rpc.protocol.Response;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ProtostuffInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ProtostuffDecoder(Request.class))
					 .addLast(new ProtostuffEncoder(Response.class))
					 .addLast(new ProtostuffRequestHandler());
	}

	
}
