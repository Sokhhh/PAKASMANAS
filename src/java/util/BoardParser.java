package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
     * @throws IOException if file cannot be read of file not a CSV file
     */
    public static Board readBoard(String filename) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line = null;

        int y = 0;
        while ((line = reader.readLine()) != null) {
            if (line.charAt(0) == ';') {
                continue;  // stands for a comment line
            }

            for (int x = 0; x < line.length(); x++) {
                switch (line.charAt(x)) {
                    case
                }
            }

            y++;
        }


        return graph;
    }
}