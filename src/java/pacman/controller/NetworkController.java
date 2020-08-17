package pacman.controller;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines basic actions that a controller with networking should
 * support.
 *
 * @version 1.0
 */
public interface NetworkController {

    /**
     * This method gets the port of the local server.
     *
     * @return the local port of the host
     */
    int getLocalPort();

    /**
     * This method gets the status of the local server.
     *
     * @return {@code true} if local server is started and {@code false} otherwise
     */
    boolean isServerStarted();

    /**
     * This method checks if any connection (server/client) is connected with local host.
     *
     * @return {@code true} if connected and {@code false} otherwise
     */
    boolean isConnected();

    /**
     * This method changes the port number of the local host and reset the server.
     *
     * @param port the local port of the host
     * @param confirm if a confirm should be sent
     * @return if the server is reestablished
     */
    boolean changePort(final String port, boolean confirm);

    /**
     * This method gets called once an incoming connection is sent to the host.
     *
     * @param remoteSocketAddress the address of the remote side
     * @param port the port of the remote side
     * @return if the user accepts the connection
     */
    boolean incomingConnection(final SocketAddress remoteSocketAddress, int port);

    /**
     * This method closes the server.
     */
    void closeServer();

    /**
     * This function let the server connect to a remote host on its own initiative.
     *
     * @param address the address of the remote host
     * @param port the port of the remote host
     * @param updateViewer if the viewer will be updated to reflect the connection
     * @return if the connection is created
     */
    boolean connectTo(final String address, final String port,
                      boolean updateViewer);

    /**
     * This method closes the connection.
     */
    void hostCloseConnection();

    /**
     * This method gets called when the remote side closes the connection.
     *
     * @param remoteSocketAddress the address of the remote side
     */
    void remoteCloseConnection(final SocketAddress remoteSocketAddress);

    /**
     * This method gets called when the remote side sends a message.
     *
     * @param from who sent this message
     * @param message the message content
     */
    void receiveRemoteMessage(SocketAddress from, String message);

    /**
     * This method gets the list of connected clients.
     *
     * @return a set containing the addresses of all connected clients
     */
    Set<SocketAddress> getClientList();

}
