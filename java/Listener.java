package WA;

import java.net.*;
import java.io.*;

public class Listener extends Thread{
	
	public boolean connection;
	public String msg;
	public Socket socket;
	private DataInputStream in;
	
	public Listener(Socket sock) {
		msg = "";
		socket = sock;
		try {
			in = new DataInputStream(socket.getInputStream());
			connection = true;
		}
		catch (IOException e) {
			connection = false;
		}
	}
	
	public void run() {
		//read message when there is a new one
		while(connection == true) {
			try {
				msg = in.readUTF();
			} 
			catch (IOException e) {
				connection = false;
			}
		}
	}
}