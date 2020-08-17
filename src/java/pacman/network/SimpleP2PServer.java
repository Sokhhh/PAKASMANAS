package pacman.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import pacman.controller.NetworkController;
import pacman.util.Logger;

/**
 * This class implements a simple P2P server that connects with other instances of
 * servers. The class uses TCP for connection and send string messages.
 *
 * @version 1.0
 */
public class SimpleP2PServer {
    /**
     * Contains the controller of this application.
     */
    private final NetworkController controller;

    /**
     * Contains a task queue to finish different task in the application.
     */
    private ExecutorService inputExecutor;
    private final ExecutorService outputExecutor;

    /**
     * Contains the local server socket that will waits for connection.
     */
    private ServerSocket serverSocket;

    /**
     * Contains a socket that will be used to communicate between local host and the
     * remote side.
     */
    private final HashMap<SocketAddress, Socket> connectionSockets;

    /**
     * Contains the maximum number of client in this server.
     */
    private int maxConnections;

    /**
     * Contains the status of the local server.
     */
    private final AtomicBoolean isListening;

    /**
     * Contains the tags for network messages.
     */
    public static final class Tags {
        /**
         *  Network protocol tag, see {@link #listen()}.
         */
        public static final String CONFIRM_TAG = "[CONFIRM]";
    }

    /**
     * Constructor that establishes the server by checking an available port
     * automatically.
     *
     * @param controller the controller of this application
     * @throws   IOException  if an I/O error occurs when opening the socket.
     * @throws   SecurityException
     *      if a security manager exists and its {@code checkListen}
     *      method doesn't allow the operation.
     */
    public SimpleP2PServer(NetworkController controller) throws IOException,
        SecurityException {
        this(controller, Short.MAX_VALUE);
    }

    /**
     * Constructor that establishes the server by checking an available port
     * automatically.
     *
     * @param controller the controller of this application
     * @param maxClientNum the maximum number of clients
     * @throws   IOException  if an I/O error occurs when opening the socket.
     * @throws   SecurityException
     *      if a security manager exists and its {@code checkListen}
     *      method doesn't allow the operation.
     */
    public SimpleP2PServer(NetworkController controller, int maxClientNum) throws IOException,
        SecurityException {
        this.controller = controller;
        this.maxConnections = maxClientNum;
        this.isListening = new AtomicBoolean(false);
        this.inputExecutor = Executors.newCachedThreadPool();
        this.outputExecutor = Executors.newCachedThreadPool();
        this.connectionSockets = new HashMap<>();
    }

    /**
     * This function returns the local ip address of the host machine.
     *
     * @return the ip address of the host machine
     * @throws IOException if an error occurs during the connection
     */
    public static String getLocalIP() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        return socket.getLocalAddress().toString().substring(1);
    }

    /**
     * This method checks the port of the local host.
     *
     * @return the local port of the host
     */
    public int getLocalPort() {
        if (serverSocket == null) {
            return 0;
        }
        return this.serverSocket.getLocalPort();
    }

    /** Contains a message telling the user that the local IP may not be accurate. */
    public static final String LOCAL_IP_TROUBLESHOOT_MSG =
              "This application gets your Local Area Network address \n"
            + "by attempting to connect with google.com (Internet \n"
            + "connection is not required but suggested). However, \n"
            + "different network adapter interfaces (ethernet, wifi, \n"
            + "virtual adapter created by virtual machine software,\n"
            + "wireshark, etc.) may affect the ability of the application \n"
            + "to retrieve your LAN address. Click \"OK\" to call a \n"
            + "system command to list IP addresses in all adapters of \n"
            + "your system and you can manually choose from it.";

    /**
     * Returns the system command that shows the ip address. Usually {@code
     * ipconfig} on windows and {@code ifconfig} on nix.
     *
     * @return the system command result
     */
    public static String getSystemIPConfig() {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb;
            if (System.getProperty("os.name", "generic")
                    .toLowerCase(Locale.ENGLISH)
                    .contains("win")) {
                pb = new ProcessBuilder("cmd", "/C", "chcp 65001"
                        + " & ipconfig");
            } else {
                pb = new ProcessBuilder("ifconfig");
            }

            Process p = pb.start();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Active code page")) {
                    continue;
                }
                sb.append(line).append(System.lineSeparator());
            }
            p.waitFor(); // Let the process finish.
            sb.delete(sb.length() - 4, sb.length());
        } catch (IOException | InterruptedException e2) {
            sb = new StringBuilder("Command failed: " + e2.getMessage());
        }
        return sb.toString();
    }

    // ==================================================================================
    //                                   LISTENING
    // ==================================================================================

    /**
     * This method changes the number of maximum allowed connections. If there is
     * already more connections, those connections won't be closed, but no more
     * connections are allowed.
     *
     * @param maxConnections the maximum number of connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * This method reestablishes the server by using a system allocated port number.
     *
     * @throws   IOException  if an I/O error occurs when opening the socket.
     * @throws   SecurityException
     *      if a security manager exists and its {@code checkListen}
     *      method doesn't allow the operation.
     * @throws   IllegalArgumentException if the port parameter is outside
     *           the specified range of valid port values, which is between
     *           0 and 65535, inclusive.
     */
    public void startListening() throws IOException, SecurityException,
            IllegalArgumentException {
        this.startListening(0);
    }

    /**
     * This method reestablishes the server by using a user specified port number.
     *
     * @param port the new port number
     * @throws   IOException  if an I/O error occurs when opening the socket.
     * @throws   SecurityException
     *      if a security manager exists and its {@code checkListen}
     *      method doesn't allow the operation.
     * @throws   IllegalArgumentException if the port parameter is outside
     *           the specified range of valid port values, which is between
     *           0 and 65535, inclusive.
     */
    public void startListening(int port) throws IOException, SecurityException,
        IllegalArgumentException {
        if (this.serverSocket != null) {
            this.serverSocket.close();
        }
        this.isListening.set(true);
        this.serverSocket = new ServerSocket(port);
        this.inputExecutor.submit(() -> {
            try {
                listen();
            } catch (IOException | SecurityException e) {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        });
    }

    /**
     * This method let the server start listening for incoming connections.
     *
     * @throws   IOException  if an I/O error occurs when opening the socket.
     * @throws   SecurityException
     *      if a security manager exists and its {@code checkListen}
     *      method doesn't allow the operation.
     */
    public void restartListening() throws IOException, SecurityException {
        this.startListening(this.getLocalPort());
    }

    /**
     * This method gets the status of the local server.
     *
     * @return {@code true} if local server is listening to connections and {@code
     *       false} otherwise
     */
    public boolean isListening() {
        return isListening.get();
    }

    /**
     * This method let the server blocks and listens to an incoming connection.
     *
     * @throws   IOException  if an I/O error occurs when waiting for a
     *               connection.
     * @throws  SecurityException  if a security manager exists and its
     *             {@code checkAccept} method doesn't allow the operation.
     */
    public void listen() throws IOException, SecurityException {
        // waiting for a client and blocking
        if (this.serverSocket.isClosed()) {
            throw new SocketException("Socket is closed");
        }

        Logger.println("Waiting for connection on " + getLocalIP() + ":" + getLocalPort());

        while (true) {
            Socket newConnectionSocket;
            try {
                newConnectionSocket = this.serverSocket.accept();
            } catch (SocketException e) {
                // server is closed from another thread
                continue;
            }
            // client accepted
            Logger.printlnf("Receive connection on %s",
                    newConnectionSocket.getRemoteSocketAddress());
            if (connectionSockets.size() + 1 > maxConnections || !isListening.get()) {
                newConnectionSocket.close();
                continue;
            }
            this.connectionSockets.put(newConnectionSocket.getRemoteSocketAddress(),
                newConnectionSocket);
            if (this.controller.incomingConnection(newConnectionSocket.getRemoteSocketAddress(),
                    newConnectionSocket.getPort())) {
                this.outputExecutor.submit(() -> acceptMessage(newConnectionSocket));
            }
        }
    }

    /**
     * Closes the server.
     */
    public void closeServer() {
        this.isListening.set(false);
    }

    /**
     * Sends back the confirmation of connection.
     *
     * @param remoteSocketAddress the address of the remote side
     * @throws IOException  if an I/O error occurs when creating the
     *              output stream or if the socket is not connected.
     */
    public void confirmConnection(SocketAddress remoteSocketAddress) throws IOException {
        this.send(remoteSocketAddress, Tags.CONFIRM_TAG);
    }

    /**
     * This method gets the list of connected clients.
     *
     * @return a set containing the addresses of all connected clients
     */
    public Set<SocketAddress> getClientList() {
        return new HashSet<>(connectionSockets.keySet());
    }

    // ==================================================================================
    //                                   CONNECT
    // ==================================================================================

    /**
     * This function checks if there is another host connected to this server.
     * @return {@code true} if another host is connecting and {@code false} otherwise.
     */
    public boolean hasConnection() {
        for (Socket clientSocket: this.connectionSockets.values()) {
            if (!clientSocket.isClosed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function let the server connect to a remote host on its own initiative.
     *
     * @param address the address of the remote host
     * @param port the port of the remote host
     * @return the address of the remote side
     * @throws     UnknownHostException if the IP address of
     *      the host could not be determined.
     * @throws     IOException  if an I/O error occurs when creating the socket.
     * @throws     SecurityException  if a security manager exists and its
     *             {@code checkConnect} method doesn't allow the operation.
     * @throws     IllegalArgumentException if the port parameter is outside
     *             the specified range of valid port values, which is between
     *             0 and 65535, inclusive. or if the address is same as the address of
     *             the local host
     */
    public SocketAddress connectTo(String address, int port) throws UnknownHostException,
            IOException, SecurityException, IllegalArgumentException {
        InetAddress addr = InetAddress.getByName(address);
        if ((addr.isAnyLocalAddress() || addr.isLoopbackAddress()) && port == getLocalPort()) {
            throw new IllegalArgumentException("Address is same as current "
                + "application instance.");
        }
        Socket outgoingSocket = new Socket(address, port);
        Logger.printlnf("Connect to %s", outgoingSocket.getRemoteSocketAddress());
        this.connectionSockets.put(outgoingSocket.getRemoteSocketAddress(),
            outgoingSocket);
        this.outputExecutor.submit(() -> acceptMessage(outgoingSocket));
        return outgoingSocket.getRemoteSocketAddress();
    }

    /**
     * This method closes the connection between local host and remote host.
     *
     * @param remoteSocketAddress the address of the remote side
     * @throws IOException if an I/O error occurs when closing this socket.
     */
    public void closeConnection(SocketAddress remoteSocketAddress) throws IOException {
        if (this.connectionSockets.containsKey(remoteSocketAddress)) {
            Logger.printlnf("Close connect with %s", remoteSocketAddress);
            this.connectionSockets.get(remoteSocketAddress).close();
        }
        this.connectionSockets.remove(remoteSocketAddress);
    }

    /**
     * This method closes all connections between local host and remote hosts.
     *
     * @throws IOException if an I/O error occurs when closing this socket.
     */
    public void closeAllConnection() throws IOException {
        for (SocketAddress address: new HashSet<>(this.connectionSockets.keySet())) {
            closeConnection(address);
        }
    }

    /**
     * This method gets called when the remote side closes the connection.
     *
     * @param client the socket representing the remote side
     */
    public void remoteCloseConnection(Socket client) {
        this.controller.remoteCloseConnection(client.getRemoteSocketAddress());
        this.connectionSockets.remove(client.getRemoteSocketAddress());
    }

    // ==================================================================================
    //                                   COMMUNICATION
    // ==================================================================================

    /**
     * This method continue receives messages from the remote side.
     *
     * @param client the socket connecting the server and a specified client
     */
    public void acceptMessage(Socket client) {
        // takes input from the client socket
        DataInputStream in;
        try {
            in = new DataInputStream(
                new BufferedInputStream(client.getInputStream()));
        } catch (IOException e) {
            this.remoteCloseConnection(client);  // Construct input stream
            // failed
            return;
        }

        String line = "";

        // reads message from client until "Over" is sent
        while (!line.equals("CLOSE")) {
            try {
                line = in.readUTF();
                Logger.printColor(Logger.ANSI_GREEN, "%s -> local: %s",
                    client.getRemoteSocketAddress(), line);
                controller.receiveRemoteMessage(client.getRemoteSocketAddress(),
                    line);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (IOException i) {
                // Remote closed
                Logger.printlnf("%s closed", client.getRemoteSocketAddress());
                if (!client.isClosed()) {
                    this.remoteCloseConnection(client);
                }
                break;
            }
        }
    }

    /**
     * This method sends a message to a specified remote host.
     *
     * @param target the target to receive this message
     * @param message the message that is about to be sent
     * @throws IOException  if an I/O error occurs when creating the
     *              output stream or if the socket is not connected.
     */
    public void send(SocketAddress target, String message) throws IOException {
        if (this.connectionSockets.get(target) == null) {
            Logger.println("Cannot send message \"" + message + "\" without connection.");
            return;
        }
        DataOutputStream out = new DataOutputStream(
            this.connectionSockets.get(target).getOutputStream());
        out.writeUTF(message);
        Logger.printColor(Logger.ANSI_YELLOW, "local -> %s: %s",
            connectionSockets.get(target).getRemoteSocketAddress(), message);
    }

    /**
     * This method sends a message to every remote host.
     *
     * @param message the message that is about to be sent
     * @throws IOException  if an I/O error occurs when creating the
     *              output stream or if the socket is not connected.
     */
    public void broadcast(String message) throws IOException {
        if (this.connectionSockets.isEmpty()) {
            Logger.println("Cannot send message \"" + message + "\" without connection.");
            return;
        }
        for (Socket client: this.connectionSockets.values()) {
            send(client.getRemoteSocketAddress(), message);
        }
    }
}
