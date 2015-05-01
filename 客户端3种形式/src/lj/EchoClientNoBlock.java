package lj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class EchoClientNoBlock {

	private SocketChannel socketaChannel = null;
	private Charset charset = Charset.forName("utf-8");
	private Selector selector = null;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);		//发往服务器
	private ByteBuffer recieveBuffer = ByteBuffer.allocate(1024);	//接受服务器发来的数据
	
	public EchoClientNoBlock() throws IOException{
		this.socketaChannel = SocketChannel.open();
		this.socketaChannel.connect(new InetSocketAddress(12121));  //阻塞连接
		this.socketaChannel.configureBlocking(false);
		this.selector = Selector.open();
		System.out.println("服务器连接成功");
	}
	
	public void recieveFromUser() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in,"utf-8"));
		String msg = "";
		while(true){
			msg = reader.readLine();
			synchronized (sendBuffer) {
				this.sendBuffer.put(this.charset.encode(msg+"\r\n")); //encode("str")返回ByteBuffer
			}
			if(msg.equals("bye"))
				break;
		}
	}
	
	public void sendAndRecieve() throws IOException {
		this.socketaChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		while(selector.select()>0){
			Set<SelectionKey> readyKeys = this.selector.selectedKeys();
			for(SelectionKey key:readyKeys){
				readyKeys.remove(key);
				if(key.isReadable())
					recieve(key);
				if(key.isWritable())
					send(key);
			}
		}
	}
	
	private void send(SelectionKey key) throws IOException {	//从sendBuffer里面拿出数据,删除已经发送的数据
		SocketChannel socketaChannel = (SocketChannel) key.channel();
		synchronized (sendBuffer) {
			sendBuffer.flip(); //准备读
			socketaChannel.write(sendBuffer);
			sendBuffer.compact(); //删除发送完毕的数据
		}
	}

	private void recieve(SelectionKey key) throws IOException {
		SocketChannel socketaChannel = (SocketChannel) key.channel();
		socketaChannel.read(recieveBuffer);
		recieveBuffer.flip();
		String recieveStr = this.charset.decode(recieveBuffer).toString();
		if(recieveStr.indexOf("\n")==-1) return;
		String outputStr = recieveStr.substring(0,recieveStr.indexOf("\n")+1);
		System.out.print(outputStr);
		ByteBuffer temp = this.charset.encode(outputStr); 
		recieveBuffer.position(temp.limit());
		recieveBuffer.compact();
	}

	public static void main(String[] args) throws IOException {
		final EchoClientNoBlock client = new EchoClientNoBlock();
		new Thread(){ //先开启接受输入数据的线程,否则阻塞
			@Override
			public void run() {
				try {
					client.recieveFromUser();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		client.sendAndRecieve();
	}
}
