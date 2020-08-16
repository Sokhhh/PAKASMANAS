package pacman.viewer;

import java.awt.CardLayout;
import javax.swing.JPanel;

/**
 * Contains a component that can hide its content.
 *
 * @version 1.0
 */
public class HideablePanel extends JPanel {

    /**
     * Contains the CardLayout object.
     */
    private final CardLayout layout;

    /**
     * Creates a new hideable panel.
     *
     * @param innerPanel the inner JPanel
     */
    public HideablePanel(JPanel innerPanel) {
        this.setBackground(innerPanel.getBackground());
        this.setOpaque(innerPanel.isOpaque());
        layout = new CardLayout();
        setLayout(layout);
        add(innerPanel, "visible");
        JPanel invisible = new JPanel();
        invisible.setBackground(innerPanel.getBackground());
        invisible.setOpaque(innerPanel.isOpaque());
        add(invisible, "invisible");
        layout.show(this, "visible");
    }

    /**
     * Shows the panel.
     */
    public void showPanel() {
        layout.show(this, "visible");
    }

    /**
     * Hides the panel.
     */
    public void hidePanel() {
        layout.show(this, "invisible");
    }

    /**
     * Makes the component content visible or invisible. Different from {@code
     * Component.setVisible}, this method does not change the size of the panel.
     *
     * @param result  true to make the component visible; false to
     *          make it invisible
     */
    public void setHide(boolean result) {
        if (result) {
            showPanel();
        } else {
            hidePanel();
        }
    }
}
