package battleship.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pacman.util.StringUtilities;
import pacman.viewer.PacmanViewUtility;

/**
 * Contains a panel for showing a network address.
 *
 * @version 1.0
 */
public class NetworkAddressPanel extends JPanel {
    /** Contains the field for inputting the address. */
    private final JTextField addressField;

    /** Contains the field for inputting the port. */
    private final JTextField portField;

    /** Contains the button for confirmation. */
    private final JButton applyBtn;

    /** Contain the labels in the component. */
    private final List<JLabel> labels;

    /**
     * Creates a new NetworkAddressPanel.
     *
     * @param addressEditable {@code true} if the address is editable and false otherwise.
     */
    public NetworkAddressPanel(boolean addressEditable) {
        this.setOpaque(false);
        this.setLayout(new BorderLayout(5, 0));
        this.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        labels = new ArrayList<>();

        // Address label
        JLabel addressLabel = ComponentFactory.createLabel("Address: ", false, 14);
        labels.add(addressLabel);

        // Address field
        this.addressField = new JTextField(10);
        this.addressField.setEditable(addressEditable);
        JPanel addressPanel = new JPanel(new BorderLayout(0, 0));
        addressPanel.add(addressLabel, BorderLayout.WEST);
        addressPanel.add(this.addressField, BorderLayout.CENTER);
        addressPanel.setOpaque(false);
        this.add(addressPanel, BorderLayout.CENTER);

        // Port label
        final JLabel portLabel = ComponentFactory.createLabel("Port: ", false, 14);
        labels.add(portLabel);

        // Confirmation button
        this.applyBtn = new JButton("APPLY");
        PacmanViewUtility.addMouseHoveringEffectAtStart(applyBtn);
        this.applyBtn.setFont(new Font("Dialog", Font.PLAIN, 17));
        this.portField = new JTextField(5);
        this.portField.setMinimumSize(new Dimension(0, applyBtn.getHeight()));

        JPanel portPanel = new JPanel(new BorderLayout(0, 0));
        portPanel.add(portLabel, BorderLayout.WEST);
        portPanel.add(this.portField, BorderLayout.CENTER);
        portPanel.add(applyBtn, BorderLayout.EAST);
        portPanel.setOpaque(false);

        this.add(portPanel, BorderLayout.EAST);
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
        if (labels != null) {
            labels.forEach(e -> e.setForeground(fg));
        }
        if (applyBtn != null) {
            applyBtn.setForeground(fg);
        }
        if (portField != null) {
            portField.setForeground(fg);
            portField.setCaretColor(fg);
        }
        if (addressField != null) {
            addressField.setForeground(fg);
            addressField.setCaretColor(fg);
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
        if (applyBtn != null) {
            applyBtn.setBackground(bg);
        }
        if (portField != null) {
            portField.setBackground(bg);
        }
        if (addressField != null) {
            addressField.setBackground(bg);
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
        if (labels != null) {
            labels.forEach(e -> e.setFont(font));
        }
    }

    /**
     * This method gets the address in the input field.
     *
     * @return the address set in the input field
     */
    public String getAddress() {
        return this.addressField.getText();
    }

    /**
     * This method gets the address in the input field.
     *
     * @return the port set in the input field
     */
    public String getPort() {
        return this.portField.getText();
    }

    /**
     * This method changes the address in the input field.
     *
     * @param address the address that will be in the input field
     */
    public void setAddress(String address) {
        this.addressField.setText(address);
    }

    /**
     * This method changes the port in the input field.
     *
     * @param port the port that will be in the input field
     */
    public void setPort(String port) {
        if (StringUtilities.isInteger(port)) {
            this.portField.setText(port);
        } else {
            this.portField.setText("");
        }
    }

    /**
     * This method changes the port in the input field.
     *
     * @param port the port that will be in the input field
     */
    public void setPort(int port) {
        this.portField.setText(String.valueOf(port));
    }

    /**
     * Sets the "APPLY" button's text.
     *
     * @param text the string used to set the text
     */
    public void setApplyBtnText(String text) {
        this.applyBtn.setText(text);
    }

    /**
     * This method adds an <code>ActionListener</code> to the "APPLY" button.
     *
     * @param action the <code>ActionListener</code> to be added
     */
    public void setApplyBtnAction(ActionListener action) {
        this.applyBtn.addActionListener(action);
    }

    /**
     * This method enables (or disables) the "APPLY" button.
     * @param b  true to enable the button, otherwise false
     */
    public void setApplyBtnEnabled(boolean b) {
        this.applyBtn.setEnabled(b);
    }
}
