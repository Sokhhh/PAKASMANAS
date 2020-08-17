package pacman.viewer;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import battleship.components.ComponentFactory;
import battleship.components.NetworkAddressPanel;
import battleship.components.NetworkStatusPanel;
import gol.viewer.UserInteraction;
import golgui.components.GuiComponentFactory;
import golgui.components.GuiMessenger;
import golgui.utils.LocalFileOperator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import pacman.agents.AbstractAgent;
import pacman.agents.ControlledGhostAgent;
import pacman.agents.ControlledPacmanAgent;
import pacman.agents.GhostAgent;
import pacman.agents.KeyboardControlledAgent;
import pacman.agents.MazePanel;
import pacman.agents.PacmanAgent;
import pacman.agents.UserControlledGhostAgent;
import pacman.agents.UserControlledPacmanAgent;
import pacman.algorithms.AbstractAlgorithm;
import pacman.algorithms.NullAlgorithm;
import pacman.controller.FakeMazeController;
import pacman.controller.PacmanController;
import pacman.model.Coordinate;
import pacman.model.Direction;
import pacman.model.Maze;
import pacman.network.SimpleP2PServer;
import pacman.util.ImageInterning;
import pacman.util.Logger;
import pacman.util.MazeFactory;
import pacman.util.Music;
import pacman.util.PacmanTheme;
import pacman.util.StringUtilities;

/**
 * This is an implementation of the program viewer in a GUI window.
 *
 * @version 1.0
 */
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
    private List<JComboBox<String>> levelChoicesComboBoxs;
    private List<JTextField> customInputFileTextFields;

    /** Contains a panel handling the local network address. */
    private NetworkAddressPanel localAddressPanel;

    /** Contains a panel handling the remote network address. */
    private NetworkAddressPanel remoteAddressPanel;

    /** Contains a panel showing the network connection status. */
    private NetworkStatusPanel networkStatusPanel;
    private JPanel startupContentPane;
    private JPanel networkSelectorContentPane;
    private JPanel clientListPanel;
    private JPanel playerPanel;
    private JPanel levelPanel;
    private List<AgentItemPanel> players;
    private ButtonGroup advancedPlayerSelectionGroup;
    private Map<JRadioButton, String> advancedPlayerSelectionName;
    /**
     * Contains a list of all choices of agents available in the maze.
     */
    private List<AgentItemPanel> agentItemPanels;
    private JLabel selectPlayerLabel;

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
        this.advancedPlayerSelectionName = new HashMap<>();
        this.agentItemPanels = new ArrayList<>();

        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            @Override
            public void windowClosing(WindowEvent event) {
                backgroundMusic.stop();
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
        levelChoicesComboBoxs = new ArrayList<>();
        customInputFileTextFields = new ArrayList<>();

        this.setUpStartInterface();
        this.setUpAdvancedStart();

        // set the frame
        this.setTitle("Pacman");

        // make the frame visible
        this.setLocationRelativeTo(null);

        this.pack();

        try {
            backgroundMusic = new Music(GUIViewer.class.getResource("/sounds"
                    + "/PacMan_Original_Theme.wav").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // arguments
        this.setVisible(true);
        this.showStartUpInterface();
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
        startupContentPane = new JPanel(new BorderLayout());
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
        PacmanViewUtility.addMouseHoveringEffectAtStart(startButton);
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
            askForInput(0);
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
        PacmanViewUtility.addMouseHoveringEffectAtStart(multiplayerButton);
        multiplayerButton.setFont(new Font("Dialog", Font.BOLD, 22));
        multiplayerButton.addActionListener(e -> {
            controller.showAdvancedStart();
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonsPanel.add(startButton, BorderLayout.NORTH);
        buttonsPanel.add(multiplayerButton, BorderLayout.SOUTH);

        // Settings
        JPanel levelChoicesPanel = createLevelChoicesPanel();

        JPanel lowerPartPanel = new JPanel(new BorderLayout(2, 7));
        lowerPartPanel.setOpaque(false);
        lowerPartPanel.add(buttonsPanel, BorderLayout.NORTH);
        lowerPartPanel.add(levelChoicesPanel, BorderLayout.SOUTH);

        startupContentPane.add(lowerPartPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a panel to select level and maze.
     *
     * @return the panel to select level and maze
     */
    private JPanel createLevelChoicesPanel() {
        JPanel levelChoicesPanel = new JPanel(new BorderLayout(15, 5));
        levelChoicesPanel.setOpaque(false);
        JLabel levelChoicesLabel = new JLabel("Choose level: ", SwingConstants.LEFT);
        levelChoicesLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        levelChoicesLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        levelChoicesPanel.add(levelChoicesLabel, BorderLayout.WEST);

        JComboBox<String> levelChoicesComboBox =
                GuiComponentFactory.createComboBox(PacmanTheme.BUTTON_HOVERED,
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
        PacmanViewUtility.addMouseHoveringEffectAtStart(previewButton);
        previewButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
        levelChoicesPanel.add(previewButton, BorderLayout.EAST);
        JLabel customLabel = new JLabel("Custom input file:");
        customLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        customLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JTextField customInputFileTextField = new JTextField(settings.get(
                "InputFile", ""));
        customInputFileTextField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        customInputFileTextField.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        customInputFileTextField.setForeground(PacmanTheme.WELCOME_TEXT);
        customInputFileTextField.setBorder(BorderFactory.createLineBorder(
                PacmanTheme.WELCOME_TEXT));
        customLabel.setLabelFor(customInputFileTextField);
        JButton browseButton = new JButton(" Browse ");
        PacmanViewUtility.addMouseHoveringEffectAtStart(browseButton);
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

        previewButton.addActionListener(e -> {
            if (controller.load((String) levelChoicesComboBox.getSelectedItem(),
                    customInputFileTextField.getText())) {
                controller.preview();
            }
        });

        JPanel customLevelInnerPanel = new JPanel(new BorderLayout(2, 0));
        customLevelInnerPanel.setOpaque(false);
        customLevelInnerPanel.add(customLabel, BorderLayout.WEST);
        customLevelInnerPanel.add(customInputFileTextField, BorderLayout.CENTER);
        customLevelInnerPanel.add(browseButton, BorderLayout.EAST);
        HideablePanel customLevelPanel = new HideablePanel(customLevelInnerPanel);
        customLevelPanel.setHide(levelChoicesComboBox.getSelectedItem()
                .equals(MazeFactory.CUSTOM_MAZE_NAME));

        // Set combobox actions
        levelChoicesComboBox.addActionListener(e -> {
            if (levelChoicesComboBox.getSelectedItem().equals(MazeFactory.CUSTOM_MAZE_NAME)) {
                customLevelPanel.setHide(true);
            } else {
                customLevelPanel.setHide(false);
            }
            for (JComboBox<String> comboBox: levelChoicesComboBoxs) {
                if (comboBox != levelChoicesComboBox) {
                    comboBox.setSelectedItem(levelChoicesComboBox.getSelectedItem());
                }
            }
        });
        levelChoicesPanel.add(customLevelPanel, BorderLayout.SOUTH);

        levelChoicesComboBoxs.add(levelChoicesComboBox);
        customInputFileTextFields.add(customInputFileTextField);
        return levelChoicesPanel;
    }

    /**
     * Creates the network selector interface.
     */
    private void setUpAdvancedStart() {
        networkSelectorContentPane = new JPanel(new BorderLayout(5, 10));
        networkSelectorContentPane.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        networkSelectorContentPane.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JPanel networkPanel = new JPanel();
        networkPanel.setOpaque(false);
        networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));
        networkPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PacmanTheme.WELCOME_TEXT, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel networkSettingsLabel = ComponentFactory.createLabel(
                "Network settings:", true, 20);
        networkSettingsLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        networkPanel.add(GuiComponentFactory.wrapInEquallyDividedPanel(networkSettingsLabel));
        networkPanel.add(new JSeparator());

        // Local address
        JLabel serverLabel = ComponentFactory.createLabel(
                "Server is closed.", true, 20);
        serverLabel.setForeground(PacmanTheme.WELCOME_TEXT);

        final Box localLabel = (Box) ComponentFactory.wrapInLeftAlign(serverLabel);
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
                if (askOkCancel(StringUtilities.makeHTML(
                        SimpleP2PServer.LOCAL_IP_TROUBLESHOOT_MSG))) {
                    JOptionPane.showConfirmDialog(null,
                            StringUtilities.makeHTML(SimpleP2PServer.getSystemIPConfig()),
                            "Troubleshoot", JOptionPane.DEFAULT_OPTION);
                }
            }
        });
        localLabel.add(troubleShoot);

        networkPanel.add(localLabel);
        this.localAddressPanel = new NetworkAddressPanel(false);
        this.localAddressPanel.setForeground(PacmanTheme.WELCOME_TEXT);
        this.localAddressPanel.setBackground(PacmanTheme.WELCOME_BACKGROUND);
        this.localAddressPanel.setApplyBtnText("Start/stop server");
        this.localAddressPanel.setToolTipText(StringUtilities.makeHTML("Start the server with "
            + "specified port number, <br> input 0 if you want a random port."));
        this.localAddressPanel.setApplyBtnAction(e -> {
            if (controller.isServerStarted()) {
                controller.closeServer();
                serverLabel.setText("Server is closed.");
            } else {
                if (controller.changePort(localAddressPanel.getPort(), true)) {
                    localAddressPanel.setPort(controller.getLocalPort());
                    serverLabel.setText("Listening for connections on:");
                }
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
        this.remoteAddressPanel.setApplyBtnAction(e -> {
            if (controller.getClientList().isEmpty()
                    || ask("Joining others will disconnect with all existing connections."
                    + "Are you sure?")) {
                connectTo();
                serverLabel.setText("Server is closed.");
            }
        });
        this.remoteAddressPanel.setApplyBtnText("Join");

        networkPanel.add(this.remoteAddressPanel);

        // Connection state
        this.networkStatusPanel = new NetworkStatusPanel();
        this.networkStatusPanel.setDisconnectButtonAction(e -> controller.hostCloseConnection());
        this.networkStatusPanel.setForeground(PacmanTheme.WELCOME_TEXT);
        // networkPanel.add(this.networkStatusPanel);

        networkPanel.add(new JSeparator());
        networkPanel.add(Box.createVerticalStrut(5));
        JLabel clientListLabel = ComponentFactory.createLabel(
                "List of connected clients:", true, 20);
        clientListLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        networkPanel.add(GuiComponentFactory.wrapInEquallyDividedPanel(clientListLabel));
        networkPanel.add(new JSeparator());

        clientListPanel = new JPanel();
        clientListPanel.setOpaque(false);
        clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.Y_AXIS));

        updateClientListPanel();

        networkPanel.add(clientListPanel);
        networkPanel.add(Box.createVerticalStrut(15));

        networkSelectorContentPane.add(networkPanel, BorderLayout.NORTH);

        // Levels
        levelPanel = new JPanel();
        levelPanel.setOpaque(false);
        levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.Y_AXIS));
        levelPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PacmanTheme.WELCOME_TEXT, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        levelPanel.add(createLevelChoicesPanel());

        JButton loadMapButton = new JButton("  Load map  ");
        PacmanViewUtility.addMouseHoveringEffectAtStart(loadMapButton);
        loadMapButton.addActionListener(e -> {
            if (controller.isClientNode()) {
                notification("You are not the host. You cannot change map settings.");
                return;
            }
            controller.advancedLoad((String) levelChoicesComboBoxs.get(1).getSelectedItem(),
                    customInputFileTextFields.get(1).getText());
        });

        levelPanel.add(GuiComponentFactory.wrapInPanel(loadMapButton));
        levelPanel.add(Box.createVerticalStrut(15));
        levelPanel.add(new JSeparator());
        levelPanel.add(Box.createVerticalStrut(5));
        selectPlayerLabel = ComponentFactory.createLabel(
                "Select player:", true, 20);
        selectPlayerLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        levelPanel.add(GuiComponentFactory.wrapInEquallyDividedPanel(selectPlayerLabel));

        levelPanel.add(new JSeparator());
        levelPanel.add(Box.createVerticalStrut(5));

        JPanel playerSelectionTitlePanel = new JPanel(new GridLayout(1, 3));
        playerSelectionTitlePanel.setOpaque(false);
        JLabel label1 = ComponentFactory.createLabel("Select icon", false, 16);
        label1.setForeground(PacmanTheme.WELCOME_TEXT);
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        playerSelectionTitlePanel.add(label1);
        JLabel label2 = ComponentFactory.createLabel(StringUtilities.makeHTML("Select "
                + "what algorithm will this <br> player use if AI takes over"), false, 16);
        label2.setForeground(PacmanTheme.WELCOME_TEXT);
        label2.setHorizontalAlignment(SwingConstants.CENTER);
        playerSelectionTitlePanel.add(label2);
        JLabel label3 = ComponentFactory.createLabel(StringUtilities.makeHTML("Will you "
                + "control <br> this player?"), false, 16);
        label3.setForeground(PacmanTheme.WELCOME_TEXT);
        label3.setHorizontalAlignment(SwingConstants.CENTER);
        playerSelectionTitlePanel.add(label3);
        levelPanel.add(playerSelectionTitlePanel);
        levelPanel.add(Box.createVerticalStrut(5));
        levelPanel.add(new JSeparator());
        levelPanel.add(Box.createVerticalStrut(5));

        playerPanel = new JPanel();
        playerPanel.setOpaque(false);
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));

        players = new ArrayList<>();
        setUpPlayerListPanel(null, null);

        levelPanel.add(playerPanel);
        levelPanel.add(Box.createVerticalStrut(15));
        JButton startGameButton = new JButton("Start Game");
        PacmanViewUtility.addMouseHoveringEffectAtStart(startGameButton);
        startGameButton.addActionListener(e -> {
            if (controller.isClientNode()) {
                notification("Only the host can start the game");
                return;
            }
            String selectedName = getSelectedName();
            controller.advancedStart(selectedName, agentItemPanels);
        });
        JButton backToMainMenuButton = new JButton("Back to main menu");
        PacmanViewUtility.addMouseHoveringEffectAtStart(backToMainMenuButton);
        backToMainMenuButton.addActionListener(e -> {
            controller.hostCloseConnection();
            showStartUpInterface();
        });

        networkSelectorContentPane.add(levelPanel, BorderLayout.CENTER);
        networkSelectorContentPane.add(GuiComponentFactory.wrapInEquallyDividedPanel(
                startGameButton, backToMainMenuButton), BorderLayout.SOUTH);
    }

    /**
     * Adds the list of client to the panel showing the clients.
     */
    private void updateClientListPanel() {
        clientListPanel.removeAll();
        Set<SocketAddress> clientAddresses = controller.getClientList();
        if (clientAddresses.isEmpty()) {
            JLabel clientLabel = ComponentFactory.createLabel(
                    "No connected client", false, 20);
            clientLabel.setHorizontalAlignment(SwingConstants.CENTER);
            clientLabel.setForeground(PacmanTheme.WELCOME_TEXT);
            clientListPanel.add(GuiComponentFactory.wrapInEquallyDividedPanel(clientLabel));
        } else {
            for (SocketAddress address : clientAddresses) {
                JLabel clientLabel = ComponentFactory.createLabel(
                        address.toString(), false, 20);
                clientLabel.setForeground(PacmanTheme.WELCOME_TEXT);
                clientListPanel.add(GuiComponentFactory.wrapInEquallyDividedPanel(clientLabel));
            }
        }
        clientListPanel.revalidate();
        clientListPanel.repaint();
    }

    /**
     * Sets up the panel that can be used to choose which agent to be controlled
     * in the game.
     *
     * @param mazeName the name of the maze
     * @param maze the the maze of the game
     */
    public void setUpPlayerListPanel(String mazeName, Maze maze) {
        players.clear();
        advancedPlayerSelectionName.clear();
        agentItemPanels.clear();
        playerPanel.removeAll();

        advancedPlayerSelectionGroup = new ButtonGroup();
        if (maze != null) {
            levelChoicesComboBoxs.get(1).setSelectedItem(mazeName);
            selectPlayerLabel.setText("Select player for \"" + mazeName + "\":");
            for (int i = 0; i < Math.min(maze.getPacmanStartLocation().length,
                PacmanAgent.NAMES.length); i++) {
                JRadioButton selectAgentButton = new JRadioButton();
                advancedPlayerSelectionGroup.add(selectAgentButton);
                advancedPlayerSelectionName.put(selectAgentButton,
                    PacmanAgent.NAMES[i]);
                AgentItemPanel agentPanel =
                    new AgentItemPanel(PacmanAgent.NAMES[i], selectAgentButton);
                agentPanel.addComboBoxActionListener(e -> {
                    if (controller.isClientNode()) {
                        notification(StringUtilities.makeHTML("Please setting the algorithm "
                            + "at the non-host side has no effect."));
                    }
                });
                playerPanel.add(agentPanel);
                agentItemPanels.add(agentPanel);
            }
            for (int i = 0; i < Math.min(maze.getGhostsStartLocation().length,
                GhostAgent.NAMES.length); i++) {
                JRadioButton selectAgentButton = new JRadioButton();
                advancedPlayerSelectionGroup.add(selectAgentButton);
                advancedPlayerSelectionName.put(selectAgentButton, GhostAgent.NAMES[i]);
                AgentItemPanel agentPanel =
                    new AgentItemPanel(GhostAgent.NAMES[i], selectAgentButton);
                agentPanel.addComboBoxActionListener(e -> {
                    if (controller.isClientNode()) {
                        notification(StringUtilities.makeHTML("Please setting the algorithm "
                            + "at the non-host side has no effect"));
                    }
                });
                playerPanel.add(agentPanel);
                agentItemPanels.add(agentPanel);
            }
        } else {
            selectPlayerLabel.setText("Select player:");
            JLabel selectPlayerLabel = ComponentFactory.createLabel(
                "You haven't load a map", false, 20);
            selectPlayerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            selectPlayerLabel.setForeground(PacmanTheme.WELCOME_TEXT);
            playerPanel
                .add(GuiComponentFactory.wrapInEquallyDividedPanel(selectPlayerLabel));
        }

        for (AgentItemPanel agentPanel: agentItemPanels) {
            agentPanel.addCheckBoxActionListener(e -> {
                Logger.printlnf("Selected \"%s\"", agentPanel.getAgentName());
                controller.userSelected(agentPanel.getAgentName());
                controller.sendAgentSelection(agentPanel.getAgentName());
            });
        }

        playerPanel.revalidate();
        playerPanel.repaint();
        levelPanel.revalidate();
        levelPanel.repaint();
        networkSelectorContentPane.revalidate();
        networkSelectorContentPane.repaint();
        this.pack();
        this.revalidate();
        this.repaint();
    }

    /**
     * Shows the start interface.
     */
    public void showStartUpInterface() {
        this.setContentPane(startupContentPane);
        this.repaint();
        this.revalidate();
        this.pack();
        this.setLocationRelativeTo(null);
    }

    /**
     * Shows the advanced settings interface. The interface is constructed in
     * {@link #setUpAdvancedStart()}.
     *
     * @requires No connection established before
     * @param mazeName the name of the maze
     * @param maze the the maze of the game
     */
    public void showAdvancedPanel(final String mazeName, final Maze maze) {
        this.localAddressPanel.setPort(controller.getLocalPort());
        // Default address
        try {
            this.localAddressPanel.setAddress(SimpleP2PServer.getLocalIP());
            this.remoteAddressPanel.setAddress(SimpleP2PServer.getLocalIP());
        } catch (IOException ignored) {
            // Ignored
        }
        this.setContentPane(networkSelectorContentPane);
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
        backgroundMusic.start();

        // Interface
        final JPanel gamePanel = new JPanel(new BorderLayout());

        // Status at the bottom
        final List<JComponent> statsPanelsList = new LinkedList<>();
        JPanel scorePanel = new JPanel(new BorderLayout());
        JLabel scoreLabel = new JLabel("PACMAN SCORES", SwingConstants.CENTER);
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
        PacmanViewUtility.addMouseHoveringEffectAtGame(musicButton);
        musicButton.addActionListener(e -> {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            } else {
                backgroundMusic.start();
            }
        });
        statsPanelsList.add(musicButton);

        JButton takeOverButton = new JButton("Start/stop AI takeover");
        PacmanViewUtility.addMouseHoveringEffectAtGame(takeOverButton);
        takeOverButton.addActionListener(e -> {
            if (self.aiTakeOver()) {
                notification("AI is now moving your player. Relax.");
            } else {
                notification("You will need to control the player with arrow keys.");
            }
        });
        statsPanelsList.add(takeOverButton);

        JButton closeGameButton = new JButton("Back to main menu");
        PacmanViewUtility.addMouseHoveringEffectAtGame(closeGameButton);
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
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 15,  15, 15));
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

    /**
     * Adds a pacman agent into the gmae view.
     *
     * @param maze the the maze of the game
     * @param x the x coordinate of the pacman
     * @param y the y coordinate of the pacman
     * @param index the index of the pacman
     * @param algorithm the algorithm chosen for determining next move
     * @param isSelf if the agent is controlled by the local host user
     * @param isNetwork if the agent is controlled by the network side
     */
    public void addPacman(final Maze maze, final int x, final int y, int index,
                   AbstractAlgorithm algorithm, boolean isSelf, boolean isNetwork) {
        PacmanAgent agent;
        if (isSelf) {
            agent = new UserControlledPacmanAgent(controller, maze, x, y, index, algorithm);
            self = (UserControlledPacmanAgent) agent;
        } else if (isNetwork) {
            agent = new ControlledPacmanAgent(controller, maze, x, y, index, algorithm);
        } else {
            agent = new PacmanAgent(controller, maze, x, y, index, algorithm);
        }
        mazePanel.addAgent(agent, true);
        pacmanAgents.put(index, agent);
    }

    /**
     * Adds a ghost agent into the gmae view.
     *
     * @param maze the the maze of the game
     * @param x the x coordinate of the ghost
     * @param y the y coordinate of the ghost
     * @param name the name of the ghost agent
     * @param algorithm the algorithm chosen for determining next move
     * @param isSelf if the agent is controlled by the local host user
     * @param isNetwork if the agent is controlled by the network side
     */
    public void addGhost(final Maze maze, final int x, final int y, String name,
                         AbstractAlgorithm algorithm, boolean isSelf, boolean isNetwork) {
        GhostAgent agent;

        if (isSelf) {
            agent = new UserControlledGhostAgent(controller, maze, x, y, name, algorithm);
            self = (UserControlledGhostAgent) agent;
        } else if (isNetwork) {
            agent = new ControlledGhostAgent(controller, maze, x, y, name, algorithm);
        } else {
            agent = new GhostAgent(controller, maze, x, y, name, algorithm);
        }
        mazePanel.addAgent(agent, true);
        ghostAgents.put(name, agent);
    }

    // ==================================================================================
    //                                     GAMEPLAY
    // ==================================================================================

    /**
     * Gets the users' choice on the input files and put them into settings.
     *
     * @param index the index of interface which is shown when the request is sent
     */
    public void askForInput(int index) {
        settings.put("Maze",
                String.valueOf(levelChoicesComboBoxs.get(index).getSelectedItem()));
        settings.put("InputFile", customInputFileTextFields.get(index).getText());
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
            mazePanel.addAgent(new UserControlledPacmanAgent(new FakeMazeController(),
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
        PacmanViewUtility.addMouseHoveringEffectAtGame(closeBtn);
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
        backgroundMusic.stop();
        showStartUpInterface();
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

    /**
     * Stops the movement of every agent in the game.
     *
     * @param win if the pacman wins
     */
    public void gameOver(boolean win) {
        for (AbstractAgent agent: ghostAgents.values()) {
            agent.stop();
        }
        for (AbstractAgent agent: pacmanAgents.values()) {
            agent.stop();
        }
        backgroundMusic.stop();
    }

    /**
     * This method gets called when the any side is lost.
     *
     * @param isLocalSideWin {@code true} if the remote side is lost and {@code false}
     *                               otherwise. {@code null} if draw
     * @return the user's choice.
     */
    public int showResultDialog(Boolean isLocalSideWin) {
        if (self instanceof GhostAgent) {
            isLocalSideWin = !isLocalSideWin;
        }
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

    /**
     * Sets the scared time of a ghost agent in the game.
     *
     * @param ghostName the name of the ghost
     * @param scaredTime the time being scared.
     */
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
        this.remoteAddressPanel.setApplyBtnEnabled(false);
        this.networkStatusPanel.connected();
        this.updateClientListPanel();
    }

    /**
     * This function let the server connect to a remote host on its own initiative.
     */
    public void connectTo() {
        boolean result = this.controller.connectTo(this.remoteAddressPanel.getAddress(),
                this.remoteAddressPanel.getPort(), false);
        if (result) {
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
        this.remoteAddressPanel.setApplyBtnEnabled(false);
        this.networkStatusPanel.connected();
    }

    /**
     * This method gets called when the remote side closes the connection.
     */
    public void remoteConnectionClosed() {
        this.remoteAddressPanel.setApplyBtnEnabled(true);
        this.networkStatusPanel.waitForConnection();
    }

    /**
     * Gets the selected user controlled agent name.
     *
     * @return the name of the selected user controlled agent
     */
    public String getSelectedName() {
        String selectedName = null;
        for (AgentItemPanel item: agentItemPanels) {
            if (item.isSelected()) {
                selectedName = item.getAgentName();
            }
        }
        return selectedName;
    }

    /**
     * Gets the list containing all components representing selection of agents in
     * the maze.
     *
     * @return a list containing all components representing selection of agents
     */
    public List<AgentItemPanel> getAgentItemPanels() {
        return agentItemPanels;
    }

    /**
     * Unselects the player choice.
     * @param token the name of the agent
     */
    public void unselectPlayer(final String token) {
        advancedPlayerSelectionGroup.clearSelection();
    }

    /**
     * Change the current moving direction of an agent immediately.
     *
     * @param direction the new direction
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @param agentName the name of the agent
     */
    public void immediateDirectionChange(final String direction, final String x,
        final String y, final String agentName) {
        Logger.printlnf("Immediate direction change at (%s, %s) to %s for %s", x, y,
            direction, agentName);
        Direction d = Direction.valueOf(direction);
        AbstractAgent agent;
        if (StringUtilities.isInteger(agentName)) {
            agent = pacmanAgents.get(Integer.parseInt(agentName));
        } else {
            agent = ghostAgents.get(agentName);
        }
        agent.networkChangeDirection(d, x, y);
    }

    /**
     * Change the current location of an agent immediately.
     *
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @param agentName the name of the agent
     */
    public void immediateLocationChange(final String x, final String y,
        final String agentName) {
        AbstractAgent agent;
        if (StringUtilities.isInteger(agentName)) {
            agent = pacmanAgents.get(Integer.parseInt(agentName));
        } else {
            agent = ghostAgents.get(agentName);
        }
        agent.networkChangeLocation(x, y);
    }
}
