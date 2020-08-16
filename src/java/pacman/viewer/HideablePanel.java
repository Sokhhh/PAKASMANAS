package pacman.viewer;

import javax.swing.*;
import java.awt.*;

public class HideablePanel extends JPanel {

    CardLayout layout;

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

    public void showPanel() {
        layout.show(this, "visible");
    }

    public void hidePanel() {
        layout.show(this, "invisible");
    }

    /**
     * Makes the component visible or invisible.
     * Different from {@code Component.setVisible}, this method does not change
     * the size of the panel.
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
