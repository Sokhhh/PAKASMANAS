package pacman.util;

import static pacman.util.MapBuilder.entry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import pacman.model.Coordinate;
import pacman.model.Maze;


/**
 * A class used to construct a maze, either from local file or from a string.
 *
 * @version 1.0
 */
public class MazeFactory {
    /** Hide the constructor. */
    private MazeFactory() {}

    /**
     * Contains preconfigured mazes.
     *
     * @version 1.0
     */
    public static class PreConfiguredMaze {
        /** Preconfigured maze with dead ends and pellets. */
        public static final String PELLET_CLASSIC_NAME = "Small Pellet Challenge";

        /** Preconfigured maze with dead ends and pellets. */
        public static final String PELLET_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%\n"
                + "%G.       G   ....%\n"
                + "%.% % %%%%%% %.%%.%\n"
                + "%.%o% %   o% %.o%.%\n"
                + "%.%%%.%  %%% %..%.%\n"
                + "%.....  P    %..%G%\n"
                + "%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured small size maze. */
        public static final String SMALL_CLASSIC_NAME = "Classic Small Size";

        /** Preconfigured small size maze. */
        public static final String SMALL_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%%\n"
                + "%......%G  G%......%\n"
                + "%.%%...%%  %%...%%.%\n"
                + "%.%o.%........%.o%.%\n"
                + "%.%%.%.%%%%%%.%.%%.%\n"
                + "%........P.........%\n"
                + "%%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured medium-sized maze.*/
        public static final String CONTEST_CLASSIC_NAME = "Medium Size";

        /** Preconfigured medium-sized maze.*/
        public static final String CONTEST_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%%\n"
                + "%o...%........%...o%\n"
                + "%.%%.%.%%..%%.%.%%.%\n"
                + "%...... G GG%......%\n"
                + "%.%.%%.%% %%%.%%.%.%\n"
                + "%.%....% ooo%.%..%.%\n"
                + "%.%.%%.% %% %.%.%%.%\n"
                + "%o%......P....%....%\n"
                + "%%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured medium-sized maze.*/
        public static final String MEDIUM_CLASSIC_NAME = "Classic Medium Size";

        /** Preconfigured medium-sized maze.*/
        public static final String MEDIUM_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%%\n"
                + "%o...%........%...o%\n"
                + "%.%%.%.%%..%%.%.%%.%\n"
                + "%...... G GG%......%\n"
                + "%.%.%%.%% %%%.%%.%.%\n"
                + "%.%....% ooo%.%..%.%\n"
                + "%.%.%%.% %% %.%.%%.%\n"
                + "%o%......P....%....%\n"
                + "%%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured medium-sized maze.*/
        public static final String TRICKY_CLASSIC_NAME = "Classic Tricky Challenge";

        /** Preconfigured medium-sized maze.*/
        public static final String TRICKY_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%%\n"
                + "%o...%........%...o%\n"
                + "%.%%.%.%%..%%.%.%%.%\n"
                + "%.%.....%..%.....%.%\n"
                + "%.%.%%.%%  %%.%%.%.%\n"
                + "%...... GGGG%.%....%\n"
                + "%.%....%%%%%%.%..%.%\n"
                + "%.%....%  oo%.%..%.%\n"
                + "%.%....% %%%%.%..%.%\n"
                + "%.%...........%..%.%\n"
                + "%.%%.%.%%%%%%.%.%%.%\n"
                + "%o...%...P....%...o%\n"
                + "%%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured large-sized maze.*/
        public static final String ORIGINAL_CLASSIC_NAME = "Classic Large Size";

        /** Preconfigured large-sized maze.*/
        public static final String ORIGINAL_CLASSIC =
                  "%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n"
                + "%............%%............%\n"
                + "%.%%%%.%%%%%.%%.%%%%%.%%%%.%\n"
                + "%o%%%%.%%%%%.%%.%%%%%.%%%%o%\n"
                + "%.%%%%.%%%%%.%%.%%%%%.%%%%.%\n"
                + "%..........................%\n"
                + "%.%%%%.%%.%%%%%%%%.%%.%%%%.%\n"
                + "%.%%%%.%%.%%%%%%%%.%%.%%%%.%\n"
                + "%......%%....%%....%%......%\n"
                + "%%%%%%.%%%%% %% %%%%%.%%%%%%\n"
                + "%%%%%%.%%%%% %% %%%%%.%%%%%%\n"
                + "%%%%%%.%            %.%%%%%%\n"
                + "%%%%%%.% %%%%  %%%% %.%%%%%%\n"
                + "%     .  %G  GG  G%  .     %\n"
                + "%%%%%%.% %%%%%%%%%% %.%%%%%%\n"
                + "%%%%%%.%            %.%%%%%%\n"
                + "%%%%%%.% %%%%%%%%%% %.%%%%%%\n"
                + "%............%%............%\n"
                + "%.%%%%.%%%%%.%%.%%%%%.%%%%.%\n"
                + "%.%%%%.%%%%%.%%.%%%%%.%%%%.%\n"
                + "%o..%%.......  .......%%..o%\n"
                + "%%%.%%.%%.%%%%%%%%.%%.%%.%%%\n"
                + "%%%.%%.%%.%%%%%%%%.%%.%%.%%%\n"
                + "%......%%....%%....%%......%\n"
                + "%.%%%%%%%%%%.%%.%%%%%%%%%%.%\n"
                + "%.............P............%\n"
                + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";

        /** Preconfigured empty maze. */
        public static final String EMPTY_MAZE_NAME = "Empty Maze";

        /** Preconfigured empty maze. */
        public static final String EMPTY_MAZE =
                  "%%%%%%%%%%%%%%%%%%%%%%%%%\n"
                + "%.. P  ....      ....   %\n"
                + "%..  ...  ...  ...  ... %\n"
                + "%..  ...  ...  ...  ... %\n"
                + "%..    ....      .... G %\n"
                + "%..  ...  ...  ...  ... %\n"
                + "%..  ...  ...  ...  ... %\n"
                + "%..    ....      ....  o%\n"
                + "%%%%%%%%%%%%%%%%%%%%%%%%%\n";

        /**
         * Gets the name of the mazes.
         */
        public static final Map<String, String> ITEMS = MapBuilder.map(
                entry(PELLET_CLASSIC_NAME, PELLET_CLASSIC),
                entry(SMALL_CLASSIC_NAME, SMALL_CLASSIC),
                entry(CONTEST_CLASSIC_NAME, CONTEST_CLASSIC),
                entry(MEDIUM_CLASSIC_NAME, MEDIUM_CLASSIC),
                entry(TRICKY_CLASSIC_NAME, TRICKY_CLASSIC),
                entry(ORIGINAL_CLASSIC_NAME, ORIGINAL_CLASSIC),
                entry(EMPTY_MAZE_NAME, EMPTY_MAZE)
        );
    }

    /** Preconfigured large-sized maze.*/
    public static final String CUSTOM_MAZE_NAME = "Custom Maze";

    /**
     * Reads from local file.
     *
     * @effects adds parsed {edge, Set-of-nodes-in-book} pairs to Map charsInBooks;
     *      adds parsed characters to Set chars
     * @param filename The path to the file containing the maze
     * @return a graph that models a social network
     * @throws IOException if file cannot be read or if file does not contain a valid
     *      formatted grid
     */
    public static Maze readBoardFromFile(String filename) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        return getMazeFromReader(reader);
    }

    /**
     * Reads from local file.
     *
     * @effects adds parsed {edge, Set-of-nodes-in-book} pairs to Map charsInBooks;
     *      adds parsed characters to Set chars
     * @param text a string representing a maze
     * @return a graph that models a social network
     * @throws IOException if file cannot be read or if file does not contain a valid
     *      formatted grid
     */
    public static Maze readBoardFromString(String text) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        return getMazeFromReader(reader);
    }

    /**
     * Reads from a buffered reader.
     *
     * @param reader the buffered reader object
     * @return a graph that models a social network
     * @throws IOException if file cannot be read or if file does not contain a valid
     *      formatted grid
     */
    private static Maze getMazeFromReader(BufferedReader reader) throws IOException {
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
                        row.add(Maze.EMPTY);
                        break;
                    case 'G':  // start location for ghost
                        ghosts.add(new Coordinate(x, y));
                        row.add(Maze.EMPTY);
                        break;
                    case ' ':  // empty
                        row.add(Maze.EMPTY);
                        break;
                    case '.':  // food
                        row.add(Maze.FOOD);
                        break;
                    case '0':  // power pellets
                    case 'o':  // power pellets
                        row.add(Maze.PELLETS);
                        break;
                    case '+':  // walls
                    case '%':  // walls
                        row.add(Maze.WALL);
                        break;
                    default:
                        Logger.printlnf("Illegal character %c at (%d, %d)",
                            line.charAt(x), x, data.size());
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
                    grid[i][j] = Maze.EMPTY;
                } else {
                    grid[i][j] = data.get(i).get(j);
                }
            }
        }

        Maze maze = null;
        try {
            maze = new Maze(width, y, grid, pacman, ghosts);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }

        return maze;
    }
}