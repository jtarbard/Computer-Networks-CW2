import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Client Class -
 * Creates a client that connects to the poll server on port 7777. Takes commands
 * vote/show from the command line and sends them as requests to the server.
 */
public class Client {

	/**
	 * main Method -
	 * Creates a new client instance.
	 * @param args
	 */
	public static void main( String[] args ) {
		Client client = new Client();
	}

	/**
	 * Client Constructor -
	 * Creates a socket instance on port 7777. Gets poll information from the server
	 * and sends a vote/show request to the server. Closing the connection upon
	 * confirmation or error.
	 */
	public Client(){

		//client variables
		Socket socket = null;
		BufferedReader input = null;
		PrintWriter output = null;
		BufferedReader stdInput = null;

		//try and create the socket instance and assign input/output instances
		try {
			socket = new Socket("localhost", 7777);

		} catch (IOException e) {
			System.err.println("Connection refused, is the server running?");
			e.printStackTrace();
		}

		//try assign input and output instances
		try {
			assert socket != null;
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			stdInput = new BufferedReader(new InputStreamReader(System.in)); //keyboard input instance
		} catch (IOException e){
			System.err.println("Failed to create input/output instances.");
			e.printStackTrace();
		}

		//try to get poll information
		try {
			assert input != null;
			String pollItems = input.readLine();
			System.out.println("Poll Items for Voting: "+pollItems);
		} catch (IOException e) {
			System.err.println("Failed to read poll information from server.");
			e.printStackTrace();
		}

		//try to send user request to server
		try {
			assert stdInput != null;
			String userLine = stdInput.readLine();
			if( userLine != null) {
				if(!userLine.equals("")){
					String[] command = userLine.split(" ");
					if (command[0].equalsIgnoreCase("vote") || command[0].equalsIgnoreCase("show")) {
						output.println(userLine); // write to server
						System.out.println("Client: "+userLine);
					}
					else {
						System.err.println("Invalid command, did not start with vote or show.");
						System.exit(-1);
					}
				}
				else {
					System.err.println("Could not get client input stream.");
					System.exit(-1);
				}
			}
		}
		catch (IOException e) {
			System.err.println("Failed to send request to server.");
			System.exit(1);
		}

		//try to read server response to request
		try {
			String serverLine;
			while ((serverLine = input.readLine()) != null) {
				// echo server string
				System.out.println("Server: " + serverLine);
				if (serverLine.equals("End of Connection.")) {
					break;
				}
			}
		} catch (IOException e){
			System.err.println("Failed to receive request response from server.");
			System.exit(1);
		}

		//try to close input/output streams and socket
		try {
			output.close();
			stdInput.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("Failed to close input/output stream or socket..");
			System.exit(1);
		}
	}
}
