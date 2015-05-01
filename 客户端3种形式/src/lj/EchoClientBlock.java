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
import java.nio.channels.SocketChannel;

public class EchoClientBlock {

	private SocketChannel socketChannel = null;
	
	public EchoClientBlock() throws IOException{
		this.socketChannel = SocketChannel.open();
		InetSocketAddress serverAddress = new InetSocketAddress(12121);
		socketChannel.connect(serverAddress);	//客户端链接服务器
		System.out.println("与服务器建立连接");
	}
	
	public void talk(){
		Socket server = this.socketChannel.socket();
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in,"utf-8"));
			OutputStream out = server.getOutputStream();
			InputStream in = server.getInputStream();
			
			BufferedReader sreader = new BufferedReader(new InputStreamReader(in,"utf-8"));
			PrintWriter swriter = new PrintWriter(new OutputStreamWriter(out,"utf-8"), true);	//自动flush
			
			String msg = null;
			while((msg=reader.readLine())!=null){
				swriter.println(msg);
				System.out.println(sreader.readLine());
				if(msg.equals("bye")){
					break;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new EchoClientBlock().talk();
	}
}
