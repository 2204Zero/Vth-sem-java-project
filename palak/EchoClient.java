import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Command-line Client for EchoServer
 * Connects to the server, sends lines of input typed by the user, and prints echoed responses.
 */
public class EchoClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port provided, using default: " + DEFAULT_PORT);
            }
        }

        System.out.println("Connecting to EchoServer at " + host + ":" + port + "...");

        try (
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to EchoServer! Type a message and press Enter (type 'quit' or Ctrl+C to exit):");
            
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if ("quit".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Disconnecting...");
                    break;
                }
                
                // Send message to server
                out.println(userInput);
                
                // Read echoed response from server
                String serverEcho = in.readLine();
                if (serverEcho == null) {
                    System.out.println("Server closed connection.");
                    break;
                }
                System.out.println("Server Echo: " + serverEcho);
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error connecting to server: " + e.getMessage());
        }
    }
}
