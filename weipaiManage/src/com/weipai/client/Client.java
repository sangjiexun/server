package com.weipai.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
	public static final String IP_ADDR = "localhost";//服务器地址 
	public static final int PORT = 10123;//服务器端口号  
	
    public static void main(String[] args) {  

        	Socket socket = null;
        	try {
        		//创建一个流套接字并将其连接到指定主机上的指定端口号
        		socket = new Socket(IP_ADDR, PORT);  
	            //读取服务器端数据  
	            DataInputStream input = new DataInputStream(socket.getInputStream());  
	            //向服务器端发送数据  
	            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	            Thread.sleep(1000);
				//登录操作，不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
				ClientSendRequest loginSend = new ClientSendRequest(0x158888);
				  Thread.sleep(1000);
				loginSend.output.writeUTF("notice");
				out.write(loginSend.entireMsg().array());//
				
				out.close();
	            input.close();
        	} catch (Exception e) {
        		System.out.println("客户端异常:" + e.getMessage()); 
        	} finally {
        		if (socket != null) {
        			try {
						socket.close();
					} catch (IOException e) {
						socket = null; 
						System.out.println("客户端 finally 异常:" + e.getMessage()); 
					}
        		}
        }  
    }
}  