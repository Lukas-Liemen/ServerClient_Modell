package WA;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientManager extends Thread{
	
	public ArrayList<Socket> clientSockets = new ArrayList<Socket>();
	public ArrayList<Listener> clientListener = new ArrayList<Listener>();
	public ArrayList<String> clientNames = new ArrayList<String>();
	public boolean loop = true;
	private ServerSocket sersoc = null;
	
	public ClientManager(ServerSocket sersock) {
		sersoc = sersock;
	}
	
	//accept every client that wants to connect, if it is a correct client
	public void run() {
		while(loop) {
			
			//update the connections, remove if connections are closed
			updateLists(clientSockets, clientListener, clientNames);
			
			try {
				sersoc.setSoTimeout(1000);
			} 
			catch (SocketException e) {
				
			}
			
			//connect to new client and authority
			try {
				Socket newClient = sersoc.accept();
				Listener newClientListener = new Listener(newClient);
				newClientListener.start();
				
				//Wait 200ms to get msg from client
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					System.out.println("Thread-Sleep error");
					System.exit(1);
				}

				if(!newClientListener.isAlive()) {
					System.out.println("ping from config");
				}
				
				//get name of client
				String name = checkForName(newClientListener);
				
				//correct first message and name unknown -> correct client, add to list
				if(name != null && checkNames(name, clientNames)) {
					System.out.println(name + " connected");
					clientSockets.add(newClient);
					clientListener.add(newClientListener);
					clientNames.add(name);
				}
				//wrong start msg -> not known client, stop connection
				else {
					newClientListener.connection = false;
					newClient.close();
				}	
				
			} 
			catch (IOException e) {
				
			}
		}
	}
	
	private String checkForName(Listener lis) {
		String msg = null;
		
		//Check if a new message arrived
		if(lis.msg != "") {
			msg = lis.msg;
			//clear msg from listener (-> no confusion with older messages)
			lis.msg = "";
			//Split new Message to check if it is correct 
			String[] splittMsg = msg.split("\\s+");
			if(splittMsg[0].equals("name")) {
				return splittMsg[1];
			}
			else {
				return null;
			}
		}
		return null;
	}
	
	//check the socket connections and remove if there is no connection anymore
	private void updateLists(ArrayList<Socket> clients, ArrayList<Listener> clientListener, ArrayList<String> clientNames) {
		for(int i = 0; i < clientListener.size(); i++) {
			if(clientListener.get(i).connection == false) {
				clientListener.remove(i);
				clients.remove(i);
				clientNames.remove(i);
			}
		}
	}
	
	//check for double names --> not allowed
	private boolean checkNames(String name, ArrayList<String> clientNames) {
		if(clientNames.contains(name)) {
			return false;
		}
		return true;
	}
	
	//checks for new messages and returns the socket. Only used outside of class
	public Socket newMsgSocket() {
		Socket newMsgSocket = null;
		
		// "" is an empty msg
		for(int i = 0; i < clientListener.size(); i++) {
			if(clientListener.get(i).msg != "") {
				newMsgSocket = clientSockets.get(i);
				return newMsgSocket;
			}
		}
		return newMsgSocket;
	}
	
	//checks for new messages and returns the name. Only used outside of class
		public String newMsgName() {
			String newMsgName = null;
			
			// "" is an empty msg
			for(int i = 0; i < clientListener.size(); i++) {
				if(clientListener.get(i).msg != "") {
					newMsgName = clientNames.get(i);
					return newMsgName;
				}
			}
			return newMsgName;
		}
	
	//checks for new messages and returns the message. Only used outside of class
	public String newMsg() {
		String newMsg = null;
		
		// "" is an empty msg
		for(int i = 0; i < clientListener.size(); i++) {
			if(clientListener.get(i).msg != "") {
				newMsg = clientListener.get(i).msg;
				
				//reset message
				clientListener.get(i).msg = "";
				return newMsg;
			}
		}
		return newMsg;
	}
	
	//Write message to specific client.
	public void writeMsgClient(String msg, Socket socket) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(msg);
			out.flush();
		} 
		catch (IOException e) {
			System.out.println("Error while writing to client: " + e);
			System.exit(1);
		}
	}
	
	//Write message to all clients. Only used outside of class
	public void writeToAllClients(String msg, Socket sender) {
		for(int i = 0; i < clientSockets.size(); i++) {
			if(clientSockets.get(i) != sender) {
				writeMsgClient(msg, clientSockets.get(i));
			}
		}
	}
}