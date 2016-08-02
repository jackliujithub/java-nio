package com.rpc.test.threads;

public class DaemonThreadTest {

	public static void main(String[] args) {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		t.setDaemon(false);
		//t.setDaemon(true);
		t.start();
	}
}
