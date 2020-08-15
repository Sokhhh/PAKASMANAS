package pacman.algorithms;

import epidemics.model.EpidemicUtilities;
import pacman.model.Direction;
import pacman.model.Maze;

import java.util.List;

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
     * @return the direction to go for next state
     */
    @Override
    public Direction getPacmanAction(int pacmanIndex, int x, int y) {
        List<Direction> nextDirections = maze.getLegalActionsIncludeStop(x, y);
        return EpidemicUtilities.randomSelect(nextDirections);
    }

    /**
     * Gets the next move based on the algorithm chosen.
     *
     * @param ghostName the name of the ghost
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param isScared  if the ghost is scared
     * @return the direction to go for next state
     */
    @Override
    public Direction getGhostAction(String ghostName, int x, int y, boolean isScared) {
        List<Direction> nextDirections = maze.getLegalActionsIncludeStop(x, y);
        return EpidemicUtilities.randomSelect(nextDirections);
    }
}
