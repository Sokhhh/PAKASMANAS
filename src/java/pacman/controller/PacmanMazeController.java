package pacman.controller;

import pacman.agents.AbstractAgent;
import pacman.model.Direction;

/**
 * This is the controller of the program. It accepts input and converts it to
 * commands for the model or view.
 *
 * @version 1.0
 */
public interface PacmanMazeController {
    /**
     * Gets called when an agent (pacman/ghost) visits a block. It checks if the
     * pacman meets a food (then earn scores), pellet (then earn scores and enter
     * "ghost buster" mode), a scared ghost (then earn scores and reset the ghost),
     * or a normal ghost (then dies). After that it updates the scores and check
     * if game is won.
     *
     * @param agent the agent of the move
     * @param x the current x coordinate of agent
     * @param y the current y coordinate of agent
     */
    void agentVisit(AbstractAgent agent, final int x, final int y);

    /**
     * Removes an agent from the game.
     *
     * @param agent the agent that is about to be removed.
     */
    void removeAgent(AbstractAgent agent);

    /**
     * Notifies the direction change (actions) of an agent.
     *
     * @param d the new direction of the agent
     * @param agent the agent that is changing
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     */
    void notifyDirectionChange(Direction d, AbstractAgent agent, int x, int y);

    /**
     * Notifies the location change (actions) of an agent.
     *
     * @param x the x coordinate in the maze
     * @param y the y coordinate in the maze
     * @param agent the agent that is changing
     */
    void notifyLocationChange(int x, int y, AbstractAgent agent);
}
