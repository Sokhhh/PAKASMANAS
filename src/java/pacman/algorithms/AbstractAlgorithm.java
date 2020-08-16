package pacman.algorithms;

import pacman.model.Direction;
import pacman.model.Maze;
import pacman.util.MapBuilder;

import java.util.Set;

/**
 * Contains the algorithm of pacman and ghost in the maze. The search is mainly
 * BFS and A star.
 *
 * @version 1.0
 */
public abstract class AbstractAlgorithm {
    /** The maze of the game. */
    protected Maze maze;

    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public AbstractAlgorithm(Maze maze) {
        this.maze = maze;
    }

    /**
     * Gets the next move based on the algorithm chosen.
     *
     * @param pacmanIndex the index of pacman
     * @param x the x coordinate
     * @param y the y coordinate
     * @param current current direction
     * @return the direction to go for next state
     */
    public abstract Direction getPacmanAction(int pacmanIndex, int x, int y,
                                              Direction current);

    /**
     * Gets the next move based on the algorithm chosen.
     *
     * @param ghostName the name of the ghost
     * @param x the x coordinate
     * @param y the y coordinate
     * @param current current direction
     * @param isScared if the ghost is scared
     * @return the direction to go for next state
     */
    public abstract Direction getGhostAction(String ghostName, int x, int y,
                                             Direction current, boolean isScared);
}
