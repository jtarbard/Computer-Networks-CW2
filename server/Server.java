import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.*;

public class Server {

	//server variables
	ServerSocket socket = null;
	//poll variables
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

	public void setVotes(int[] newVotes){
		//todo: remove print
		System.out.println("'apples' " +newVotes[0]+", 'pears' "+newVotes[1]+", 'oranges' "+newVotes[2]);
		for(int i = 0; i < votes.length; i++){
			votes[i] = votes[i] + newVotes[i];
		}
	}

	public int[] getVotes(){
		//todo: remove print
		System.out.println("'apples' " +votes[0]+", 'pears' "+votes[1]+", 'oranges' "+votes[2]);
		return votes;
	}

	class handler extends Thread {

		//client socket variable
		private Socket socket = null;

		public handler(Socket client) {
			//get and store client input
			this.socket = client;
		}

		public void run(){

			//assign input and output variables
			BufferedReader input = null;
			PrintWriter output = null;
			try {
				input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//get log information
			//TODO: log
			InetAddress inet = socket.getInetAddress();
			Date date = new Date();

			System.out.println("\nDate " + date.toString() );
			System.out.println("Connection made from " + inet.getHostName() );

			//initialise client input stream
			String line = null;
			String[] command;

			while( true ) {
				if (input != null) {
					//process client input
					try {
						line = input.readLine();
						//
						if(!line.equals("")){
							command = line.split(" ");
							int votesCount = 0;
							int[] votesBuffer = {0, 0, 0}; //apple, pear, orange
							if (command[0].equalsIgnoreCase("vote")) {
								for(String word : command){
									if(word.equalsIgnoreCase("apple")){
										votesBuffer[0] = votesBuffer[0] + 1;
										votesCount++;
									}
									else if(word.equalsIgnoreCase("pear")){
										votesBuffer[1] = votesBuffer[1] + 1;
										votesCount++;
									}
									else if(word.equalsIgnoreCase("orange")){
										votesBuffer[2] = votesBuffer[2] + 1;
										votesCount++;
									}
									else if(word.equalsIgnoreCase("vote")){
										//do nothing
									}
									else {
										System.err.println("Unidentifiable word '"+word+"' in command.");
										System.exit(-1);
									}
								}
								if(votesCount >= 2){
									setVotes(votesBuffer);
									break;
								}
								else{
									System.err.println("Too few votes casts, must be a minimum of two.");
									System.exit(-1);
								}
							}
							else if (command[0].equalsIgnoreCase("show")) {
								int[] temp = getVotes();
								String votes = String.format(
										"'apple' has %d vote(s).\n" +
										"'pear' has %d vote(s).\n" +
										"'orange' has %d vote(s).\n",
										temp[0], temp[1], temp[2]
								);
								output.println(votes);
								break;
							}
							else {
								System.err.println("Invalid command, did not start with vote or show.");
								System.exit(-1);
							}
						}
						else{
							System.err.println("Input is empty.");
							System.exit(-1);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("Could not get client input stream.");
					System.exit(-1);
				}
			}
			try {
				output.println("Connection Terminated.");
				input.close();
				output.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}

