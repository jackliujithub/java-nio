package com.rpc.protocol.registry.impl;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.rpc.protocol.Constans;
import com.rpc.protocol.heart.ServerHeartBeat;
import com.rpc.protocol.registry.ServiceDataNode;
import com.rpc.protocol.registry.ServiceRegistry;

public class ZookeeperServiceRegistry implements ServiceRegistry,InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

	/**zk 连接时间 3s*/
	private static final int ZK_SESSION_TIMEOUT = 3000;

	/**zk地址*/
	private String zkAdress; 
	
	/**存放路径*/
	//private String dataPath = "/rpc/provider";
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	/**zookeeper客户端*/
	private ZooKeeper zooKeeper;
	
	public void init(){
		this.zooKeeper = connectServer();
	}
	
	@Override
	public void register(ServiceDataNode serviceDataNode) {
		String path = Constans.DATA_PATH + "/" +serviceDataNode.getPath();
		Stat stat = null;
		//根据服务id,创建类目
		try {
			stat = zooKeeper.exists(Constans.DATA_PATH, false);
			
			stat = zooKeeper.exists(path, false);
			if(stat == null){
				zooKeeper.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (KeeperException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//根据ip，创建临时节点
		try {	
			String ipNodePath = path + "/"+serviceDataNode.getData();
			stat = zooKeeper.exists(ipNodePath, false);
			if(null == stat){
				String ipPath = zooKeeper.create(ipNodePath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				LOGGER.info("register service,path:" + ipPath +";data:" + serviceDataNode.getData());
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(zkAdress, ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }
	
	public String getZkAdress() {
		return zkAdress;
	}

	public void setZkAdress(String zkAdress) {
		this.zkAdress = zkAdress;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//zk连接，注册等信息
		init();
		ServerHeartBeat.init();
	}
}
