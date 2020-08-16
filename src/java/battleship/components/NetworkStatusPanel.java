package battleship.components;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel to show the networking connection state.
 *
 * @version 1.0
 */
public class NetworkStatusPanel extends JPanel {
    /** The card layout for showing different panels. */
    private final CardLayout cardLayout;

    /** The button that will disconnect the connection. */
    private final JButton closeButton;
    private final JLabel labelWaitForConnection;
    private final JLabel labelConnected;

    /**
     * Creates a new NetworkStatusPanel.
     */
    public NetworkStatusPanel() {
        this.setOpaque(false);
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        // "Wait for connection" state
        labelWaitForConnection = ComponentFactory.createLabel(
                "Waiting for connection...", false, 18);
        JComponent waitPanel = ComponentFactory
            .wrapInLeftAlign(labelWaitForConnection);
        this.add(waitPanel, "WAIT");

        // "Connected" state
        labelConnected = ComponentFactory.createLabel(
                "Connected", false, 18);
        JComponent connectPanel = ComponentFactory
            .wrapInLeftAlign(labelConnected);
        this.closeButton = ComponentFactory.createButton("DISCONNECT");
        connectPanel.add(this.closeButton);
        this.add(connectPanel, "CONNECTED");
        this.waitForConnection();
    }

    /**
     * Sets the foreground color of this component.  It is up to the
     * look and feel to honor this property, some may choose to ignore
     * it.
     *
     * @param fg the desired foreground <code>Color</code>
     * @see Component#getForeground
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (labelWaitForConnection != null) {
            labelWaitForConnection.setForeground(fg);
        }
    }

    /**
     * Sets the background color of this component.  The background
     * color is used only if the component is opaque, and only
     * by subclasses of <code>JComponent</code> or
     * <code>ComponentUI</code> implementations.  Direct subclasses of
     * <code>JComponent</code> must override
     * <code>paintComponent</code> to honor this property.
     *
     * <p>It is up to the look and feel to honor this property, some may
     * choose to ignore it.
     *
     * @param bg the desired background <code>Color</code>
     * @see Component#getBackground
     * @see #setOpaque
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (closeButton != null) {
            this.closeButton.setBackground(bg);
        }
    }

    /**
     * Sets the font for this component.
     *
     * @param font the desired <code>Font</code> for this component
     * @see Component#getFont
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (labelConnected != null) {
            this.labelConnected.setFont(font);
        }
        if (labelWaitForConnection != null) {
            this.labelWaitForConnection.setFont(font);
        }
    }

    /**
     * This function adds an action to the disconnect button.
     * @param action the new button action
     */
    public void setDisconnectButtonAction(ActionListener action) {
        this.closeButton.addActionListener(action);
    }

    /**
     * Changes the state to "Wait for connection".
     */
    public void waitForConnection() {
        this.cardLayout.show(this, "WAIT");
    }

    /**
     * Changes the state to "Connected".
     */
    public void connected() {
        this.cardLayout.show(this, "CONNECTED");
    }
}
