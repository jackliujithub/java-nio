package com.rpc.protocol.text;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TextStreamingHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger logger = Logger
			.getLogger(TextStreamingHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		logger.info("rev msg:" + msg);
		// {获得客户端传入参数
		JSONObject jsonObject = JSONObject.parseObject(msg);
		long seq = jsonObject.getLongValue("seq");
		String className = jsonObject.getString("className");
		String methodStr = jsonObject.getString("method");
		// }
		JSONObject resultJsonObject = new JSONObject();
		try {
			// {调用相应类方法
			// JSONObject param = jsonObject.getJSONObject("param");
			Class targetClass = Class.forName(className);
			Object object = targetClass.newInstance();
			Method method = targetClass.getMethod(methodStr, new Class[] {});
			Object resultObject = method.invoke(object, new Object[] {});
			// }

			// {调用结果的转换，//TODO 序列化
			String resultStr = resultObject.toString();
			// }

			// {组装返回结果
			resultJsonObject.put("seq", seq);
			resultJsonObject.put("result", resultStr);
			resultJsonObject.put("code", 200);
			// }
		} catch (Exception e) {
			e.printStackTrace();
			resultJsonObject.put("seq", seq);
			resultJsonObject.put("code", 500);
			resultJsonObject.put("errorMsg", e.getMessage());
		}
		ctx.writeAndFlush(resultJsonObject.toJSONString() + "\n");

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		System.out.println("======server active ==========");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("======server channelInactive ==========");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		System.out.println("Unexpected exception from downstream");
		cause.printStackTrace();
	}
}
