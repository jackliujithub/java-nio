
package com.rpc.client.route;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpc.client.ReadTimeOut;
import com.rpc.client.Result;
import com.rpc.client.RpcClientBasic;
import com.rpc.client.route.impl.RandomRoute;
import com.rpc.client.route.impl.RollPollingRoute;
import com.rpc.protocol.Constans;
import com.rpc.protocol.Request;
import com.rpc.util.RpcException;

/**
 *路由信息和心跳相关类 
 */
public class IRouteUtil {

	static final Logger LOGGER = LoggerFactory.getLogger(IRouteUtil.class);
	/**key为ip信息:ip信息:"ip,port"，value为RpcClientBasic实例*/
	static Map<String, RpcClientBasic> rpcClientBasicMap = new ConcurrentHashMap<String, RpcClientBasic>(); 
	
	/**key为serviceId,value为ip信息:"ip,port"*/
	private static Map<String, List<String>> remoteServiceMap = new ConcurrentHashMap<String, List<String>>(); 
	
	/**已经删除的ip信息,key为"ip,port"，value为ip,port*/
	private static Map<String, String> removedIPinfo = null;
	
	/**定时心跳检测线程*/
	private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
	
	/**默认为随机路由*/
	//private static IRoute route = new RandomRoute();
	//private static IRoute route = new RollPollingRoute();
	private static IRoute route = Constans.ROUTE;
	/**
	 * 根据路由策略拿到路由信息
	 * 
	 */
	public static RpcClientBasic getRoute(Request request) {
		RpcClientBasic  rpcClientBasic = null;
		//没有此远程服务的配置信息
		if(!remoteServiceMap.containsKey(request.getServiceId())){
			return null;
		}
		//动态路由
		List<String> ipinfos = remoteServiceMap.get(request.getServiceId());
		
		//{动态路由start
		String ipInfo = getRouteIp(ipinfos,request);
		if(null == ipInfo){
			throw new RpcException("获取动态路由出错");
		}
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("动态路由获得的IP为:{}",new Object[]{ipInfo});
		}
		rpcClientBasic =  rpcClientBasicMap.get(ipInfo);
		if(null != rpcClientBasic && !request.getTimeOutRpcClientBasics().contains(rpcClientBasic)){
			return rpcClientBasic;
		}
		//}动态路由start
		
		for (String ip:ipinfos) {
			//没有此ip对应的连接信息，继续查找ip
			if(!rpcClientBasicMap.containsKey(ip)){
				continue;
			}
			rpcClientBasic =  rpcClientBasicMap.get(ip);
			//是否在本次调用超时的连接信息里
			if(request.getTimeOutRpcClientBasics().contains(rpcClientBasic)){
				rpcClientBasic = null;
				continue;
			}
			break;
		}
		
		//没有远程连接信息时
		if(null == rpcClientBasic){
			//加锁有问题，需要对正在连接的ip和端口号进行加锁
			synchronized (IRouteUtil.class) {
				//TODO 需要再检测一次
				rpcClientBasic = connect(ipinfos,request);
			}
		}
		
		return rpcClientBasic;
	}

	/**
	 *获得路由ip
	 */
	private static String getRouteIp(List<String> ipinfos,Request request) {
		return route.getRouteIp(ipinfos, request);
	}

	/**
	 * 
	 *连接信息的初始化
	 */
	public static void init(Map<String, List<String>> remoteServiceMap){
		//初始化远程连接信息
		initRemoteConnect(remoteServiceMap);
		//发送心跳包
		startSendHeartBeat();
	}
	/***
	 * 
	 *初始化远程服务器连接信息
	 */
	private static void initRemoteConnect(Map<String, List<String>> remoteServiceMap){
		IRouteUtil.remoteServiceMap = remoteServiceMap;
		//复制远程节点信息
		for(Map.Entry<String, List<String>> entry : remoteServiceMap.entrySet()){
			remoteServiceMap.put(entry.getKey(), entry.getValue());
		}
		//初始化已删除的节点
		removedIPinfo = new ConcurrentHashMap<String, String>();
		
		for(Map.Entry<String, List<String>> entry:IRouteUtil.remoteServiceMap.entrySet()){
			String serviceId = entry.getKey();
			List<String> ipInfos = entry.getValue();
			//TODO 连接信息的负载均衡
			if(ipInfos.size() < 1){
				continue;
			}
			serviceConnect(ipInfos);
		}
	}
	
	private static void serviceConnect(List<String> ipInfos){
		for(String ipInfo:ipInfos){
			//已删除ip，不需要在连接
			if(removedIPinfo.containsKey(ipInfo)){
				continue;
			}
			//如果已经有此ip的连接信息，则不需要在连接
			if(rpcClientBasicMap.containsKey(ipInfo)){
				return;
			}
			String[] ipinfoArray = ipInfo.split(",");
			RpcClientBasic rpcClientBasic = new RpcClientBasic();
			//初始化连接信息
			try {
				rpcClientBasic.init(ipinfoArray[0], Integer.parseInt(ipinfoArray[1]));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			rpcClientBasicMap.put(ipInfo, rpcClientBasic);
		}
	}
	
	/**
	 *带请求信息的连接
	 */
	private static RpcClientBasic connect(List<String> ipinfos, Request request) {
		for(String ipInfo:ipinfos){
			//已删除ip，不需要在连接
			if(removedIPinfo.containsKey(ipInfo)){
				continue;
			}
			//已调用过，超时ip不需要在连接
			if(request.isTimeOutConnect(ipInfo)){
				continue;
			}
			//已经连接过ip信息，不需要在连接
			if(rpcClientBasicMap.containsKey(ipInfo)){
				return rpcClientBasicMap.get(ipInfo);
			}
			String[] ipinfoArray = ipInfo.split(",");
			RpcClientBasic rpcClientBasic = new RpcClientBasic();
			//初始化连接信息
			try {
				rpcClientBasic.init(ipinfoArray[0], Integer.parseInt(ipinfoArray[1]));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			rpcClientBasicMap.put(ipInfo, rpcClientBasic);
			return rpcClientBasic;
		}
		return null;
	}
	
	/**
	 *开始发送心跳
	 */
	private static void startSendHeartBeat(){
		LOGGER.info("start send Heart......");
		scheduledExecutorService.scheduleAtFixedRate(new HeartBeatTask(), 5, 10, TimeUnit.SECONDS);
	}
	
	
	/**
	 *删除无效的连接 
	 *心跳3此检测失效，或者netty抛出异常，终止该调连接
	 */
	public static RpcClientBasic removeConnect(String ipinfo){
		//删除列表中加入已删除的ip信息
		removedIPinfo.put(ipinfo, ipinfo);
		//删除缓存中的连接信息
		RpcClientBasic rpcClientBasic =  rpcClientBasicMap.remove(ipinfo);
		if(null == rpcClientBasic){
			return null;
		}
		//关闭连接
		rpcClientBasic.closeHandler();
		
		return rpcClientBasic;
	}
	
	/**
	 *系统退出，清理资源
     *
	 */
	public static void destroy(){
		for (Map.Entry<String, RpcClientBasic> entry:IRouteUtil.rpcClientBasicMap.entrySet()) {
			RpcClientBasic rpcClientBasic = entry.getValue();
			rpcClientBasic.closeHandler();
		}
		scheduledExecutorService.shutdown();
	}
	
	/**
	 * 
	 *更新节点信息
	 */
	public static void updateServiceNodeInfo(String serviceId, List<String> ipNodes) {
		List<String> olderIpNodes = remoteServiceMap.get(serviceId);
		if(null == olderIpNodes || olderIpNodes.size() < 1){
			remoteServiceMap.put(serviceId, ipNodes);
			serviceConnect(ipNodes);
			return ;
		}
		//只关注新增节点。删除节点通过心跳加入删除队列;此处对zk来说失效的服务端节点，不做删除
		//避免服务端和zk的网络问题导致节点丢失，误删除
		synchronized (IRouteUtil.class) {
			for(String ipNode:ipNodes){
				//TODO 检测每个ip连接的有效性，无效重新连接，有效继续，新增服务器进行网络连接，加入服务队列
				RpcClientBasic rpcClientBasic = rpcClientBasicMap.get(ipNode);
				if(removedIPinfo.containsKey(ipNode)){
					removedIPinfo.remove(ipNode);
					LOGGER.info("recover ip node:" + ipNode);
					serviceConnect(Arrays.asList(ipNode));
				}
				if(olderIpNodes.contains(ipNode)){
					continue;
				}
				LOGGER.info("添加服务端调用节点信息:{}",new Object[]{ipNode});
				olderIpNodes.add(ipNode);
				serviceConnect(Arrays.asList(ipNode));
			}
		}
	}

	
	/**
	 * 尝试重新进行连接
	 */
	public static void retryConnect(RpcClientBasic rpcClientBasic) {
		boolean retrySuccess = rpcClientBasic.retryConnect();
		if(!retrySuccess){
			removeConnect(Constans.buildIpINfo(rpcClientBasic.getHost(),rpcClientBasic.getPort()));
		}
	}

}

/**
 * 
 *心跳任务类
 */
class HeartBeatTask implements Runnable{
	@Override
	public void run() {
		try {
			IRouteUtil.LOGGER.info("send Heart......");
			for (Map.Entry<String, RpcClientBasic> entry:IRouteUtil.rpcClientBasicMap.entrySet()) {
				RpcClientBasic rpcClientBasic = entry.getValue();
				Result result = rpcClientBasic.sendHeaderBeat();
				Object resultHeart = result.getResult();
				//心跳超时次数大于等于3次，就把该ip连接加入删除状态。
				if(resultHeart instanceof ReadTimeOut){ 
					rpcClientBasic.addHeartBeatFail();
					IRouteUtil.LOGGER.info("heartBeat timeout .rpc:" + rpcClientBasic.getHost()+":" + rpcClientBasic.getPort()+".fail send heart beat count:" + rpcClientBasic.getHeartBeartFail());
					if(rpcClientBasic.getHeartBeartFail() >= 3){
						IRouteUtil.LOGGER.warn("try retry the channel;key:" + entry.getKey()+";value:" + rpcClientBasic.getHost()+":" + rpcClientBasic.getPort());
						//重新尝试连接
						IRouteUtil.retryConnect(rpcClientBasic);
					}
					continue;
				}
				//成功重置心跳计算
				rpcClientBasic.resetHeartBeat();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


