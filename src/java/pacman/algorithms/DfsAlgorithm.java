package pacman.algorithms;

import java.util.List;
import pacman.model.Direction;
import pacman.model.Maze;

/**
 * Defines an algorithm that let the agent applies the dfs algorithm at each
 * step: the agent does not change its current direction unless it has to.
 *
 * @version 1.0
 */
public class DfsAlgorithm extends AbstractAlgorithm {
    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public DfsAlgorithm(Maze maze) {
        super(maze);
    }

    /**
     * Gets the next direction first from the current direction; if current
     * direction is not possible, choose the direction other than reverse one,
     * then following clockwise next direction.
     *
     * @param x           the x coordinate
     * @param y           the y coordinate
     * @param current current direction
     * @return the next direction
     */
    protected Direction getDfsDirection(int x, int y, Direction current) {
        List<Direction> nextDirections = maze.getLegalActions(x, y);
        if (nextDirections.isEmpty()) {
            return Direction.STOP;
        }
        if (nextDirections.contains(current)) {
            return current;
        } else {
            for (int i = 0; i < nextDirections.size(); i++) {
                if (nextDirections.get(i).reverse() != current) {
                    return nextDirections.get(i);
                }
            }
            return nextDirections.get(0);
        }
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
        return getDfsDirection(x, y, current);
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
        return getDfsDirection(x, y, current);
    }
}
