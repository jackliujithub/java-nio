package com.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

import com.rpc.protocol.Constans;
import com.rpc.protocol.protostuff.ProtostuffInitializer;
import com.rpc.protocol.text.TextStreamingHandler;
import com.rpc.protocol.text.TextStreamingInitializer;

public class BasicServer {
	 
	private static final Logger logger = Logger.getLogger(BasicServer.class);

	 
	public void start(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				int bossThreadCount = 1;
				int workThreadCount = 0;
				int port = Constans.DEFAULT_PORT;
				//ChannelHandler childHandler = new TextStreamingInitializer();
				ChannelHandler childHandler = new ProtostuffInitializer();
				EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
                EventLoopGroup workerGroup = new NioEventLoopGroup(workThreadCount);
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(childHandler);

                    b.bind(port).sync().channel().closeFuture().sync();
                   // logger.info("rpc server Listening is on... Port:" + port);
                    System.out.println("rpc server Listening is on... Port:" + port);
                } catch (InterruptedException e) {
                    logger.error("端口 " + port + " 的服务启动失败！",e);
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
			}
		}).start();
	}
	
	public static void main(String[] args) {
		//new BasicServer().start();
		for (int i = 512; i > 0; i <<= 1) {
			System.out.println(i);
		}
	}
}
