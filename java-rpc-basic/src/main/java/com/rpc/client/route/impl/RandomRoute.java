
package com.rpc.client.route.impl;

import java.util.List;
import java.util.Random;

import com.rpc.client.route.IRoute;
import com.rpc.protocol.Request;

/*
 * 随机路由
 */
public class RandomRoute implements IRoute {

	@Override
	public String getRouteIp(List<String> ipinfos, Request request) {
		if(null == ipinfos | ipinfos.size() < 1){
			return null;
		}
		int size = ipinfos.size() ;
		Random random = new Random();
		int position = random.nextInt(size);
		return ipinfos.get(position);
	}

}
