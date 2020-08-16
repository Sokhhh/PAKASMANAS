package pacman.controller;

import golgui.components.GuiMessenger;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
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
import pacman.model.Maze;
import pacman.network.SimpleP2PServer;
import pacman.util.Logger;
import pacman.util.MazeFactory;
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
     * @return if the maze is loaded successfully
     */
    public boolean advancedLoad(final String preConfiguredMazeName,
                                final String filename) {
        boolean ret = this.load(preConfiguredMazeName, filename);
        if (ret) {
            view.setUpPlayerListPanel(maze);
        }
        return ret;
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
     * This method changes the port number of the local host and reset the server.
     *
     * @param port    the local port of the host
     * @param confirm if a confirm should be sent
     * @return if the server is reestablished
     */
    @Override
    public boolean changePort(String port, boolean confirm) {
        if (confirm && !this.view.ask("Are you sure you want to change the port to "
                + "\"" + port + "\"")) {
            return false;
        }
        try {
            this.server.closeAllConnection();
        } catch (IOException e) {
            this.view.alert("<html> Failed to change port due to system restriction. "
                    + "</html>");
            return false;
        }
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                    + "inclusive.");
            return false;
        }
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
            try {
                this.server.restartListening();
            } catch (IOException | SecurityException e) {
                this.view.alert("<html>Failure when continue listening for connections"
                        + ". <br> Please restart the application.</html>");
            }
            return false;
        }
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
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            this.view.alert("Port number should be a number between 1024 and 65535, "
                    + "inclusive.");
            return false;
        }
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
            this.server.connectTo(address, portNum);
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
    }

    /**
     * This method gets called when the remote side sends a message.
     *
     * @param message the message content
     */
    @Override
    public void receiveRemoteMessage(String message) {
        if (message.equals(SimpleP2PServer.Tags.CONFIRM_TAG)) {
            // Remote confirmed
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
        view.askForInput(1);
        isAdvancedStart = true;
        // Start the game
        String mazeName = settings.get("Maze", null);
        view.setTitle(mazeName);
        view.start(maze);

        // Add ghosts and pacman
        AlgorithmFactory algorithmFactory = new AlgorithmFactory(maze);

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
                this.maze.pacmanVisit(pacmanNum, pacman.get(pacmanNum).getX(),
                        pacman.get(pacmanNum).getY());
                this.view.addPacman(maze,
                        pacman.get(pacmanNum).getX(), pacman.get(pacmanNum).getY(), pacmanNum,
                        algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()),
                        isSelf);
                pacmanNum++;
            } else {
                this.maze.ghostVisit(GhostAgent.NAMES[ghostNum], ghosts.get(ghostNum).getX(),
                        ghosts.get(ghostNum).getY());
                this.view.addGhost(maze, ghosts.get(ghostNum).getX(), ghosts.get(ghostNum).getY(),
                        GhostAgent.NAMES[ghostNum],
                        algorithmFactory.createAlgorithm(selectionItem.getAlgorithmName()), isSelf);
                ghostNum++;
            }
        }
    }

    /**
     * Starts the game with advanced configurations.
     */
    public void showAdvancedStart() {
        view.askForInput(0);
        String mazeName = settings.get("Maze", null);
        String filename = settings.get("InputFile", null);
        try {
            server.startListening(settings.getInt("Port", 0));
        } catch (IOException | SecurityException e) {
            this.view.notification("Cannot ");
        }
        load(mazeName, filename);
        view.showAdvancedPanel();
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

        try (FileOutputStream out = new FileOutputStream("prefs.xml")) {
            settings.exportSubtree(out);
        } catch (IOException | SecurityException | BackingStoreException e) {
            this.view.alert("Save settings failed due to system restrictions.");
        }
        System.exit(0);
    }

    // ==================================================================================
    //                              GAME CONFIGURATION
    // ==================================================================================

    /**
     * Adds the pacman to the maze and to the view.
     */
    @Override
    public void addPacman() {
        List<Coordinate> pacman = Arrays.asList(maze.getPacmanStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(pacman, ThreadLocalRandom.current());
        this.maze.pacmanVisit(0, pacman.get(0).getX(),
                pacman.get(0).getY());
        this.view.addPacman(maze, pacman.get(0).getX(), pacman.get(0).getY(), 0,
                new GreedyAlgorithm(maze), true);
    }

    /**
     * Adds the ghosts to the maze and to the view.
     */
    @Override
    public void addGhosts() {
        List<Coordinate> ghosts = Arrays.asList(maze.getGhostsStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(ghosts, ThreadLocalRandom.current());
        for (int i = 0; i < Math.min(ghosts.size(), GhostAgent.NAMES.length); i++) {
            this.maze.ghostVisit(GhostAgent.NAMES[i], ghosts.get(i).getX(),
                ghosts.get(i).getY());
            this.view.addGhost(maze, ghosts.get(i).getX(), ghosts.get(i).getY(),
                GhostAgent.NAMES[i], new GreedyAlgorithm(maze), false);
        }
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

}
