package pacman.viewer;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import battleship.components.ComponentFactory;
import battleship.components.NetworkAddressPanel;
import battleship.components.NetworkStatusPanel;
import gol.viewer.UserInteraction;
import golgui.components.GuiComponentFactory;
import golgui.components.GuiMessenger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import golgui.utils.LocalFileOperator;
import pacman.algorithms.AbstractAlgorithm;
import pacman.agents.*;
import pacman.algorithms.NullAlgorithm;
import pacman.controller.FakeMazeController;
import pacman.controller.PacmanController;
import pacman.model.Coordinate;
import pacman.model.Maze;
import pacman.network.SimpleP2PServer;
import pacman.util.*;

public class GUIViewer extends JFrame implements UserInteraction {
    /** Contains the controller of the application. */
    private final PacmanController controller;

    /**
     * Contains a GUI utility that can be used to handle communication with the user.
     */
    private final GuiMessenger messenger;
    private JLabel scoreText;
    private MazePanel mazePanel;
    private JLabel lifeText;
    private HashMap<Integer, PacmanAgent> pacmanAgents;
    private HashMap<String, GhostAgent> ghostAgents;
    private KeyboardControlledAgent self;
    private Music backgroundMusic;
    private final Preferences settings;
    private JComboBox<String> levelChoicesComboBox;
    private JTextField customInputFileTextField;

    /** Contains a panel handling the local network address. */
    private NetworkAddressPanel localAddressPanel;

    /** Contains a panel handling the remote network address. */
    private NetworkAddressPanel remoteAddressPanel;

    /** Contains a panel showing the network connection status. */
    private NetworkStatusPanel networkStatusPanel;

    /**
     * Constructor.
     *
     * @param controller the controller of the application
     */
    public GUIViewer(PacmanController controller) {
        Preferences root = Preferences.userRoot();
        this.settings = root.node("/edu/rpi/csci4963/pacman");
        this.messenger = new GuiMessenger(this);
        this.controller = controller;
        this.pacmanAgents = new HashMap<>();
        this.ghostAgents = new HashMap<>();

        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            @Override
            public void windowClosing(WindowEvent event) {
                backgroundMusic.stopMusic();
                controller.exit();
            }
        });
    }

    /**
     * This function starts the GUI window.
     *
     * @requires None
     * @modifies None
     * @effects None
     */
    public void run() {
        this.setUpStartInterface();

        // set the frame
        this.setTitle("Pacman");

        // make the frame visible
        this.setLocationRelativeTo(null);

        this.pack();

        try {
            backgroundMusic = new Music(GUIViewer.class.getResource("/sounds" +
                    "/PacMan_Original_Theme.wav").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // arguments
        this.setVisible(true);
    }

    // ==================================================================================
    //                                  USER INTERACTION
    // ==================================================================================

    /**
     * This function shows an alert message to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail message string
     */
    @Override
    public void alert(String text) {
        this.messenger.alert(text);
    }

    /**
     * This function shows a message dialog to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail message string
     */
    @Override
    public void message(String text) {
        this.messenger.message(text);
    }

    /**
     * This function shows a notification message to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     * @param text the detail message string
     */
    public void notification(String text) {
        this.messenger.notification(text);
    }

    /**
     * This function shows a question to the user and wait for response.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail question string
     * @return user's response. {@code true} if the user chose yes and {@code false} if
     *      the user chose no.
     */
    @Override
    public boolean ask(String text) {
        return this.messenger.ask(text);
    }

    /**
     * This function shows a question to the user and wait for response.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail question string
     * @return user's response. {@code true} if the user chose OK and {@code false} if
     *      the user chose CANCEL.
     */
    @Override
    public boolean askOkCancel(String text) {
        return this.messenger.askOkCancel(text);
    }

    // ==================================================================================
    //                                      SETUP
    // ==================================================================================

    /**
     * Sets up the start interface, with buttons to start the game.
     * <code>
     *    +---------------------------------+   <  startupContentPane
     *    |           PACMAN                |   <  title
     *    |            /\ ......            |
     *    |            \/                   |
     *    |                                 |
     *    |         [START GAME]            |
     *    |         [MULTIPLAYER]           |
     *    |                                 |   <  lowerPartPanel
     *    |  Choose level: [...]  [Preview] |
     *    |  Custom: ____________           |
     *    +---------------------------------+
     * </code>
     */
    private void setUpStartInterface() {
        // Area for title and other information
        final JPanel startupContentPane = new JPanel(new BorderLayout());
        startupContentPane.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        startupContentPane.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("PACMAN", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Dialog", Font.BOLD, 52));
        startupContentPane.add(title, BorderLayout.NORTH);
        try {
            startupContentPane.add(new JLabel(ImageInterning.getImageIconFromFile("loading.gif",
                    null)), BorderLayout.CENTER);
        } catch (IOException e) {
            Logger.println("ERROR:" + e.getMessage());
        }

        // Lower part of the content pane

        // Area for buttons
        JButton startButton = new JButton("START GAME");
        startButton.setToolTipText("Quick start a game");
        PacmanViewFactory.addMouseHoveringEffectAtStart(startButton);
        /*
         *               Layout:
         *   +--------------------------------------------------lowerPartPanel--+
         *   | +-buttonsPanel-------------------------------------------------+ |
         *   | |                          startButton                         | |
         *   | |                       multiplayerButton                      | |
         *   | +--------------------------------------------------------------+ |
         *   |                                                                  |
         *   | +-levelChoicesPanel--+-----------------------+-----------------+ |
         *   | |  levelChoicesLabel | levelChoicesComboBox  |  previewButton  | |
         *   | +--------------------+-----------------------+-----------------+ |
         *   | | +--------------+-------------------------customLevelPanel--+ | |
         *   | | |  customLabel | customInputFileTextField  |  browseButton | | |
         *   | | +--------------+---------------------------+---------------+ | |
         *   | +--------------------------------------------------------------+ |
         *   +------------------------------------------------------------------+
         */
        startButton.addActionListener(e -> {
            controller.start();
        });

        // Enter key to start the game
        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);

        InputMap inputMap = startupContentPane.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyCodePressed, keyCodePressed.toString());

        ActionMap actionMap = startupContentPane.getActionMap();
        actionMap.put(keyCodePressed.toString(), new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                startButton.doClick();
            }
        });

        JButton multiplayerButton = new JButton("ADVANCED / MULTIPLAYER");
        multiplayerButton.setToolTipText("Customize a game");
        PacmanViewFactory.addMouseHoveringEffectAtStart(multiplayerButton);
        multiplayerButton.setFont(new Font("Dialog", Font.BOLD, 22));
        multiplayerButton.addActionListener(e -> {
            controller.advancedStart();
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonsPanel.add(startButton, BorderLayout.NORTH);
        buttonsPanel.add(multiplayerButton, BorderLayout.SOUTH);

        // Settings
        JPanel levelChoicesPanel = new JPanel(new BorderLayout(15, 5));
        levelChoicesPanel.setOpaque(false);
        JLabel levelChoicesLabel = new JLabel("Choose level: ", SwingConstants.LEFT);
        levelChoicesLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        levelChoicesLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        levelChoicesPanel.add(levelChoicesLabel, BorderLayout.WEST);

        levelChoicesComboBox = GuiComponentFactory.createComboBox(PacmanTheme.BUTTON_HOVERED,
                PacmanTheme.WELCOME_BACKGROUND, PacmanTheme.WELCOME_TEXT);
        for (String mazeName: MazeFactory.PreConfiguredMaze.ITEMS.keySet()) {
            levelChoicesComboBox.addItem(mazeName);
        }
        levelChoicesComboBox.addItem(MazeFactory.CUSTOM_MAZE_NAME);
        levelChoicesComboBox.setFont(new Font("SansSerif", Font.PLAIN, 18));
        levelChoicesComboBox.setSelectedItem(settings.get("Maze",
                MazeFactory.PreConfiguredMaze.SMALL_CLASSIC_NAME));
        levelChoicesPanel.add(levelChoicesComboBox, BorderLayout.CENTER);
        JButton previewButton = new JButton(" PREVIEW ");
        PacmanViewFactory.addMouseHoveringEffectAtStart(previewButton);
        previewButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
        previewButton.addActionListener(e -> {
            if (controller.load((String) levelChoicesComboBox.getSelectedItem(),
                    customInputFileTextField.getText())) {
                controller.preview();
            }
        });
        levelChoicesPanel.add(previewButton, BorderLayout.EAST);
        JLabel customLabel = new JLabel("Custom input file:");
        customLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        customLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        customInputFileTextField = new JTextField(settings.get("InputFile", ""));
        customInputFileTextField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        customInputFileTextField.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        customInputFileTextField.setForeground(PacmanTheme.WELCOME_TEXT);
        customInputFileTextField.setBorder(BorderFactory.createLineBorder(
                PacmanTheme.WELCOME_TEXT));
        customLabel.setLabelFor(customInputFileTextField);
        JButton browseButton = new JButton(" Browse ");
        PacmanViewFactory.addMouseHoveringEffectAtStart(browseButton);
        browseButton.addActionListener(new ActionListener() {
            /**
             * {@inheritDoc} Invoked when a mouse button
             * has been pressed on a component.
             *
             * @param e the mouse event
             * @requires None
             * @modifies None
             * @effects creates a dialog to confirm
             *     the save/load action
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                File input = new LocalFileOperator(customInputFileTextField.getText())
                        .browseInputFile(GUIViewer.this,
                                new FileNameExtensionFilter("CSV "
                                + "Format (*.csv)", "csv"));
                if (input == null) {
                    return;
                }
                Logger.println(
                        "Attempt to read from input file " + customInputFileTextField.getText());
            }
        });
        browseButton.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JPanel customLevelPanel = new JPanel(new BorderLayout(2, 0));
        customLevelPanel.setOpaque(false);
        customLevelPanel.add(customLabel, BorderLayout.WEST);
        customLevelPanel.add(customInputFileTextField, BorderLayout.CENTER);
        customLevelPanel.add(browseButton, BorderLayout.EAST);
        customLevelPanel.setVisible(levelChoicesComboBox.getSelectedItem().equals(MazeFactory.CUSTOM_MAZE_NAME));

        levelChoicesComboBox.addActionListener(e -> {
            if (levelChoicesComboBox.getSelectedItem().equals(MazeFactory.CUSTOM_MAZE_NAME)) {
                customLevelPanel.setVisible(true);
            } else {
                customLevelPanel.setVisible(false);
            }
        });

        levelChoicesPanel.add(customLevelPanel, BorderLayout.SOUTH);

        JPanel lowerPartPanel = new JPanel(new BorderLayout(2, 7));
        lowerPartPanel.setOpaque(false);
        lowerPartPanel.add(buttonsPanel, BorderLayout.NORTH);
        lowerPartPanel.add(levelChoicesPanel, BorderLayout.SOUTH);

        startupContentPane.add(lowerPartPanel, BorderLayout.SOUTH);

        this.setContentPane(startupContentPane);
        this.repaint();
        this.revalidate();
        this.pack();
        this.setLocationRelativeTo(null);
    }

    /**
     * Creates the network selector interface.
     */
    public void setUpAdvancedStart(Maze maze) {
        JPanel networkSelectorPanel = new JPanel(new BorderLayout());
        networkSelectorPanel.setBackground(PacmanTheme.WELCOME_BACKGROUND);

        // Title
        JPanel networkPanel = new JPanel();
        networkPanel.setOpaque(false);
        networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));

        // Local address
        JLabel label = ComponentFactory.createLabel(
                "Your local address is:", true, 20);
        label.setForeground(PacmanTheme.WELCOME_TEXT);

        final Box localLabel = (Box) ComponentFactory.wrapInLeftAlign(label);
        JLabel troubleShoot = ComponentFactory.createLabel("?", false, 18);
        troubleShoot.setForeground(PacmanTheme.WELCOME_TEXT);
        troubleShoot.setToolTipText("The address is incorrect? Check troubleshoot.");
        troubleShoot.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse button has been clicked (pressed
             * and released) on a component.
             *
             * @param e the mouse event
             */
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (askOkCancel(PacmanViewFactory.makeHTML(SimpleP2PServer.LOCAL_IP_TROUBLESHOOT_MSG))) {
                    JOptionPane.showConfirmDialog(null,
                            PacmanViewFactory.makeHTML(SimpleP2PServer.getSystemIPConfig()),
                            "Troubleshoot", JOptionPane.DEFAULT_OPTION);
                }
            }
        });
        localLabel.add(troubleShoot);

        networkPanel.add(localLabel);
        this.localAddressPanel = new NetworkAddressPanel(false);
        this.localAddressPanel.setForeground(PacmanTheme.WELCOME_TEXT);
        this.localAddressPanel.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        this.localAddressPanel.setApplyBtnText("Change port");
        this.localAddressPanel.setApplyBtnAction(e -> {
            if (!controller.changePort(localAddressPanel.getPort(), true)) {
                localAddressPanel.setPort(controller.getLocalPort());
            }
        });
        this.localAddressPanel.setPort(controller.getLocalPort());
        networkPanel.add(this.localAddressPanel);

        // Remote address
        networkPanel.add(Box.createVerticalStrut(5));
        JLabel joinOthersLabel = ComponentFactory.createLabel(
                "Or you can connect to others:", true, 20);
        joinOthersLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        networkPanel.add(ComponentFactory.wrapInLeftAlign(joinOthersLabel));
        this.remoteAddressPanel = new NetworkAddressPanel(true);
        this.remoteAddressPanel.setForeground(PacmanTheme.WELCOME_TEXT);
        this.remoteAddressPanel.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        this.remoteAddressPanel.setApplyBtnAction(e -> connectTo());

        // Default address
        try {
            this.localAddressPanel.setAddress(SimpleP2PServer.getLocalIP());
            this.remoteAddressPanel.setAddress(SimpleP2PServer.getLocalIP());
        } catch (IOException ignored) {
            // Ignored
        }

        networkPanel.add(this.remoteAddressPanel);

        // Connection state
        this.networkStatusPanel = new NetworkStatusPanel();
        this.networkStatusPanel.setDisconnectButtonAction(e -> controller.hostCloseConnection());
        this.networkStatusPanel.setForeground(PacmanTheme.WELCOME_TEXT);
        networkPanel.add(this.networkStatusPanel);

        networkSelectorPanel.add(networkPanel, BorderLayout.SOUTH);
        this.setContentPane(networkSelectorPanel);
        this.repaint();
        this.revalidate();
        this.pack();
        this.setLocationRelativeTo(null);
    }

    /**
     * Starts the game.
     *
     * @param maze the maze of the game
     */
    public void start(final Maze maze) {
        // Music
        backgroundMusic.startMusic();

        // Interface
        final JPanel gamePanel = new JPanel(new BorderLayout());

        // Status at the bottom
        final List<JComponent> statsPanelsList = new LinkedList<>();
        JPanel scorePanel = new JPanel(new BorderLayout());
        JLabel scoreLabel = new JLabel("SCORES", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif",  Font.BOLD, 27));
        scorePanel.add(scoreLabel, BorderLayout.NORTH);
        scoreText = new JLabel("0", SwingConstants.CENTER);
        scoreText.setFont(new Font("Monospaced",  Font.BOLD, 24));
        scorePanel.add(scoreText, BorderLayout.CENTER);
        statsPanelsList.add(scorePanel);

        JPanel lifePanel = new JPanel(new BorderLayout());
        try {
            lifePanel.add(new JLabel(ImageInterning.getImageIconFromFile("pacman_right.gif",
                    new Dimension(55, 55))), BorderLayout.WEST);
        } catch (IOException e) {
            JLabel lifeLabel = new JLabel("Life", SwingConstants.LEFT);
            lifeLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
            lifePanel.add(lifeLabel, BorderLayout.WEST);
        }
        lifeText = new JLabel("x3", SwingConstants.LEFT);
        lifeText.setFont(new Font("SansSerif", Font.BOLD, 40));
        lifePanel.add(lifeText, BorderLayout.CENTER);
        statsPanelsList.add(lifePanel);

        JButton musicButton = new JButton("Music");
        PacmanViewFactory.addMouseHoveringEffectAtGame(musicButton);
        musicButton.addActionListener(e -> {
            if (backgroundMusic.isStop()) {
                backgroundMusic.startMusic();
            } else {
                backgroundMusic.stopMusic();
            }
        });
        statsPanelsList.add(musicButton);

        JButton takeOverButton = new JButton("Start/stop AI takeover");
        PacmanViewFactory.addMouseHoveringEffectAtGame(takeOverButton);
        takeOverButton.addActionListener(e -> {
            if (self.aiTakeOver()) {
                notification("AI is now moving your player. Relax.");
            } else {
                notification("You will need to control the player with arrow keys.");
            }
        });
        statsPanelsList.add(takeOverButton);

        JButton closeGameButton = new JButton("Back to main menu");
        PacmanViewFactory.addMouseHoveringEffectAtGame(closeGameButton);
        closeGameButton.addActionListener(e -> {
            if (ask("Are you sure you want to exit?")) {
                controller.backToMainMenu();
            }
        });
        statsPanelsList.add(closeGameButton);

        JPanel statusPanel = new JPanel(new GridLayout(1, statsPanelsList.size(),
                2, 0));
        for (JComponent comp: statsPanelsList) {
            statusPanel.add(comp);
        }
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 15,  15,15));
        gamePanel.add(statusPanel, BorderLayout.SOUTH);

        // Game at the center
        mazePanel = new MazePanel(maze);
        gamePanel.add(mazePanel, BorderLayout.CENTER);

        this.setContentPane(gamePanel);
        this.repaint();
        this.revalidate();
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void addPacman(final Maze maze, final int x, final int y, int index,
                   AbstractAlgorithm algorithm, boolean isSelf) {
        PacmanAgent agent;
        if (isSelf) {
            agent = new ControlledPacmanAgent(controller, maze, x, y, index, algorithm);
            self = (ControlledPacmanAgent) agent;
        } else {
            agent = new PacmanAgent(controller, maze, x, y, index, algorithm);
        }
        mazePanel.addAgent(agent, true);
        pacmanAgents.put(index, agent);
    }

    public void addGhost(final Maze maze, final int x, final int y, String name,
                         AbstractAlgorithm algorithm, boolean isSelf) {
        GhostAgent agent = new GhostAgent(controller, maze, x, y, name, algorithm);
        mazePanel.addAgent(agent, true);
        ghostAgents.put(name, agent);
    }

    // ==================================================================================
    //                                     GAMEPLAY
    // ==================================================================================

    /**
     * Gets the users' choice on the input files.
     */
    public void askForInput() {
        settings.put("Maze", String.valueOf(levelChoicesComboBox.getSelectedItem()));
        settings.put("InputFile", customInputFileTextField.getText());
    }

    /**
     * Preview the maze.
     *
     * @param maze the maze that is about to be previewed.
     */
    public void preview(Maze maze) {
        MazePanel mazePanel = new MazePanel(maze);
        int i = 0;
        for (Coordinate coordinate: maze.getPacmanStartLocation()) {
            mazePanel.addAgent(new ControlledPacmanAgent(new FakeMazeController(),
                    maze, coordinate.getX(), coordinate.getY(), i,
                    new NullAlgorithm(maze)), false);
            i++;
        }
        i = 0;
        for (Coordinate coordinate: maze.getGhostsStartLocation()) {
            mazePanel.addAgent(new GhostAgent(new FakeMazeController(),
                    maze, coordinate.getX(), coordinate.getY(), "red",
                    new NullAlgorithm(maze)), false);
            i++;
        }
        JButton closeBtn = new JButton("CLOSE");
        PacmanViewFactory.addMouseHoveringEffectAtGame(closeBtn);
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.add(mazePanel, BorderLayout.CENTER);
        closeBtn.addActionListener(e -> frame.setVisible(false));
        frame.add(closeBtn, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
        frame.pack();
        frame.setResizable(false);
        frame.setTitle("Preview maze");
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Stops the game.
     */
    public void stopGame() {
        for (PacmanAgent agent: pacmanAgents.values()) {
            agent.stop();
        }
        pacmanAgents.clear();
        for (GhostAgent agent: ghostAgents.values()) {
            agent.stop();
        }
        ghostAgents.clear();
        setUpStartInterface();
    }

    /**
     * Updates the score panel.
     *
     * @param scoresTotal the total score earned
     * @param scoresDiff the score earned in last step
     */
    public void updateScore(final int scoresTotal, final int scoresDiff) {
        if (scoresDiff > 0) {
            this.scoreText.setText(scoresTotal + " (+" + scoresDiff + ")");
        } else if (scoresDiff < 0) {
            this.scoreText.setText(scoresTotal + " (" + scoresDiff + ")");
        }
    }

    /**
     * Update the lives panel.
     *
     * @param lives current number of lives remaining
     */
    public void updateLives(final int lives) {
        this.lifeText.setText("x" + lives);
    }


    public void gameOver(boolean win) {
        for (AbstractAgent agent: ghostAgents.values()) {
            agent.stop();
        }
        for (AbstractAgent agent: pacmanAgents.values()) {
            agent.stop();
        }
        backgroundMusic.stopMusic();
    }

    /**
     * This method gets called when the any side is lost.
     *
     * @param isLocalSideWin {@code true} if the remote side is lost and {@code false}
     *                               otherwise. {@code null} if draw
     * @return the user's choice.
     */
    public int showResultDialog(Boolean isLocalSideWin) {
        Object[] options = {"Restart the game", "Disconnect", "Close the game"};
        String message;
        if (isLocalSideWin == null) {
            message = "DRAW";
        } else if (isLocalSideWin) {
            message = "YOU WIN!";
        } else {
            message = "YOU LOST...";
        }
        return JOptionPane.showOptionDialog(this,
            GuiComponentFactory
                .wrapInEquallyDividedPanel(GuiComponentFactory.createLabel(message,
                    true, 44)),
            "",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     //do not use a custom Icon
            options,  //the titles of buttons
            options[0]);
    }

    /**
     * Resets the location of a pacman agent.
     *
     * @param pacmanIndex the index of pacman
     */
    public void resetAgent(Integer pacmanIndex) {
        this.pacmanAgents.get(pacmanIndex).reset();
    }

    /**
     * Resets the location of a ghost agent.
     *
     * @param ghostName the name of ghost
     */
    public void resetAgent(String ghostName) {
        this.ghostAgents.get(ghostName).reset();
    }

    public void setGhostScared(String ghostName, int scaredTime) {
        this.ghostAgents.get(ghostName).setScared(scaredTime);
    }

    // ==================================================================================
    //                                   NETWORK
    // ==================================================================================

    /**
     * This method gets called once an incoming connection is sent to the host.
     *
     * @param remoteSocketAddress the address of the remote side
     * @param port the port of the remote side
     */
    public void incomingConnection(final SocketAddress remoteSocketAddress, int port) {
        this.notification("Connection established");
        String address = remoteSocketAddress.toString().split(":")[0];
        if (address.contains("/")) {
            address = address.split("/")[1];
        }
        this.remoteAddressPanel.setAddress(address);
        this.localAddressPanel.setApplyBtnEnabled(false);
        this.remoteAddressPanel.setApplyBtnEnabled(false);
        this.networkStatusPanel.connected();
    }

    /**
     * This function let the server connect to a remote host on its own initiative.
     */
    public void connectTo() {
        boolean result = this.controller.connectTo(this.remoteAddressPanel.getAddress(),
                this.remoteAddressPanel.getPort(), false);
        if (result) {
            this.localAddressPanel.setApplyBtnEnabled(false);
            this.remoteAddressPanel.setApplyBtnEnabled(false);
            this.networkStatusPanel.connected();
        }
    }

    /**
     * This function gets called when the controller connects to others and want to
     * update the viewer. Normally for testing purpose.
     *
     * @param address the address of the remote host
     * @param port the port of the remote host
     */
    public void updateConnectTo(String address, int port) {
        this.remoteAddressPanel.setAddress(address);
        this.remoteAddressPanel.setPort(port);
        this.localAddressPanel.setApplyBtnEnabled(false);
        this.remoteAddressPanel.setApplyBtnEnabled(false);
        this.networkStatusPanel.connected();
    }

    /**
     * This method gets called when the remote side closes the connection.
     */
    public void remoteConnectionClosed() {
        this.localAddressPanel.setApplyBtnEnabled(true);
        this.remoteAddressPanel.setApplyBtnEnabled(true);
        this.networkStatusPanel.waitForConnection();
    }
}
