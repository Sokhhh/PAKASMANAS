package pacman.util;

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;
import pacman.agents.AbstractAgent;

public class ImageInterning {

    /** Contains images with their names. */
    private static final HashMap<String, Image> imageLibrary = new HashMap<>();

    /**
     * Hide the constructor of an utility class.
     */
    private ImageInterning() {}

    /**
     * Creates a new Image object containing an image load from a local file.
     *
     * @param filename the file containing the image
     * @return a new ImageIcon object
     * @throws IOException if the file is not found
     */
    public static Image getImageInstance(String filename) throws IOException {
        if (imageLibrary.containsKey(filename)) {
            return imageLibrary.get(filename);
        }
        URL path = AbstractAgent.class.getResource("/images/" + filename);
        if (path == null) {
            throw new IOException("File \"" + filename + "\" is not found.");
        }
        Image image = new ImageIcon(path).getImage();
        imageLibrary.put(filename, image);
        return image;
    }

    /**
     * Creates a new ImageIcon object containing an image load from a local file.
     *
     * @param image the image that is about to be resized
     * @param size the size of the image; {@code null} if not required to resize
     * @return a new ImageIcon object
     */
    public static ImageIcon getResizedImageIcon(Image image, Dimension size) {
        return new ImageIcon(image.getScaledInstance(size.width, size.height,
                Image.SCALE_DEFAULT));
    }

    /**
     * Creates a new ImageIcon object containing an image load from a local file.
     *
     * @param filename the file containing the image
     * @param size the size of the image; {@code null} if not required to resize
     * @return a new ImageIcon object
     * @throws IOException if the file is not found
     */
    public static ImageIcon getImageIconFromFile(String filename, Dimension size) throws IOException {
        Image image = getImageInstance(filename);
        if (size != null) {
            return getResizedImageIcon(image, size);
        }
        return new ImageIcon(image);
    }
}
