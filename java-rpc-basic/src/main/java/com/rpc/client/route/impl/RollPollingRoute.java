
package com.rpc.client.route.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rpc.client.route.IRoute;
import com.rpc.protocol.Request;

/**
 *轮询算法
 */
public class RollPollingRoute implements IRoute{

	private Map<String, Integer> nextIpinfo = new ConcurrentHashMap<String, Integer>();
	@Override
	public String getRouteIp(List<String> ipinfos, Request request) {
		String serviceId = request.getServiceId();
		String ipfo = null;
		Integer position = nextIpinfo.get(serviceId);
		position = position == null?0:position;
		int size = ipinfos.size();
		ipfo = ipinfos.get(position);
		if(size > 1 && position + 1 < size) {
			nextIpinfo.put(serviceId, ++position);
		}else {
			nextIpinfo.put(serviceId, 0);
		}
		return ipfo;
	}

}
