package com.rpc.protocol.protostuff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtostuffEncoder extends MessageToByteEncoder{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffEncoder.class);
	
	private Class<?> genericClass;
	
	public ProtostuffEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		  LOGGER.info("thread name:" + Thread.currentThread().getName());
		  if (genericClass.isInstance(msg)) {
	            byte[] data = ProtostuffSerialization.serialize(msg);
	            out.writeInt(data.length);
	            out.writeBytes(data);
	        }
	}

	
}
