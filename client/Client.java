import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	public static void main( String[] args ) {

		Socket socket = null;
		BufferedReader input = null;
		PrintWriter output = null;

		try {
			//try and create the socket
			socket = new Socket("localhost", 7777);

			//assign input and output instances
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Connection refused, is the server running?");
			e.printStackTrace();
		}

		//assign keybaord input instance
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer;
		String fromUser;


		try {

			String pollItems = input.readLine();
			System.out.println("Poll Items for Voting: "+pollItems);

			fromUser = stdIn.readLine();
			if( fromUser != null) {
				if(!fromUser.equals("")){
					String command[] = fromUser.split(" ");
					if (command[0].equalsIgnoreCase("vote") || command[0].equalsIgnoreCase("show")) {
						// write to server
						assert output != null;
						output.println(fromUser);
						System.out.println("Client: "+fromUser);
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

			while ((fromServer = input.readLine()) != null) {
				// echo server string
				System.out.println("Server: " + fromServer);
				if (fromServer.equals("End of Connection.")) {
					break;
				}
			}

			assert output != null;
			output.close();
			stdIn.close();
			socket.close();
		}
		catch (IOException e) {
			System.err.println("I/O exception during execution\n");
			System.exit(1);
		}
	}
}
