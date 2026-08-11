import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-Client Socket Server
 * Uses ServerSocket and a fixed-size ExecutorService thread pool to handle multiple
 * client connections concurrently without unbounded thread creation or blocking.
 */
public class EchoServer {
    private static final int DEFAULT_PORT = 8888;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port provided, using default: " + DEFAULT_PORT);
            }
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        System.out.println("=================================================");
        System.out.println("  Echo Server Started on Port " + port);
        System.out.println("  Fixed Thread Pool Size: " + THREAD_POOL_SIZE);
        System.out.println("=================================================");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New client connected: " + clientSocket.getRemoteSocketAddress());
                    
                    // Submit client task to fixed thread pool
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("[SERVER] Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Could not listen on port " + port + ": " + e.getMessage());
        } finally {
            threadPool.shutdown();
            System.out.println("[SERVER] Server shut down.");
        }
    }

    /**
     * Runnable task to handle individual client communication in dedicated pool thread.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            String clientAddr = socket.getRemoteSocketAddress().toString();
            System.out.println("[" + threadName + "] Handling client: " + clientAddr);

            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    System.out.println("[" + threadName + " <- " + clientAddr + "] Received: " + inputLine);
                    // Echo message back directly to this specific client
                    writer.println(inputLine);
                    System.out.println("[" + threadName + " -> " + clientAddr + "] Echoed back: " + inputLine);
                }
            } catch (IOException e) {
                System.out.println("[" + threadName + "] Client disconnected (" + clientAddr + "): " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("[" + threadName + "] Closed socket for client: " + clientAddr);
                } catch (IOException e) {
                    System.err.println("[" + threadName + "] Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
