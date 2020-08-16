package pacman.algorithms;

import pacman.model.Coordinate;
import pacman.model.Maze;

import java.util.Set;

/**
 * Contains utilities that will be used in pacman algorithms.
 *
 * @version 1.0
 */
public class AlgorithmsUtility {
    /**
     * Gets the Manhattan distance between two points.
     *
     * @param p1 the first coordinate
     * @param p2 the second coordinate
     * @return the Manhattan distance between points
     */
    public static int manhattanDistance(Coordinate p1, Coordinate p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }
}
