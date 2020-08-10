package model;


/**
 * This class is an ADT that represents a pacman map. The data is represented by a
 * 2-dimensional array. Data located at {@code data[y][x]} contains the detail of the
 * cell: (x,y) represents the position on the map with x horizontal y vertical and the
 * origin (0,0) in the upper left corner.
 *
 * @version 1.0
 */
public class Board {
    /** Contains the height of the grid. */
    private final int height;

    /** Contains the width of the grid. */
    private final int width;

    /** Represents that the cell is empty. */
    public static final int EMPTY = 0;

    /** Represents that the cell is occupied by a wall. */
    public static final int WALL = -1;

    /** Represents that the cell is occupied by a food. */
    public static final int FOOD = 1;

    /** Represents that the cell is occupied by a power pellet. */
    public static final int PELLETS = 2;

    public static final int INVALID = -2;

    /**
     * Contains the detail of the grid. Data is accessed via grid[y][x] where (x,y)
     * are positions on a Pacman map with x horizontal, y vertical and the origin (0,0)
     * in the upper left corner.
     */
    private final int[][] data;

    /**
     * This is the constructor of the grid, which creates an empty board.
     *
     * @requires {@code width} >= 0 &amp {@code height} >= 0
     * @modifies {@link #width}, {@link #height}
     * @effects {@code this.width = width},
     *          {@code this.height = height}
     * @param width the width of the grid
     * @param height the height of the grid
     */
    public Board(final int width, final int height) {
        this.height = height;
        this.width = width;
        this.data = new int[height][width];
    }

    /**
     * This is the constructor of the grid, which creates a board with specified
     * initial values.
     *
     * @requires {@code width} >= 0 &amp {@code height} >= 0 &amp data.length == height
     *          &amp for row in data row.length == width
     * @modifies {@link #width}, {@link #height}, {@link #data}
     * @effects {@code this.width = width},
     *          {@code this.height = height}
     *          {@code this.data = data}
     * @param width the width of the grid
     * @param height the height of the grid
     * @param data the pre-configured grid detail
     */
    public Board(final int width, final int height, final int[][] data) {
        this.height = height;
        this.width = width;
        this.data = data;
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
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int get(int x, int y) {
        if (y >= height || y < 0 || x >= width || x < 0) {
            return INVALID;
        }
        return this.data[y][x];
    }

    public boolean isValidDirection(int x, int y, final Direction d) {
        return this.get(x + d.getDirectionX(), y + d.getDirectionY()) > Board.WALL;
    }

    public static final int SURROUNDING_RIGHT = 0;
    public static final int SURROUNDING_DOWN = 1;
    public static final int SURROUNDING_LEFT = 2;
    public static final int SURROUNDING_UP = 3;

    public boolean[] shouldDrawBorderSurroundings(int x, int y) {
        boolean[] surroundings = new boolean[4];

        if (this.get(x, y) == WALL) {
            surroundings[SURROUNDING_RIGHT] = this.get(x + 1, y) >= EMPTY; // draw right
            surroundings[SURROUNDING_DOWN] = this.get(x, y + 1) >= EMPTY; // draw down
            surroundings[SURROUNDING_LEFT] = this.get(x - 1, y) >= EMPTY; // draw left
            surroundings[SURROUNDING_UP] = this.get(x, y - 1) >= EMPTY; // draw up
        }

        return surroundings;
    }

    // public Coordinate[] getPacmanStartLocation() {
    //
    // }
}
