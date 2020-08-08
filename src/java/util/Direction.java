package util;

/**
 * Represents the direction of placing a ship.
 */
public enum Direction {
    /** Direction up. */
    UP,
    /** Direction down. */
    DOWN,
    /** Direction left. */
    LEFT,
    /** Direction write. */
    RIGHT;

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
            default:
                return UP;
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
            default:
                return UP;
        }
    }
}
