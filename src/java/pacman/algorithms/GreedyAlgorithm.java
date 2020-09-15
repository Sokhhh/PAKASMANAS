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
 * Defines an algorithm that let the agent applies the greedy search algorithm at each
 * step.
 *
 * @version 1.0
 */
public class GreedyAlgorithm extends AbstractAlgorithm  {
    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public GreedyAlgorithm(Maze maze) {
        super(maze);
    }

    /**
     * A evaluation function that let the pacman chase only the food.
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

        // calculate the score
        int score = maze.getPacmanScores().getOrDefault(pacmanIndex, 0);
        for (int scaredTime : currScaredTimes.values()) {
            score += scaredTime;
        }
        score -= 2 * closestFoodDist;
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
                ghostEvaluationFunction(ghostName, i.getX(), i.getY(), isScared)))
            .orElse(null);
        if (bestAction != null) {
            return actions.get(bestAction);
        } else {
            return Direction.STOP;
        }
    }

}
