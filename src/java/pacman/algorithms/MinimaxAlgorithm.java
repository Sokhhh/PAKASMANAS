package pacman.algorithms;

import pacman.model.Direction;
import pacman.model.Maze;

public class MinimaxAlgorithm extends GreedyAlgorithm {
    private int depth = 2;

    /**
     * Creates a search algorithm utility.
     *
     * @param maze the maze of the game.
     */
    public MinimaxAlgorithm(Maze maze) {
        super(maze);
    }

    private final static boolean MAX = true;
    private final static boolean MIN = false;

    /**
     * The minimax search function.
     *
     * <p>Pseudocode: <br>
     * {@code
     *         function minimax(node, depth, maximizingPlayer) :=
     *             if depth = 0 or node is a terminal node then
     *                 return the heuristic value of node
     *             if maximizingPlayer then
     *                 value := -inf
     *                 for each child of node do
     *                     value := max(value, minimax(child, depth - 1, FALSE))
     *                 return value
     *             else (* minimizing player *)
     *                 value := +inf
     *                 for each child of node do
     *                     value := min(value, minimax(child, depth - 1, TRUE))
     *                 return value
     * }
     *
     * @param x the x coordinate if the action is applied
     * @param y the y coordinate if the action is applied
     * @param currDepth the depth of searching in the recursive function
     * @param isMaximizing if the searching is trying to maximizing the player
     * @param agentIndex the index of the agent
     * @return the minimax evaluation score for the current state
     */
    double minimax(int x, int y, int currDepth, boolean isMaximizing,
                  int agentIndex) {
        if (currDepth == 0 || maze.isWin(x, y) || maze.isLose(x, y)) {
            return pacmanEvaluationFunction(agentIndex, x, y);
        }
        if (isMaximizing) {              // maximizing
            double value = Double.MIN_VALUE;
            for (Direction action : maze.getLegalActions(x, y)) {
                value = Math.min(value, minimax(x + action.getDirectionX(),
                        y + action.getDirectionY(), currDepth - 1, MAX, agentIndex));
            }
            return value;
        } else {                         // minimizing
            double value = Double.MAX_VALUE;
            for (Direction action : maze.getLegalActions(x, y)) {
                value = Math.max(value, minimax(x + action.getDirectionX(),
                        y + action.getDirectionY(), currDepth - 1, MIN, agentIndex));
            }
            return value;
        }
    }

    /**
     * The minimax search function.
     *
     * <p>Pseudocode: <br>
     * {@code
     *         function minimax(node, depth, maximizingPlayer) :=
     *             if depth = 0 or node is a terminal node then
     *                 return the heuristic value of node
     *             if maximizingPlayer then
     *                 value := -inf
     *                 for each child of node do
     *                     value := max(value, minimax(child, depth - 1, FALSE))
     *                 return value
     *             else (* minimizing player *)
     *                 value := +inf
     *                 for each child of node do
     *                     value := min(value, minimax(child, depth - 1, TRUE))
     *                 return value
     * }
     *
     * @param x the x coordinate if the action is applied
     * @param y the y coordinate if the action is applied
     * @param currDepth the depth of searching in the recursive function
     * @param isMaximizing if the searching is trying to maximizing the player
     * @param agentName the name of the agent
     * @return the minimax evaluation score for the current state
     */
    double minimax(int x, int y, int currDepth, boolean isMaximizing,
                   String agentName, boolean isScared) {
        if (currDepth == 0 || maze.isWin(x, y) || maze.isLose(x, y)) {
            return ghostEvaluationFunction(agentName, x, y, isScared);
        }
        if (isMaximizing) {              // maximizing
            double value = Double.MIN_VALUE;
            for (Direction action : maze.getLegalActions(x, y)) {
                value = Math.min(value, minimax(x + action.getDirectionX(),
                        y + action.getDirectionY(), currDepth - 1, MAX, agentName,
                        isScared));
            }
            return value;
        } else {                         // minimizing
            double value = Double.MAX_VALUE;
            for (Direction action : maze.getLegalActions(x, y)) {
                value = Math.max(value, minimax(x + action.getDirectionX(),
                        y + action.getDirectionY(), currDepth - 1, MIN, agentName,
                        isScared));
            }
            return value;
        }
    }

    /**
     * Gets the next minimax action from the current game state using current depth.
     *
     * @param pacmanIndex the index of pacman
     * @param x           the x coordinate
     * @param y           the y coordinate
     * @return the direction to go for next state
     */
    @Override
    public Direction getPacmanAction(int pacmanIndex, int x, int y) {
        Direction ret = Direction.STOP;
        double score = 0;
        for (Direction d: maze.getLegalActions(x, y)) {
            if (minimax(x + d.getDirectionX(), y + d.getDirectionY(), depth, MIN,
                    pacmanIndex) > score) {
                ret = d;
            }
        }
        return ret;
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
        Direction ret = Direction.STOP;
        double score = 0;
        for (Direction d: maze.getLegalActions(x, y)) {
            if (minimax(x + d.getDirectionX(), y + d.getDirectionY(), depth, MIN,
                    ghostName, isScared) > score) {
                ret = d;
            }
        }
        return ret;
    }
}
