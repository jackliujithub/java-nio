
package com.rpc.protocol.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.rpc.protocol.Constans;

public class ThreadPoolUtil {

	static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtil.class);

	public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Constans.corePoolSize, Constans.maximumPoolSize, Integer.MAX_VALUE, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200), new NamedThreadFactory("rpc-business-", true), new defaultRejectHandler());
	
	/**
	 *提交任务
	 */
	public static void submit(Runnable command){
		threadPoolExecutor.execute(command);
	}
	
	/**
	 *关闭
	 */
	public static void shutdown(){
		threadPoolExecutor.shutdown();
	}
}



/**
 * 线程工厂
 */
class NamedThreadFactory implements ThreadFactory
{
	private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

	private final AtomicInteger mThreadNum = new AtomicInteger(1);

	private final String mPrefix;

	private final boolean mDaemo;

	private final ThreadGroup mGroup;

	public NamedThreadFactory()
	{
		this("pool-" + POOL_SEQ.getAndIncrement(),false);
	}

	public NamedThreadFactory(String prefix)
	{
		this(prefix,false);
	}

	public NamedThreadFactory(String prefix,boolean daemo)
	{
		mPrefix = prefix + "-thread-";
		mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        mGroup = ( s == null ) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
	}

	public Thread newThread(Runnable runnable)
	{
		String name = mPrefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(mGroup,runnable,name,0);
        ret.setDaemon(mDaemo);
        return ret;
	}

	public ThreadGroup getThreadGroup()
	{
		return mGroup;
	}
}

/**
 * 任务丢弃策越
 */
class defaultRejectHandler implements RejectedExecutionHandler {

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if(r instanceof RpcBusinessTask){
			ThreadPoolUtil.LOGGER.error("discard task :" + JSON.toJSONString(((RpcBusinessTask)r).getMsg()));
			return;
		}
		ThreadPoolUtil.LOGGER.error("discard task :" + JSON.toJSONString(r));
	}
	
}