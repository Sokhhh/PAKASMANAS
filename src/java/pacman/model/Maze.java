package pacman.model;

import java.util.*;

import pacman.util.Logger;

/**
 * This class is an ADT that represents a pacman map. The data is represented by a
 * 2-dimensional array. Data located at {@code data[y][x]} contains the detail of the
 * cell: (x,y) represents the position on the map with x horizontal y vertical and the
 * origin (0,0) in the upper left corner.
 *
 * @version 1.0
 */
public class Maze {

    /** Contains the height of the grid. */
    protected final int height;

    /** Contains the width of the grid. */
    protected final int width;

    /** Contains the start locations for pacmans. */
    protected final Set<Coordinate> pacmanStartLocations;

    /** Contains the start locations for ghosts. */
    protected final Set<Coordinate> ghostStartLocations;

    /** Contains the locations for pacman. */
    protected final Map<Integer, Coordinate> pacmanLocations;

    /** Contains the scores for each pacman. */
    protected final Map<Integer, Integer> pacmanScores;

    /** Contains the locations for ghosts. */
    protected final Map<String, Coordinate> ghostLocations;

    /** Contains the remaining scared time for each ghost in the maze. */
    protected final Map<String, Integer> ghostScaredTimes;

    /** Contains the remaining scared timer for each ghost in the maze. */
    protected final Map<String, Timer> ghostScaredTimers;

    /** Contains the locations for foods in the maze. */
    protected final Set<Coordinate> foods;

    /** Contains the locations for pellets in the maze. */
    protected final Set<Coordinate> pellets;

    /** Contains the locations for walls in the maze. */
    protected final Set<Coordinate> walls;

    /** Represents that the cell is out of bounds. */
    public static final int INVALID = -2;

    /** Represents that the cell is occupied by a wall. */
    public static final int WALL = -1;

    /** Represents that the cell is empty. */
    public static final int EMPTY = 0;

    /** Represents that the cell is occupied by a food. */
    public static final int FOOD = 1;

    /** Represents that the cell is occupied by a power pellet. */
    public static final int PELLETS = 2;

    /** Represents that the cell is occupied by a pacman. */
    public static final int PACMAN = 3;

    /** Represents that the cell is occupied by a ghost (both scared and normal). */
    public static final int GHOST = 4;

    /** Represents that the cell is occupied by a scared ghost. */
    public static final int SCARED_GHOST = 5;

    /** Represents the scores added when eating a food. */
    public static final int[] SCORES = {-1, 10, 100, 0, 0, 200};

    /** 
     * Contains the index representing the edge on top of the cell in function
     * {@link #shouldDrawEdge(int, int)}.
     */
    public static final int EDGE_RIGHT = 0;
    
    /** 
     * Contains the index representing the edge below the cell in function
     * {@link #shouldDrawEdge(int, int)}.
     */
    public static final int EDGE_DOWN = 1;

    /**
     * Contains the index representing the edge on left of the cell in function
     * {@link #shouldDrawEdge(int, int)}.
     */
    public static final int EDGE_LEFT = 2;

    /**
     * Contains the index representing the edge on right of the cell in function
     * {@link #shouldDrawEdge(int, int)}.
     */
    public static final int EDGE_UP = 3;

    /**
     * Contains the detail of the grid. Data is accessed via grid[y][x] where (x,y)
     * are positions on a Pacman map with x horizontal, y vertical and the origin (0,0)
     * in the upper left corner.
     */
    private final int[][] data;

    /**
     * This is the constructor of the grid, which creates a board with specified
     * initial values.
     *
     * @requires {@code width} >= 0 &amp {@code height} >= 0 &amp data.length == height
     *          &amp for row in data row.length == width
     * @modifies {@link #width}, {@link #height}, {@link #data}, {@link #pacmanStartLocations},
     *      {@link #ghostStartLocations}
     * @effects {@code this.width = width},
     *          {@code this.height = height}
     *          {@code this.data = data}
     *          {@code this.pacman = pacman}
     *          {@code this.ghosts = ghosts}
     * @param width the width of the grid
     * @param height the height of the grid
     * @param data the pre-configured grid detail
     * @param pacmanStartLocations the pre-configured starter positions for pacman
     * @param ghostStartLocations the pre-configured starter positions for ghosts
     */
    public Maze(final int width, final int height, final int[][] data,
                final Set<Coordinate> pacmanStartLocations, final Set<Coordinate> ghostStartLocations) {
        this.height = height;
        this.width = width;
        this.data = data;
        this.foods = new HashSet<>();
        this.pellets = new HashSet<>();
        this.walls = new HashSet<>();
        for (int y = 0; y < this.data.length; y++) {
            for (int x = 0; x < this.data[y].length; x++) {
                if (this.data[y][x] == Maze.FOOD) {
                    foods.add(new Coordinate(x, y));
                } else if (this.data[y][x] == Maze.PELLETS) {
                    pellets.add(new Coordinate(x, y));
                } else if (this.data[y][x] == Maze.WALL) {
                    walls.add(new Coordinate(x, y));
                }
            }
        }
        this.pacmanStartLocations = pacmanStartLocations;
        this.pacmanLocations = new HashMap<>();
        this.pacmanScores = new HashMap<>();
        this.ghostStartLocations = ghostStartLocations;
        this.ghostLocations = new HashMap<>();
        this.ghostScaredTimes = new HashMap<>();
        this.ghostScaredTimers = new HashMap<>();
        this.checkRep();
    }

    /**
     * Checks the representation invariant.
     *
     * @throws RuntimeException if the representation invariant is not satisfied
     */
    private void checkRep() throws RuntimeException {
        if (data.length != height) {
            throw new RuntimeException("The height of the board does not equal to the "
                + "height of data");
        }
        for (int[] row: data) {
            if (row.length != width) {
                throw new RuntimeException("The width of the board does not equal to "
                    + "the width of data");
            }
        }
        for (Coordinate coordinate: pacmanStartLocations) {
            if (!isValidBlock(coordinate.getX(), coordinate.getY())) {
                throw new RuntimeException(
                    "The starter location of pacman at " + coordinate
                        + " is located on a wall or out of bounds.");
            }
        }
        if (pacmanStartLocations.size() < 1) {
            throw new RuntimeException("There should at lease be one pacman");
        }
        for (Coordinate coordinate: ghostStartLocations) {
            if (!isValidBlock(coordinate.getX(), coordinate.getY())) {
                throw new RuntimeException(
                    "The starter location of ghost at " + coordinate
                        + " is located on a wall or out of bounds.");
            }
        }
    }

    /**
     * This function gets the height of the maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return the height of the maze
     */
    public int getHeight() {
        return height;
    }

    /**
     * This function gets the width of the maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return the height of the maze
     */
    public int getWidth() {
        return width;
    }

    /**
     * This function will get all starter locations of pacman in this maze.
     * 
     * @requires None
     * @modifies None
     * @effects None
     * @return an array containing all starter locations of pacman in this maze
     */
    public Coordinate[] getPacmanStartLocation() {
        return this.pacmanStartLocations.toArray(new Coordinate[0]);
    }

    /**
     * This function returns the position of all pacman in the maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return locations of all pacman in this maze
     */
    public Map<Integer, Coordinate> getPacmanLocation() {
        return new HashMap<>(this.pacmanLocations);
    }

    /**
     * Gets the number of visible pacman in the maze.
     * @return the number of visible pacman in the maze
     */
    public int getPacmanNum() {
        return this.pacmanLocations.size();
    }

    /**
     * Gets a copy of pacman scores.
     *
     * @return a copy of scores for each pacman.
     */
    public Map<Integer, Integer> getPacmanScores() {
        return new HashMap<>(pacmanScores);
    }

    /**
     * This function will get all starter locations of ghosts in this maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return an array containing all starter locations of ghosts in this maze
     */
    public Coordinate[] getGhostsStartLocation() {
        return this.ghostStartLocations.toArray(new Coordinate[0]);
    }

    /**
     * This function will get the locations for all ghosts.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a collection containing all locations of ghosts in this maze
     */
    public Map<String, Coordinate> getGhostsLocation() {
        return new HashMap<>(ghostLocations);
    }

    /**
     * This function will get the scared time for all ghosts.
     *
     * @return  the scared time for all ghosts
     */
    public Map<String, Integer> getGhostScaredTimes() {
        return new HashMap<>(ghostScaredTimes);
    }

    public Collection<String> getGhostNames() {
        return ghostScaredTimes.keySet();
    }

    public Collection<String> getVisibleGhostNames() {
        return ghostLocations.keySet();
    }

    /**
     * This function will get a copy of food locations.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a set containing all locations of foods in this maze
     */
    public Set<Coordinate> getFoods() {
        return this.foods;
    }

    /**
     * This function will get a copy of pellet locations.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return an array containing all locations of pellets in this maze
     */
    public Coordinate[] getPellets() {
        return this.pellets.toArray(new Coordinate[0]);
    }

    /**
     * This function will get the number of foods remaining in this maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return number of foods remaining in this maze.
     */
    public int getFoodsNum() {
        return this.foods.size();
    }

    /**
     * This function will get the number of pellets remaining in this maze.
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return number of pellets remaining in this maze.
     */
    public int getPelletsNum() {
        return this.pellets.size();
    }

    /**
     * This function gets the content of a certain block in the cell.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies None
     * @effects None
     * @return the content of the block located at (x, y). Should be one of
     *       {@link #INVALID}, {@link #WALL}, {@link #EMPTY}, {@link #FOOD},
     *       {@link #PELLETS}
     */
    public int get(int x, int y) {
        if (y < 0 || y >= height || x < 0 || x >= width) {
            return INVALID;
        }
        return this.data[y][x];
    }

    /**
     * This function gets the content of a certain block in the cell, including
     * ghosts and pacman.
     *
     * @param pos the coordinate of the block
     * @requires None
     * @modifies None
     * @effects None
     * @return the content of the block located at (x, y). Should be one of
     *       {@link #INVALID}, {@link #WALL}, {@link #EMPTY}, {@link #FOOD},
     *       {@link #PELLETS}, {@link #PACMAN}, {@link #GHOST}
     */
    public int get(Coordinate pos) {
        if (pacmanLocations.containsValue(pos)) {
            return PACMAN;
        } else if (ghostLocations.containsValue(pos)) {
            return GHOST;
        }
        return this.get(pos.getX(), pos.getY());
    }

    /**
     * Checks if a certain block is valid for a character to stay (not wall).
     * 
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies None
     * @effects None
     * @return {@code true} if the coordinate is not a wall or out of bounds
     *        and {@code false} otherwise
     */
    public boolean isValidBlock(int x, int y) {
        return this.get(x, y) >= EMPTY;
    }

    /**
     * Checks if a certain direction is valid for a character to move from a certain 
     * block.
     * 
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies None
     * @effects None
     * @return {@code true} if the direction is valid and {@code false} otherwise
     */
    public boolean isValidDirection(int x, int y, final Direction d) {
        return this.isValidBlock(x + d.getDirectionX(), y + d.getDirectionY());
    }

    /**
     * Checks if the edges should be drawn on a certain block.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies None
     * @effects None
     * @return an array of booleans with each containing either {@code true} if the
     *      edge on that direction should be drawn and {@code false} otherwise
     */
    public boolean[] shouldDrawEdge(int x, int y) {
        boolean[] surroundings = new boolean[4];

        if (this.get(x, y) == WALL) {
            surroundings[EDGE_RIGHT] = this.isValidBlock(x + 1, y); // draw right
            surroundings[EDGE_DOWN] = this.isValidBlock(x, y + 1); // draw down
            surroundings[EDGE_LEFT] = this.isValidBlock(x - 1, y); // draw left
            surroundings[EDGE_UP] = this.isValidBlock(x, y - 1); // draw up
        }

        return surroundings;
    }

    /**
     * Gets the local directions for an agent.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the legal directions from the coordinate
     */
    public List<Direction> getLegalActions(int x, int y) {
        List<Direction> actions = getLegalActionsIncludeStop(x, y);
        actions.remove(Direction.STOP);
        return actions;
    }

    /**
     * Gets the local directions for an agent.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the legal directions from the coordinate
     */
    public List<Direction> getLegalActionsIncludeStop(int x, int y) {
        List<Direction> actions = new ArrayList<>();
        for (Direction d: Direction.values()) {
            if (isValidDirection(x, y, d)) {
                actions.add(d);
            }
        }
        return actions;
    }

    /**
     * Gets a valid neighbor of a block in case the block is occupied.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return coordinate of a legal neighbor
     */
    public Coordinate getRandomNeighbor(int x, int y) {
        for (Direction d: Direction.values()) {
            if (isValidDirection(x, y, d)) {
                return new Coordinate(x + d.getDirectionX(), y + d.getDirectionY());
            }
        }
        return null;
    }

    /**
     * Gets called when pacman visits a block.
     *
     * @param index the index of the pacman
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies {@link #data}, {@link #foods}, {@link #pellets},
     *          {@link #pacmanLocations}, {@link #pacmanScores}
     * @effects food/pellet located on the specified block is removed
     *          pacman index is added to locations and scores if not added before
     *          pacman location updated
     * @return scores earned in this move
     */
    public int pacmanVisit(final int index, final int x, final int y) {
        if (!this.isValidBlock(x, y)) {
            Logger.printf("Pacman shouldn't be at %s, check what happened?",  new Coordinate(x, y));
            return 0;
        }
        if (!this.pacmanScores.containsKey(index)) {
            this.pacmanScores.put(index, 0);
        }
        this.pacmanLocations.put(index, new Coordinate(x, y));
        int scores = 0;
        switch (this.get(x, y)) {
            case FOOD:
                foods.remove(new Coordinate(x, y));
                scores += SCORES[FOOD];
                break;
            case PELLETS:
                pellets.remove(new Coordinate(x, y));
                scores += SCORES[PELLETS];
                break;
            case EMPTY:
            case PACMAN:
                scores += SCORES[EMPTY];
                break;
            default:
                break;
        }
        this.data[y][x] = EMPTY;
        for (String ghostName: ghostLocations.keySet()) {
            if (ghostLocations.get(ghostName).equals(new Coordinate(x, y))) {
                if (ghostScaredTimes.get(ghostName) > 0) {
                    scores += SCORES[SCARED_GHOST];
                }
            }
        }

        this.pacmanScores.put(index, this.pacmanScores.getOrDefault(index, 0) + scores);
        return scores;
    }

    /**
     * Removes a pacman from the game maze.
     *
     * @param index the index of the pacman
     * @requires None
     * @modifies {@link #pacmanLocations}, {@link #pacmanScores}
     * @effects pacman index is removed from locations and scores
     */
    public void removePacman(int index) {
        this.pacmanLocations.remove(index);
    }

    /**
     * Gets called when ghost visits a block.
     *
     * @param name the name of the ghost
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @requires None
     * @modifies {@link #ghostLocations}, {@link #ghostScaredTimes}
     * @effects ghost name is added to locations and scared times if not
     *      added before; ghost location is updated
     */
    public void ghostVisit(final String name, final int x, final int y) {
        if (!this.ghostLocations.containsKey(name)) {
            // first time visit (aka "add")
            this.ghostScaredTimes.put(name, 0);
        }
        this.ghostLocations.put(name, new Coordinate(x, y));
    }

    /**
     * Removes a ghost from the game maze.
     *
     * @param name the name of the ghost
     * @requires None
     * @modifies {@link #ghostLocations}, {@link #ghostScaredTimes}
     * @effects ghost name is removed from locations and scared times
     */
    public void removeGhost(String name) {
        this.ghostLocations.remove(name);
    }

    /**
     * Set one ghost to start the "Scared" state.
     *
     * @param ghostName the name of the ghost
     * @param defaultScaredTime the default scared time duration
     */
    public void setGhostScared(String ghostName, int defaultScaredTime) {
        final int[] scaredTime = {defaultScaredTime};
        ghostScaredTimes.put(ghostName, scaredTime[0]);
        Timer countDownGhostBuster = new Timer();
        countDownGhostBuster.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                scaredTime[0]--;
                ghostScaredTimes.put(ghostName, scaredTime[0]);
            }
        }, 1000, 1000);
        if (ghostScaredTimers.get(ghostName) != null) {
            ghostScaredTimers.get(ghostName).cancel();
        }
        ghostScaredTimers.put(ghostName, countDownGhostBuster);
    }

    /**
     * Resets a ghost's scared time and timer.
     *
     * @param ghostName the name of a ghost
     */
    public void resetGhost(String ghostName) {
        ghostScaredTimers.get(ghostName).cancel();
        ghostScaredTimers.put(ghostName, null);
        ghostScaredTimes.put(ghostName, 0);
    }

    public boolean isLose(int x, int y) {
        for (String ghostName: ghostLocations.keySet()) {
            if (ghostLocations.get(ghostName).equals(new Coordinate(x, y))
                    && ghostScaredTimes.get(ghostName) <= 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isWin(int x, int y) {
        return foods.contains(new Coordinate(x, y)) && foods.size() == 1;
    }
}
