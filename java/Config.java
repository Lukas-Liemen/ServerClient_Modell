package WA;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Config {
	public static void main(String[] args){
		
		String fileName = "kvList.txt";
		int check = 0;
		
		//Enable Start Parameters
		if(args.length > 0) {
			fileName = args[0];
		}
		
		//Allows reading, writing and deleting key and value entries from file
		FileManager fManager = new FileManager(fileName);
		System.out.println("Using File: " + fileName);
		
		//set up server
		ServerSocket sersoc = null;
		try {
			sersoc = new ServerSocket(5555);
		}
		catch (IOException e) {
			System.out.println("Port already in use");
			System.exit(1);
		}
		
		//Thread that checks incoming clients and adds them to a list when correct
		ClientManager cManager = new ClientManager(sersoc);
		cManager.start();
		
		while(cManager.isAlive()) {
			//Pause to slow down loop
			try {
				Thread.sleep(100);
			} 
			catch (InterruptedException e) {
				System.out.println("Thread sleeping error");
				System.exit(1);
			}
			
			//check if servers are alive all 5 seconds to reduce traffic on servers
			check++;
			if(check > 50) {
				//check if all servers in list are still available
				fManager.controllAllEntries();
				check = 0;
			}
			
			//Read new Message and Socket it is from, if there is a new Message
			String newMsgName = cManager.newMsgName();
			Socket newMsgSocket = cManager.newMsgSocket();
			String newMsg = cManager.newMsg();
			if(newMsg != null) {
				
				//Split message into command (splitMsg[0]) and actual input (all other spaces in array)
				String[] splitMsg = newMsg.split("\\s+");
				
				//Command to write to list
				if(splitMsg[0].equals("write") && splitMsg.length == 3) {
					fManager.writeToList(splitMsg[1], splitMsg[2]);
					System.out.println("Wrote: '" + splitMsg[1] + " " + splitMsg[2] + "' to list.");
				}
				
				//command to read from list
				else if(splitMsg[0].equals("read") && splitMsg.length == 2) {
					String answer = fManager.getValueToKey(splitMsg[1]);
					//When there is an entry
					if(answer != null) {
						System.out.println("Sending: " + answer + " to " + newMsgName);
						cManager.writeMsgClient(answer, newMsgSocket);
					}
					//When there is no entry
					else {
						cManager.writeMsgClient("data not available", newMsgSocket);
					}
				}
			}
		}
		System.out.println("ClientManager stopped working. Shutting down now");
		System.exit(1);
	}
}