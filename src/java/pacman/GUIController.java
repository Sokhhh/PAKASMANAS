package pacman;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import pacman.algorithms.GreedyAlgorithm;
import pacman.components.PacmanAgent;
import pacman.components.AbstractAgent;
import pacman.components.GhostAgent;
import pacman.model.Coordinate;
import pacman.model.Maze;
import pacman.util.BoardParser;
import pacman.util.Logger;

public class GUIController {
    private GUIViewer view;

    private int scoresTotal;

    private int totalLives;
    private AtomicInteger lives;

    private Preferences settings;

    /** Contains the maze of the game. */
    private Maze maze;

    public GUIController() {
        Preferences root = Preferences.userRoot();
        this.settings = root.node("/edu/rpi/csci4963/pacman");
        this.scoresTotal = 0;
        this.totalLives = settings.getInt("totallives", 3);
        this.lives = new AtomicInteger(totalLives);
    }

    public void setView(GUIViewer view) {
        this.view = view;
        this.view.run();
    }

    public void load(String filename) {
        try {
            this.maze = BoardParser.readBoard(filename);
        } catch (IOException e) {
            if (this.view != null) {
                this.view.alert(
                    "Failed to load from file \"" + filename + "\":" + e.getMessage());
            } else {
                System.err.println(
                    "Failed to load from file \"" + filename + "\":" + e.getMessage());
            }
        }
    }

    public void start() {
        // TODO ask for Load file
        this.load("data/small_board.txt");
        view.start(maze);
        this.addPacman();
        this.addGhosts();
    }

    private void restartGame() {
        this.start();
        this.lives = new AtomicInteger(totalLives);
    }

    public void exit() {
        settings.putInt("lives", lives.get());

        try (FileOutputStream out = new FileOutputStream("prefs.xml")) {
            settings.exportSubtree(out);
        } catch (IOException | SecurityException | BackingStoreException e) {
            this.view.alert("Save settings failed due to system restrictions.");
        }

        System.exit(0);
    }

    void addPacman() {
        List<Coordinate> pacman = Arrays.asList(maze.getPacmanStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(pacman, ThreadLocalRandom.current());
        this.maze.pacmanVisit(0, pacman.get(0).getX(),
                pacman.get(0).getY());
        this.view.addPacman(maze, pacman.get(0).getX(), pacman.get(0).getY(), 0,
                new GreedyAlgorithm(maze), true);
    }

    void addGhosts() {
        List<Coordinate> ghosts = Arrays.asList(maze.getGhostsStartLocation());
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(ghosts, ThreadLocalRandom.current());
        String[] ghostNames = new String[] {"pink", "red", "yellow", "blue", "green"};
        for (int i=0; i < Math.min(ghosts.size(), ghostNames.length); i++) {
            this.maze.ghostVisit(ghostNames[i], ghosts.get(i).getX(),
                ghosts.get(i).getY());
            this.view.addGhost(maze, ghosts.get(i).getX(), ghosts.get(i).getY(),
                ghostNames[i], new GreedyAlgorithm(maze), false);
        }
    }

    /**
     * When the game over, ask the user to restart or close.
     *
     * @param win if the user wins
     */
    public void gameOver(boolean win) {
        Logger.printf("Game over");
        this.view.gameOver(win);
        int choice = this.view.showResultDialog(win);
        // Show panel of lose/win
        if (choice == 1) {
            // Disconnect
            this.hostCloseConnection();
            this.restartGame();
        } else if (choice == 2) {
            // Close the game
            this.exit();
        } else {
            // Restart the game
            this.restartGame();
        }
    }

    public void agentVisit(AbstractAgent agent, final int coordinateX,
        final int coordinateY) {
        int scoresDiff = 0;
        if (agent instanceof PacmanAgent) {
            scoresDiff += maze.pacmanVisit(((PacmanAgent) agent).getIndex(),
                    coordinateX, coordinateY);
            if (maze.get(coordinateX, coordinateY) == Maze.PELLETS) {
                this.hitPellets();
            }
            for (String ghostName: maze.getGhostsLocation().keySet()) {
                if (maze.getGhostsLocation().get(ghostName).equals(new Coordinate(coordinateX,
                        coordinateY))) {
                    if (maze.getGhostScaredTimes().get(ghostName) > 0) {
                        this.pacmanEat(ghostName);
                    } else {
                        this.pacmanDie(agent);
                        return;
                    }
                }
            }
        } else if (agent instanceof GhostAgent) {
            maze.ghostVisit(((GhostAgent) agent).getAgentName(), coordinateX,
                coordinateY);
        }
        scoresTotal += scoresDiff;
        view.updateScore(scoresTotal, scoresDiff);
        if (maze.getFoodsNum() + maze.getPelletsNum() == 0) {
            this.gameOver(true);
        }
    }

    private void pacmanEat(String ghostName) {

    }

    private void hitPellets() {

    }

    private void pacmanDie(AbstractAgent agent) {
        if (lives.get() > 1) {
            lives.decrementAndGet();
            this.view.message("Pacman died! You have " + lives.get() + " lives left");
            agent.reset();
            this.view.updateLives(lives.get());
        } else {
            this.gameOver(false);
        }
    }

    private void hostCloseConnection() {

    }
}
