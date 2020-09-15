package pacman.algorithms;

import java.util.Map;
import java.util.Set;
import pacman.model.Coordinate;
import pacman.model.Maze;


/**
 * Defines an algorithm that let the agent applies the A* search algorithm at each step.
 *
 * @version 1.0
 */
public class AStar extends GreedyAlgorithm {
    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public AStar(Maze maze) {
        super(maze);
    }

    /**
     * An extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
     * evaluation function.
     *
     * <p>In this betterEvaluationFunction, the total score of an action
     * is decided by seven factors:
     * Starts from the current score of the current game state, the score would first
     * added with the sum of scared time of all scared ghosts. Then it minus five other
     * factors with increasing weights:
     * - the distance to the closest food (the closer to the food, the better);
     * - the distance to the closest scared ghost (the closer the better)
     * - the inverse of the distance to the closest ghost (the closer the worse)
     * - the number of remaining food (the more the worse)
     * - the number of capsules (the more the worse)
     *
     * @param pacmanIndex the index of the pacman
     * @param x the x coordinate of the action
     * @param y the y coordinate of the action
     * @return the evaluation score of the action
     */
    protected double pacmanEvaluationFunction(int pacmanIndex, int x, int y) {
        final Set<Coordinate> currFood = maze.getFoods();
        final Coordinate[] pellets = maze.getPellets();
        final Map<String, Coordinate> currGhostStates = maze.getGhostsLocation();
        final Map<String, Integer> currScaredTimes = maze.getGhostScaredTimes();
        // check if the current state is the final winning state
        if (maze.isLose(x, y)) {
            // Lose
            return Integer.MIN_VALUE;
        } else if (maze.isWin(x, y)) {
            // Win
            return Integer.MAX_VALUE;
        }

        // find the closest / farthest point
        int closestFoodDist = Integer.MAX_VALUE;
        for (Coordinate foodCoordinate : currFood) {
            int currFoodDist = AlgorithmsUtility.manhattanDistance(
                    new Coordinate(x, y), foodCoordinate);
            if (currFoodDist < closestFoodDist) {
                closestFoodDist = currFoodDist;
            }
        }

        int closestPelletsDist = Integer.MAX_VALUE;
        for (Coordinate pelletCoordinate : pellets) {
            int currPelletDist = AlgorithmsUtility.manhattanDistance(
                    new Coordinate(x, y), pelletCoordinate);
            if (currPelletDist < closestFoodDist) {
                closestPelletsDist = currPelletDist;
            }
        }

        int closestGhostDist = 0;
        int closestGhostScaredDist = 0;
        if (currGhostStates.size() > 0) {
            closestGhostDist = Integer.MAX_VALUE;
            for (String ghostName : currGhostStates.keySet()) {
                if (currScaredTimes.get(ghostName) <= 0) {
                    Coordinate ghostCoordinate = currGhostStates.get(ghostName);
                    int currGhostDist = AlgorithmsUtility.manhattanDistance(
                            new Coordinate(x, y), ghostCoordinate);
                    if (currGhostDist < closestGhostDist) {
                        closestGhostDist = currGhostDist;
                    }
                }
            }

            // get scared ghosts information
            boolean isScared = currScaredTimes.values().stream().anyMatch(i -> i > 0);

            closestGhostScaredDist = Integer.MAX_VALUE;
            if (isScared) {
                for (String ghostName : currGhostStates.keySet()) {
                    if (currScaredTimes.get(ghostName) > 0) {
                        Coordinate ghostCoordinate = currGhostStates.get(ghostName);
                        int currGhostDist = AlgorithmsUtility.manhattanDistance(
                                        new Coordinate(x, y), ghostCoordinate);
                        if (currGhostDist < closestGhostScaredDist) {
                            closestGhostScaredDist = currGhostDist;
                        }
                    }
                }
            } else {
                closestGhostScaredDist = 0;
            }
        }

        // calculate the score
        int score = maze.getPacmanScores().getOrDefault(pacmanIndex, 0);
        for (int scaredTime : currScaredTimes.values()) {
            score += scaredTime;
        }
        score -= 2 * closestFoodDist;
        score -= 3 * closestPelletsDist;
        score -= 4 * closestGhostScaredDist;
        score -= 5 * (1.0 / closestGhostDist);
        score -= 6 * maze.getFoodsNum();
        score -= 7 * maze.getPelletsNum();
        return score;
    }
}
