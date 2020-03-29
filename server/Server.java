import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Server Class -
 * Creates a server that hosts a poll, with poll items given at the commandline, and
 * accepts incoming client connections on port 7777 for casted votes.
 */
//todo: fix overload behaviour
public class Server {

	//poll variables
	private String[] items = {};
	private int[] votes = {};

	/**
	 * Main Method -
	 * Gets a minimum of two poll items from commandline arguments and creates an
	 * instance of the Server class.
	 * @param args passed as a parameter to the server instance.
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException {
		if(args.length >= 2) {
			Server server = new Server(args);
		}
		else{
			System.err.println("Too few arguments, minimum of two poll items required.");
		}
	}

	/**
	 * Server Constructor -
	 * Takes args parameter and assigns the array to the items field.
	 * Creates a socket instance on port 7777. Creates a log.txt file.
	 * Creates an Executor instance with a fixed thread pool size of 20.
	 * Enters a loop that accepts synchronised socket requests and calls the handler.
	 * @param args
	 * @throws IOException
	 */
	public Server(String[] args) throws IOException {

		items = args; //assign poll items to items array
		votes = new int[items.length]; //initialise votes array

		ServerSocket socket = null; //create socket variable

		//try to open connection port
		try {
			socket = new ServerSocket(7777);

			//try to create a log file
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

		ExecutorService service = Executors.newFixedThreadPool(20); //initialise executor with thread poll of 20

		//for each new client submit a new handler to the thread pool
		while( true ) {
			synchronized (socket){
				Socket client = socket.accept();
				service.submit(new Handler(client));
			}
		}
	}

	/**
	 * getItems -
	 * @return items field.
	 */
	public String[] getItems(){
		return items;
	}

	/**
	 * setVotes -
	 * Iterates over the newVotes array adding the new votes for an item to
	 * the current votes array.
	 * @param newVotes an array of new item votes to be added to the votes field.
	 */
	public void setVotes(int[] newVotes){
		for(int i = 0; i < votes.length; i++){
			votes[i] = votes[i] + newVotes[i];
		}
	}

	/**
	 * getVotes -
	 * @return votes field.
	 */
	public int[] getVotes(){
		return votes;
	}

	/**
	 * Handler Class -
	 * Handles the inbound client request: updating poll and/or sending poll status to client.
	 */
	class Handler extends Thread {

		//client socket variable
		private Socket socket = null;

		/**
		 * Handler Constructor -
		 * @param client is assigned to the socket field.
		 */
		public Handler(Socket client) {
			//get and store client input
			this.socket = client;
		}

		/**
		 * run Method -
		 * Creates input/output streams for the client. Writes client information to the
		 * log file. Sends poll information to the client. Processes and logs client request
		 * either adding client votes to the poll or sending the poll to the client.
		 */
		public void run() {

			//assign socket input and output instances
			BufferedReader input = null;
			PrintWriter output = null;
			try {
				input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//add connection to log
			FileWriter log = null;
			try {
				log = new FileWriter("log.txt", true);
				InetAddress inet = socket.getInetAddress();
				Date date = new Date();

				log.append(date.toString() + ":"); //log date
				log.append(inet.getHostName() + ":"); //log ip address
			} catch (IOException e) {
				e.printStackTrace();
			}

			//inform client of poll information
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

			//declare while scope variables
			String line = null;
			String[] request;
			Boolean validOption;

			badOption: //break point for bad vote option
			//loop until input processed
			while (true) {
				if (input != null) {

					//try to read input
					try {
						line = input.readLine(); //read first line of input
						request = line.split(" "); //spilt line into an array of words

						int[] newVotes; //array for holding new votes made by client
						newVotes = new int[items.length]; //initialise array to match number of item

						//process client request
						if (request[0].equalsIgnoreCase("vote")) {
							for (String word : request) {
								validOption = false; //reset option validity check
								for (int i = 0; i < items.length; i++) {
									if (word.equalsIgnoreCase(items[i])) {
										newVotes[i] = newVotes[i] + 1; //add 1 newVotes array
										validOption = true; //mark option as valid
									}
									else if(word.equalsIgnoreCase("vote")){
										validOption = true;
									}
								}

								//if option invalid return error
								if(!validOption){
									output.println("Error - '"+word+"' is not an item. No vote made.");
									break badOption;
								}
							}

							//update vote count
							setVotes(newVotes);
						}

						//print poll status
						int[] votes = getVotes();
						output.println("Poll Count:");
						for (int i = 0; i < votes.length; i++) {
							output.println("'" + items[i] + "' has " + votes[i] + " votes(s)."); //print items vote count
						}
						break;

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			//send end connection signal
			output.println("End of Connection.");

			//try to close log, input/output instances and client socket
			try {
				log.append(line + "\r\n"); //log request
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

