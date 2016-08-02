package com.rpc.test.memory.netty.business;

import java.nio.charset.Charset;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ServerChannelInit extends ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new LineBasedFrameDecoder(8192*10));
		ch.pipeline().addLast(new StringDecoder(Charset.forName("utf-8")));
		ch.pipeline().addLast(new ServerBusinessPackHandler());
		//ch.pipeline().addLast(new StringEncoder(Charset.forName("utf-8")));
	}

}
