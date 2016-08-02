package com.rpc.spring;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.rpc.protocol.Constans;
import com.rpc.protocol.registry.ServiceDataNode;
import com.rpc.protocol.registry.ServiceRegistry;

/**
 * 获取spring bean
 * @author Administrator
 *
 */

@Component
public class SpringObjectFactoryUtil implements ApplicationContextAware{

	private static Map<String, Object> serviceBeanMap = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringObjectFactoryUtil.class);
	@Resource
	private ServiceRegistry servieRegistry;
	
	public static Object getObject(String serviceId){
		return serviceBeanMap.get(serviceId);
	}

	public static Map<String, Object> getServiceBeanMap() {
		return serviceBeanMap;
	} 
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(Service.class);
		SpringObjectFactoryUtil.serviceBeanMap = serviceBeanMap;
		//注册服务
		registeServiceBean();
	}

	/**
	 * 注册服务bean
	 */
	private void registeServiceBean(){
		if(serviceBeanMap == null || serviceBeanMap.size() < 1){
			return ;
		}
		String adressInfo = getLocalIp() + ","+Constans.DEFAULT_PORT;
		for(Map.Entry<String, Object> entry:serviceBeanMap.entrySet()){
			String serviceId = entry.getKey();
			ServiceDataNode dataNode = new ServiceDataNode();
			dataNode.setPath(serviceId);
			dataNode.setData(adressInfo);
			servieRegistry.register(dataNode);
		}		
	}
	
	
//	private static String getLocalIp(){
//		InetAddress addr = null;
//		try {
//			addr = InetAddress.getLocalHost();
//			return addr.getHostAddress().toString();//获得本机IP
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "";
//	}
	
	private static String getLocalIp(){
		InetAddress addr = null;
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress ip =  addresses.nextElement();
					if(null != ip && ip instanceof Inet4Address){
						String hostAddress = ip.getHostAddress(); 
						LOGGER.info("本机的IP =" + hostAddress);
						if(!"127.0.0.1".equals(hostAddress) && !"/127.0.0.1".equals(hostAddress)){
							return hostAddress;
						}
					}
					
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(getLocalIp());
	}
}
