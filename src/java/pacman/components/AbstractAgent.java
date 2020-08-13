package pacman.components;

import static pacman.components.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import pacman.GUIController;
import pacman.model.Maze;
import pacman.model.Direction;

/**
 * This is a component that shows a character (ghost/pacman) in the game with moving
 * animations.
 */
public abstract class AbstractAgent extends JLabel {
    /** Contains the controller of the application. */
    private final GUIController controller;

    /** Contains the size of horizontal screen distance moved in each movement animation. */
    private final int deltaX;

    /** Contains the size of vertical screen distance moved in each movement animation. */
    private final int deltaY;

    /** Contains the milliseconds of delay between each step of movement animation. */
    private final int delay;

    /** Contains the game maze. */
    protected final Maze maze;

    /** Contains the start x coordinate of the icon in the board. */
    protected int startCoordinateX;

    /** Contains the start y coordinate of the icon in the board. */
    protected int startCoordinateY;

    /** Contains the x coordinate of the icon in the board. */
    protected int coordinateX;

    /** Contains the y coordinate of the icon in the board. */
    protected int coordinateY;

    /** Contains the movement direction of the icon. */
    protected Direction direction;

    /** Contains the icon of the character. */
    protected ImageIcon rightIcon;

    /** Contains the icon of the character. */
    protected ImageIcon leftIcon;

    /** Contains the icon of the character. */
    protected ImageIcon upIcon;

    /** Contains the icon of the character. */
    protected ImageIcon downIcon;

    /** Contains the timer to constantly move the character. */
    private final Timer autoMoving;

    /**
     * Contains a flag to show that the direction of the character is changed and need
     * to be updated.
     */
    private boolean directionChanged;

    /**
     * Contains a queue that stores the pending changed directions inputted by the
     * user.
     */
    protected final ConcurrentLinkedQueue<Direction> pendingDirections =
        new ConcurrentLinkedQueue<>();

    public void stop() {
        this.autoMoving.stop();
    }


    /**
     * Contains an Action object that applied when the user inputs a new direction. It
     * checks if that direction can be applied immediately: if so, it changes to that
     * direction; otherwise, it puts the direction into a queue.
     */
    class NewDirectionAction extends AbstractAction {
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
                if (maze.isValidDirection(coordinateX, coordinateY, newDirection)) {
                    changeDirection(newDirection);
                } else {
                    pendingDirections.add(newDirection);
                }
            }
        }
    }

    /**
     * Constructor that creates a new AbstractCharacterIcon.
     *  @param controller  the controller of the application
     * @param maze  the game maze
     * @param startCoordinateX the start x coordinate of the icon in the board
     * @param startCoordinateY the start y coordinate of the icon in the board
     * @param delay the milliseconds of delay between each step of movement animation
     */
    public AbstractAgent(final GUIController controller, final Maze maze,
        int startCoordinateX, int startCoordinateY, int delay) {
        this.controller = controller;
        this.deltaX = BLOCK_SIZE / 5;
        this.deltaY = BLOCK_SIZE / 5;
        this.delay = delay;
        this.maze = maze;
        this.startCoordinateX = startCoordinateX;
        this.startCoordinateY = startCoordinateY;
        this.coordinateX = startCoordinateX;
        this.coordinateY = startCoordinateY;
        this.direction = Direction.STOP;

        // Changes the image icon of the component
        setSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        setPreferredSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));

        // Move the icon to the start coordinate in the board
        setMazeLocation(startCoordinateX, startCoordinateY);

        // Start moving
        this.autoMoving = new Timer(delay * 5, e -> move());
    }

    /**
     * Sets the location in the maze.
     *
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     */
    public void setMazeLocation(int x, int y) {
        setLocation(x * BLOCK_SIZE, y * BLOCK_SIZE);
    }

    /**
     * Sets the location back to the beginning state.
     */
    public void reset() {
        setMazeLocation(startCoordinateX, startCoordinateY);
        coordinateX = startCoordinateX;
        coordinateY = startCoordinateY;
    }

    /**
     * Starts the animation that let the character moves along the direction.
     */
    public void startMovingAnimation() {
        autoMoving.start();
    }

    /**
     * Moves the image for a block following the current direction.
     */
    public void move() {
        // Update image based on direction changes
        final Direction currDirection = direction;
        if (directionChanged) {
            SwingUtilities.invokeLater(() -> {
                switch (currDirection) {
                    case UP:
                        setIcon(upIcon);
                        break;
                    case DOWN:
                        setIcon(downIcon);
                        break;
                    case LEFT:
                        setIcon(leftIcon);
                        break;
                    case RIGHT:
                        setIcon(rightIcon);
                        break;
                    default:
                }
                directionChanged = false;
            });
        }

        Timer animation = null;
        if (direction != Direction.STOP) {
            // Use an animation to go to next block
            final int num = BLOCK_SIZE / deltaX;
            if (maze.isValidDirection(coordinateX, coordinateY, currDirection)) {
                // If next block following current direction is not WALL, go to that block
                animation = new Timer(delay, new ActionListener() {
                    private int counter = 0;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        counter++;

                        //  Determine next screen position
                        int nextScreenX = AbstractAgent.this.getLocation().x
                            + (deltaX * currDirection.getDirectionX());
                        int nextScreenY = AbstractAgent.this.getLocation().y
                            + (deltaY * currDirection.getDirectionY());

                        //  Move the image
                        setLocation(nextScreenX, nextScreenY);
                        if (counter == num) {
                            ((Timer) e.getSource()).stop();
                            checkPossibleNextDirection(currDirection);
                        }
                    }
                });
                animation.start();
                coordinateX += currDirection.getDirectionX();
                coordinateY += currDirection.getDirectionY();
                controller.agentVisit(this, coordinateX, coordinateY);
            } else {
                // Stop if the next block is a wall
                changeDirection(Direction.STOP);
            }
        } else {
            checkPossibleNextDirection(currDirection);
        }
    }

    /**
     * Find the next direction before moving at each step.
     *
     * @param currDirection the current direction
     */
    protected abstract void checkPossibleNextDirection(final Direction currDirection);

    /**
     * Change the current moving direction.
     *
     * @param d the new direction
     */
    protected void changeDirection(Direction d) {
        direction = d;
        directionChanged = true;
    }
}
