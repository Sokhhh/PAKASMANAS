package pacman;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import gol.viewer.UserInteraction;
import golgui.components.GuiComponentFactory;
import golgui.components.GuiMessenger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import pacman.algorithms.AbstractAlgorithm;
import pacman.components.*;
import pacman.model.Maze;
import pacman.util.ImageFactory;
import pacman.util.Logger;

public class GUIViewer extends JFrame implements UserInteraction {
    /** Contains the controller of the application. */
    private final GUIController controller;

    /**
     * Contains a GUI utility that can be used to handle communication with the user.
     */
    private final GuiMessenger messenger;
    private JLabel scoreText;
    private int currScore = 0;
    private MazePanel mazePanel;
    private JLabel lifeText;
    private HashMap<Integer, PacmanAgent> pacmanAgents;
    private HashMap<String, GhostAgent> ghostAgents;
    private KeyboardControlledAgent self;

    /**
     * Constructor.
     *
     * @param controller the controller of the application
     */
    public GUIViewer(GUIController controller) {
        this.messenger = new GuiMessenger(this);
        this.controller = controller;
        this.pacmanAgents = new HashMap<>();
        this.ghostAgents = new HashMap<>();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
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
        this.setResizable(false);

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
    private void setUpStartInterface() {
        JPanel welcome = new JPanel();
        welcome.setBackground(Color.BLACK);
        welcome.setLayout(new BorderLayout());
        welcome.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel title = new JLabel("PACMAN", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Dialog", Font.BOLD, 52));
        welcome.add(title, BorderLayout.NORTH);
        try {
            welcome.add(new JLabel(ImageFactory.getImageIconFromFile("loading.gif",
                null)), BorderLayout.CENTER);
        } catch (IOException e) {
            Logger.println("ERROR:" + e.getMessage());
        }
        JButton startButton = new JButton("START");
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setOpaque(false);
        startButton.setBackground(new Color(110, 110, 110));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Dialog", Font.BOLD, 32));

        // Add hovering effect
        startButton.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse enters a component.
             *
             * @param evt the mouse enter event
             */
            @Override
            public void mouseEntered(MouseEvent evt) {
                startButton.setOpaque(true);
                startButton.setBackground(new Color(110, 110, 110));
            }

            /**
             * Invoked when a mouse button has been pressed on a component.
             *
             * @param evt the mouse pressed event
             */
            @Override
            public void mousePressed(final MouseEvent evt) {
                startButton.setBackground(new Color(189, 189, 189));
            }

            /**
             * Invoked when a mouse button has been released on a component.
             *
             * @param evt the mouse release event
             */
            @Override
            public void mouseReleased(final MouseEvent evt) {
                this.mouseEntered(evt);
            }

            /**
             * Invoked when the mouse exits a component.
             *
             * @param evt the mouse exit event
             */
            @Override
            public void mouseExited(MouseEvent evt) {
                startButton.setOpaque(false);
            }
        });
        startButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        startButton.addActionListener(e -> {
            controller.start();
        });

        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);

        InputMap inputMap = welcome.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyCodePressed, keyCodePressed.toString());

        ActionMap actionMap = welcome.getActionMap();
        actionMap.put(keyCodePressed.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                startButton.doClick();
            }
        });

        welcome.add(startButton, BorderLayout.SOUTH);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BorderLayout());
        buttons.setOpaque(false);
        buttons.add(new JTextField(), BorderLayout.CENTER);
        buttons.add(new JButton(""), BorderLayout.EAST);
        this.setContentPane(welcome);
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
        final JPanel gamePanel = new JPanel(new BorderLayout());

        // Status at the bottom
        final List<JComponent> statsPanels = new LinkedList<>();
        JPanel scorePanel = new JPanel(new BorderLayout());
        JLabel scoreLabel = new JLabel("SCORES", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif",  Font.BOLD, 27));
        scorePanel.add(scoreLabel, BorderLayout.NORTH);
        scoreText = new JLabel("0", SwingConstants.CENTER);
        scoreText.setFont(new Font("Monospaced",  Font.BOLD, 24));
        scorePanel.add(scoreText, BorderLayout.CENTER);
        statsPanels.add(scorePanel);

        JButton takeOver = GuiComponentFactory.createButtonWithHoveringEffect();
        takeOver.setText("Start/stop AI takeover");
        takeOver.addActionListener(e -> {
            if (self.aiTakeOver()) {
                notification("AI is now moving your player. Relax.");
            } else {
                notification("You will need to control the player with arrow keys.");
            }
        });
        statsPanels.add(takeOver);

        JPanel lifePanel = new JPanel(new BorderLayout());
        try {
            lifePanel.add(new JLabel(ImageFactory.getImageIconFromFile("pacman_right.gif",
                new Dimension(55, 55))), BorderLayout.WEST);
        } catch (IOException e) {
            JLabel lifeLabel = new JLabel("Life", SwingConstants.LEFT);
            lifeLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
            lifePanel.add(lifeLabel, BorderLayout.WEST);
        }
        lifeText = new JLabel("x3", SwingConstants.LEFT);
        lifeText.setFont(new Font("SansSerif", Font.BOLD, 40));
        lifePanel.add(lifeText, BorderLayout.CENTER);
        statsPanels.add(lifePanel);

        JPanel statusPanel = new JPanel(new GridLayout(1, statsPanels.size(), 5, 0));
        for (JComponent comp: statsPanels) {
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


    void addPacman(final Maze maze, final int x, final int y, int index,
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

    void addGhost(final Maze maze, final int x, final int y, String name, AbstractAlgorithm algorithm, boolean isSelf) {
        GhostAgent agent = new GhostAgent(controller, maze, x, y, name, algorithm);
        mazePanel.addAgent(agent, true);
        ghostAgents.put(name, agent);
    }

    // ==================================================================================
    //                                     GAMEPLAY
    // ==================================================================================

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

}
