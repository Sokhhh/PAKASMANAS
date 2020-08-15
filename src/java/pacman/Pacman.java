package pacman;

import pacman.controller.PacmanController;
import pacman.viewer.GUIViewer;

public class Pacman {
    public static void main(String[] args) {
        PacmanController controller = new PacmanController();
        GUIViewer viewer = new GUIViewer(controller);
        controller.setView(viewer);
    }
}
