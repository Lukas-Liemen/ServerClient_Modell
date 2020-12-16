package WA;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Server {

	public static void main(String[] args) throws InterruptedException {
		
		Scanner scanner = new Scanner(System.in);
		
		String configIp = null;
		String configPort = null;
		int cPort = -1;
		String name = null;
		String ownIp = null;
		String ownPort = null;
		int oPort = -1;
		String adminClient = null;
		
		//start parameters
		if(args.length >= 5) {
			configIp = args[0];
			configPort = args[1];
			name = args[2];
			ownIp = args[3];
			ownPort = args[4];
			
			if(args.length > 5) {
				adminClient = args[5];
			}
		}
		else {
			//manually enter values if no start parameters
			System.out.println("Please enter Configserver IP: ");
			configIp = scanner.next();
			System.out.println("Please enter Configserver Port: ");
			configPort = scanner.next();
			System.out.println("Please enter your name: ");
			name = scanner.next();
			System.out.println("Please enter own IP: ");
			ownIp = scanner.next();
			System.out.println("Please enter own Port: ");
			ownPort = scanner.next();
			
			//optional admin client
			System.out.println("Add admin client? (y/n)");
			String answer = scanner.next();
			if(answer.toLowerCase().equals("y")) {
				System.out.println("Please enter admin client name: ");
				adminClient = scanner.next();
			}
			else if(answer.toLowerCase().equals("n")) {
				
			}
			else {
				System.out.println("Wrong input!");
				System.exit(1);
			}
			
		}
		cPort = Integer.parseInt(configPort);
		oPort = Integer.parseInt(ownPort);
		
		//Init server
		ServerSocket sersoc = null;
		try {
			sersoc = new ServerSocket(oPort);
		} 
		catch (IOException e) {
			System.out.println("Port already in use");
			System.exit(1);
		}
		
		//Accept incoming clients, when they are known and enable communication to them
		ClientManager cManager = new ClientManager(sersoc);
		cManager.start();
		
		//connect to config server
		try {
			Socket socket = new Socket(configIp, cPort);
			Listener lis = new Listener(socket);
			lis.start();
			
			writeMsg("name " + name, socket);
			Thread.sleep(200);
			writeMsg("write " + name + ".ip " + ownIp, socket);
			Thread.sleep(500);
			writeMsg("write " + name + ".port " + ownPort, socket);
			Thread.sleep(200);
			
			//Get Admin Client from Config
			if(adminClient == null) {
				writeMsg("read " + name + ".admin", socket);
				Thread.sleep(500);
				if(lis.msg.equals("") || lis.msg.equals("data not available")) {
					System.out.println("No admin client available");
					System.exit(1);
				}
				else {
					adminClient = lis.msg;
					System.out.println("Admin Client: " + adminClient);
				}
			}
		} 
		catch (UnknownHostException e) {
			System.out.println("Could not connect to config server");
			System.exit(1);
		} 
		catch (IOException e) {
			System.out.println("Could not connect to config server");
			System.exit(1);
		}
		
		
		//main loop
		while(cManager.isAlive()) {
			Thread.sleep(100);
			
			//get socket, name and message from client
			Socket senderSocket = cManager.newMsgSocket();
			String senderName = cManager.newMsgName();
			String newMsg = cManager.newMsg();
			
			if(newMsg != null) {
				if(newMsg.split("\\s+")[0].equals("msg") && !newMsg.split("\\s+")[1].equals("PWRDWNSYS")) {
					//send to all clients but not the sender client
					cManager.writeToAllClients(senderName + ":" + newMsg.split("msg")[1], senderSocket);
				}
				//shut down command
				else if(newMsg.split("\\s+")[0].equals("msg") && newMsg.split("\\s+")[1].equals("PWRDWNSYS")) {
					if(adminClient == null || !senderName.equals(adminClient)) {
						//send to all clients but not the sender client
						cManager.writeToAllClients(senderName + ":" + newMsg.split("msg")[1], senderSocket);
					}
					else if(senderName.equals(adminClient)){
						cManager.writeToAllClients("Server is going to shut down NOW", null);
						System.exit(1);
					}
				}
			}
		}
	}
	
	//Write a message to socket
	public static void writeMsg(String msg, Socket sock) {
		try {
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			out.writeUTF(msg);
			out.flush();
		} 
		catch (IOException e) {
			System.out.println("DataOutputStream error: " + e);
			System.exit(1);
		}
	}
}
