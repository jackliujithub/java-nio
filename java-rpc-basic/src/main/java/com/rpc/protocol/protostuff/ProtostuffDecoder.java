package com.rpc.protocol.protostuff;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ProtostuffDecoder extends ByteToMessageDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffDecoder.class);
	
	private Class<?> genericClass;

	public ProtostuffDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		LOGGER.info("thread name:" + Thread.currentThread().getName());
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (dataLength < 0) {
			ctx.close();
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);

		Object obj = ProtostuffSerialization.deserialize(data, genericClass);
		out.add(obj);
	}
}
