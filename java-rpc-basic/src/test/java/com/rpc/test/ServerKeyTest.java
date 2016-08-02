package com.rpc.test;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class ServerKeyTest {

	@Test
	public void testServerSelecttionKey() {
		try {
			// 获得selector
			final Selector selector = SelectorProvider.provider().openSelector();
			// 打开ServerSocketChannel
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel
					.open();
			// 配置为非阻塞模式
			serverSocketChannel.configureBlocking(false);
			// 注册事件
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							System.out.println("sub thread start....");
							int keyCount = selector.select();
							if (keyCount < 1) {
								System.out.println("......key count less than one");
								continue;
							}
							Set<SelectionKey> keySets = selector.selectedKeys();
							Iterator<SelectionKey> keysetIterator = keySets
									.iterator();
							while (keysetIterator.hasNext()) {
								SelectionKey selectionKey = keysetIterator.next();
								keysetIterator.remove();
								if(!selectionKey.isValid()){
									continue;
								}
								
								if (selectionKey.isAcceptable()) {
									System.out.println(".......accept");
									SocketChannel sc = serverSocketChannel.accept();
									sc.configureBlocking(false);
									sc.register(selector, SelectionKey.OP_READ);
									System.out.println("sc connected..." + sc.isConnected());
									ByteBuffer byteBuf = ByteBuffer.allocate(10);
									byteBuf.clear();
									byteBuf.putInt(123);
									byteBuf.flip();
									while(byteBuf.hasRemaining()){
										int byteLength = sc.write(byteBuf);
										System.out.println("byteLength......" + byteLength);
									}			
								}

								if (selectionKey.isConnectable()) {
									System.out.println(".......connect");
								}

								if (selectionKey.isReadable()) {
									System.out.println(".......readAble");
									SocketChannel sc = (SocketChannel)selectionKey.channel();
									ByteBuffer dst = ByteBuffer.allocate(10);
									dst.clear();
									int readLenth = sc.read(dst);
									//第一次读出来是-1表示对方已经关闭连接
									if(readLenth == -1){
										sc.close();
										selectionKey.cancel();
									}
									dst.flip();
									System.out.println("server readLenth:" + readLenth);
								}
							}
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			Thread.sleep(1000);
			// 监听本地端口
			serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 9002), 100);
			System.out.println("server start bind .....");
			Thread.sleep(1000 * 86400);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChunkSize(){
		System.out.println(81922 << 11);
		System.out.println((8192 << 11) > (Integer.MAX_VALUE/4));
		int pageSize = 8192;
		System.out.println("pageSize >>=1:"+(pageSize >>=1));
		System.out.println((2&1) == 0);
		int pageShift = 0;
		for (int i = pageSize; i != 0 ; i >>= 1) {
			pageShift++;
		}
		System.out.println("pageShift:" + pageShift);
		System.out.println("~pagesize:"+ (~(8192-1)));
		System.out.println("512>>>4:" + (512>>>4));
		System.out.println("512>>4:" + (512>>4));
		
		System.out.println("4096 & -8192:" + (4096 & -8192));
		
		System.out.println("((long) Integer.MAX_VALUE + 1) / 2:" + ((long) Integer.MAX_VALUE + 1) / 2);
		System.out.println("1<<12:" + (1<<12));
		
		System.out.println("(160*1024*1024) >>> 13:" + ((160*1024*1024) >>> 13));
	}
	
	@Test
	public void testMemoryMap(){
		int ST_UNUSED = 0;
		int maxSubpageAllocs = 2048;
		int maxOrder = 11;
		int chunkSizeInPages = 20480;
		int[] memoryMap = new int[maxSubpageAllocs << 1];
	        int memoryMapIndex = 1;
	        for (int i = 0; i <= maxOrder; i ++) {
	            int runSizeInPages = chunkSizeInPages >>> i;
	            for (int j = 0; j < chunkSizeInPages; j += runSizeInPages) {
	                //noinspection PointlessBitwiseExpression
	                memoryMap[memoryMapIndex ++] = j << 17 | runSizeInPages << 2 | ST_UNUSED;
	                System.out.println(memoryMapIndex-1 + ":" + memoryMap[memoryMapIndex-1]);
	            }
	        }
	}
}
