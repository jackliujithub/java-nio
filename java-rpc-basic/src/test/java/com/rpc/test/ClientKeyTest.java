package com.rpc.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class ClientKeyTest {

	@Test
	public void clienSelectKeyTest(){
		try {
			final Selector selector = SelectorProvider.provider().openSelector();
			
			SocketChannel socketChannel = SocketChannel.open();
			
			socketChannel.configureBlocking(false);
			System.out.println("valid ops:" + socketChannel.validOps());
			socketChannel.register(selector, SelectionKey.OP_READ);
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						while (true) {
							System.out.println("client select....");
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

								if (selectionKey.isConnectable()) {
									System.out.println(".......connect");
								}

								if (selectionKey.isReadable()) {
									System.out.println(".......readAble");
									SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
									ByteBuffer dst = ByteBuffer.allocate(10);
									dst.clear();
									int readLenth = socketChannel.read(dst);
									dst.flip();
									System.out.println("clint readLenth:" + readLenth);
									System.out.println("getint:" + dst.getInt());
									socketChannel.close();
									selectionKey.cancel();
								}

							}
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();;
			Thread.sleep(1000);
			
			boolean connected = socketChannel.connect(new InetSocketAddress("127.0.0.1",9002));
			System.out.println("client connected...." + connected);
			boolean finishConnect = false;
			if(!connected){
				while(!(finishConnect = socketChannel.finishConnect())){
					System.out.println("finishConnect...." + finishConnect);
				}
			}
			
			Thread.sleep(1000 * 86400);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
