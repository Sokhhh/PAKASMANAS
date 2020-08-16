package pacman.agents;

import static pacman.agents.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import pacman.algorithms.AbstractAlgorithm;
import pacman.controller.PacmanMazeController;
import pacman.model.Direction;
import pacman.model.Maze;
import pacman.util.ImageInterning;


/**
 * This is a component that shows a ghost in the game with moving animations.
 *
 * @version 1.0
 */
public class GhostAgent extends AbstractAgent {
    /**
     * Available names of ghost.
     */
    public static final String[] NAMES = {"pink", "red", "yellow", "blue",
        "green", "black"};

    /**
     * Name of the ghost.
     */
    private final String name;

    /**
     * Contains the icon of a scared ghost.
     */
    private final ImageIcon scaredIcon;

    /**
     * Contains if the ghost is scared.
     */
    private boolean isScared = false;

    /**
     * Contains a timer to set the ghost to be scared.
     */
    private Timer countDownGhostBuster;

    /**
     * Constructor that creates a new GhostAgent.
     *
     * @param controller       the controller of the application
     * @param maze             the game maze
     * @param startCoordinateX the start x coordinate of the icon in the board
     * @param startCoordinateY the start y coordinate of the icon in the board
     * @param name             name of the ghost
     * @param algorithm        the algorithm chosen for determining next move
     */
    public GhostAgent(final PacmanMazeController controller, final Maze maze,
                      final int startCoordinateX, final int startCoordinateY,
                      final String name, AbstractAlgorithm algorithm) {
        super(controller, maze, startCoordinateX, startCoordinateY, 50, algorithm);
        if (!Arrays.asList(NAMES).contains(name)) {
            throw new RuntimeException("Ghost name \"" + name + "\" does not have its "
                    + "image.");
        }
        try {
            rightIcon = ImageInterning.getImageIconFromFile("ghost_" + name + "_right.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            leftIcon = ImageInterning.getImageIconFromFile("ghost_" + name + "_left.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            upIcon = ImageInterning.getImageIconFromFile("ghost_" + name + "_up.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            downIcon = ImageInterning.getImageIconFromFile("ghost_" + name + "_down.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            scaredIcon = ImageInterning.getImageIconFromFile("ghost_scared.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Irrecoverable error: ghost image missing: " + e.getMessage());
        }
        setIcon(rightIcon);
        this.name = name;
    }

    /**
     * Stops everything of this agent.
     */
    @Override
    public void fullStop() {
        super.fullStop();
        countDownGhostBuster.cancel();
        countDownGhostBuster = null;
        maze.resetGhost(name);
    }

    /**
     * Find the next direction before moving at each step.
     *
     * @param currDirection the current direction
     */
    @Override
    protected void checkPossibleNextDirection(final Direction currDirection) {
        changeDirection(this.algorithm.getGhostAction(name, coordinateX,
                coordinateY, currDirection, isScared));
    }

    /**
     * This function will get the name of the ghost agent.
     *
     * @return name
     * @requires None
     * @modifies None
     * @effects None
     */
    public String getAgentName() {
        return this.name;
    }

    /**
     * Changes the image of the agent based on the direction of the agent.
     *
     * @param currDirection the current direction of the agent
     */
    @Override
    protected void setIconBasedOnDirection(Direction currDirection) {
        if (!isScared) {
            super.setIconBasedOnDirection(currDirection);
        }
    }

    /**
     * Start the duration of a scared ghost.
     *
     * @param defaultScaredTime the time being scared.
     */
    public void setScared(int defaultScaredTime) {
        setIcon(scaredIcon);
        isScared = true;
        if (countDownGhostBuster != null) {
            countDownGhostBuster.cancel();
        }
        countDownGhostBuster = new Timer();
        countDownGhostBuster.scheduleAtFixedRate(new TimerTask() {
            /**
             * The action to be performed by this timer task.
             */
            public void run() {
                isScared = false;
                if (direction == Direction.STOP) {
                    setIconBasedOnDirection(Direction.RIGHT);
                } else {
                    setIconBasedOnDirection(direction);
                }
                countDownGhostBuster.cancel();
            }
        }, defaultScaredTime * 1000, 1000);
    }

    /**
     * Sets the location back to the beginning state.
     */
    @Override
    public void reset() {
        countDownGhostBuster.cancel();
        countDownGhostBuster = null;
        isScared = false;
        maze.resetGhost(name);
        setIconBasedOnDirection(Direction.RIGHT);
        super.reset();
    }
}
