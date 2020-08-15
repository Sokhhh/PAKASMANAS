package pacman.agents;

import static pacman.agents.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import pacman.algorithms.AbstractAlgorithm;
import pacman.controller.PacmanMazeController;
import pacman.model.Direction;
import pacman.model.Maze;
import pacman.util.ImageInterning;

public class GhostAgent extends AbstractAgent {
    /**
     * Name of the ghost.
     */
    private final String name;

    /**
     * Contains the algorithm chosen for determining next move.
     */
    private final AbstractAlgorithm algorithm;

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
        super(controller, maze, startCoordinateX, startCoordinateY, 50);
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
        this.algorithm = algorithm;
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
                coordinateY, isScared));
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
