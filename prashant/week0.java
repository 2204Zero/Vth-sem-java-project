package prashant;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class week0 {

    // Simple Student domain object
    public static class Student {
        private String name;
        private int id;

        public Student() {}

        public Student(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Student{id=" + id + ", name='" + name + "'}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  4-Byte Length-Prefixed JSON Protocol Test     ");
        System.out.println("=================================================");

        // Tricky input: containing double quote ("), pipe (|), and newline (\n)
        String trickyName = "John \"The Ace\" | Special\nNewline";
        Student originalStudent = new Student(101, trickyName);

        System.out.println("\n--- [SENDER] Original Data ---");
        System.out.println("Original Student : " + originalStudent);
        System.out.println("Original Name    : " + escapeString(trickyName));
        byte[] originalNameBytes = trickyName.getBytes(StandardCharsets.UTF_8);
        System.out.println("Original Bytes   : " + Arrays.toString(originalNameBytes));

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(originalStudent);
        byte[] jsonBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);

        System.out.println("\n--- [SERIALIZATION] Gson Payload ---");
        System.out.println("Serialized JSON  : " + jsonPayload);
        System.out.println("JSON Byte Length : " + jsonBytes.length + " bytes");

        // Start ServerSocket on an available port (0)
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            System.out.println("\n--- [NETWORK] Server socket opened on port " + port + " ---");

            // Client thread to send data over socket
            Thread clientThread = new Thread(() -> {
                try (Socket socket = new Socket("localhost", port);
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    System.out.println("[CLIENT] Connected to server.");
                    System.out.println("[CLIENT] Sending 4-byte length prefix (" + jsonBytes.length + ")...");
                    out.writeInt(jsonBytes.length); // 4-byte Big-Endian integer

                    System.out.println("[CLIENT] Sending JSON payload (" + jsonBytes.length + " bytes)...");
                    out.write(jsonBytes);
                    out.flush();
                    System.out.println("[CLIENT] Data sent and flushed successfully.");

                } catch (IOException e) {
                    System.err.println("[CLIENT ERROR] " + e.getMessage());
                    e.printStackTrace();
                }
            });

            clientThread.start();

            // Server receives and decodes data
            try (Socket serverConn = serverSocket.accept();
                 DataInputStream in = new DataInputStream(serverConn.getInputStream())) {

                System.out.println("[SERVER] Client connected from " + serverConn.getRemoteSocketAddress());

                // Step 1: Read 4-byte length prefix
                int payloadLength = in.readInt();
                System.out.println("[SERVER] Step 1 - Received 4-byte length prefix: " + payloadLength);

                // Step 2: Read exactly payloadLength bytes
                byte[] receivedPayload = new byte[payloadLength];
                in.readFully(receivedPayload);
                System.out.println("[SERVER] Step 2 - Read exactly " + receivedPayload.length + " bytes from input stream.");

                // Step 3: Decode back to JSON string and deserialize to Student object
                String receivedJsonStr = new String(receivedPayload, StandardCharsets.UTF_8);
                System.out.println("[SERVER] Step 3 - Decoded JSON payload: " + receivedJsonStr);

                Student receivedStudent = gson.fromJson(receivedJsonStr, Student.class);
                System.out.println("[SERVER] Reconstructed Student Object: " + receivedStudent);

                // Verification
                String receivedName = receivedStudent.getName();
                byte[] receivedNameBytes = receivedName.getBytes(StandardCharsets.UTF_8);

                System.out.println("\n=================================================");
                System.out.println("          VERIFICATION & PROOF RESULT            ");
                System.out.println("=================================================");
                System.out.println("Sent Name String    : " + escapeString(trickyName));
                System.out.println("Received Name String: " + escapeString(receivedName));
                System.out.println("Sent Name Bytes     : " + Arrays.toString(originalNameBytes));
                System.out.println("Received Name Bytes : " + Arrays.toString(receivedNameBytes));

                boolean stringsMatch = trickyName.equals(receivedName);
                boolean bytesMatch = Arrays.equals(originalNameBytes, receivedNameBytes);

                System.out.println("Strings Match       : " + stringsMatch);
                System.out.println("Byte-for-Byte Match : " + bytesMatch);

                if (bytesMatch && stringsMatch) {
                    System.out.println("\n>>> PROOF SUCCESSFUL: Tricky string arrived 100% identical, byte-for-byte! <<<");
                } else {
                    System.err.println("\n>>> PROOF FAILED: Data corruption detected! <<<");
                }
            }

            clientThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String escapeString(String str) {
        if (str == null) return "null";
        return str.replace("\n", "\\n").replace("\r", "\\r");
    }
}
