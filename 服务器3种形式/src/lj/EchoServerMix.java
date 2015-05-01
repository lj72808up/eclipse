package lj;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class EchoServerMix {
	private ServerSocketChannel serversoketChannel = null;
	private Selector selector = null;
	private Charset charSet = Charset.forName("utf-8");
	private Object obj = new Object();
	
	public EchoServerMix() throws IOException{
		this.selector = Selector.open();
		this.serversoketChannel = ServerSocketChannel.open();
		this.serversoketChannel.configureBlocking(true);
		this.serversoketChannel.socket().setReuseAddress(true);
		this.serversoketChannel.socket().bind(new InetSocketAddress(12121));
		System.out.println("服务器启动. . .");
	}
	
	public void accept(){
		while(true){
			try {
				SocketChannel socketChannel = this.serversoketChannel.accept(); //阻塞
				socketChannel.configureBlocking(false); //非阻塞
				
				Socket socket = socketChannel.socket();
				System.out.println("请求来自" + socket.getInetAddress() + ":" + socket.getPort());
				
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				synchronized (obj) {
					selector.wakeup(); // 业务处理的Selector.select()一开始处于阻塞状态,有连接时唤醒,同时防止socketChannel.register()和selector.select()一起阻塞
					socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE,buffer);;
				}
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	private void service() throws IOException{
		while(true){
			synchronized (obj) {}; //里面可以不放任何代码
			int i = selector.select();
			if(i==0) continue;	   //没有事件发生就继续循环
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			for(SelectionKey key:readyKeys){
				readyKeys.remove(key);
				if(key.isReadable()){
					 recieve(key);
				}
				if(key.isWritable()){
					send(key);
				}
			}
		}
	}
	
	private void send(SelectionKey key) throws IOException {
		//从大Buffer中取得一行string,编码后放到发送Buffer
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		buffer.flip();
		CharBuffer charBuffer = charSet.decode(buffer);
		String data = charBuffer.toString();
		
		if(data.indexOf("\r\n")==-1) // 若此时的Buffer中不是一整行数据,就返回,先不去写
			return;
		
		data = data.substring(0,data.indexOf("\n")+1);
		System.out.print(""+data);
		
		ByteBuffer outputBuffer = charSet.encode("echo:"+data);
		while(outputBuffer.hasRemaining()){
			socketChannel.write(outputBuffer);
		}
		
		//发送后在大Buffer中删除已发送完毕的数据 : 大Buffer中前面的data已被发送,先致position
		ByteBuffer compareBuffer = charSet.encode(data);
		buffer.position(compareBuffer.limit());
		buffer.compact();	//compact会把position前面的数据清除,并把后面的数据从Buffer的0位置放置
		
		//最后发现bye结束,则释放key,关闭channel
		if(data.equals("bye\r\n")){
			key.cancel();
			socketChannel.close();
			System.out.println("客户端退出:"+socketChannel.socket().getInetAddress());
		}
	}

	private void recieve(SelectionKey key) throws IOException {
		//从key里面拿到连接的socketChannel和buffer
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		SocketChannel socketChannel = (SocketChannel)key.channel();
		
		//非阻塞模式下,read(Buffer)方法读到多少数据是不确定的,不一定是一行, 所以在写事件中,应该截取读buffer中的一行数据
		ByteBuffer readBuffer = ByteBuffer.allocate(64); //假设一次read不会导致buffer溢出
		socketChannel.read(readBuffer);
		readBuffer.flip();  //一次read完毕后,把readBuffer中的数据复制到大Buffer中
		buffer.limit(buffer.capacity());
		buffer.put(readBuffer);
	}
	
	public static void main(String[] args) throws IOException {
		final EchoServerMix server  = new EchoServerMix();
		new Thread(){
			@Override
			public void run() {
				server.accept();
			}
		}.start();
		
		server.service();
	}
}
