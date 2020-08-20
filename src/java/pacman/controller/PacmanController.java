package pacman.controller;

import golgui.components.GuiMessenger;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private String prevSelectedAgentName;

    /**
     * Contain tags to communicate in networking.
     * @see PacmanController#receiveRemoteMessage(SocketAddress, String)
     * @version 1.0
     */
    private static final class Tags {

        /** DIRECTION, {direction}, {pacman_index/ghost_name}, x, y. */
        static final String DIRECTION = "[DIRECTION]";

        /** LOCATION, {pacman_index/ghost_name}, x, y. */
        static final String LOCATION = "[LOCATION]";

        /** HURRY. */
        static final String HURRY = "[HURRY]";

        /** MAP, {maze_name}. */
        static final String MAP = "[MAP]";

        /** START, {maze_name}, {pacman_index/ghost_name}~{x}~{y}~{algorithm}... */
        static final String START = "[START]";

        /** SELECT, {agent_name}. */
        static final String SELECT = "[SELECT]";

        /** SELECT_GOOD, {agent_name}. */
        static final String SELECT_GOOD = "[SELECT_GOOD]";

        /** SELECT_FAIL, {agent_name}, {reason: who uses this agent/unknown agent}.  */
        static final String SELECT_FAIL = "[SELECT_FAIL]";

        /** EAT, {ghost_name}. */
        static final String EAT = "[EAT]";

        /** DIE, {index}, {life}. */
        static final String DIE = "[DIE]";

        /** SCARE, {ghost_name}, {scare_time}. */
        static final String SCARE = "[SCARE]";

        /** GAME_OVER, {pacman_wins}. */
        static final String GAME_OVER = "[GAME_OVER]";
    }

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
            networkMapLoaded = true;
            agentNameClientAddressMap.clear();
            clientAddressToAgentNameMap.clear();
            updateAgentNameMap();
            if (isServerStarted()) {
                try {
                    server.broadcast(Tags.MAP, preConfiguredMazeName);
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
     * Checks if the side is the central server of the game.
     *
     * @return {@code true} if all information are passed through this node and {@code
     *      false} otherwise or networking not enabled
     */
    public boolean isServerNode() {
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
                    this.view.askForInput(1);
                    this.server.send(remoteSocketAddress, Tags.MAP,
                        settings.get("Maze", null));
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
        this.view.restartListening();
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
            Logger.printlnf("Try to close all connection");
            this.server.closeAllConnection();
            if (isServerStarted()) {
                this.server.restartListening();
                this.view.restartListening();
            } else {
                view.reenableJoin();
            }
        } catch (IOException | SecurityException e) {
            this.view.alert("Failed to close the connection and restart server for "
                    + "listening.");
        }
    }

    /**
     * This method gets called when the remote side closes the connection.
     *
     * @param remoteSocketAddress the address of the remote side
     */
    @Override
    public void remoteCloseConnection(SocketAddress remoteSocketAddress) {
        if (remoteSocketAddress.equals(center)) {
            center = null;
            view.alert(StringUtilities.makeHTML("Pacman connection is not available at "
                + "this <br> time. Please try again later."));
            view.reenableJoin();
            restartGame();
        }
        this.view.alert(
            "The remote side " + remoteSocketAddress + " has closed the connection.");
        // Set the agent to auto mode
        String agentName = clientAddressToAgentNameMap.get(remoteSocketAddress);
        if (agentName == null) {
            Logger.err("clientAddressToAgentNameMap = %s", clientAddressToAgentNameMap);
            return;
        }
        clientAddressToAgentNameMap.remove(remoteSocketAddress);
        agentNameClientAddressMap.put(agentName, null);
        if (agentName.equals(PacmanAgent.NAMES[0])) {
            view.aiTakeOver(0);
        } else {
            view.aiTakeOver(agentName);
        }
        view.updateClientListPanel();
    }

    /**
     * This method gets called when the remote side sends a message.
     *
     * @param from who sent this message
     * @param message the message content
     * @see PacmanController.Tags
     */
    @Override
    public void receiveRemoteMessage(SocketAddress from, String message) {
        Logger.printColor(Logger.ANSI_GREEN, 0, "%s -> : %s", from, message);
        if (message.equals("")) {
            return;
        }
        String[] tokens = message.split(",");
        String tag = tokens[0];
        switch (tag) {
            case SimpleP2PServer.Tags.CONFIRM_TAG:
                // Remote confirmed
                break;
            case Tags.MAP:
                // [MAP], <maze_name>
                if (tokens.length < 2) {
                    return;
                }
                String mazeName = tokens[1];
                view.notification("Server set the level to " + mazeName);
                advancedLoad(mazeName, null);
                break;
            case Tags.SELECT:
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                String remoteSelectedAgentName = tokens[1];
                if (agentNameClientAddressMap.get(remoteSelectedAgentName) != null) {
                    // this agent is already chosen by someone
                    if (!agentNameClientAddressMap.get(remoteSelectedAgentName)
                        .equals(from)) {
                        // the agent is selected by another client
                        try {
                            server.send(from,
                                Tags.SELECT_FAIL, remoteSelectedAgentName,
                                agentNameClientAddressMap.get(remoteSelectedAgentName)
                                    .toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                if (remoteSelectedAgentName.equals(localhostSelectedAgentName)) {
                    // this agent is selected by local host
                    try {
                        server.send(from, Tags.SELECT_FAIL, remoteSelectedAgentName,
                            "host");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (!agentNameClientAddressMap.containsKey(remoteSelectedAgentName)) {
                    // Unknown agent selected
                    updateAgentNameMap();
                    Logger.printlnf("Update maps due to unknown agent name.");
                    if (!agentNameClientAddressMap.containsKey(remoteSelectedAgentName)) {
                        try {
                            server.send(from, Tags.SELECT_FAIL, remoteSelectedAgentName,
                                "unknown host");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                String oldAgentName = clientAddressToAgentNameMap.get(from);
                if (oldAgentName != null) {
                    agentNameClientAddressMap.put(oldAgentName, null);
                }
                agentNameClientAddressMap.put(remoteSelectedAgentName, from);
                clientAddressToAgentNameMap.put(from, remoteSelectedAgentName);
                try {
                    server.send(from, Tags.SELECT_GOOD, remoteSelectedAgentName);
                } catch (IOException e) {
                    Logger.err("");
                    e.printStackTrace();
                }
                break;
            case Tags.SELECT_GOOD:
                prevSelectedAgentName = null;
                break;
            case Tags.SELECT_FAIL:
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                view.notification("Cannot select agent \"" + tokens[1] + "\": someone "
                    + "else is playing it.");
                localhostSelectedAgentName = null;
                view.unselectPlayer(tokens[1]);
                userSelected(prevSelectedAgentName, true);
                prevSelectedAgentName = null;
                break;
            case Tags.START:
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                this.networkStart(tokens);
                break;
            case Tags.HURRY:
                view.notification("The server is trying to start the game but you're "
                    + "still not ready. Hurry up and choose an agent");
                break;
            case Tags.DIRECTION:
                if (tokens.length < 5) {
                    Logger.err("Length < 3");
                    return;
                }
                String direction = tokens[1];
                String agentName = tokens[2];
                boolean isPacman = StringUtilities.isInteger(agentName);
                String x = tokens[3];
                String y = tokens[4];
                boolean shouldJump = false;
                if (isServerNode()) {
                    // Only cares the info from who controls this agent
                    if (isPacman) {
                        shouldJump =
                            !from.equals(
                                agentNameClientAddressMap.get(PacmanAgent.NAMES[0]));
                    } else {
                        shouldJump =
                            !from.equals(agentNameClientAddressMap.get(agentName));
                    }
                }
                if (shouldJump) {
                    Logger.printColor(Logger.ANSI_PURPLE, 0, "Ignored");
                    break;
                }
                if (isPacman && PacmanAgent.NAMES[0].equals(localhostSelectedAgentName)) {
                    Logger.printColor(Logger.ANSI_PURPLE, 0, "Ignored");
                    break;
                } else if (!isPacman && agentName.equals(localhostSelectedAgentName)) {
                    Logger.printColor(Logger.ANSI_PURPLE, 0, "Ignored");
                    break;
                }
                if (isServerNode()) {
                    for (SocketAddress connection : server.getClientList()) {
                        if (!connection.equals(from)) {
                            try {
                                server.send(connection, message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                view.immediateDirectionChange(direction, x, y, agentName);
                break;
            case Tags.LOCATION:
                if (tokens.length < 4) {
                    Logger.err("Length < 4");
                    return;
                }
                agentName = tokens[1];
                isPacman = StringUtilities.isInteger(agentName);
                x = tokens[2];
                y = tokens[3];
                shouldJump = false;
                if (isServerNode()) {
                    // Only cares the info from who controls this agent
                    if (isPacman) {
                        shouldJump =
                            !from.equals(
                                agentNameClientAddressMap.get(PacmanAgent.NAMES[0]));
                    } else {
                        shouldJump =
                            !from.equals(agentNameClientAddressMap.get(agentName));
                    }
                }
                if (shouldJump) {
                    Logger.printlnf(Logger.ANSI_PURPLE, "Ignored");
                    break;
                }
                if (isPacman && PacmanAgent.NAMES[0].equals(localhostSelectedAgentName)) {
                    Logger.printlnf(Logger.ANSI_PURPLE, "Ignored");
                    break;
                } else if (!isPacman && agentName.equals(localhostSelectedAgentName)) {
                    Logger.printlnf(Logger.ANSI_PURPLE, "Ignored");
                    break;
                }
                view.immediateLocationChange(x, y, agentName);
                if (isServerNode()) {
                    for (SocketAddress connection : server.getClientList()) {
                        if (!connection.equals(from)) {
                            try {
                                server.send(connection, message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case Tags.SCARE:
                if (tokens.length < 3) {
                    Logger.err("Length < 3");
                    return;
                }
                String ghostName = tokens[1];
                int scaredTime = Integer.parseInt(tokens[2]);
                maze.setGhostScared(ghostName, scaredTime);
                view.setGhostScared(ghostName, scaredTime);
                break;
            case Tags.EAT:
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                ghostName = tokens[1];
                view.resetAgent(ghostName);
                break;
            case Tags.DIE:
                if (tokens.length < 3) {
                    Logger.err("Length < 3");
                    return;
                }
                int pacmanIndex = Integer.parseInt(tokens[1]);
                int life = Integer.parseInt(tokens[2]);

                this.view.resetAgent(pacmanIndex);
                lives.set(life);
                this.view.updateLives(life);
                this.view.notification("Pacman died! It has " + life + " "
                    + "lives left");
                break;
            case Tags.GAME_OVER:
                if (tokens.length < 2) {
                    Logger.err("Length < 2");
                    return;
                }
                boolean result = Boolean.parseBoolean(tokens[1]);
                gameOver(result);
                break;
            default:
        }
    }

    /**
     * Updates the map containing the relationship between each agent and the clients.
     */
    private void updateAgentNameMap() {
        for (int i = 0; i < Math.min(maze.getPacmanStartLocation().length,
            PacmanAgent.NAMES.length); i++) {
            agentNameClientAddressMap.putIfAbsent(PacmanAgent.NAMES[i], null);
        }
        for (int i = 0; i < Math.min(maze.getGhostsStartLocation().length,
            GhostAgent.NAMES.length); i++) {
            agentNameClientAddressMap.putIfAbsent(GhostAgent.NAMES[i], null);
        }
        for (SocketAddress address: server.getClientList()) {
            clientAddressToAgentNameMap.putIfAbsent(address, null);
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
     * Send the selection to central to receive response on the selection of player.
     *
     * @param agentName the name of the agent selected
     */
    public void sendAgentSelection(final String agentName) {
        if (center != null) {
            try {
                server.send(center, Tags.SELECT, agentName);
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
        view.setTitle("Pacman");
        view.askForInput(0);
        String mazeName = settings.get("Maze", null);
        String filename = settings.get("InputFile", null);
        // load(mazeName, filename);
        view.showAdvancedPanel(mazeName, maze);
    }

    /**
     * Starts the game with advanced options.
     * @param selectedName name of the user selected agent
     * @param agentItemPanels Contains a list of all choices of agents
     *                                 available in the maze
     */
    public void advancedStart(String selectedName,
        HashMap<String, AgentItemPanel> agentItemPanels) {
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
                        server.send(address, Tags.HURRY);
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
        if (isServerStarted() && isServerNode()) {
            view.setTitle(mazeName + " (Server)");
        } else {
            view.setTitle(mazeName);
        }
        view.start(maze);

        // Add ghosts and pacman
        AlgorithmFactory algorithmFactory = new AlgorithmFactory(maze);
        List<String> info = new ArrayList<>(Arrays.asList(Tags.START, mazeName));

        List<Coordinate> pacman = Arrays.asList(maze.getPacmanStartLocation());
        List<Coordinate> ghosts = Arrays.asList(maze.getGhostsStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(pacman, ThreadLocalRandom.current());
        Collections.shuffle(ghosts, ThreadLocalRandom.current());
        int pacmanNum = 0;
        int ghostNum = 0;
        for (AgentItemPanel selectionItem : agentItemPanels.values()) {
            boolean isSelf = selectedName.equals(selectionItem.getAgentName());
            if (Arrays.asList(PacmanAgent.NAMES).contains(selectionItem.getAgentName())) {
                boolean isNetwork = agentNameClientAddressMap.get(PacmanAgent.NAMES[0]) != null;
                this.maze.pacmanVisit(pacmanNum, pacman.get(pacmanNum).getX(),
                    pacman.get(pacmanNum).getY());
                this.view.addPacman(maze,
                    pacman.get(pacmanNum).getX(), pacman.get(pacmanNum).getY(), pacmanNum,
                    algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()),
                    isSelf, isNetwork);
                info.add(String.join("~", new String[]{String.valueOf(pacmanNum),
                    String.valueOf(pacman.get(pacmanNum).getX()),
                    String.valueOf(pacman.get(pacmanNum).getY()),
                    selectionItem.getAlgorithmName()}));
                pacmanNum++;
            } else {
                String agentName = selectionItem.getAgentName();
                boolean isNetwork = agentNameClientAddressMap.get(agentName) != null;
                this.maze.ghostVisit(agentName, ghosts.get(ghostNum).getX(),
                    ghosts.get(ghostNum).getY());
                this.view.addGhost(maze, ghosts.get(ghostNum).getX(),
                    ghosts.get(ghostNum).getY(),
                    agentName,
                    algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()),
                    isSelf, isNetwork);
                info.add(String.join("~", new String[]{selectionItem.getAgentName(),
                    String.valueOf(ghosts.get(ghostNum).getX()),
                    String.valueOf(ghosts.get(ghostNum).getY()),
                    selectionItem.getAlgorithmName()}));
                ghostNum++;
            }
        }

        if (server.isListening() && server.hasConnection()) {
            try {
                server.broadcast(info.toArray(new String[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Logger.println("\n"
            + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n"
            + "%%%                       PACMAN                          %%%\n"
            + "%%%                                                       %%%\n"
            + "%%%                       SERVER                          %%%\n"
            + "%%%                                                       %%%\n"
            + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
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
        isAdvancedStart = true;
        view.start(maze);

        Logger.printlnf("Server started game, the local side is controlling %s",
            localhostSelectedAgentName);
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
                isSelf = localhostSelectedAgentName.equals(PacmanAgent.NAMES[0]);
                this.maze.pacmanVisit(Integer.parseInt(agentName), x, y);
                this.view.addPacman(maze, x, y, Integer.parseInt(agentName),
                    algorithmFactory.createAlgorithm(algorithm),
                    isSelf, !isSelf);
                if (isSelf) {
                    view.setTitle(mazeName + " (Controlling pacman)");
                }
            } else {
                this.maze.ghostVisit(agentName, x, y);
                this.view.addGhost(maze, x, y, agentName,
                    algorithmFactory.createAlgorithm(algorithm), isSelf, !isSelf);
                if (isSelf) {
                    view.setTitle(mazeName + " (Controlling " + agentName + " ghost)");
                }
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
        view.stopGame();
        lives = new AtomicInteger(totalLives);
        if (isAdvancedStart) {
            this.showAdvancedStart();
        } else {
            this.view.showStartUpInterface();
        }
    }

    /**
     * Restarts the game with same configuration after gameplay ends.
     */
    private void restartGame() {
        view.stopGame();
        this.lives = new AtomicInteger(totalLives);
        if (isAdvancedStart) {
            this.showAdvancedStart();
        } else {
            this.view.showStartUpInterface();
        }
    }

    /**
     * Exits the game.
     */
    public void exit() {
        view.stopGame();
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
                if (maze.getGhostsLocation().get(ghostName).equals(new Coordinate(x, y))) {
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
            maze.ghostVisit(((GhostAgent) agent).getAgentName(), x, y);
            // If meet with a pacman
            for (Integer pacmanIndex: maze.getPacmanLocation().keySet()) {
                if (maze.getPacmanLocation().get(pacmanIndex).equals(new Coordinate(x, y))) {
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
        if (!isConnected() || isServerNode()) {
            if (maze.getFoodsNum() + maze.getPelletsNum() == 0) {
                this.gameOver(true);
                try {
                    server.broadcast(Tags.GAME_OVER, String.valueOf(true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * When the game over, ask the user to restart or close.
     *
     * @param win if the user wins
     */
    public void gameOver(boolean win) {
        this.view.stopMoving();
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
        if (!isConnected() || isServerNode()) {
            int scaredTime = settings.getInt("DefaultScaredTime", 10);
            for (String ghostName : maze.getVisibleGhostNames()) {
                maze.setGhostScared(ghostName, scaredTime);
                view.setGhostScared(ghostName, scaredTime);
                try {
                    server.broadcast(Tags.SCARE, ghostName, String.valueOf(scaredTime));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Logger.printlnf("Ignored pellets die event");
        }
    }

    /**
     * When pacman meets a normal ghost, the pacman dies.
     *
     * @param pacmanIndex the index of the pacman
     */
    private void pacmanDie(Integer pacmanIndex) {
        if (!isConnected() || isServerNode()) {
            if (lives.get() > 1) {
                lives.decrementAndGet();
                this.view.resetAgent(pacmanIndex);
                this.view.updateLives(lives.get());
                this.view.notification("Pacman died! It has " + lives.get() + " "
                    + "lives left");
                try {
                    server.broadcast(Tags.DIE, String.valueOf(pacmanIndex),
                        String.valueOf(lives.get()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    server.broadcast(Tags.GAME_OVER, String.valueOf(false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.gameOver(false);
            }
        } else {
            Logger.printlnf("Ignored pacman die event");
        }
    }

    /**
     * When pacman meets a scared ghost, the ghost dies.
     *
     * @param ghostName the name of the ghost
     */
    private void pacmanEat(String ghostName) {
        if (!isConnected() || isServerNode()) {
            view.resetAgent(ghostName);
            try {
                server.broadcast(Tags.EAT, ghostName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Logger.printlnf("Ignored eat event");
        }
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
        try {
            if (agent instanceof PacmanAgent) {
                if (!localhostSelectedAgentName.equals("pacman")) {
                    return;
                }
                this.server.broadcast(Tags.DIRECTION, d.name(),
                    String.valueOf(((PacmanAgent) agent).getIndex()),
                    String.valueOf(x), String.valueOf(y));
            } else {
                if (!localhostSelectedAgentName.equals(((GhostAgent) agent).getAgentName())) {
                    return;
                }
                this.server.broadcast(Tags.DIRECTION, d.name(),
                    ((GhostAgent) agent).getAgentName(),
                    String.valueOf(x), String.valueOf(y));
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
                if (!PacmanAgent.NAMES[0].equals(localhostSelectedAgentName)) {
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
                this.server.broadcast(Tags.LOCATION,
                    String.valueOf(((PacmanAgent) agent).getIndex()),
                    String.valueOf(x), String.valueOf(y));
            } else {
                this.server.broadcast(Tags.LOCATION, ((GhostAgent) agent).getAgentName(),
                    String.valueOf(x), String.valueOf(y));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * User selects a player to control in the game.
     *
     * @param agentName the name of the agent being selected
     * @param updateView if the view should be updated
     */
    public void userSelected(final String agentName, boolean updateView) {
        if (agentNameClientAddressMap.get(agentName) != null) {
            view.notification("Cannot select agent \"" + agentName + "\": someone "
                + "else is playing it.");
            view.unselectPlayer(agentName);
            return;
        }
        this.prevSelectedAgentName = localhostSelectedAgentName;
        this.localhostSelectedAgentName = agentName;
        if (updateView) {
            view.userSelected(agentName);
        }
    }

}
