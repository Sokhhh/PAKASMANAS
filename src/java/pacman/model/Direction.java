package pacman.model;

/**
 * Represents a direction of moving in the maze.
 *
 * @version 1.1
 */
public enum Direction {
    /** Direction up. */
    UP,
    /** Direction down. */
    DOWN,
    /** Direction left. */
    LEFT,
    /** Direction right. */
    RIGHT,
    /** Stops. */
    STOP;

    /**
     * Gets the next direction in clockwise rotation.
     *
     * @return the next direction in clockwise rotation.
     */
    public Direction getClockwise() {
        switch (this) {
            case UP:
                return RIGHT;
            case RIGHT:
                return DOWN;
            case DOWN:
                return LEFT;
            case LEFT:
                return UP;
            default:
                return STOP;
        }
    }

    /**
     * Gets the next direction in counter clockwise rotation.
     *
     * @return the next direction in counter clockwise rotation.
     */
    public Direction getCounterClockwise() {
        switch (this) {
            case UP:
                return LEFT;
            case LEFT:
                return DOWN;
            case DOWN:
                return RIGHT;
            case RIGHT:
                return UP;
            default:
                return STOP;
        }
    }

    /**
     * Gets the reverse of the current direction.
     *
     * @return the reverse of the current
     */
    public Direction reverse() {
        switch (this) {
            case UP:
                return DOWN;
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            case RIGHT:
                return DOWN;
            default:
                return STOP;
        }
    }

    /**
     * Gets the difference in x coordinate if an action is taken in current
     * direction.
     *
     * @return the difference in x coordinate if an action
     */
    public int getDirectionX() {
        switch (this) {
            case LEFT:
                return -1;
            case RIGHT:
                return 1;
            default:
                return 0;
        }

    }

    /**
     * Gets the difference in y coordinate if an action is taken in current
     * direction.
     *
     * @return the difference in y coordinate if an action
     */
    public int getDirectionY() {
        switch (this) {
            case UP:
                return -1;
            case DOWN:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Checks if the direction is perpendicular to the current direction.
     *
     * @param newDirection the direction to be checked
     * @return {@code true} if the direction is perpendicular to the current
     *      direction and {@code false} otherwise
     */
    public boolean isPerpendicular(Direction newDirection) {
        switch (this) {
            case UP:
            case DOWN:
                return newDirection == LEFT || newDirection == RIGHT;
            case LEFT:
            case RIGHT:
                return newDirection == UP || newDirection == DOWN;
            default:
                return false;
        }
    }
}
