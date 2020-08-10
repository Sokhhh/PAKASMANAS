package view;

import static model.Board.SURROUNDING_DOWN;
import static model.Board.SURROUNDING_LEFT;
import static model.Board.SURROUNDING_RIGHT;
import static model.Board.SURROUNDING_UP;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.Board;
import util.BoardParser;

public class MazePanel extends JPanel {

    private final Board board;

    public static final int BLOCK_SIZE = 50;

    /**
     * Creates a panel that contains the maze.
     *
     * @param maze
     */
    public MazePanel(Board maze) {
        this.board = maze;
        this.setPreferredSize(new Dimension(board.getWidth() * BLOCK_SIZE,
            board.getHeight() * BLOCK_SIZE));
    }


    private void drawMaze(Graphics2D g2d) {

        int x, y;

        for (y = 0; y < board.getHeight(); y++) {
            for (x = 0; x < board.getWidth(); x++) {

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));

                boolean[] surr = board.shouldDrawBorderSurroundings(x, y);

                int drawX = x * BLOCK_SIZE;
                int drawY = y * BLOCK_SIZE;

                if (surr[SURROUNDING_LEFT]) {
                    g2d.drawLine(drawX, drawY, drawX, drawY + BLOCK_SIZE - 1);
                }

                if (surr[SURROUNDING_UP]) {
                    g2d.drawLine(drawX, drawY, drawX + BLOCK_SIZE - 1, drawY);
                }

                if (surr[SURROUNDING_RIGHT]) {
                    g2d.drawLine(drawX + BLOCK_SIZE - 1, drawY, drawX + BLOCK_SIZE - 1,
                            drawY + BLOCK_SIZE - 1);
                }

                if (surr[SURROUNDING_DOWN]) {
                    g2d.drawLine(drawX, drawY + BLOCK_SIZE - 1, drawX + BLOCK_SIZE - 1,
                            drawY + BLOCK_SIZE - 1);
                }

                // if ((screenData[i] & 16) != 0) {
                //     g2d.setColor(dotColor);
                //     g2d.fillRect(drawX + 11, drawY + 11, 2, 2);
                // }

            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawMaze((Graphics2D) g);
    }

    public static void main(String[] args) throws IOException {

        JFrame frame = new JFrame();
        frame.setContentPane(new MazePanel(BoardParser.readBoard("data/small_board.txt")));
        frame.pack();
        frame.setVisible(true);

    }
}
