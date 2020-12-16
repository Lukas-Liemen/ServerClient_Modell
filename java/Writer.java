package WA;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Writer extends Thread{

	private Socket socket;
	public Scanner scanner = new Scanner(System.in);
	private DataOutputStream out;
	private boolean connected;
	
	public Writer(Socket sock) {
		socket = sock;
		connected = true;
	}
	
	public void run() {
		while(connected) {
			try {
				String msg = scanner.nextLine();

				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("msg " + msg);
				out.flush();
			} 
			catch (Exception e) {
				connected = false;
			}
		}
	}
}