package WA;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws InterruptedException{
		Scanner scanner = new Scanner(System.in);
		
		String name = null;
		String configIp = null;
		String configPort = null;
		int cPort = -1;
		String serverName = null;
		
		//Init Data
		//start Parameters
		if(args.length == 4) {
			name = args[0];
			configIp = args[1];
			configPort = args[2];
			serverName = args[3];
		}
		//If no start parameters, enter manually
		else {
			System.out.println("Please enter your name: ");
			name = scanner.next();
			System.out.println("Please enter config server ip: ");
			configIp = scanner.next();
			System.out.println("Please enter config server port: ");
			configPort = scanner.next();
			System.out.println("Please enter servername to connect: ");
			serverName = scanner.next();
		}
		
		cPort = Integer.parseInt(configPort);
		
		
		//Connection to config server
		Socket socket = null;
		Listener lis = null;
		
		try {
			socket = new Socket(configIp, cPort);
			lis = new Listener(socket);
			lis.start();
		} 
		catch (UnknownHostException e) {
			System.out.println("Could not connect to config server!");
			System.exit(1);
		} 
		catch (IOException e) {
			System.out.println("Could not connect to config server!");
			System.exit(1);
		}
		
		//Authorize
		writeMsg("name " + name, socket);
		Thread.sleep(500);
		
		//get server ip
		writeMsg("read " + serverName + ".ip", socket);
		String serverIp = getServerData(lis, 10);
		System.out.println(serverIp);
		if(serverIp == null || serverIp.equals("data not available")) {
			System.out.println("Server IP could not be detected");
			System.exit(1);
		}
		
		//get server port
		writeMsg("read " + serverName + ".port", socket);
		String serverPort = getServerData(lis, 10);
		System.out.println("serverPort: " + serverPort);
		if(serverPort == null || serverPort.equals("data not available")) {
			System.out.println("Server Port could not be detected");
			System.exit(1);
		}
		
		//Get informed about rights on server (admin or not)
		writeMsg("read " + serverName + ".admin", socket);
		String admin = getServerData(lis, 10);
		if(admin.equals(name)) {
			System.out.println("You have admin rights on server: " + serverName);
		}
		else {
			System.out.println("You don't have admin rights on server: " + serverName);
		}
		
		int sPort = Integer.parseInt(serverPort);
		try {
			socket.close();
			System.out.println("Connecting to server...");
			socket = new Socket(serverIp, sPort);
			System.out.println("Connected");
			writeMsg("name " + name, socket);
			Thread.sleep(200);
		} 
		catch (UnknownHostException e) {
			System.out.println("Connection to server failed: " + e);
			System.exit(1);
		} 
		catch (IOException e) {
			System.out.println("Connection to server failed: " + e);
			System.exit(1);
		}
		lis.connection = false;
		lis = new Listener(socket);
		Writer wri = new Writer(socket);
		
		lis.start();
		wri.start();
		
		while(lis.isAlive()) {
			Thread.sleep(100);
			String msg = getNewMsg(lis);
			if(msg != null) {
				System.out.println(msg);
			}
		}
		System.exit(1);
	}
	
	//Write a message to server
	public static void writeMsg(String msg, Socket sock) {
		try {
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			out.writeUTF(msg);
			out.flush();
		} 
		catch (IOException e) {
			System.out.println("DataOutputStream error");
			System.exit(1);
		}
	}
	
	//Returns message if there is a new one and null if there is not
	public static String getNewMsg(Listener lis) {
		String msg = null;
		
		if(!lis.msg.equals("")) {
			msg = lis.msg;
			lis.msg = "";
		}
		return msg;
	}
	
	public static String getServerData(Listener lis, int interval) {
		
		for(int i = 0; i < interval; i++) {
			try {
				Thread.sleep(200);
			} 
			catch (InterruptedException e) {
				System.out.println("Sleep error");
				System.exit(1);
			}
			
			if(!lis.msg.equals("")) {
				return lis.msg;
			}
		}
		return null;
	}
}
