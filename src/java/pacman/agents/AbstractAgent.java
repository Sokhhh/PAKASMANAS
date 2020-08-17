package pacman.agents;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import pacman.algorithms.AbstractAlgorithm;
import pacman.controller.PacmanMazeController;
import pacman.model.Coordinate;
import pacman.model.Direction;
import pacman.model.Maze;

/**
 * This is a component that shows a character (ghost/pacman) in the game with moving
 * animations.
 *
 * @version 1.0
 */
public abstract class AbstractAgent extends JLabel {
    /** Contains the controller of the application. */
    private final PacmanMazeController controller;

    /** Contains the size of horizontal screen distance moved in each movement animation. */
    private int deltaX;

    /** Contains the size of vertical screen distance moved in each movement animation. */
    private int deltaY;

    /** Contains the milliseconds of delay between each step of movement animation. */
    private final int delay;

    /** Contains the length of any side of a block. */
    protected int blockSize = MazePanel.BLOCK_SIZE;

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

    /** Contains the timer to constantly move the character within a step. */
    private Timer animation;

    /**
     * Contains a flag to show that the direction of the character is changed and need
     * to be updated.
     */
    private boolean directionChanged;

    /**
     * Contains a queue that stores the pending changed directions inputted by the
     * user.
     */
    protected final LinkedList<Direction> pendingDirections =
        new LinkedList<>();

    /**
     * Contains the algorithm chosen for determining next move.
     */
    protected final AbstractAlgorithm algorithm;
    private java.util.Timer hold;


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
                if (direction.reverse() == newDirection
                        || maze.isValidDirection(coordinateX, coordinateY, newDirection)) {
                    changeDirection(newDirection);
                } else {
                    if (pendingDirections.isEmpty()) {
                        pendingDirections.add(newDirection);
                    } else if (pendingDirections.getLast() != newDirection) {
                        pendingDirections.add(newDirection);
                    }
                }
            }
        }
    }

    /**
     * Constructor that creates a new AbstractCharacterIcon.
     *
     * @param controller  the controller of the application
     * @param maze  the game maze
     * @param startCoordinateX the start x coordinate of the icon in the board
     * @param startCoordinateY the start y coordinate of the icon in the board
     * @param delay the milliseconds of delay between each step of movement animation
     * @param algorithm  the algorithm chosen for determining next move
     */
    public AbstractAgent(final PacmanMazeController controller, final Maze maze,
                         int startCoordinateX, int startCoordinateY, int delay,
                         AbstractAlgorithm algorithm) {
        this.controller = controller;
        this.deltaX = blockSize / 5;
        this.deltaY = blockSize / 5;
        this.delay = delay;
        this.maze = maze;
        this.startCoordinateX = startCoordinateX;
        this.startCoordinateY = startCoordinateY;
        this.coordinateX = startCoordinateX;
        this.coordinateY = startCoordinateY;
        this.direction = Direction.STOP;
        this.algorithm = algorithm;

        // Changes the image icon of the component
        setSize(new Dimension(blockSize, blockSize));
        setPreferredSize(new Dimension(blockSize, blockSize));

        // Move the icon to the start coordinate in the board
        setMazeLocation(startCoordinateX, startCoordinateY);

        // Start moving
        this.autoMoving = new Timer(delay * 5, e -> move());
    }

    /**
     * Changes the size of a block.
     *
     * @param newBlockSize the new size of a block
     */
    public void setBlockSize(int newBlockSize) {
        this.blockSize = newBlockSize;
        this.deltaX = blockSize / 5;
        this.deltaY = blockSize / 5;
        setSize(new Dimension(blockSize, blockSize));
        setPreferredSize(new Dimension(blockSize, blockSize));
    }

    /**
     * Starts the animation that let the character moves along the direction.
     */
    public void startAutoMoving() {
        autoMoving.start();
    }

    /**
     * Stops the auto moving.
     */
    public void stop() {
        this.autoMoving.stop();
    }

    /**
     * Stops everything of this agent.
     */
    public void fullStop() {
        this.autoMoving.stop();
        this.animation.stop();
    }

    /**
     * Sets the location back to the beginning state.
     */
    public void reset() {
        controller.removeAgent(this);
        if (animation != null) {
            animation.stop();
        }
        autoMoving.stop();
        pendingDirections.clear();
        direction = Direction.STOP;
        setVisible(false);
        hold = new java.util.Timer();
        hold.scheduleAtFixedRate(new TimerTask() {
            /**
             * The action to be performed by this timer task.
             */
            public void run() {
                new Thread(() -> {
                    Coordinate curr = new Coordinate(startCoordinateX, startCoordinateY);
                    while (maze.get(curr) == Maze.PACMAN || maze.get(curr) == Maze.GHOST) {
                        curr = maze.getRandomNeighbor(coordinateX, coordinateY);
                    }
                    coordinateX = curr.getX();
                    coordinateY = curr.getY();
                    setMazeLocation(coordinateX, coordinateY);
                    controller.notifyLocationChange(coordinateX, coordinateY,
                        AbstractAgent.this);
                    setVisible(true);
                    controller.agentVisit(AbstractAgent.this, coordinateX, coordinateY);
                    autoMoving.start();
                }).start();
                hold.cancel();
            }
        }, 2000, 2000);
    }

    /**
     * Moves the image for a block following the current direction.
     */
    public void move() {
        // Update image based on direction changes
        final Direction currDirection = direction;
        if (directionChanged) {
            SwingUtilities.invokeLater(() -> {
                setIconBasedOnDirection(currDirection);
                directionChanged = false;
            });
        }

        animation = null;
        if (direction != Direction.STOP) {
            // Use an animation to go to next block
            final int num = blockSize / deltaX;
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
     * Changes the image of the agent based on the direction of the agent.
     *
     * @param currDirection the current direction of the agent
     */
    protected void setIconBasedOnDirection(Direction currDirection) {
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
    }

    /**
     * Sets the location in the maze.
     *
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     */
    public void setMazeLocation(int x, int y) {
        setLocation(x * blockSize, y * blockSize);
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
        controller.notifyDirectionChange(d, this, coordinateX, coordinateY);
    }

    /**
     * Change the current moving direction immediately.
     *
     * @param d the new direction
     * @param x the new x coordinate
     * @param y the new y coordinate
     */
    public void networkChangeDirection(Direction d, String x, String y) {
        if (animation != null) {
            animation.stop();
        }
        setMazeLocation(Integer.parseInt(x), Integer.parseInt(y));
        direction = d;
        directionChanged = true;
    }

    /**
     * Change the current location of an agent immediately.
     *
     * @param x the new x coordinate
     * @param y the new y coordinate
     */
    public void networkChangeLocation(final String x, final String y) {
        if (hold != null) {
            hold.cancel();
        }
        setMazeLocation(Integer.parseInt(x), Integer.parseInt(y));
        setVisible(true);
        if (hold != null) {
            controller.agentVisit(AbstractAgent.this, coordinateX, coordinateY);
            autoMoving.start();
        }
    }
}
