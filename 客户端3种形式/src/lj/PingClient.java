package lj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class PingClient {

	private Selector selector;
	private LinkedList<Target> targets = new LinkedList<Target>(); //存放要ping的url
	private LinkedList<Target> finishedTarget = new LinkedList<Target>();  //存放完成连接的url
	boolean shutdown = false;
	
	public PingClient() throws IOException{
		this.selector = Selector.open();
		new Connector().start(); // 开启连接线程
		//new Printer().start();	 // 开启打印线程
		this.reieveInput();		 // 接受用户输入的hostname
	}
	
	
	public void reieveInput() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String hostname = null;
		while((hostname=reader.readLine())!=null){
			if(!hostname.equals("bye")){ //转换target对象,并进行连接
				this.addTarget(new Target(hostname));
			}else{ //输入bye时
				this.shutdown = true;
				this.selector.wakeup(); //唤醒阻塞的线程
				break;
			}
		}
	}
	
	public void regeistConnect(){	//取出targets的一个主机名向selector注册监听事件
		synchronized (targets) {	
			Target target = this.targets.removeFirst();
			try {
				target.socketChannel.register(this.selector,SelectionKey.OP_CONNECT,target);
			} catch (ClosedChannelException e) {
				try {
					target.socketChannel.close();
				} catch (IOException e1) {}
				target.failException = e;
				addFinishTarget(target);
			}
		}
	}
	
	private void addTarget(Target target) { //加入目标url
		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);	//非阻塞监听
			socketChannel.connect(target.inetSocketAddress);
			
			target.socketChannel = socketChannel;
			target.startConnect = System.currentTimeMillis();
			
			synchronized (targets) {
				targets.add(target);
			}
			this.selector.wakeup();
			
		} catch (IOException e) {
			if(socketChannel != null){
				try {
					socketChannel.close();
				} catch (IOException e1) {}
			}
			target.failException = e;
			addFinishTarget(target);
		}
	}
	
	public void addFinishTarget(Target target){
		synchronized (finishedTarget) {
			this.finishedTarget.notify();	//唤醒等待finishedTarget的线程
			finishedTarget.add(target);
		}
	}

	class Target{
		InetSocketAddress inetSocketAddress = null;	//让url转换成地址
		SocketChannel socketChannel = null;			//获取连接
		Exception failException = null;				//连接出现的异常
		long startConnect = 0;	//开始尝试连接的时间
		long endConnect = 0;	//成功连接时的时间
		boolean isPrint = false; 
		
		Target(String hostname){
			try {
				//InetAddress.getByName(hostname)会触发dns查找,若本地禁止查找dns,则报异常
				this.inetSocketAddress = new InetSocketAddress(InetAddress.getByName(hostname),80);
			} catch (UnknownHostException e) {
				this.failException = e;
			}
		}
		
		public void  prinTime(){
			if(endConnect!=0){
				System.out.println(this.inetSocketAddress+"::"+(endConnect-startConnect)+"ms");
			}else if(failException != null){
				System.out.println(failException);
			}else{
				System.out.println(this.inetSocketAddress+"::"+"Time Out");
			}
		}
	}
	
	class Connector extends Thread{
		@Override
		public void run() {
			while(!shutdown){
				
			}
		}
		
	}
}
