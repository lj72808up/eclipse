package lj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServerBlock {

	private ServerSocketChannel serverSocketChannel = null;	//nio获取服务器端口通道
	private ExecutorService executorService = null;			//线程池处理客户端请求
	
	//构造函数实例化服务器端口 + 线程池
	public EchoServerBlock() throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		/* serverSocketChannel.socket()获取服务器打开的socket, Socket的setReuseAddress(true)让服务器可以快速重连*/
		serverSocketChannel.socket().setReuseAddress(true); 
		serverSocketChannel.socket().bind(new InetSocketAddress(12121)); //绑定serverSocketChannel到本机的端口
		
		int num = Runtime.getRuntime().availableProcessors()*4;//让每个核心运行4个线程
		executorService = Executors.newFixedThreadPool(num);
		System.out.println("阻塞版服务器启动 . . .");
	}
	
	//客户端请求加入线程池处理
	public void service(){
		while(true){
			try {
				//SocketChannel获取客户端的socket流, accept()方法是阻塞的, 没有请求的时候会阻塞
				SocketChannel socketChannel = this.serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	class Handler implements Runnable{
		private SocketChannel socketChannel;
		private Handler(SocketChannel socketChannel){
			this.socketChannel = socketChannel;
		}
		public void run() {
			handle(socketChannel);
		}
		public void handle(SocketChannel socketChannel){
			Socket socket = socketChannel.socket();	//阻塞模式从nio中获取socket与serversocket
			System.out.println("接收客户端"+socket.getInetAddress()+":"+socket.getPort());
			try {
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,"utf-8"), true);	//自动flush
				
				String msg = null;
				while((msg=reader.readLine())!=null){
					System.out.println("客户端发来:"+msg);
					writer.println("echo:"+msg);	//把数据发回客户端
					if(msg.equals("bye")){
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					if(socketChannel!=null)  socketChannel.close();
					System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public static void main(String[] args) throws IOException {
		new EchoServerBlock().service();
	}
}
