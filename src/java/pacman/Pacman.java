package pacman;

import pacman.controller.PacmanController;
import pacman.viewer.GUIViewer;

/**
 * The multi-player pacman game main class.
 *
 * @version 1.0
 */
public class Pacman {
    /**
     * Main method to start the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PacmanController controller = new PacmanController();
        GUIViewer viewer = new GUIViewer(controller);
        controller.setView(viewer);
    }
}
