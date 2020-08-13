package pacman.components;

import static pacman.model.Maze.EDGE_DOWN;
import static pacman.model.Maze.EDGE_LEFT;
import static pacman.model.Maze.EDGE_RIGHT;
import static pacman.model.Maze.EDGE_UP;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;
import pacman.model.Maze;
import pacman.util.PacmanTheme;

/**
 * This is a component to show the maze of the game.
 *
 * @version 1.0
 */
public class MazePanel extends JPanel {

    /**
     * Contains the maze of the game.
     */
    private final Maze maze;

    /**
     * Contains the screen size of each edge of a block in the maze.
     */
    public static final int BLOCK_SIZE = 50;
    /**
     * Contains the screen size of the radius of food in the maze.
     */
    public static final int FOOD_RADIUS = 8;

    /**
     * Contains the screen size of the radius of pellets in the maze.
     */
    public static final int PELLETS_RADIUS = 18;

    /**
     * Creates a panel that contains the maze.
     *
     * @param maze the game maze
     */
    public MazePanel(Maze maze) {
        this.maze = maze;
        this.setPreferredSize(new Dimension(this.maze.getWidth() * BLOCK_SIZE,
            this.maze.getHeight() * BLOCK_SIZE));
        this.setLayout(null);
    }

    /**
     * Draws the maze of the game in the panel.
     *
     * @param g2d the <code>Graphics</code> object to protect
     */
    private void drawMaze(Graphics2D g2d) {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                int drawX = x * BLOCK_SIZE;
                int drawY = y * BLOCK_SIZE;
                g2d.setStroke(new BasicStroke(2));
                if (maze.get(x, y) == Maze.WALL) {
                    g2d.setColor(PacmanTheme.WALL);
                    boolean[] edges = maze.shouldDrawEdge(x, y);
                    if (edges[EDGE_LEFT]) {
                        g2d.drawLine(drawX, drawY, drawX, drawY + BLOCK_SIZE - 1);
                    }
                    if (edges[EDGE_UP]) {
                        g2d.drawLine(drawX, drawY, drawX + BLOCK_SIZE - 1, drawY);
                    }
                    if (edges[EDGE_RIGHT]) {
                        g2d.drawLine(drawX + BLOCK_SIZE - 1, drawY,
                            drawX + BLOCK_SIZE - 1,
                            drawY + BLOCK_SIZE - 1);
                    }
                    if (edges[EDGE_DOWN]) {
                        g2d.drawLine(drawX, drawY + BLOCK_SIZE - 1,
                            drawX + BLOCK_SIZE - 1,
                            drawY + BLOCK_SIZE - 1);
                    }
                } else if (maze.get(x, y) == Maze.FOOD) {
                    g2d.fillOval(drawX + BLOCK_SIZE / 2 - FOOD_RADIUS / 2,
                        drawY + BLOCK_SIZE / 2 - FOOD_RADIUS / 2, FOOD_RADIUS,
                        FOOD_RADIUS);
                } else if (maze.get(x, y) == Maze.PELLETS) {
                    g2d.fillOval(drawX + BLOCK_SIZE / 2 - PELLETS_RADIUS / 2,
                        drawY + BLOCK_SIZE / 2 - PELLETS_RADIUS / 2, PELLETS_RADIUS,
                        PELLETS_RADIUS);
                }
            }
        }
    }

    /**
     * Adds a pacman/ghost to the panel.
     *
     * @param agent the pacman/ghost
     * @param autoStart if the agent should start automatically
     */
    public void addAgent(AbstractAgent agent, boolean autoStart) {
        this.add(agent);
        if (autoStart) {
            agent.startMovingAnimation();
        }
    }

    /**
     * Calls the UI delegate's paint method, if the UI delegate is non-<code>null</code>.
     * We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes (for example, <code>Graphics.translate</code>).
     *
     * <p>If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you should not alter
     * the clip <code>Rectangle</code> or modify the transform. If you need to do these
     * operations you may find it easier to create a new <code>Graphics</code> from the
     * passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoker super's implementation you must honor the opaque property, that is if this
     * component is opaque, you must completely fill in the background in a non-opaque
     * color. If you do not honor the opaque property you will likely see visual
     * artifacts.
     *
     * <p>The passed in <code>Graphics</code> object might have a transform other than the
     * identify transform installed on it.  In this case, you might get unexpected results
     * if you cumulatively apply another transform.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint
     * @see ComponentUI
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMaze((Graphics2D) g);
    }
}
