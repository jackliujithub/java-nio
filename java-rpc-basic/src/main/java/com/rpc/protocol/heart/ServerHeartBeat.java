
package com.rpc.protocol.heart;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 *心跳类 
 */
public class ServerHeartBeat {

	 static final Logger LOGGER = LoggerFactory.getLogger(ServerHeartBeat.class);
	/**长连接通道*/
	private Channel channel;
	
	/**客户端ip端口号，对应心跳*/
	static Map<String, ServerHeartBeat> heartBeatMap = new ConcurrentHashMap<String, ServerHeartBeat>();
	
	/**定时检测心跳*/
	private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

	/**最后一次心跳时间*/
	private long lastHeaderBeatTime = new Date().getTime();
	
	/**两分钟内收到一次心跳就算有效*/
	private static long heartValidTime = 2 * 60 * 1000; 
	
	public void updateHeaderBeatTime(){
		lastHeaderBeatTime = new Date().getTime();
	}
	
	public void addHeartBeat(String ipinfo){
		heartBeatMap.put(ipinfo, this);
	}
	
	public static ServerHeartBeat removeHearBeat(String ipInfo){
		ServerHeartBeat heartBeat = heartBeatMap.remove(ipInfo);
		LOGGER.warn("===========client connecte closed===========" + ipInfo);
		if(null == heartBeat){
			return null;
		}
		try {
			heartBeat.getChannel().closeFuture().sync();
			heartBeat.getChannel().close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return heartBeat;
	}
	
	/**
	 *获得心跳
	 */
	public static ServerHeartBeat getHeartBeat(String ipInfo){
		return heartBeatMap.get(ipInfo);
	}
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public static void init(){
		checkHeartBeatValid();
	}
	/**
	 * 心跳检测类
	 *
	 */
	public static void checkHeartBeatValid(){
		scheduledExecutorService.scheduleAtFixedRate(new HeartBeatCheckTask(), 5, 60, TimeUnit.SECONDS);
	}
	
	public boolean isValid() {
		return new Date().getTime() - this.lastHeaderBeatTime < heartValidTime;
	}

}

/**
 * 
 *心跳任务类
 */
class HeartBeatCheckTask implements Runnable{
	@Override
	public void run() {
		for (Map.Entry<String, ServerHeartBeat> entry:ServerHeartBeat.heartBeatMap.entrySet()) {
			ServerHeartBeat heartBeat = entry.getValue();
			if(!heartBeat.isValid()){
				ServerHeartBeat.removeHearBeat(entry.getKey());
			}
		}
	}
}
