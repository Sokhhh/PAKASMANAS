package gol.viewer;

/**
 * This interface defines the general behavior for sending messages to the user.
 *
 * @version 1.0
 */
public interface UserInteraction {
    /**
     * This function shows an alert message to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     * @param text the detail message string
     */
    void alert(String text);

    /**
     * This function shows a notification dialog to the user.
     *
     * <p>- requires: {@code text != null}
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     * @param text the detail message string
     */
    void message(String text);

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
    void notification(String text);

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
    boolean ask(String text);

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
    boolean askOkCancel(String text);
}
