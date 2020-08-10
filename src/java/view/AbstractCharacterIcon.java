package view;

import static view.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import model.Board;
import model.Direction;
import util.BoardParser;

public class AbstractCharacterIcon extends JLabel {
    /** Contains the size of horizontal screen distance moved in each movement animation. */
    private final int deltaX;

    /** Contains the size of vertical screen distance moved in each movement animation. */
    private final int deltaY;

    /** Contains the milliseconds of delay between each step of movement animation. */
    private final int delay;

    /** Contains the game maze. */
    private final Board board;

    /** Contains the x coordinate of the icon in the board. */
    private int coordinateX;

    /** Contains the y coordinate of the icon in the board. */
    private int coordinateY;

    /** Contains the movement direction of the icon. */
    private Direction direction;

    /** Contains the icon of the character. */
    private final ImageIcon icon;

    /** Contains the timer to constantly move the character. */
    private final Timer autoMoving;

    /**
     * Contains a queue that stores the pending changed directions inputted by the
     * user.
     */
    private final ConcurrentLinkedQueue<Direction> pendingDirections =
        new ConcurrentLinkedQueue<>();

    /**
     * Contains an Action object that applied when the user inputs a new direction. It
     * checks if that direction can be applied immediately: if so, it changes to that
     * direction; otherwise, it puts the direction into a queue.
     */
    private class NewDirectionAction extends AbstractAction {
        /**
         * Contains the user specified new direction.
         */
        private final Direction newDirection;

        /**
         * Creates a new NewDirectionAction object.
         *
         * @param newDirection the user specified new direction
         */
        public NewDirectionAction(Direction newDirection) {
            this.newDirection = newDirection;
        }

        /**
         * Invoked when an action occurs.
         * @param e the action event
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (direction != newDirection) {
                if (board.isValidDirection(coordinateX, coordinateY, newDirection)) {
                    direction = newDirection;
                } else {
                    pendingDirections.add(newDirection);
                }
            }
        }
    }

    /**
     * Constructor that creates a new AbstractCharacterIcon.
     *
     * @param board  the game maze
     * @param startCoordinateX the start x coordinate of the icon in the board
     * @param startCoordinateY the start y coordinate of the icon in the board
     * @param delay the milliseconds of delay between each step of movement animation
     */
    public AbstractCharacterIcon(final Board board, int startCoordinateX,
        int startCoordinateY, int delay) {
        this.deltaX = BLOCK_SIZE / 5;
        this.deltaY = BLOCK_SIZE / 5;
        this.delay = delay;
        this.board = board;
        this.coordinateX = startCoordinateX;
        this.coordinateY = startCoordinateY;
        this.direction = Direction.STOP;

        // Set hotkeys
        setKeyBindings(Direction.LEFT, KeyEvent.VK_LEFT);
        setKeyBindings(Direction.RIGHT, KeyEvent.VK_RIGHT);
        setKeyBindings(Direction.DOWN, KeyEvent.VK_DOWN);
        setKeyBindings(Direction.UP, KeyEvent.VK_UP);

        // Changes the image icon of the component
        icon = new ImageIcon(new ImageIcon("pacman.gif").getImage()
            .getScaledInstance(BLOCK_SIZE, BLOCK_SIZE, Image.SCALE_DEFAULT));
        setIcon(icon);
        setSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        setPreferredSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));

        // Move the icon to the start coordinate in the board
        setLocation(startCoordinateX * MazePanel.BLOCK_SIZE, startCoordinateY * MazePanel.BLOCK_SIZE);

        // Start moving
        this.autoMoving = new Timer(delay * 5, e -> move());
    }

    /**
     * Starts the animation that let the character moves along the direction.
     */
    public void startMovingAnimation() {
        autoMoving.start();
    }

    public void move() {
        final Direction currDirection = direction;
        if (direction != Direction.STOP) {
            final int num = BLOCK_SIZE / deltaX;
            if (board.isValidDirection(coordinateX, coordinateY, currDirection)) {
                // If next block following current direction is not WALL, go to that block
                new Timer(delay, new ActionListener() {
                    private int counter = 0;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        counter++;
                        if (counter == num) {
                            ((Timer) e.getSource()).stop();
                        }

                        //  Determine next screen position
                        int nextScreenX = AbstractCharacterIcon.this.getLocation().x
                            + (deltaX * currDirection.getDirectionX());
                        int nextScreenY = AbstractCharacterIcon.this.getLocation().y
                            + (deltaY * currDirection.getDirectionY());

                        //  Move the image
                        setLocation(nextScreenX, nextScreenY);
                    }
                }).start();
                this.coordinateX += currDirection.getDirectionX();
                this.coordinateY += currDirection.getDirectionY();
            } else {
                // Stop if the next block is a wall
                direction = Direction.STOP;
            }
        }

        // Checks for possible direction changes
        for (Iterator<Direction> iterator = pendingDirections.iterator(); iterator.hasNext(); ) {
            Direction possibleDirection = iterator.next();

            if (board.isValidDirection(coordinateX, coordinateY, possibleDirection)) {
                direction = possibleDirection;
                iterator.remove();
                return;
            }
        }
        if (currDirection == Direction.STOP) {
            direction = currDirection;
            pendingDirections.clear();
        }
    }

    /**
     * Binds a hotkey with the action of changing the direction.
     *
     * @param newDirection the new direction specified by the user
     * @param keyCode an int specifying the numeric code for a keyboard key
     */
    private void setKeyBindings(Direction newDirection, int keyCode) {
        KeyStroke keyPressed = KeyStroke.getKeyStroke(keyCode, 0, false);

        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyPressed, keyPressed.toString());

        ActionMap actionMap = getActionMap();
        actionMap.put(keyPressed.toString(), new NewDirectionAction(newDirection));
    }

    public static void main(String[] args) throws IOException {
        JPanel panel = new JPanel();
        JFrame frame = new JFrame();

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        Board board = BoardParser.readBoard("data/small_board.txt");

        MazePanel mp = new MazePanel(board);
        mp.setLayout(null);

        AbstractCharacterIcon ta = new AbstractCharacterIcon(board, 1, 1,  40);
        ta.startMovingAnimation();
        mp.add(ta);
        frame.setContentPane(mp);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
