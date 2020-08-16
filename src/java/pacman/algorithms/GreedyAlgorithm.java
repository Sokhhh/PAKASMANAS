package pacman.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import pacman.model.Coordinate;
import pacman.model.Direction;
import pacman.model.Maze;


/**
 * Defines an algorithm that let the agent applies the greedy algorithm at each step.
 *
 * @version 1.0
 */
public class GreedyAlgorithm extends AbstractAlgorithm {
    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public GreedyAlgorithm(Maze maze) {
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
        Set<Coordinate> currFood = maze.getFoods();
        Coordinate[] pellets = maze.getPellets();
        Map<String, Coordinate> currGhostStates = maze.getGhostsLocation();
        Map<String, Integer> currScaredTimes = maze.getGhostScaredTimes();
        int score = 0;
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
        score = maze.getPacmanScores().getOrDefault(pacmanIndex, 0);
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
        List<Direction> nextDirections = maze.getLegalActions(x, y);
        Map<Coordinate, Direction> actions = new HashMap<>();
        nextDirections.forEach((d) ->
                actions.put(new Coordinate(x + d.getDirectionX(), y + d.getDirectionY()), d));
        Coordinate bestAction = actions.keySet().stream().max(
                Comparator.comparing(i ->
                        pacmanEvaluationFunction(0, i.getX(), i.getY()))).orElse(null);
        if (bestAction != null) {
            return actions.get(bestAction);
        } else {
            return Direction.STOP;
        }
    }

    /**
     * Evaluates the position for a ghost.
     *
     * @param ghostName the name of the ghost
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param isScared  if the ghost is scared
     * @return the evaluation score of the action
     */
    protected int ghostEvaluationFunction(String ghostName, int x, int y, boolean isScared) {
        int score = 100;
        if (maze.getPacmanNum() > 0) {
            IntStream pacmanDists = maze.getPacmanLocation().values().stream()
                    .mapToInt(pacmanCoordinate
                            -> AlgorithmsUtility.manhattanDistance(new Coordinate(x, y),
                            pacmanCoordinate));

            int closestPacmanDist = pacmanDists.min().orElse(-1);

            if (closestPacmanDist > 0) {
                if (isScared) {
                    score += closestPacmanDist;
                } else {
                    score -= closestPacmanDist;
                }
            }
        } else {
            // If no pacman on the board, go to the start position
            IntStream starterDists = Arrays.stream(maze.getGhostsStartLocation())
                    .mapToInt(startCoordinate
                            -> AlgorithmsUtility.manhattanDistance(new Coordinate(x, y),
                            startCoordinate));

            int closestStartDist = starterDists.min().orElse(-1);

            if (closestStartDist > 0) {
                if (isScared) {
                    score += closestStartDist;
                } else {
                    score -= closestStartDist;
                }
            }

        }
        return score;
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
        List<Direction> nextDirections = maze.getLegalActions(x, y);
        Map<Coordinate, Direction> actions = new HashMap<>();
        nextDirections.forEach((d) ->
                actions.put(new Coordinate(x + d.getDirectionX(), y + d.getDirectionY()), d));
        Coordinate bestAction = actions.keySet().stream().max(
                Comparator.comparing(i ->
                        ghostEvaluationFunction(ghostName, i.getX(), i.getY(), isScared))).orElse(null);
        if (bestAction != null) {
            return actions.get(bestAction);
        } else {
            return Direction.STOP;
        }
    }
}
