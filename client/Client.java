import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	public static void main( String[] args ) {

		Socket socket = null;
		PrintWriter socketOutput = null;
		BufferedReader socketInput = null;

		try {

			// try and create the socket
			socket = new Socket("localhost", 7777);

			// chain a writing stream
			socketOutput = new PrintWriter(socket.getOutputStream(), true);

			// chain a reading stream
			socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// chain a reader from the keyboard
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer;
		String fromUser;

		// read from server
		try {
			fromUser = stdIn.readLine();
			if( fromUser != null) {
				// write to server
				assert socketOutput != null;
				socketOutput.println(fromUser);
			}

			while ((fromServer = socketInput.readLine()) != null) {

				// echo server string
				System.out.println("Server: " + fromServer);
				if (fromServer.equals("Connection Terminated.")) {
					break;
				}
			}

			assert socketOutput != null;
			socketOutput.close();
			stdIn.close();
			socket.close();
		}
		catch (IOException e) {
			System.err.println("I/O exception during execution\n");
			System.exit(1);
		}
	}
}
