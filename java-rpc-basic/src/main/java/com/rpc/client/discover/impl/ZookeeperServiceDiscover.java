package com.rpc.client.discover.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.rpc.client.discover.ServiceDiscover;
import com.rpc.client.route.IRouteUtil;
import com.rpc.protocol.Constans;

public class ZookeeperServiceDiscover implements ServiceDiscover, InitializingBean, DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceDiscover.class);

	/**zk 连接时间 3s*/
	private static final int ZK_SESSION_TIMEOUT = 3000;

	/**zk地址*/
	private String zkAdress;

	private CountDownLatch latch = new CountDownLatch(1);

	/**zookeeper客户端*/
	private ZooKeeper zooKeeper;

	/**远程服务信息，key为serviceId，value为提供服务信息,(ip,port)"*/
	private Map<String, List<String>> remoteServiceMap = new ConcurrentHashMap<String, List<String>>();

	/**服务节点初始化*/
	private static final int SERVICE_NODE_INIT = 0;
	/**服务节点添加*/
	private static final int SERVICE_NODE_ADD = 1;
	/**服务节点删除*/
	private static final int SERVICE_NODE_DELETED = 2;

	/**提供服务节点增加*/
	private static final int IP_NODE_ADD = 1;
	/**提供服务节点减少*/
	private static final int IP_NODE_DELETED = 2;

	public void init() {
		//连接zookeeper
		connectServer();
		//获得节点信息
		discover();
		//连接信息的初始化
		IRouteUtil.init(remoteServiceMap);
	}

	@Override
	public void discover() {
		watchNode(this.zooKeeper, SERVICE_NODE_INIT);
	}

	public void watchNode(final ZooKeeper zk, int type) {
		try {
			List<String> nodeList = zk.getChildren(Constans.DATA_PATH, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					LOGGER.info("event......" + JSON.toJSONString(event));
					//TODO 节点变化的时候
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						//watchNode(zk);
					}
					
				}
			});
			switch (type) {
			case SERVICE_NODE_INIT:
				initNode(zk, nodeList);
				break;
			case SERVICE_NODE_ADD:
				break;
			case SERVICE_NODE_DELETED:
				break;
			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
		}
	}

	/**
	 *初始化节点信息 
	 *
	 */
	private void initNode(final ZooKeeper zk, List<String> nodeList) throws KeeperException, InterruptedException {
		LOGGER.info("initNode......" + JSON.toJSONString(nodeList));
		List<String> dataList = new ArrayList<String>();
		for (String serviceId : nodeList) {
			dataList.add(serviceId);
			if (!remoteServiceMap.containsKey(serviceId)) {
				List<String> ips = Collections.synchronizedList(new ArrayList<String>());
				remoteServiceMap.put(serviceId, ips);
			}
			//拿到ip信息
			List<String> ipINfoList = zk.getChildren(Constans.DATA_PATH + "/" + serviceId, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					LOGGER.info("event......" + JSON.toJSONString(event));
					onIPNodeEvent(zk, event);
				}
			});

			for (String ipInfo : ipINfoList) {
				remoteServiceMap.get(serviceId).add(ipInfo);
			}
		}
		LOGGER.debug("node data: {}", dataList);
	}

	/***
	 * 节点添加事件处理
	 *
	 */
	private void onIPNodeEvent(final ZooKeeper zk, WatchedEvent event) {
		try {
			String path = event.getPath();
			String serviceId = path.substring(path.lastIndexOf("/")+1, path.length());
			List<String> ipNodes = zk.getChildren(path, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					onIPNodeEvent(zk, event);
				}
			});
			//更新节点信息
			IRouteUtil.updateServiceNodeInfo(serviceId,ipNodes);
			
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	private void connectServer() {
		try {
			this.zooKeeper = new ZooKeeper(getZkAdress(), ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			//并且等待zk连接上才能进行下一步
			latch.await();
			LOGGER.info("connected zookeeper:" + getZkAdress());
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public String getZkAdress() {
		return zkAdress;
	}

	public void setZkAdress(String zkAdress) {
		this.zkAdress = zkAdress;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		this.zooKeeper.close();
		//释放相应的资源
		IRouteUtil.destroy();
	}

}
