package view;

import static view.MazePanel.BLOCK_SIZE;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import model.Board;
import model.Direction;
import util.BoardParser;
import util.Logger;

public class AbstractCharacterIcon extends JLabel {

    private int deltaX;
    private int deltaY;
    private int coordinateX;
    private int coordinateY;
    private Direction motionDirection = Direction.STOP;
    private Board board;


    private class ChangeDirection extends AbstractAction {

        private Direction newDirection;

        public ChangeDirection(Direction newDirection) {
            this.newDirection = newDirection;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            motionDirection = newDirection;
        }
    }

    private int getLocationX() {
        return getLocation().x;
    }
    private int getLocationY() {
        return getLocation().y;
    }

    private boolean checkX() {
        return getLocation().x % (double) BLOCK_SIZE != 0;
    }
    private boolean checkY() {
        return getLocation().y % (double) BLOCK_SIZE != 0;
    }

    JPanel parent;

    public AbstractCharacterIcon(int startCoordinateX, int startCoordinateY, int delay, Board board, JPanel parent) {
        this.deltaX = 10;
        this.deltaY = 10;
        this.coordinateX = startCoordinateX;
        this.coordinateY = startCoordinateY;
        this.board = board;
        this.parent = parent;

        setKeyBindings(Direction.LEFT, KeyEvent.VK_LEFT);
        setKeyBindings(Direction.RIGHT, KeyEvent.VK_RIGHT);
        setKeyBindings(Direction.DOWN, KeyEvent.VK_DOWN);
        setKeyBindings(Direction.UP, KeyEvent.VK_UP);

        setIcon(new ImageIcon(new ImageIcon("pacman.gif").getImage().getScaledInstance(BLOCK_SIZE, BLOCK_SIZE, Image.SCALE_DEFAULT)));

        setSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        setPreferredSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        setLocation(startCoordinateX * MazePanel.BLOCK_SIZE, startCoordinateY * MazePanel.BLOCK_SIZE);
        // this.start();
    }

    private void start() {
        new javax.swing.Timer(40, new ActionListener() {
            private int counter = 0;
            private final int num = BLOCK_SIZE / deltaX;
            private Direction direction = motionDirection;

            @Override
            public void actionPerformed(ActionEvent e) {
                counter++;
                if (counter == 5) {
                    ((Timer) e.getSource()).stop();
                }

                //  Determine next screen position
                int nextScreenX = AbstractCharacterIcon.this.getLocation().x + (deltaX * direction.getDirectionX());

                //  Determine next Y position
                int nextScreenY = AbstractCharacterIcon.this.getLocation().y + (deltaY * direction.getDirectionY());

                // Check border
                boolean outOfBounds = false;
                if (nextScreenX < 0) {
                    nextScreenX = 0;
                    outOfBounds = true;
                } else if (nextScreenX + AbstractCharacterIcon.this.getSize().width > parent.getSize().width) {
                    nextScreenX = parent.getSize().width - AbstractCharacterIcon.this.getSize().width;
                    outOfBounds = true;
                }
                if (nextScreenY < 0) {
                    nextScreenY = 0;
                    outOfBounds = true;
                } else if (nextScreenY + AbstractCharacterIcon.this.getSize().height > parent.getSize().height) {
                    nextScreenY = parent.getSize().height - AbstractCharacterIcon.this.getSize().height;
                    outOfBounds = true;
                }

                //  Move the image
                int x = coordinateX;
                if (direction == Direction.LEFT) {
                    x = (int) Math.floor(nextScreenX / (double) BLOCK_SIZE);
                } else if (direction == Direction.RIGHT) {
                    x = (int) Math.ceil(nextScreenX / (double) BLOCK_SIZE);
                }

                int y = coordinateY;
                if (direction == Direction.UP) {
                    y = (int) Math.floor(nextScreenY / (double) BLOCK_SIZE);
                } else if (direction == Direction.DOWN) {
                    y = (int) Math.ceil(nextScreenY / (double) BLOCK_SIZE);
                }

                if (board.get(x, y) != Board.WALL) {
                    AbstractCharacterIcon.this.coordinateX = x;
                    AbstractCharacterIcon.this.coordinateY = y;
                    setLocation(nextScreenX, nextScreenY);
                    if (outOfBounds) {
                        direction = Direction.STOP;
                        motionDirection = Direction.STOP;
                    }
                } else {
                    setLocation(coordinateX * BLOCK_SIZE, coordinateY * BLOCK_SIZE);
                    direction = Direction.STOP;
                    motionDirection = Direction.STOP;
                }
            }
        }).start();
    }

    private void setKeyBindings(Direction newDirection, int keyCode) {
        int condition = WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = getInputMap(condition);
        ActionMap actionMap = getActionMap();

        KeyStroke keyPressed = KeyStroke.getKeyStroke(keyCode, 0, false);
        KeyStroke keyReleased = KeyStroke.getKeyStroke(keyCode, 0, true);

        inputMap.put(keyPressed, keyPressed.toString());
        inputMap.put(keyReleased, keyReleased.toString());

        actionMap.put(keyPressed.toString(), new ChangeDirection(newDirection));
        // actionMap.put(keyReleased.toString(), new ChangeDirection(newDirection));
    }

    public static void main(String[] args) throws IOException {
        JPanel panel = new JPanel();
        JFrame frame = new JFrame();

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        Board board = BoardParser.readBoard("data/small_board.txt");

        MazePanel mp = new MazePanel(board);
        mp.setLayout(null);

        AbstractCharacterIcon ta = new AbstractCharacterIcon(1, 1,  40, board, mp);
        mp.add(ta);
        frame.setContentPane(mp);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
//      frame.getContentPane().add( new TimerAnimation(10, 10, 2, 3, 1, 1, 10) );
//      frame.getContentPane().add( new TimerAnimation(10, 10, 3, 0, 1, 1, 10) );
    }
}
