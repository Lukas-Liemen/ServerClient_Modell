package WA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {
	
	private String fileName = null;
	File kvFile = null;
	
	//init FileManager
	public FileManager(String fileName){
		this.fileName = fileName;
		kvFile = new File(fileName);
		try {
			kvFile.createNewFile();
		}
		catch (IOException e) {
			System.out.println("Failed to create new File");
			System.exit(1);
		}
	}
	
	//read value from file
	public String getValueToKey(String key) {
		String value = null;
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(kvFile);
			while(scanner.hasNextLine()) {
				String fileLine = scanner.nextLine();
				
				//Split line from file into key (splitLine[0]) and value (splitLine[1])
				String[] splitLine = fileLine.split("\\s+");
				if(splitLine[0].equals(key)) {
					//apply value to String "value"
					value = splitLine[1];
				}
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Error while trying to open file");
			System.exit(1);
		}

		scanner.close();
		return value;
	}
	
	//delete from file
	public void deleteFromList(String key) {
		Scanner scanner = null;
		ArrayList<String> tempList = new ArrayList<String>();
		
		//save all entrys in tempList but not the one that shall be deleted
		//then delete all entrys in file and add the saved ones
		try {
			scanner = new Scanner(kvFile);
			while(scanner.hasNextLine()) {
				String fileLine = scanner.nextLine();
				
				if(!fileLine.contains(key)) {
					tempList.add(fileLine);
				}
			}
			scanner.close();
	
			//delete file and create new one
			kvFile.delete();
			kvFile = new File(fileName);
			
			try {
				if(kvFile.createNewFile()) {
					
				}
			} 
			catch (IOException e) {
				System.out.println("Error while trying to create new File");
			}
			
			//write all items of tempList to File
			for(int i = 0; i < tempList.size(); i++) {
				String[] keyValues = tempList.get(i).split("\\s+");
				if(keyValues[0] != null && keyValues[1] != null) {
					writeToList(keyValues[0], keyValues[1]);
				}
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Error while trying to open file");
			System.exit(1);
		}
	}
	
	//write to file
	public void writeToList(String key, String value) {
		
		//check if key is known and delete if so
		if(getValueToKey(key) != null) {
			deleteFromList(key);
		}

		try {
			FileWriter fWriter = new FileWriter(kvFile, true);
			fWriter.write(key + " " + value + "\n");
			fWriter.flush();
			fWriter.close();
		} 
		catch (IOException e) {
			System.out.println("Error while trying to write to file. File could not be opened");
			System.exit(1);
		}
	}
	
	//Check if all servers in file are still available
	public void controllAllEntries() {
		Scanner scanner = null;
		String ipKey = null;
		String portKey = null;
		String ip = null;
		String port = null;
		
		try {
			//Go through file
			scanner = new Scanner(kvFile);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				//Get next Ip and port
				if(line.contains(".ip")) {
					String[] splitter = line.split("\\s+");
					ipKey = splitter[0];
					ip = splitter[1];
				}
				else if(line.contains(".port")) {
					String[] splitter = line.split("\\s+");
					portKey = splitter[0];
					port = splitter[1];
				}
				
				//Check if connection is available, if not delete
				if(ip != null && port != null) {
					int prt = Integer.parseInt(port);
					
					//checks if connection is available
					try {
						Socket conn = new Socket(ip, prt);
						conn.close();
					} 
					//deletes if no connection
					catch (UnknownHostException e) {
						scanner.close();
						System.out.println("Server: " + ipKey + " not available. Gets deleted.");
						deleteFromList(ipKey);
						deleteFromList(portKey);
						scanner = new Scanner(kvFile);
					} 
					catch (IOException e) {
						scanner.close();
						System.out.println("Server: " + ipKey + " not available. Gets deleted.");
						deleteFromList(ipKey);
						deleteFromList(portKey);
						scanner = new Scanner(kvFile);
					}
					//reset values
					ipKey = null;
					portKey = null;
					ip = null;
					port = null;
				}
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Error while reading File");
			System.exit(1);
		}	
	}
}