import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Automated test program that connects multiple clients (e.g. 3 clients)
 * simultaneously to the EchoServer.
 * Verifies that all clients receive their own distinct echoed responses without mixing or blocking.
 */
public class MultiClientTest {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int NUM_CLIENTS = 3;

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        final int targetPort = port;

        System.out.println("=================================================");
        System.out.println(" Starting Concurrent Multi-Client Verification ");
        System.out.println(" Spawning " + NUM_CLIENTS + " clients simultaneously to " + HOST + ":" + port);
        System.out.println("=================================================");

        ExecutorService clientExecutor = Executors.newFixedThreadPool(NUM_CLIENTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_CLIENTS);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 1; i <= NUM_CLIENTS; i++) {
            final int clientId = i;
            final String testMessage = "Unique message from Client #" + clientId + " [" + System.currentTimeMillis() + "]";

            results.add(clientExecutor.submit(() -> {
                try {
                    // Wait for all client threads to be ready so they start simultaneously
                    startLatch.await();

                    try (
                        Socket socket = new Socket(HOST, targetPort);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    ) {
                        System.out.println("[Client Thread " + clientId + "] Connected to server.");
                        
                        // Send distinct message
                        out.println(testMessage);
                        System.out.println("[Client Thread " + clientId + "] Sent: " + testMessage);

                        // Read response
                        String echo = in.readLine();
                        System.out.println("[Client Thread " + clientId + "] Received Echo: " + echo);

                        boolean matched = testMessage.equals(echo);
                        if (matched) {
                            System.out.println("[Client Thread " + clientId + "] SUCCESS: Received exact matching echo!");
                        } else {
                            System.err.println("[Client Thread " + clientId + "] ERROR: Echo mismatch! Expected: " + testMessage + ", Got: " + echo);
                        }
                        return matched;
                    }
                } catch (Exception e) {
                    System.err.println("[Client Thread " + clientId + "] EXCEPTION: " + e.getMessage());
                    return false;
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // Release the latch to start all client threads simultaneously
        System.out.println("[TEST RUNNER] Triggering simultaneous client requests...");
        startLatch.countDown();

        try {
            doneLatch.await();
            boolean allPassed = true;
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).get()) {
                    allPassed = false;
                }
            }

            System.out.println("=================================================");
            if (allPassed) {
                System.out.println(" TEST RESULTS: ALL " + NUM_CLIENTS + " CLIENTS PASSED VERIFICATION!");
                System.out.println(" Multi-client concurrency & isolation confirmed.");
            } else {
                System.err.println(" TEST RESULTS: ONE OR MORE CLIENT TESTS FAILED!");
            }
            System.out.println("=================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientExecutor.shutdown();
        }
    }
}
