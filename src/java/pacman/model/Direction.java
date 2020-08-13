package pacman.model;

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

    public boolean isRotate(Direction newDirection) {
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
