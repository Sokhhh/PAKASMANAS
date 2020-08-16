package pacman.algorithms;

import pacman.model.Direction;
import pacman.model.Maze;

/**
 * Defines an algorithm that let the agent stay still at any time.
 *
 * @version 1.0
 */
public class NullAlgorithm extends AbstractAlgorithm {
    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public NullAlgorithm(Maze maze) {
        super(maze);
    }

    /**
     * Gets the next move based on the algorithm chosen.
     *
     * @param pacmanIndex the index of pacman
     * @param x           the x coordinate
     * @param y           the y coordinate
     * @param current current direction
     * @return the direction to go for next state
     */
    @Override
    public Direction getPacmanAction(int pacmanIndex, int x, int y,
                                          Direction current) {
        return Direction.STOP;
    }

    /**
     * Gets the next move based on the algorithm chosen.
     *
     * @param ghostName the name of the ghost
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param current current direction
     * @param isScared  if the ghost is scared
     * @return the direction to go for next state
     */
    @Override
    public Direction getGhostAction(String ghostName, int x, int y,
                                    Direction current, boolean isScared) {
        return Direction.STOP;
    }
}
