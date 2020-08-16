package pacman.algorithms;

import static pacman.util.MapBuilder.entry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import pacman.model.Maze;
import pacman.util.MapBuilder;

/**
 * Defines an utility for creating algorithms.
 *
 * @version 1.0
 */
public class AlgorithmFactory {
    /** The maze of the game. */
    protected Maze maze;

    /** Applying the Interning Pattern and stores existing instances. */
    private HashMap<String, AbstractAlgorithm> library;

    /**
     * Contains the list of supported algorithms in this application.
     */
    public static final class SupportedAlgorithms {

        /** An algorithm that let the agent stay still at any time. */
        public static final String NULL_ALGORITHM = "Stay still";

        /** Creator of an algorithm that let the agent stay still at any time. */
        private static final Function<Maze, AbstractAlgorithm> NULL_ARG_GENERATOR =
                NullAlgorithm::new;

        /** An algorithm that let the agent random choose an action at each step. */
        public static final String RANDOM_ALGORITHM = "Random walk";

        /**
         * Creator of an algorithm that let the agent random choose an action at
         * each step.
         */
        private static final Function<Maze, AbstractAlgorithm> RANDOM_ALG_GENERATOR =
                RandomSelectionAlgorithm::new;

        /**
         * An algorithm that let the agent applies the dfs algorithm at each
         * step.
         */
        public static final String DFS_ALGORITHM = "DFS/BFS algorithm";

        /**
         * Creator of an algorithm that let the agent applies the dfs algorithm
         * at each step.
         */
        private static final Function<Maze, AbstractAlgorithm> DFS_ALG_GENERATOR =
                DfsAlgorithm::new;

        /**
         * An algorithm that let the agent applies the greedy algorithm at each
         * step.
         */
        public static final String GREEDY_ALGORITHM = "Greedy algorithm";

        /**
         * Creator of an algorithm that let the agent applies the greedy algorithm
         * at each step.
         */
        private static final Function<Maze, AbstractAlgorithm> GREEDY_ALG_GENERATOR =
                GreedyAlgorithm::new;

        /**
         * An algorithm that let the agent applies the greedy algorithm at each
         * step.
         */
        public static final String MINIMAX_ALGORITHM = "Minimax algorith";

        /**
         * Creator of an algorithm that let the agent applies the greedy algorithm
         * at each step.
         */
        private static final Function<Maze, AbstractAlgorithm> MINIMAX_ALG_GENERATOR =
                MinimaxAlgorithm::new;

        /**
         * Contains a map to get the generators.
         */
        public static final Map<String, Function<Maze, AbstractAlgorithm>> values
                = MapBuilder.map(
                    entry(NULL_ALGORITHM, NULL_ARG_GENERATOR),
                    entry(RANDOM_ALGORITHM, RANDOM_ALG_GENERATOR),
                    entry(DFS_ALGORITHM, DFS_ALG_GENERATOR),
                    entry(GREEDY_ALGORITHM, GREEDY_ALG_GENERATOR)
        );
    }

    /**
     * Creates a new AlgorithmFactory for a specified maze.
     *
     * @param maze the maze of the game
     */
    public AlgorithmFactory(Maze maze) {
        this.maze = maze;
        this.library = new HashMap<>();
    }

    /**
     * Creates a search algorithm for the specified maze with a given algorithm name.
     *
     * @param name the name of the algorithm
     * @return a search algorithm for the specified maze with a given algorithm name
     * @throws IllegalArgumentException if the name is not one of
     *      {@link SupportedAlgorithms#values}
     */
    public AbstractAlgorithm createAlgorithm(String name) throws IllegalArgumentException {
        if (!SupportedAlgorithms.values.containsKey(name)) {
            throw new IllegalArgumentException("Unknown algorithm name.");
        }
        return library.computeIfAbsent(name,
            k -> SupportedAlgorithms.values.get(k).apply(maze));
    }
}
