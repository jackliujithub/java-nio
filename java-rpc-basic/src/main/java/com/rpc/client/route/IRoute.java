package com.rpc.client.route;

import java.util.List;

import com.rpc.protocol.Request;

/**
 * 
 *获得路由ip信息
 */
public interface IRoute {

	public String getRouteIp (List<String> ipinfos,Request request);
}
