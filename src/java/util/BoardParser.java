package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import model.Board;
import model.Coordinate;

/**
 * A class used to load a board from local file.
 *
 * @version 1.0
 */
public class BoardParser {
    /**
     * Reads from local file.
     *
     * @effects adds parsed {edge, Set-of-nodes-in-book} pairs to Map charsInBooks;
     *      adds parsed characters to Set chars
     * @param filename The path to the "CSV" file that contains the {node, node} pairs
     * @return a graph that models a social network
     * @throws IOException if file cannot be read or if file does not contain a valid
     * formatted grid
     */
    public static Board readBoard(String filename) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line = null;

        Set<Coordinate> pacman = new HashSet<>();
        Set<Coordinate> ghosts = new HashSet<>();
        ArrayList<ArrayList<Integer>> data = new ArrayList<>();

        int y = 0;
        int width = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0 || line.charAt(0) == ';') {
                continue;  // stands for a comment line
            }

            line = line.trim();

            ArrayList<Integer> row = new ArrayList<>();
            for (int x = 0; x < line.length(); x++) {
                switch (line.charAt(x)) {
                    case 'P':  // start location for pacman
                        pacman.add(new Coordinate(x, y));
                        row.add(Board.EMPTY);
                        break;
                    case 'G':  // start location for ghost
                        ghosts.add(new Coordinate(x, y));
                        row.add(Board.EMPTY);
                        break;
                    case ' ':  // empty
                        row.add(Board.EMPTY);
                        break;
                    case '.':  // food
                        row.add(Board.FOOD);
                        break;
                    case '0':  // power pellets
                        row.add(Board.PELLETS);
                        break;
                    case '+':  // walls
                        row.add(Board.WALL);
                        break;
                    default:
                        Logger.printf("Illegal character %c at %s:(%d, %d)\n",
                            line.charAt(x), filename, x, data.size());
                }
            }

            if (line.length() > width) {
                width = line.length();
            }

            data.add(row);
            y++;
        }


        int[][] grid = new int[y][width];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i >= data.size() || j >= data.get(i).size()) {
                    grid[i][j] = Board.EMPTY;
                } else {
                    grid[i][j] = data.get(i).get(j);
                }
            }
        }

        Board board = new Board(width, y, grid);

        return board;
    }
}