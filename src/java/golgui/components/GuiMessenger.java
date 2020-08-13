package golgui.components;

import gol.viewer.UserInteraction;
import golgui.components.GuiComponentFactory;
import java.awt.Font;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Contains a GUI utility that can be used to handle communication with the user.
 *
 * @version 1.0
 */
public class GuiMessenger implements UserInteraction {
    /**
     * Contains the parent component.
     */
    private final JFrame parent;

    /**
     * Contains a task queue to finish different task in the application.
     */
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    /**
     * This is the constructor that creates a new Messenger instance.
     *
     * @param parent the parent JFrame
     */
    public GuiMessenger(JFrame parent) {
        this.parent = parent;
    }

    // ==================================================================================
    //                                  USER INTERACTION
    // ==================================================================================

    /**
     * This function shows an alert message to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail message string
     */
    public void alert(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("sans-serif", Font.PLAIN, 16));
        JOptionPane.showConfirmDialog(parent, label, "", JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This function shows a message dialog to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail message string
     */
    public void message(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("sans-serif", Font.PLAIN, 16));
        JOptionPane.showConfirmDialog(parent, label, "", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * This function shows a notification message to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     * @param text the detail message string
     */
    public void notification(String text) {
        exec.submit(() -> {
            // Create a dialog window
            JDialog dialog = GuiComponentFactory.createDialog(text, parent,
                false);
            dialog.setAlwaysOnTop(true);
            dialog.setOpacity(0.8f);

            // Let the dialog be visible.
            dialog.setVisible(true);
            try {
                Thread.sleep(1800);
            } catch (InterruptedException e) {
                // Ignored
            }

            // Let the dialog disappear
            GuiComponentFactory.closeDialog(dialog);
        });
    }


    /**
     * This function shows a question to the user and wait for response.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail question string
     * @return user's response. {@code true} if the user chose yes and {@code false} if
     *      the user chose no.
     */
    public boolean ask(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("sans-serif", Font.PLAIN, 16));
        int result =  JOptionPane.showConfirmDialog(parent, label, "",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * This function shows a question to the user and wait for response.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param text the detail question string
     * @return user's response. {@code true} if the user chose OK and {@code false} if
     *      the user chose CANCEL.
     */
    public boolean askOkCancel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("sans-serif", Font.PLAIN, 16));
        int result =  JOptionPane.showConfirmDialog(parent, label, "",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    /**
     * This function shows a waiting dialog/a progress bar in case the application need
     * to do some time-consuming task.
     *
     * <p>- requires: None
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @param task the task that will be executed
     * @return the result of the task
     */
    public boolean showWaitDialog(Callable<Boolean> task) {
        JDialog dialog = GuiComponentFactory.createDialog("Please wait while the "
            + "application is resizing...", null, true);
        dialog.setAlwaysOnTop(true);
        dialog.setOpacity(0.8f);
        dialog.setVisible(true);

        AtomicReference<Boolean> ret = new AtomicReference<>(false);
        try {
            ret.set(task.call());
        } catch (Exception e) {
            e.printStackTrace();
        }

        GuiComponentFactory.closeDialog(dialog);
        return ret.get();
    }
}
