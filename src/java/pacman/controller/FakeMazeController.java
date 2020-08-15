package pacman.controller;

import pacman.agents.AbstractAgent;

public class FakeMazeController implements PacmanMazeController {
    /**
     * Adds the pacman to the maze and to the view.
     */
    @Override
    public void addPacman() {
        // Ignored
    }

    /**
     * Adds the ghosts to the maze and to the view.
     */
    @Override
    public void addGhosts() {
        // Ignored
    }

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
}
