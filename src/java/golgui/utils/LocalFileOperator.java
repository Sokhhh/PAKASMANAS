package golgui.utils;

import golgui.components.GuiComponentFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This is an object that the application uses to handle the interaction with local
 * files.
 *
 * @version <b>1.0</b>
 */
public class LocalFileOperator {
    /**
     * Contains the absolute path of the local file if the file can be found at that
     * path, or the path from user's input if such file cannot be found.
     */
    private Path fullPath;

    /**
     * The name of the local file.
     */
    private final String fileName;

    /**
     * The path of the root directory of the file.
     */
    private final String directory;

    /**
     * Constructor that creates a LocalFileOperator.
     *
     * <p>- requires: None
     *
     * <p>- modifies: {@link #fullPath},
     * {@link #fileName},
     * {@link #directory}
     *
     * <p>- effects: For example, input "data/sample.txt" will create
     *      {@link #fullPath} = "C:\User\....\data\sample.txt"
     *      {@link #fileName} = "sample.txt"
     *      {@link #directory} = "C::\User\....\data\"
     *
     *
     * @param inputPath the path which user inputs
     */
    public LocalFileOperator(String inputPath) {
        // create object of Path
        try {
            this.fullPath = Paths.get(new File(inputPath).getCanonicalPath());
        } catch (IOException e) {
            this.fullPath = Paths.get(inputPath);
        }

        // get the name of the file
        if (this.fullPath.getFileName() != null) {
            this.fileName = this.fullPath.getFileName().toString();
        } else {
            this.fileName = "";
        }

        // get the directory path
        if (this.fullPath.toFile().exists() && this.fullPath.toFile().isDirectory()) {
            this.directory = this.fullPath.toString();
        } else {
            if (this.fullPath.getParent() != null) {
                this.directory = this.fullPath.getParent().toString();
            } else {
                this.directory = ".";
            }
        }
    }

    /**
     * This function creates a file chooser and let the user choose a file as an input
     * file.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param frame the GUI window
     * @param extensions the available file extension filters
     * @return the file chose by user, null if nothing was chosen
     */
    public File browseInputFile(JFrame frame, FileNameExtensionFilter... extensions) {
        JFileChooser fileChooser = GuiComponentFactory.createFileChooser(this.fileName);
        if (extensions != null && extensions.length > 0) {
            fileChooser.setFileFilter(extensions[0]);
            for (int i = 1; i < extensions.length; i++) {
                fileChooser.addChoosableFileFilter(extensions[i]);
            }
        }
        fileChooser.setCurrentDirectory(new File(this.getDirectory()));
        int result = fileChooser.showOpenDialog(frame);
        File choice = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            // If 'Save/Open' is clicked
            choice = fileChooser.getSelectedFile();
        }

        return choice;
    }

    /**
     * This function creates a file chooser and let the user choose a folder as an
     * output folder.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param frame the GUI window
     * @return the file chose by user, null if nothing was chosen
     */
    public File browseOutputFolder(JFrame frame) {
        JFileChooser fileChooser = GuiComponentFactory.createFileChooser("");
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(this.getDirectory()));
        int result = fileChooser.showOpenDialog(frame);
        File choice = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            // If 'Save/Open' is clicked
            choice = fileChooser.getSelectedFile();
        }

        return choice;
    }

    /**
     * This function creates a file chooser and let the user choose a file as an output
     * file.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param frame the GUI window
     * @return the file chose by user, null if nothing was chosen
     */
    public File browseOutputFile(JFrame frame) {
        JFileChooser fileChooser = GuiComponentFactory.createFileChooser(this.fileName);
        fileChooser.setCurrentDirectory(new File(this.getDirectory()));
        int result = fileChooser.showSaveDialog(frame);
        File choice = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            // If 'Save/Open' is clicked
            choice = fileChooser.getSelectedFile();
        }

        return choice;
    }

    /**
     * This function returns the directory of the local file from user's input.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @return directory of the local file from user's input.
     */
    public String getDirectory() {
        if (new File(this.directory).exists()) {
            return this.directory;
        } else {
            return ".";
        }
    }

    /**
     * This function returns the filename of the local file from user's input.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @return the filename of the local file from user's input.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * This function generates the alert message when the application faces an
     * IOException.
     *
     * @param e the IOException
     * @return an alert message about this message
     */
    public static String alertIoException(IOException e) {
        return "Save failed because " + e.getMessage();
    }

    /**
     * This function gets a relative path for an absolute path. If the common path element
     *
     * @param targetPath the absolute path of the target
     * @return the relative path of the target path
     */
    public static String getRelativePath(String targetPath) {
        try {
            File working = new File(System.getProperty("user.dir"));
            Path common = working.toPath();
            File target = new File(targetPath);
            return common.relativize(Paths.get(target.getAbsolutePath())).toString();
        } catch (Exception e) {
            return targetPath;
        }
    }
}
