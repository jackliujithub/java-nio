package com.rpc.test.memory;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.rpc.test.memory.netty.client.ClientChannelInit;
import com.rpc.util.RpcException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClientHeapOutOfException {

	private static AtomicLong count = new AtomicLong();
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHeapOutOfException.class);
	
	public static void main(String[] args) {
		int count = 10;
		for(int i=0;i<count;i++){
			startClient();
		}
	}
	
	public static void startClient() {
		final int port = 9001;
		final String host = "192.168.192.115";
		//final String host = "127.0.0.1";
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				EventLoopGroup parent = new NioEventLoopGroup();
				Bootstrap client = new Bootstrap();
				ClientChannelInit clientChannelInit = new ClientChannelInit();
				client.group(parent).channel(NioSocketChannel.class).handler(clientChannelInit);
				try {
					Channel ch = client.connect(host, port).sync().channel();
					boolean isConnected = ch.isActive();
					//System.out.println("is connect:" + isConnected);
					LOGGER.info("is connect:" + isConnected);
					if(!isConnected){
						throw new RpcException("连接失败:"+host+":" + port);
					}
					for(int i=0;i<5;i++){
						Thread senderThread = new Thread(new NettyClientHeapOutOfException.clientSendThread(ch));
						senderThread.setName("sendThread-" + NettyClientHeapOutOfException.count.getAndIncrement());
						senderThread.start();
					}
					ch.closeFuture().sync();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					parent.shutdownGracefully();
				}
			}
		});
		t.setDaemon(false);
		t.start();
	}
	
	static class clientSendThread implements Runnable{
		private Channel ch;
		private AtomicLong longCount = new AtomicLong();
		private Random random = new Random();
		
		public clientSendThread(Channel ch) {
			this.ch = ch;
		}


		@Override
		public void run() {
			JSONObject jsonObject = new JSONObject();
			//5K的数据
			for(int i=0;i<10;i++){
				jsonObject.put("hellohellohellohellohellohellohellohellohellohello"+i, "nettynettynettynettynettynettynettynettynettynetty"+i);
			}
			
			while(true){
				jsonObject.put("msgId", longCount.getAndIncrement()+"_"+random.nextInt(1000000));
				String msgStr = jsonObject.toJSONString()+"\r\n";
				byte[] src;
				try {
					//src = msgStr.getBytes("utf-8");
					//ch.writeAndFlush(Unpooled.buffer().writeBytes(src));
					ch.writeAndFlush(msgStr);
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		}
	}

	
}
