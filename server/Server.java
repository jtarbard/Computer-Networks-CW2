import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Server {
	public static void main( String[] args ) throws IOException {
		System.out.println("Server Running");

		ServerSocket server = null;
		ExecutorService service = null;

		try {
			server = new ServerSocket(2323);
		}
		catch (IOException e) {
			System.err.println("Could not listen on port: 2323.");
			System.exit(-1);
		}

		// Initialise the executor.
		service = Executors.newFixedThreadPool(20);

		// For each new client, submit a new handler to the thread pool.
		while( true ) {
//			synchronised ( serverSock ) {
				Socket client = server.accept();
				service.submit(new handler(client));
//			}
		}
	}

}

class handler implements Runnable {

	InputStream input = null;

	public handler(Socket client) {
		try {
			input = client.getInputStream();

		} catch (IOException e) {
			System.err.println("Could not get client input stream.");
			System.exit(-1);
		}
	}

	public void run(){
		String line = null;

		if (input != null){
			BufferedReader stream = new BufferedReader(new InputStreamReader(input));
			try {

				while ((line = stream.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Could not get client input stream.");
		}
	}
}