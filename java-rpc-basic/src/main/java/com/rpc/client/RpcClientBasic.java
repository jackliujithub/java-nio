package com.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.rpc.client.route.IRouteUtil;
import com.rpc.protocol.Constans;
import com.rpc.protocol.Request;
import com.rpc.protocol.Response;
import com.rpc.protocol.protostuff.ProtostuffDecoder;
import com.rpc.protocol.protostuff.ProtostuffEncoder;
import com.rpc.service.User;
import com.rpc.service.UserService;
import com.rpc.util.RpcException;

public class RpcClientBasic {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientBasic.class);
	
	/**结果集*/
	private Map<String, Result> resultMap = new ConcurrentHashMap<String, Result>();
	/**连接channel*/
	private Channel ch;
	/**ip信息*/
	private String host;
	/**端口信息*/
	private int port;
	/**心跳失败次数*/
	private AtomicInteger heartBeartFail = new AtomicInteger(0);
	
	public static void main(String[] args) {

		//启动spring ，加载相应的服务
		//		context = new ClassPathXmlApplicationContext("classpath:spring-config.xml");
		//		UserService userService = (UserService)context.getBean("userService");
		//		User user = new User();
		//		user.setName("张思_"+Thread.currentThread().getName());
		//		user.setPassword("abc");
		//		UserService userService = getUserService(UserService.class);
		//		userService.addUser(user);
		RpcClientBasic rpcClientBasic = new RpcClientBasic();
		try {
			rpcClientBasic.init("192.168.192.115", 21888);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UserService getUserService(final Class<?> serviceClass) {
		UserService userService = (UserService) Proxy.newProxyInstance(serviceClass.getClassLoader(),
				new Class[] { serviceClass }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String seq = SequenceUtil.getSeq();
						Request request = new Request();
						request.setSeq(seq);
						request.setClassName(serviceClass.getName());
						request.setMethodName(method.getName());
						request.setParams(args);
						if (null != args) {
							Class[] paraTypes = new Class[args.length];
							for (int i = 0; i < args.length; i++) {
								paraTypes[i] = args[i].getClass();
							}
							request.setParameterTypes(paraTypes);
						}
						Result result = sendMsg(request, seq);
						return result.getResult();
					}
				});
		return userService;
	}

	public void testRpcServer() {
		//模拟1000个业务线程
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					//{//TODO 模拟组包
					String seq = SequenceUtil.getSeq();
					Request request = new Request();
					request.setSeq(seq);
					request.setClassName("com.rpc.service.impl.UserServiceImpl");
					request.setMethodName("addUser");
					User user = new User();
					user.setName("张思_" + Thread.currentThread().getName());
					user.setPassword("abc");
					request.setParameterTypes(new Class[] { user.getClass() });
					request.setParams(new Object[] { user });
					//}

					//{TODO 发送信息
					Result result = sendMsg(request, seq);
					//}

					//{获得执行结果
					String resultstr = JSON.toJSONString(result.getResult());
					System.out.println("response result:" + resultstr + ";thread:" + Thread.currentThread().getName());
					//}
				}
			}).start();
		}
	}

	public void init(String host, int port) throws InterruptedException {
		this.host = host;
		this.port = port;
		final int connectTimeout = 2000;
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.option(ChannelOption.SO_KEEPALIVE,true);
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
		b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				ch.pipeline().addLast(new ProtostuffDecoder(Response.class))
						.addLast(new ProtostuffEncoder(Request.class));

				pipeline.addLast("handler", new SimpleChannelInboundHandler<Response>() {
					@Override
					protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
						msgHander(msg);
					}

					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						logger.error("server caught exception", cause);
						closeHandler();
					}
					
				});
			}
		});
		ch = b.connect(host, port).sync().channel();
		boolean isConnected = ch.isActive();
		logger.info("is connect:" + isConnected);
		if(!isConnected){
			throw new RpcException("连接失败:"+this.host+":" + this.port);
		}
	}

	public Channel getChannel() {
		return this.ch;
	}

	public Channel getChannel(Request request) {
		return this.ch;
	}

	public Result sendMsg(Request request, String id) {
		Result result = new Result();
		result.setRequest(request);
		resultMap.put(id, result);
		Channel ch = getChannel(request);
		ch.writeAndFlush(request);
		logger.info(Thread.currentThread().getName() + ":send msg:" + JSON.toJSONString(request));
		return result;
	}

	/**
	 * 关闭连接处理
	 * */
	public void closeHandler() {
		try {
			InetSocketAddress inetSocketAddress = (InetSocketAddress)ch.remoteAddress();
			String serverIpInfo = inetSocketAddress.getAddress().getHostAddress() + "," + inetSocketAddress.getPort();
			logger.error("close the channel:{}",new Object[]{serverIpInfo});
			RpcClientBasic rpcClientBasic = IRouteUtil.removeConnect(serverIpInfo);
			ch.close().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 心跳包的发送
	 * 
	 */
	public Result sendHeaderBeat() {
		Request request = new Request();
		request.setPackageType(Constans.HEADRT_PACKAGE);
		String seq = SequenceUtil.getSeq();
		request.setSeq(seq);
		return this.sendMsg(request, seq);
	}

	/**
	 * 结果处理
	 * @param msg
	 */
	private void msgHander(Response resp) {
		switch (resp.getPackageType()) {
		case Constans.HEADRT_PACKAGE:
			headerHander(resp);
			break;
		case Constans.BUSINESS_PACKAGE:
			businessHander(resp);
		default:
			break;
		}
	}

	/**
	 * 心跳返回处理
	 *
	 */
	private void headerHander(Response resp) {
		String ip = ((InetSocketAddress) ch.remoteAddress()).getAddress().getHostAddress();
		int port = ((InetSocketAddress) ch.remoteAddress()).getPort();
		logger.info("receive heart beat from:" + ip+":"+port+".result:" + JSON.toJSONString(resp));
		businessHander(resp);
	}

	/**
	 * 业务方法返回处理
	 * 
	 */
	private void businessHander(Response resp) {
		String seq = resp.getSeq();
		Object resultStr = resp.getResult();
		//TODO 反序列化
		Result result = resultMap.remove(seq);
		if (result == null) {
			logger.error("invoke timeout.ip:"+ host +";port:" + port+";detail:" + JSON.toJSONString(result.getRequest()));
			return;
		}
		synchronized (result) {
			if(result.isTimeOut()){
				logger.error("invoke timeout.ip:"+ host +";port:" + port+";detail:" + JSON.toJSONString(result.getRequest()));
				return;
			}
			result.setResult(resultStr);
			result.setResultOk(true);
			result.notifyAll();
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 *添加失败次数
	 */
	public void addHeartBeatFail() {
		this.heartBeartFail.incrementAndGet();
	}

	/**
	 *获得失败次数
	 */
	public int getHeartBeartFail() {
		return this.heartBeartFail.intValue();
	}

	/**
	 *心跳有效计数复位
	 */
	public void resetHeartBeat() {
		this.heartBeartFail.set(0);
	}

	/**
	 *尝试进行重新连接，重试两次
	 */
	public boolean retryConnect() {
		int reConnectCount = 2;
		int retryCount = 0;
		boolean success = true;
		logger.info("retryConnect.....{}:{}",new Object[]{this.host,this.port});
		for(retryCount=0;retryCount<reConnectCount;retryCount++){
			try {
				init(this.host, this.port);
				logger.info("retryConnect.....{}:{}. count:",new Object[]{this.host,this.port,retryCount+1});
			} catch (InterruptedException e) {
				success = false;
				logger.error("retry connnect fail," + (retryCount + 1)+"detail",e);
				continue;
			}catch (Exception e) {
				success = false;
				logger.error("retry connnect fail," + (retryCount + 1)+"detail",e);
				continue;
			}
			if(!success){
				continue;
			}
		}
		return success;
	}
}
