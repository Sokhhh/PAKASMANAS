package pacman.agents;

import java.util.Iterator;
import pacman.algorithms.AbstractAlgorithm;
import pacman.controller.PacmanMazeController;
import pacman.model.Direction;
import pacman.model.Maze;


/**
 * This is a component that shows a user controlled ghost in the game with
 * moving animations.
 *
 * @version 1.0
 */
public class ControlledGhostAgent extends GhostAgent implements KeyboardControlledAgent {
    private boolean aiTakeOver = false;

    /**
     * Constructor that creates a new PacmanAgent.
     *
     * @param controller  the controller of the application
     * @param maze            the game maze
     * @param startCoordinateX the start x coordinate of the icon in the board
     * @param startCoordinateY the start y coordinate of the icon in the board
     * @param name             name of the ghost
     * @param algorithm  the algorithm chosen for determining next move
     */
    public ControlledGhostAgent(final PacmanMazeController controller, final Maze maze,
                                 final int startCoordinateX, final int startCoordinateY,
                                 final String name, AbstractAlgorithm algorithm) {
        super(controller, maze, startCoordinateX, startCoordinateY, name, algorithm);
        this.setArrowKeyToControl(this);
    }

    /**
     * Find the next direction before moving at each step.
     *
     * @param currDirection the current direction
     */
    protected void checkPossibleNextDirection(final Direction currDirection) {
        if (aiTakeOver) {
            super.checkPossibleNextDirection(currDirection);
        } else {
            if (direction != Direction.STOP) {
                // Checks for possible direction changes
                for (Iterator<Direction> iterator = pendingDirections.iterator();
                     iterator.hasNext(); ) {
                    Direction possibleDirection = iterator.next();
                    if (possibleDirection.reverse() == direction) {
                        iterator.remove();
                    } else if (maze.isValidDirection(coordinateX, coordinateY,
                            possibleDirection)) {
                        changeDirection(possibleDirection);
                        iterator.remove();
                        return;
                    }
                }
                if (currDirection == Direction.STOP) {
                    changeDirection(currDirection);
                    pendingDirections.clear();
                }
            }
        }
    }

    /**
     * Let AI takes over from the user.
     *
     * @return {@code true} if the user is no longer in control and {@code false}
     *      otherwise
     */
    @Override
    public boolean aiTakeOver() {
        if (!aiTakeOver) {
            aiTakeOver = true;
            disableArrowKeyToControl(this);
        } else {
            aiTakeOver = false;
            setArrowKeyToControl(this);
        }
        return aiTakeOver;
    }
}

