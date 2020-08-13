package pacman.util;

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import pacman.components.AbstractAgent;

public class ImageFactory {

    /**
     * Hide the constructor of an utility class.
     */
    private ImageFactory() {}

    /**
     * Creates a new ImageIcon object containing an image load from a local file.
     *
     * @param filename the file containing the image
     * @param size the size of the image; {@code null} if not required to resize
     * @return a new ImageIcon object
     * @throws IOException if the file is not found
     */
    public static ImageIcon getImageIconFromFile(String filename, Dimension size) throws IOException {
        URL path = AbstractAgent.class.getResource("/images/" + filename);
        if (path == null) {
            throw new IOException("File \"" + filename + "\" is not found.");
        }
        Image image = new ImageIcon(path).getImage();
        if (size != null) {
            image = image.getScaledInstance(size.width, size.height, Image.SCALE_DEFAULT);
        }
        return new ImageIcon(image);
    }
}
