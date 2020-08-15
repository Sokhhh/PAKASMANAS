package pacman.controller;

import pacman.agents.AbstractAgent;

public interface PacmanMazeController {
    /**
     * Adds the pacman to the maze and to the view.
     */
    void addPacman();

    /**
     * Adds the ghosts to the maze and to the view.
     */
    void addGhosts();

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
}
