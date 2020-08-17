package pacman.controller;

import golgui.components.GuiMessenger;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import pacman.agents.AbstractAgent;
import pacman.agents.GhostAgent;
import pacman.agents.PacmanAgent;
import pacman.algorithms.AlgorithmFactory;
import pacman.algorithms.GreedyAlgorithm;
import pacman.model.Coordinate;
import pacman.model.Direction;
import pacman.model.Maze;
import pacman.network.SimpleP2PServer;
import pacman.util.Logger;
import pacman.util.MazeFactory;
import pacman.util.StringUtilities;
import pacman.viewer.AgentItemPanel;
import pacman.viewer.GUIViewer;


/**
 * This is the controller of the game Pacman.
 *
 * @version 1.0
 */
public class PacmanController implements PacmanMazeController, NetworkController {
    /** Settings of the application. */
    private final Preferences settings;

    /** Contains a utility to handle all network connections. */
    private SimpleP2PServer server;

    /**
     * Contains the view that handles the information that is presented to and
     * accepted from the user.
     */
    private GUIViewer view;

    /**
     * Contains the score earned by the player.
     */
    private int scoresTotal;

    /**
     * Contains the total lives of the pacman.
     */
    private final int totalLives;

    /**
     * Contains the remaining number of lives of the pacman.
     */
    private AtomicInteger lives;

    /** Contains the maze of the game. */
    private Maze maze;

    /** Contains a flag if the start of game is with additional parameters. */
    private boolean isAdvancedStart = false;

    /** The center host of the game. */
    private SocketAddress center;
    private final HashMap<String, SocketAddress> agentNameClientAddressMap;
    private final HashMap<SocketAddress, String> clientAddressToAgentNameMap;
    private String localhostSelectedAgentName;
    private boolean networkMapLoaded = false;

    /**
     * Creates a new instance of PacmanController.
     */
    public PacmanController() {
        Preferences root = Preferences.userRoot();
        this.settings = root.node("/edu/rpi/csci4963/pacman");
        this.scoresTotal = 0;
        this.totalLives = settings.getInt("TotalLives", 3);
        this.lives = new AtomicInteger(totalLives);
        try {
            this.server = new SimpleP2PServer(this);
        } catch (IOException | SecurityException e) {
            (new GuiMessenger(null)).notification("Trying to connect to the "
                    + "internet...");
            (new GuiMessenger(null)).alert("Network connection failed. "
                    + "Please check your network and firewall settings");
        }
        agentNameClientAddressMap = new HashMap<>();
        clientAddressToAgentNameMap = new HashMap<>();
    }

    /**
     * Sets the view of the application.
     *
     * @param view the view that handles the information that is presented to and
     *      accepted from the user.
     */
    public void setView(GUIViewer view) {
        this.view = view;
        this.view.run();
    }

    // ==================================================================================
    //                                  GAME PREPARATION
    // ==================================================================================

    /**
     * Loads the maze from the file.
     *
     * @param preConfiguredMazeName name of a preconfigured maze; {@code null} if
     *                              user chooses a custom maze
     * @param filename name of an input file of a maze; {@code null} if user
     *                 does not choose a custom maze
     * @return if the maze is loaded successfully
     */
    public boolean load(String preConfiguredMazeName, String filename) {
        if (preConfiguredMazeName == null) {
            this.view.alert("You didn't choose a level.");
            return false;
        }
        try {
            if (!preConfiguredMazeName.equals(MazeFactory.CUSTOM_MAZE_NAME)) {
                this.maze = MazeFactory.readBoardFromString(
                        MazeFactory.PreConfiguredMaze.ITEMS.get(preConfiguredMazeName));
            } else {
                this.maze = MazeFactory.readBoardFromFile(filename);
            }
            return true;
        } catch (IOException e) {
            if (this.view != null) {
                this.view.alert(
                        "Failed to load from file \"" + filename + "\":" + e.getMessage());
            } else {
                System.err.println(
                        "Failed to load from file \"" + filename + "\":" + e.getMessage());
            }
        }
        return false;
    }


    /**
     * Loads the maze from the file.
     *
     * @param preConfiguredMazeName name of a preconfigured maze; {@code null} if
     *                              user chooses a custom maze
     * @param filename name of an input file of a maze; {@code null} if user
     *                 does not choose a custom maze
     */
    public void advancedLoad(final String preConfiguredMazeName,
                                final String filename) {
        if (preConfiguredMazeName.equals(MazeFactory.CUSTOM_MAZE_NAME) && isServerStarted()) {
            view.alert("Cannot use a custom maze in multiplayer mode. You need to "
                + "disconnect and shutdown the server first");
            return;
        }
        boolean ret = this.load(preConfiguredMazeName, filename);
        if (ret) {
            if (isConnected()) {
                networkMapLoaded = true;
            }
            agentNameClientAddressMap.clear();
            clientAddressToAgentNameMap.clear();
            for (int i = 0; i < Math.min(maze.getPacmanStartLocation().length,
                PacmanAgent.NAMES.length); i++) {
                agentNameClientAddressMap.put(PacmanAgent.NAMES[i], null);
            }
            for (int i = 0; i < Math.min(maze.getGhostsStartLocation().length,
                GhostAgent.NAMES.length); i++) {
                agentNameClientAddressMap.put(GhostAgent.NAMES[i], null);
            }
            for (SocketAddress address: server.getClientList()) {
                clientAddressToAgentNameMap.put(address, null);
            }
            if (isServerStarted()) {
                try {
                    server.broadcast("[MAP]," + preConfiguredMazeName);
                } catch (IOException e) {
                    view.alert("Failed to sync level settings to clients: poor "
                        + "connection");
                    return;
                }
            }
            view.setUpPlayerListPanel(preConfiguredMazeName, maze);
        }
    }

    // ==================================================================================
    //                              NETWORK
    // ==================================================================================

    /**
     * This method gets the port of the local server.
     *
     * @return the local port of the host
     */
    @Override
    public int getLocalPort() {
        return this.server.getLocalPort();
    }

    /**
     * This method gets the status of the local server.
     *
     * @return {@code true} if local server is started and {@code false} otherwise
     */
    @Override
    public boolean isServerStarted() {
        return server.isListening();
    }

    /**
     * This method checks if any connection (server/client) is connected with local host.
     *
     * @return {@code true} if connected and {@code false} otherwise
     */
    @Override
    public boolean isConnected() {
        return server.hasConnection();
    }

    /**
     * This method changes the port number of the local host and reset the server.
     *
     * @param port    the local port of the host
     * @param confirm if a confirm should be sent
     * @return if the server is reestablished
     */
    @Override
    public boolean changePort(String port, boolean confirm) {
        if (confirm) {
            if (port.equals("0") && !this.view.ask("Are you sure you want to set the "
                + "port to a system allocated random port?")) {
                return false;
            } else if (!port.equals("0") && !this.view.ask("Are you sure you want to "
                + "set the port to \"" + port + "\"")) {
                return false;
            }
        }
        try {
            this.server.closeAllConnection();
        } catch (IOException e) {
            this.view.alert("<html> Failed to change port due to system restriction. "
                    + "</html>");
            return false;
        }
        if (!StringUtilities.isInteger(port)) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                + "inclusive.");
            return false;
        }
        int portNum = Integer.parseInt(port);
        if (portNum < 0 || portNum > 65535) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                    + "inclusive.");
            return false;
        }
        if (portNum < 1024 && portNum != 0) {
            this.view.alert("Port number under 1024 is reserved for the system. "
                    + "Please select another port.");
            return false;
        }
        try {
            this.server.startListening(portNum);
        } catch (IOException | SecurityException e) {
            this.view.alert("Connection denied by system security restrictions. "
                    + "Please restart the application.");
            return false;
        } catch (IllegalArgumentException e) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                    + "inclusive.");
            return false;
        }
        return true;
    }

    /**
     * This method gets called once an incoming connection is sent to the host.
     *
     * @param remoteSocketAddress the address of the remote side
     * @param port                the port of the remote side
     * @return if the user accepts the connection
     */
    @Override
    public boolean incomingConnection(SocketAddress remoteSocketAddress, int port) {
        if (this.view.ask(
                "<html>Receive an incoming connection from " + remoteSocketAddress.toString()
                        + ". <br> Do you want to connect?</html>")) {
            this.view.incomingConnection(remoteSocketAddress, port);
            try {
                this.server.confirmConnection(remoteSocketAddress);
                if (networkMapLoaded) {
                    this.server.send(remoteSocketAddress, "[MAP],"
                        + settings.get("Maze", null));
                }
            } catch (IOException e) {
                this.view.alert("Failed to send confirm due to poor "
                        + "connection.");
            }
            return true;
        } else {
            try {
                this.server.closeConnection(remoteSocketAddress);
            } catch (IOException e) {
                this.view.alert("<html>Failure when denying the connection. "
                        + "<br>Please restart the application.</html>");
            }
            return false;
        }
    }

    /**
     * This method closes the server.
     */
    @Override
    public void closeServer() {
        try {
            this.server.closeAllConnection();
        } catch (IOException | SecurityException e) {
            this.view.alert("Failed to close the connection and restart server for "
                + "listening.");
        }
        this.server.closeServer();
        this.view.remoteConnectionClosed();
    }

    /**
     * This function let the server connect to a remote host on its own initiative.
     *
     * @param address      the address of the remote host
     * @param port         the port of the remote host
     * @param updateViewer if the viewer will be updated to reflect the connection
     * @return if the connection is created
     */
    @Override
    public boolean connectTo(String address, String port, boolean updateViewer) {
        if (!StringUtilities.isInteger(port)) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                + "inclusive.");
            return false;
        }
        int portNum = Integer.parseInt(port);
        if (portNum < 0 || portNum > 65535) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                    + "inclusive.");
            return false;
        }
        if (portNum < 1024) {
            this.view.alert("Port number under 1024 is reserved for the system. "
                    + "Please select another port.");
            return false;
        }
        // Connection
        try {
            this.server.closeAllConnection();
        } catch (IOException e) {
            this.view.alert("<html> Failed to connect due to system restriction. "
                + "</html>");
            return false;
        }
        try {
            this.center = this.server.connectTo(address, portNum);
        } catch (UnknownHostException e) {
            this.view.alert("Unrecognizable address \"" + address + "\"");
            return false;
        } catch (IOException e) {
            this.view.alert("Address " + address + ":" + port + " is unreachable.");
            return false;
        } catch (SecurityException e) {
            this.view.alert("Connection denied by system security restrictions.");
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            this.view.alert(e.getMessage());
            return false;
        }
        if (updateViewer) {
            this.view.updateConnectTo(address, portNum);
        }
        return true;
    }

    /**
     * This method closes the connection.
     */
    @Override
    public void hostCloseConnection() {
        try {
            this.server.closeAllConnection();
            this.server.restartListening();
        } catch (IOException | SecurityException e) {
            this.view.alert("Failed to close the connection and restart server for "
                    + "listening.");
        }
        // this.remoteReady.set(false); TODO ---------------------------------
        this.view.remoteConnectionClosed();

    }

    /**
     * This method gets called when the remote side closes the connection.
     *
     * @param remoteSocketAddress the address of the remote side
     */
    @Override
    public void remoteCloseConnection(SocketAddress remoteSocketAddress) {
        this.view.alert("The remote side has closed the connection.");
        // TODO ---------------------------------
        this.view.remoteConnectionClosed();
        if (remoteSocketAddress.equals(center)) {
            center = null;
        }
    }

    /**
     * This method gets called when the remote side sends a message.
     *
     * @param from who sent this message
     * @param message the message content
     */
    @Override
    public void receiveRemoteMessage(SocketAddress from, String message) {
        if (message.equals("")) {
            return;
        }
        String[] tokens = message.split(",");
        String tag = tokens[0];
        switch (tag) {
            case SimpleP2PServer.Tags.CONFIRM_TAG:
                // Remote confirmed
                break;
            case "[MAP]":
                // [MAP], <maze_name>
                if (tokens.length < 2) {
                    return;
                }
                String mazeName = tokens[1];
                view.notification("Server set the level to " + mazeName);
                advancedLoad(mazeName, null);
                break;
            case "[SELECT]":
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                String remoteSelectedAgentName = tokens[1];
                if (agentNameClientAddressMap.get(remoteSelectedAgentName) != null) {
                    // this agent is already chosen by someone
                    if (!agentNameClientAddressMap.get(remoteSelectedAgentName).equals(from)) {
                        // the agent is selected by another client
                        try {
                            server.send(from,
                                "[SELECT_FAIL]," + remoteSelectedAgentName + ","
                                    + agentNameClientAddressMap
                                    .get(remoteSelectedAgentName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                if (remoteSelectedAgentName.equals(localhostSelectedAgentName)) {
                    // this agent is selected by local host
                    try {
                        server.send(from, "[SELECT_FAIL]," + remoteSelectedAgentName
                            + ",host");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (!agentNameClientAddressMap.containsKey(remoteSelectedAgentName)) {
                    // Unknown agent selected
                    for (int i = 0; i < Math.min(maze.getPacmanStartLocation().length,
                        PacmanAgent.NAMES.length); i++) {
                        agentNameClientAddressMap.put(PacmanAgent.NAMES[i], null);
                    }
                    for (int i = 0; i < Math.min(maze.getGhostsStartLocation().length,
                        GhostAgent.NAMES.length); i++) {
                        agentNameClientAddressMap.put(GhostAgent.NAMES[i], null);
                    }
                    if (!agentNameClientAddressMap.containsKey(remoteSelectedAgentName)) {
                        try {
                            server.send(from, "[SELECT_FAIL]," + remoteSelectedAgentName
                                + ",unknown host");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                String oldAgentName = clientAddressToAgentNameMap.get(from);
                if (oldAgentName != null) {
                    agentNameClientAddressMap.put(oldAgentName, null);
                }
                agentNameClientAddressMap.put(remoteSelectedAgentName, from);
                clientAddressToAgentNameMap.put(from, remoteSelectedAgentName);
                try {
                    server.send(from, "[SELECT_GOOD]," + remoteSelectedAgentName);
                } catch (IOException e) {
                    Logger.err("");
                    e.printStackTrace();
                }
                break;
            case "[SELECT_FAIL]":
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                view.notification("Cannot select agent \"" + tokens[1] + "\": someone "
                    + "else is playing it.");
                localhostSelectedAgentName = null;
                view.unselectPlayer(tokens[1]);
                break;
            case "[START]":
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                this.networkStart(tokens);
                break;
            case "[HURRY]":
                view.notification("The server is trying to start the game but you're "
                    + "still not ready. Hurry up and choose an agent");
                break;
            case "[SELECT_GOOD]":
                break;
            case "[DIRECTION]":
                if (tokens.length < 5) {
                    Logger.err("Length < 3");
                    return;
                }
                if (StringUtilities.isInteger(tokens[2])
                    && localhostSelectedAgentName.equals("pacman")) {
                    break;
                }
                if (localhostSelectedAgentName.equals(tokens[2])) {
                    break;
                }
                view.immediateDirectionChange(tokens[1], tokens[3], tokens[4], tokens[2]);
                for (SocketAddress connection: server.getClientList()) {
                    if (!connection.equals(from)) {
                        try {
                            server.send(connection, message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case "[LOCATION]":
                if (tokens.length < 4) {
                    Logger.err("Length < 3");
                    return;
                }
                if (StringUtilities.isInteger(tokens[2])
                    && localhostSelectedAgentName.equals("pacman")) {
                    break;
                }
                if (localhostSelectedAgentName.equals(tokens[2])) {
                    break;
                }
                view.immediateLocationChange(tokens[1], tokens[2], tokens[3]);
                for (SocketAddress connection: server.getClientList()) {
                    if (!connection.equals(from)) {
                        try {
                            server.send(connection, message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            default:
        }
    }

    /**
     * This method gets the list of connected clients.
     *
     * @return a set containing the addresses of all connected clients
     */
    @Override
    public Set<SocketAddress> getClientList() {
        return server.getClientList();
    }

    /**
     * Checks if the side is the central server of the game.
     *
     * @return {@code true} if all information are passed through this node and {@code
     *      false} otherwise or networking not enabled
     */
    public boolean isCentralServer() {
        return isServerStarted() && isConnected();
    }

    /**
     * Checks if the side is the central server of the game.
     *
     * @return {@code true} if all information are passed through this node and {@code
     *      false} otherwise or networking not enabled
     */
    public boolean isClientNode() {
        return !isServerStarted() && isConnected();
    }

    /**
     * Send the selection to central to receive response on the selection of player.
     *
     * @param agentName the name of the agent selected
     */
    public void sendAgentSelection(final String agentName) {
        if (center != null) {
            try {
                server.send(center, "[SELECT]," + agentName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ==================================================================================
    //                                    QUICK START GAME
    // ==================================================================================

    /**
     * Starts the game.
     */
    public void start() {
        isAdvancedStart = false;
        String mazeName = settings.get("Maze", null);
        String filename = settings.get("InputFile", null);
        if (this.load(mazeName, filename)) {
            view.setTitle(mazeName);
            view.start(maze);
            this.addPacman();
            this.addGhosts();
        }
    }

    /**
     * Adds the pacman to the maze and to the view.
     */
    public void addPacman() {
        List<Coordinate> pacman = Arrays.asList(maze.getPacmanStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(pacman, ThreadLocalRandom.current());
        this.maze.pacmanVisit(0, pacman.get(0).getX(),
            pacman.get(0).getY());
        this.view.addPacman(maze, pacman.get(0).getX(), pacman.get(0).getY(), 0,
            new GreedyAlgorithm(maze), true, false);
    }

    /**
     * Adds the ghosts to the maze and to the view.
     */
    public void addGhosts() {
        List<Coordinate> ghosts = Arrays.asList(maze.getGhostsStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(ghosts, ThreadLocalRandom.current());
        for (int i = 0; i < Math.min(ghosts.size(), GhostAgent.NAMES.length); i++) {
            this.maze.ghostVisit(GhostAgent.NAMES[i], ghosts.get(i).getX(),
                ghosts.get(i).getY());
            this.view.addGhost(maze, ghosts.get(i).getX(), ghosts.get(i).getY(),
                GhostAgent.NAMES[i], new GreedyAlgorithm(maze), false, false);
        }
    }

    // ==================================================================================
    //                              ADVANCED START
    // ==================================================================================

    /**
     * Starts the game with advanced configurations.
     */
    public void showAdvancedStart() {
        view.askForInput(0);
        String mazeName = settings.get("Maze", null);
        String filename = settings.get("InputFile", null);
        load(mazeName, filename);
        view.showAdvancedPanel(mazeName, maze);
    }

    /**
     * Starts the game with advanced options.
     * @param selectedName name of the user selected agent
     * @param agentItemPanels Contains a list of all choices of agents
     *                                 available in the maze
     */
    public void advancedStart(String selectedName, List<AgentItemPanel> agentItemPanels) {
        if (maze == null) {
            view.notification("You haven't set the map");
            return;
        }
        if (selectedName == null) {
            view.notification("You haven't select an agent to control");
            return;
        }
        if (server.isListening() && server.hasConnection()) {
            for (SocketAddress address : clientAddressToAgentNameMap.keySet()) {
                if (clientAddressToAgentNameMap.get(address) == null) {
                    view.notification("User " + address + " is not ready.");
                    try {
                        server.send(address, "[HURRY]");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
        view.askForInput(1);
        isAdvancedStart = true;
        // Start the game
        String mazeName = settings.get("Maze", null);
        load(mazeName, null);
        view.setTitle(mazeName);
        view.start(maze);

        // Add ghosts and pacman
        AlgorithmFactory algorithmFactory = new AlgorithmFactory(maze);
        StringBuilder info = new StringBuilder("[START],").append(mazeName).append(",");

        List<Coordinate> pacman = Arrays.asList(maze.getPacmanStartLocation());
        List<Coordinate> ghosts = Arrays.asList(maze.getGhostsStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(pacman, ThreadLocalRandom.current());
        Collections.shuffle(ghosts, ThreadLocalRandom.current());
        int pacmanNum = 0;
        int ghostNum = 0;
        for (AgentItemPanel selectionItem : agentItemPanels) {
            boolean isSelf = selectedName.equals(selectionItem.getAgentName());
            if (Arrays.asList(PacmanAgent.NAMES).contains(selectionItem.getAgentName())) {
                boolean isNetwork = agentNameClientAddressMap.get("pacman") != null;
                this.maze.pacmanVisit(pacmanNum, pacman.get(pacmanNum).getX(),
                    pacman.get(pacmanNum).getY());
                this.view.addPacman(maze,
                    pacman.get(pacmanNum).getX(), pacman.get(pacmanNum).getY(), pacmanNum,
                    algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()),
                    isSelf, isNetwork);
                info.append(pacmanNum).append("~").append(pacman.get(pacmanNum).getX())
                    .append("~").append(pacman.get(pacmanNum).getY())
                    .append("~").append(selectionItem.getAlgorithmName()).append(",");
                pacmanNum++;
            } else {
                String agentName = GhostAgent.NAMES[ghostNum];
                boolean isNetwork = agentNameClientAddressMap.get(agentName) != null;
                this.maze.ghostVisit(agentName, ghosts.get(ghostNum).getX(),
                    ghosts.get(ghostNum).getY());
                this.view.addGhost(maze, ghosts.get(ghostNum).getX(),
                    ghosts.get(ghostNum).getY(),
                    agentName,
                    algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()),
                    isSelf, isNetwork);
                info.append(GhostAgent.NAMES[ghostNum]).append("~")
                    .append(ghosts.get(ghostNum).getX())
                    .append("~").append(ghosts.get(ghostNum).getY())
                    .append("~").append(selectionItem.getAlgorithmName()).append(",");
                ghostNum++;
            }
        }
        info.deleteCharAt(info.length() - 1);

        if (server.isListening() && server.hasConnection()) {
            try {
                server.broadcast(info.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the game from network signal.
     *
     * @param tokens tokens of the network message
     */
    private void networkStart(final String[] tokens) {
        String mazeName = tokens[1];
        settings.put("Maze", mazeName);
        view.notification("The host has started the game");
        load(mazeName, null);
        view.setTitle(mazeName);
        view.start(maze);

        // Add ghosts and pacman
        AlgorithmFactory algorithmFactory = new AlgorithmFactory(maze);
        for (int i = 2; i < tokens.length; i++) {
            String[] tokens2 = tokens[i].split("~");
            String agentName = tokens2[0];
            int x = Integer.parseInt(tokens2[1]);
            int y = Integer.parseInt(tokens2[2]);
            String algorithm = tokens2[3];
            Logger.printlnf("Adding %s at (%d, %d) using %s", agentName, x, y, algorithm);
            boolean isSelf = agentName.equals(localhostSelectedAgentName);
            if (StringUtilities.isInteger(agentName)) {
                this.maze.pacmanVisit(Integer.parseInt(agentName), x, y);
                this.view.addPacman(maze, x, y, Integer.parseInt(agentName),
                    algorithmFactory.createAlgorithm(algorithm),
                    isSelf, !isSelf);
            } else {
                this.maze.ghostVisit(agentName, x, y);
                this.view.addGhost(maze, x, y, agentName,
                    algorithmFactory.createAlgorithm(algorithm), isSelf, !isSelf);
            }
        }
        view.repaint();
    }

    // ==================================================================================
    //                                     GAME CONTROL
    // ==================================================================================

    /**
     * Preview the maze.
     */
    public void preview() {
        view.preview(maze);
    }

    /**
     * Goes back to the main menu and stops the game.
     */
    public void backToMainMenu() {
        lives = new AtomicInteger(totalLives);
        view.stopGame();
    }

    /**
     * Restarts the game with same configuration after gameplay ends.
     */
    private void restartGame() {
        if (isAdvancedStart) {
            this.advancedStart(view.getSelectedName(), view.getAgentItemPanels());
        } else {
            this.start();
        }
        this.lives = new AtomicInteger(totalLives);
    }

    /**
     * Exits the game.
     */
    public void exit() {
        settings.putInt("TotalLives", totalLives);
        System.exit(0);
    }

    // ==================================================================================
    //                                   GAME PLAY
    // ==================================================================================

    /**
     * Gets called when an agent (pacman/ghost) visits a block. It checks if the
     * pacman meets a food (then earn scores), pellet (then earn scores and enter
     * "ghost buster" mode), a scared ghost (then earn scores and reset the ghost),
     * or a normal ghost (then dies). After that it updates the scores and check
     * if game is won.
     *
     * @param agent the agent of the move
     * @param x the current x coordinate of agent
     * @param y the current y coordinate of agent
     */
    @Override
    public void agentVisit(AbstractAgent agent, final int x,
        final int y) {
        int scoresDiff = 0;
        if (agent instanceof PacmanAgent) {
            // If pellets is hit, scare the ghosts and turn pacman into ghost buster
            if (maze.get(x, y) == Maze.PELLETS) {
                Logger.printlnf("Ghost buster!");
                this.hitPellets();
            }
            // Update location and scores
            scoresDiff += maze.pacmanVisit(((PacmanAgent) agent).getIndex(),
                    x, y);
            // Check if the curr location is the same as any ghost
            for (String ghostName: maze.getVisibleGhostNames()) {
                if (maze.getGhostsLocation().get(ghostName).equals(new Coordinate(x,
                        y))) {
                    if (maze.getGhostScaredTimes().get(ghostName) > 0) {
                        this.pacmanEat(ghostName);
                    } else {
                        this.pacmanDie(((PacmanAgent) agent).getIndex());
                        return;
                    }
                }
            }
        } else if (agent instanceof GhostAgent) {
            // Update location
            maze.ghostVisit(((GhostAgent) agent).getAgentName(), x,
                    y);
            // If meet with a pacman
            for (Integer pacmanIndex: maze.getPacmanLocation().keySet()) {
                if (maze.getPacmanLocation().get(pacmanIndex).equals(new Coordinate(x,
                        y))) {
                    if (maze.getGhostScaredTimes().get(((GhostAgent) agent).getAgentName()) > 0) {
                        this.pacmanEat(((GhostAgent) agent).getAgentName());
                    } else {
                        this.pacmanDie(pacmanIndex);
                        return;
                    }
                }
            }
        }
        scoresTotal += scoresDiff;
        view.updateScore(scoresTotal, scoresDiff);
        if (maze.getFoodsNum() + maze.getPelletsNum() == 0) {
            this.gameOver(true);
        }
    }

    /**
     * When the game over, ask the user to restart or close.
     *
     * @param win if the user wins
     */
    public void gameOver(boolean win) {
        Logger.printlnf("Game over");
        this.view.gameOver(win);
        int choice = this.view.showResultDialog(win);
        // Show panel of lose/win
        if (choice == 1) {
            // Disconnect
            this.hostCloseConnection();
            this.restartGame();
        } else if (choice == 2) {
            // Close the game
            this.exit();
        } else {
            // Restart the game
            this.restartGame();
        }
    }

    /**
     * When the pacman hits a pellet, all ghosts are scared.
     */
    private void hitPellets() {
        int scaredTime = settings.getInt("DefaultScaredTime", 10);
        for (String ghostName: maze.getVisibleGhostNames()) {
            maze.setGhostScared(ghostName, scaredTime);
            view.setGhostScared(ghostName, scaredTime);
        }
    }

    /**
     * When pacman meets a normal ghost, the pacman dies.
     *
     * @param pacmanIndex the index of the pacman
     */
    private void pacmanDie(Integer pacmanIndex) {
        if (lives.get() > 1) {
            lives.decrementAndGet();
            this.view.resetAgent(pacmanIndex);
            this.view.updateLives(lives.get());
            this.view.notification("Pacman died! It has " + lives.get() + " "
                    + "lives left");
        } else {
            this.gameOver(false);
        }
    }

    /**
     * When pacman meets a scared ghost, the ghost dies.
     *
     * @param ghostName the name of the ghost
     */
    private void pacmanEat(String ghostName) {
        view.resetAgent(ghostName);
    }

    /**
     * Removes an agent from the game.
     *
     * @param agent the agent that is about to be removed.
     */
    @Override
    public void removeAgent(AbstractAgent agent) {
        if (agent instanceof PacmanAgent) {
            maze.removePacman(((PacmanAgent) agent).getIndex());
        } else if (agent instanceof GhostAgent) {
            maze.removeGhost(((GhostAgent) agent).getAgentName());
        }
    }

    /**
     * Notifies the direction change (actions) of an agent.
     *
     * @param d     the new direction of the agent
     * @param agent the agent that is changing
     */
    @Override
    public void notifyDirectionChange(final Direction d, final AbstractAgent agent,
        int x, int y) {
        if (!isConnected()) {
            return;
        }
        if (isClientNode()) {
            if (agent instanceof PacmanAgent) {
                if (!"pacman".equals(localhostSelectedAgentName)) {
                    return;
                }
            } else {
                if (!((GhostAgent) agent).getAgentName().equals(localhostSelectedAgentName)) {
                    return;
                }
            }
        }
        try {
            if (agent instanceof PacmanAgent) {
                this.server.broadcast(
                    "[DIRECTION]," + d.name() + "," + ((PacmanAgent) agent).getIndex()
                        + "," + x + "," + y);
            } else {
                this.server.broadcast(
                    "[DIRECTION]," + d.name() + "," + ((GhostAgent) agent).getAgentName()
                        + "," + x + "," + y);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the location change (actions) of an agent.
     *
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     * @param agent the agent that is changing
     */
    @Override
    public void notifyLocationChange(final int x, final int y, final AbstractAgent agent) {
        if (!isConnected()) {
            return;
        }
        if (isClientNode()) {
            if (agent instanceof PacmanAgent) {
                if (!"pacman".equals(localhostSelectedAgentName)) {
                    return;
                }
            } else {
                if (!((GhostAgent) agent).getAgentName().equals(localhostSelectedAgentName)) {
                    return;
                }
            }
        }
        try {
            if (agent instanceof PacmanAgent) {
                this.server.broadcast(
                    "[LOCATION]," + x + "," + y + "," + ((PacmanAgent) agent).getIndex());
            } else {
                this.server.broadcast(
                    "[LOCATION]," + x + "," + y + "," + ((GhostAgent) agent).getAgentName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * User selects a player to controll in the game.
     *
     * @param agentName the name of the agent being selected
     */
    public void userSelected(final String agentName) {
        if (agentNameClientAddressMap.get(agentName) != null) {
            view.notification("Cannot select agent \"" + agentName + "\": someone "
                + "else is playing it.");
            view.unselectPlayer(agentName);
            return;
        }
        this.localhostSelectedAgentName = agentName;
    }

}
