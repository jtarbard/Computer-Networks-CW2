import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.*;

public class Server {

	//server variables
	private ServerSocket socket = null;
	//poll variables
	private String[] items = {"apple", "pear", "orange"};
	private int[] votes = {0, 0, 0}; //apple, pear, orange

	//todo: fix overload behaviour
	public static void main( String[] args ) throws IOException {
		//server instance
		Server server = new Server();
	}

	public Server() throws IOException {

		//open connection port
		try {
			socket = new ServerSocket(7777);
			try {
				File logFile = new File("log.txt");
				if (logFile.createNewFile()){
					System.out.println("Log file: log.txt created. Logging inbound connections.");
				}
				else{
					System.out.println("Log file: log.txt found. Logging inbound connections.");
				}
			} catch (IOException e){
				System.err.println("Could not create log file.");
				System.exit(-1);
			}
		}
		catch (IOException e) {
			System.err.println("Could not listen on port: 2323.");
			System.exit(-1);
		}

		//initialise the executor with a thread limit of 20
		ExecutorService service = Executors.newFixedThreadPool(20);

		//for each new client submit a new handler to the thread pool.
		while( true ) {
			synchronized (socket){
				Socket client = socket.accept();
				service.submit(new handler(client));
			}
		}
	}

	public String[] getItems(){
		return items;
	}

	public void setVotes(int[] newVotes){
		for(int i = 0; i < votes.length; i++){
			votes[i] = votes[i] + newVotes[i];
		}
	}

	public int[] getVotes(){
		return votes;
	}

	class handler extends Thread {

		//client socket variable
		private Socket socket = null;

		public handler(Socket client) {
			//get and store client input
			this.socket = client;
		}

		public void run() {

			//assign input and output instances
			BufferedReader input = null;
			PrintWriter output = null;
			try {
				input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//get log information
			FileWriter log = null;
			try {
				log = new FileWriter("log.txt", true);
				InetAddress inet = socket.getInetAddress();
				Date date = new Date();

				log.append(date.toString() + ":");
				log.append(inet.getHostName() + ":");
			} catch (IOException e) {
				e.printStackTrace();
			}
			//send poll items
			//This is a method that allows poll items to change on server side
			String pollItems = "";
			String[] items = getItems();
			for(int i = 0; i < items.length; i++){
				pollItems = pollItems.concat(items[i]);
				if(i != items.length-1){
					pollItems = pollItems.concat(", ");
				}
				else{
					pollItems = pollItems.concat(".");

				}
			}
			output.println(pollItems);

			//initialise client input stream
			String line = null;
			String[] command;
			outOfWhile:
			while (true) {
				if (input != null) {
					//process client input
					try {
						line = input.readLine();
						//
						command = line.split(" ");
						int votesCount = 0;
						int[] votesBuffer = {0, 0, 0}; //apple, pear, orange
						if (command[0].equalsIgnoreCase("vote")) {
							for (String word : command) {
								if (word.equalsIgnoreCase("apple")) {
									votesBuffer[0] = votesBuffer[0] + 1;
									votesCount++;
								} else if (word.equalsIgnoreCase("pear")) {
									votesBuffer[1] = votesBuffer[1] + 1;
									votesCount++;
								} else if (word.equalsIgnoreCase("orange")) {
									votesBuffer[2] = votesBuffer[2] + 1;
									votesCount++;
								} else if (word.equalsIgnoreCase("vote")) {
									//do nothing
								} else {
									System.err.println("Unidentifiable word '" + word + "' in command.");
									output.println("Error - '" + word + "' is not a valid poll item. Vote rejected.");
									break outOfWhile;
								}
							}
							if (votesCount >= 2) {
								setVotes(votesBuffer);
								int[] votes = getVotes();
								output.println("Updated Poll Count:");
								for(int i = 0; i < items.length; i++){
									output.println("	> '"+items[i]+"' has "+votes[i]+" votes(s).");
								}
								break outOfWhile;
							} else {
								System.err.println("Too few votes casts, must be a minimum of two.");
								System.exit(-1);
							}
						} else {
							int[] votes = getVotes();
							output.println("Poll Count:");
							for(int i = 0; i < items.length; i++){
								output.println("	> '"+items[i]+"' has "+votes[i]+" votes(s).");
							}
							break outOfWhile;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			output.println("End of Connection");
			try {
				log.append(line + "\r\n");
				log.close();
				input.close();
				output.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

