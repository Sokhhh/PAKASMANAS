package pacman.controller;

import pacman.agents.AbstractAgent;
import pacman.model.Direction;

/**
 * Contains a fake controller which does nothing in every action, mainly for
 * testing purposes.
 *
 * @version 1.0
 */
public class FakeMazeController implements PacmanMazeController {

    /**
     * Gets called when an agent (pacman/ghost) visits a block. It checks if the
     * pacman meets a food (then earn scores), pellet (then earn scores and enter
     * "ghost buster" mode), a scared ghost (then earn scores and reset the ghost),
     * or a normal ghost (then dies). After that it updates the scores and check
     * if game is won.
     *
     * @param agent the agent of the move
     * @param x     the current x coordinate of agent
     * @param y     the current y coordinate of agent
     */
    @Override
    public void agentVisit(AbstractAgent agent, int x, int y) {
        // Ignored
    }

    /**
     * Removes an agent from the game.
     *
     * @param agent the agent that is about to be removed.
     */
    @Override
    public void removeAgent(AbstractAgent agent) {
        // Ignored
    }

    /**
     * Notifies the direction change (actions) of an agent.
     *
     * @param d     the new direction of the agent
     * @param agent the agent that is changing
     */
    @Override
    public void notifyDirectionChange(final Direction d, final AbstractAgent agent,
        int x, int y) {
        // Ignored
    }

    /**
     * Notifies the location change (actions) of an agent.
     *
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     * @param agent the agent that is changing
     */
    @Override
    public void notifyLocationChange(final int x, final int y, final AbstractAgent agent) {

    }
}
