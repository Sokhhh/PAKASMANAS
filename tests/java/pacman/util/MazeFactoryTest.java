package pacman.util;

import org.junit.Test;
import pacman.agents.*;
import pacman.algorithms.NullAlgorithm;
import pacman.controller.FakeMazeController;
import pacman.model.Coordinate;
import pacman.model.Maze;

import javax.swing.*;

import java.io.IOException;

public class MazeFactoryTest {
    public void previewBoard(Maze maze) {
        JFrame frame = new JFrame();
        MazePanel mazePanel = new MazePanel(maze);
        int i = 0;
        for (Coordinate coordinate: maze.getPacmanStartLocation()) {
            mazePanel.addAgent(new UserControlledPacmanAgent(new FakeMazeController(),
                    maze, coordinate.getX(), coordinate.getY(), i,
                    new NullAlgorithm(maze)), false);
            i++;
        }
        i = 0;
        for (Coordinate coordinate: maze.getGhostsStartLocation()) {
            mazePanel.addAgent(new GhostAgent(new FakeMazeController(),
                            maze, coordinate.getX(), coordinate.getY(), "red",
                            new NullAlgorithm(maze)), false);
            i++;
        }
        frame.setContentPane(mazePanel);
        frame.revalidate();
        frame.repaint();
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while (true) {}
    }

    @Test
    public void previewTest() throws IOException {
        Maze maze = MazeFactory.readBoardFromFile("data/trickyClassic.lay");
        System.out.printf("Maze size is: height = %d, width = %d",
                maze.getHeight(), maze.getWidth());
        previewBoard(maze);
    }
}