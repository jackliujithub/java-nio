package com.rpc.test.memory;

import com.rpc.test.memory.netty.business.ServerChannelInit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServerHeapOutOfException {

	public static void main(String[] args) {
		final int port = 9001;
		final String host = "127.0.0.1";
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				EventLoopGroup boss = new NioEventLoopGroup(1);
				EventLoopGroup work = new NioEventLoopGroup();

				ServerBootstrap serverBootstrap = null;
				try {
					serverBootstrap = new ServerBootstrap();
					ServerChannelInit testChannelInit = new ServerChannelInit();
					serverBootstrap.group(boss, work)
							.channel(NioServerSocketChannel.class)
							.childHandler(testChannelInit);
					serverBootstrap.bind(port).sync().channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					boss.shutdownGracefully();
					work.shutdownGracefully();
				}
			}
		}).start();
		
	}
}
