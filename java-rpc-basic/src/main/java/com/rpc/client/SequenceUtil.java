package com.rpc.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceUtil {

	private static AtomicLong number = new AtomicLong();
	
	private static String ipString = "";
	static{
		getLocalIp();
	}
	public static String getSeq(){
		return ipString + "_" + number.getAndIncrement();
	}
	
	private static void getLocalIp(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
			ipString=addr.getHostAddress().toString();//获得本机IP
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println(SequenceUtil.ipString);
	}
}
