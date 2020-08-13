package pacman;

public class Pacman {
    public static void main(String[] args) {
        GUIController controller = new GUIController();
        GUIViewer viewer = new GUIViewer(controller);
        controller.setView(viewer);
    }
}
