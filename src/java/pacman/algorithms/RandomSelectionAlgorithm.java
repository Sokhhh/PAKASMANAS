package pacman.algorithms;

import epidemics.model.EpidemicUtilities;
import java.util.List;
import pacman.model.Direction;
import pacman.model.Maze;

/**
 * Defines an algorithm that let the agent random choose an action at each step.
 *
 * @version 1.0
 */
public class RandomSelectionAlgorithm extends AbstractAlgorithm {

    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public RandomSelectionAlgorithm(Maze maze) {
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
        List<Direction> nextDirections = maze.getLegalActionsIncludeStop(x, y);
        return EpidemicUtilities.randomSelect(nextDirections);
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
        List<Direction> nextDirections = maze.getLegalActionsIncludeStop(x, y);
        return EpidemicUtilities.randomSelect(nextDirections);
    }
}
