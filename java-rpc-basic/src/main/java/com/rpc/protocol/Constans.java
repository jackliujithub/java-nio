package com.rpc.protocol;

import com.rpc.client.route.IRoute;
import com.rpc.client.route.impl.RollPollingRoute;

public class Constans {

	/**服务端默认端口*/
	public static final int DEFAULT_PORT = 20888; 
	
	/**zooKeeper 路径根目录*/
	public static final String DATA_PATH = "/rpc/provider";
	
	/**心跳包*/
	public static final int HEADRT_PACKAGE = -1;
	
	/**业务包*/
	public static final int BUSINESS_PACKAGE = 1;

	/**等待服务端返回的超时时间,默认3秒*/
	public static final long RESULT_WAIT_TIMEOUT = 3 * 1000;
	
	/**
	 * 构建ipinfo
	 *
	 */
	public static String buildIpINfo(String host,int port){
		return host+"," + port;
	}
	
	/**业务处理核心线程数*/
	public static final int corePoolSize = 200;
	
	/**业务处理最大线程数*/
	public static final int maximumPoolSize = 200;
	
	/**路由算法*/
	public static final IRoute ROUTE = new RollPollingRoute();
	
	
}
