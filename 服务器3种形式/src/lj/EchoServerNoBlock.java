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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


//主线程同时处理客户端tcp连接与数据处理
public class EchoServerNoBlock {
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector;
	private Charset charSet = Charset.forName("utf-8");
	
	public EchoServerNoBlock() throws IOException {
		selector = Selector.open();
		this.serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().setReuseAddress(true);	//服务器可立即重启
		serverSocketChannel.configureBlocking(false);		//服务器配置成非阻塞模式
		serverSocketChannel.socket().bind(new InetSocketAddress(12121)); //地址一旦绑定,服务器开启
		System.out.println("服务器开启. . . ");
	}
	
	public void service() throws IOException {
		//服务器端注册监听的事件
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	//	System.out.println(selector.select()); //打印1  
	//	System.out.println(selector.select()); // 打印0 所以不能直接打印发生事件的个数
		while(selector.select()>0){  // selector.select():返回发生的事件个数
			Set<SelectionKey> readyKeys = selector.selectedKeys(); //该方法返回的集合是"单例的,公用的",下次循环得到的还是上次循环的集合
			for(SelectionKey key:readyKeys){
				try{
				    //因为下面取客户端时serverSocketChannel.accept()在非阻塞模式下会立刻返回,如果没有客户端连接就返回null 
				    //并且serverSocketChannel.accept()总是返回新连接的客户端,已经接收的客户端就不去返回
					readyKeys.remove(key);  //集合中必须删除处理过的事件,否则在下次循环中还需处理一遍
					if(key.isAcceptable()){ //连接事件触发
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
						SocketChannel socketChannel = serverSocketChannel.accept();
						socketChannel.configureBlocking(false);
						
						Socket socket = socketChannel.socket();
						System.out.println("请求来自" + socket.getInetAddress() + ":" + socket.getPort());
						
						ByteBuffer buffer = ByteBuffer.allocate(1024);	//存放读来的数据
						socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE , buffer);
					}
					if(key.isReadable()){ //服务器读事件触发
						recieve(key);
					}
					if(key.isWritable()){  //服务器写事件触发  (客户端不发来信息,会在此处无限循环)
						send(key);
					}
				}catch(IOException e){
					e.printStackTrace();
					try{
						if(key != null){
							key.cancel();
							key.channel().close();
						}
					}catch(IOException e1){
						e1.printStackTrace();
					}
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
		new EchoServerNoBlock().service();
	}
}

























