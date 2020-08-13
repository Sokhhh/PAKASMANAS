package pacman.components;

import static pacman.components.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.io.IOException;
import pacman.GUIController;
import pacman.algorithms.AbstractAlgorithm;
import pacman.model.Direction;
import pacman.model.Maze;
import pacman.util.ImageFactory;

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
     * Contains if the ghost is scared.
     */
    private boolean isScared = false;

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
    public GhostAgent(final GUIController controller, final Maze maze,
                      final int startCoordinateX, final int startCoordinateY,
                      final String name, AbstractAlgorithm algorithm) {
        super(controller, maze, startCoordinateX, startCoordinateY, 50);
        try {
            rightIcon = ImageFactory.getImageIconFromFile("ghost_" + name + "_right.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            leftIcon = ImageFactory.getImageIconFromFile("ghost_" + name + "_left.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            upIcon = ImageFactory.getImageIconFromFile("ghost_" + name + "_up.gif",
                    new Dimension(BLOCK_SIZE, BLOCK_SIZE));
            downIcon = ImageFactory.getImageIconFromFile("ghost_" + name + "_down.gif",
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
}
