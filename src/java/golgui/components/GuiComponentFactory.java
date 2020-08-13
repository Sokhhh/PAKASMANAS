package golgui.components;

import golgui.utils.NumberAndSpaceFormatter;
import golgui.utils.Theme;
import golgui.viewer.SidebarLayoutInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * This is a factory object that creates basic pacman.components of the application.
 *
 * @version <b>1.0</b>
 */
public class GuiComponentFactory {
    /**
     * Method that hides the constructor since the factory does not need initialization.
     */
    private GuiComponentFactory() {
        // Ignored
    }

    /**
     * Creates an empty JPanel with specified transparency and size.
     *
     * @param width         the preferred / minimum width
     * @param height        the absolute height of this area
     * @param isTransparent transparency
     * @return a new JPanel with required width, height and transparency
     */
    public static JPanel createPanel(int width, int height, boolean isTransparent) {
        JPanel panel = new JPanel();
        panel.setOpaque(!isTransparent);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setMinimumSize(new Dimension(width, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setBounds(0, 0, 0, 0);
        return panel;
    }

    /**
     * Creates an empty area with specified height.
     *
     * @param height        the absolute height of this area; null if such information
     *                      is not needed
     * @param isTransparent transparency
     * @return a new JPanel with required height and transparency
     */
    public static JPanel createEmptyArea(Integer height, boolean isTransparent) {
        if (height != null) {
            return createPanel(0, height, isTransparent);
        }
        JPanel panel = new JPanel();
        panel.setOpaque(!isTransparent);
        return panel;
    }

    /**
     * Creates a white line as a separator.
     *
     * @param color the color for the current line
     * @return a 1 pixel height JPanel
     */
    public static JPanel createLineSeparator(Color color) {
        JPanel line =  GuiComponentFactory.createEmptyArea(1, false);
        line.setBackground(color);
        return line;
    }

    /**
     * Creates a label with given style.
     *
     * @param text the text in the label
     * @param isBold if the font should be bold
     * @param size the size of the font
     * @return the required label
     */
    public static JLabel createLabel(String text, boolean isBold, int size) {
        JLabel titleText = new JLabel(text);
        titleText.setFont(new Font("Sans Serif", isBold ? Font.BOLD : Font.PLAIN, size));
        return titleText;
    }

    /**
     * This function would create a JLabel with transparent background and white
     * text. Font is set to be Sans-Serif by default
     *
     * @param text the string in this JLabel
     * @param fontSize the size of the font
     * @param textColor the color of the text
     * @return a JLabel with text, color and size specified.
     */
    public static JLabel createColoredLabel(String text, int fontSize, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Sans-Serif", Font.PLAIN, fontSize));
        label.setForeground(textColor);
        return label;
    }

    /**
     * This method changes the appearance of a JTextfield and let it look better (no
     * border, flat design, etc.) to fit the requirement of this application.
     *
     * @param field the JTextField object
     */
    private static void setTextFieldAppearance(final JTextField field) {
        field.setBackground(Theme.SIDEBAR_HIGHLIGHT);
        field.setForeground(Theme.TEXT);
        field.setFont(new Font("Serif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        field.setCaretColor(Theme.TEXT);
        field.setMaximumSize(
            new Dimension(SidebarLayoutInterface.DefaultSize.SIDEBAR_INNER_WIDTH, 16));
    }

    /**
     * Creates a simple text field which asks for input.
     *
     * @param width the maximum number of columns in the JTextField. null if such
     *              information is not needed and will not set a maximum width
     * @return a textfield which allows the user to send input
     */
    public static JTextField createTextField(Integer width) {
        JTextField field;
        if (width != null) {
            field = new JTextField(width);
        } else {
            field = new JTextField();
        }
        setTextFieldAppearance(field);
        return field;
    }

    /**
     * Creates a simple text field which asks for input and restrict it to only numbers
     * and white spaces.
     *
     * @param emptyStringPolicy a flag stands for the policy that how the
     *                          JFormattedTextField treats an empty string
     * @return a textfield which allows the user to send numeric input
     */
    public static JFormattedTextField createNumOnlyTextfield(int emptyStringPolicy) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        JFormattedTextField field =
            new JFormattedTextField(
                new NumberAndSpaceFormatter(format, emptyStringPolicy));
        setTextFieldAppearance(field);
        return field;
    }

    /**
     * This function creates a transparent background.
     *
     * @return a new JButton object that has a transparent background
     */
    public static JButton createButton() {
        JButton button = new JButton();
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    /**
     * This function creates a transparent background and a hovering effect.
     *
     * @return a new JButton object that has a transparent background and a hovering
     *      effect
     */
    public static JButton createButtonWithHoveringEffect() {
        JButton button = GuiComponentFactory.createButton();
        button.setBackground(Theme.TOOLBAR_BUTTON_HOVER);
        button.setBorder(BorderFactory.createLineBorder(Theme.TOOLBAR_BUTTON_EDGE, 2));

        // Add hovering effect
        SwingUtilities.invokeLater(() -> button.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse enters a component.
             *
             * @param evt the mouse enter event
             */
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBorderPainted(true);
                button.setOpaque(true);
            }

            /**
             * Invoked when the mouse exits a component.
             *
             * @param evt the mouse exit event
             */
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setOpaque(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
            }
        }));
        button.setPreferredSize(new Dimension(0, 35));
        button.setFont(new Font("sans-serif", Font.PLAIN, 16));
        return button;
    }

    /**
     * Creates an icon that can be used on buttons. The icon changes its color to fit
     * the text color in the application.
     *
     * @param path      path to the image file containing the icon
     * @param textColor the color for the text
     * @return the icon object
     */
    public static ImageIcon createIconOfButton(String path, Color textColor) {

        // Set an icon for the button
        URL iconPath = GuiComponentFactory.class.getResource(path);
        if (iconPath == null) {
            return null;
        }

        BufferedImage img;
        try {
            img = ImageIO.read(iconPath);
        } catch (IOException e) {
            return null;
        }
        if (img == null) {
            // If the image does not exist
            return null;
        }
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(),
            img.getType());

        // change the color of the icon
        int color;
        int alpha;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                color = img.getRGB(i, j);
                alpha = color >> 24;
                if (alpha != 0) {
                    newImage.setRGB(i, j, textColor.getRGB());
                } else {
                    newImage.setRGB(i, j, color);
                }
            }
        }

        // resize
        ImageIcon icon = new ImageIcon(newImage);
        icon = new ImageIcon(
            icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        return icon;

    }

    /**
     * This function would create a JButton with Flat design and an icon: Hover effect,
     * grey background and no border. Need to set font manually after calling this function.
     *
     * @param path      path to the image file containing the icon
     * @param text      if the icon cannot be found, text will appear on the button
     * @param textColor the color for the text
     * @return a new JButton with icon or text label
     */
    public static JButton createButtonWithIcon(String path, String text, Color textColor) {
        JButton button = GuiComponentFactory.createButtonWithHoveringEffect();
        if (path != null) {
            ImageIcon icon = GuiComponentFactory.createIconOfButton(path, textColor);
            if (icon != null) {
                button.setIcon(icon);
            } else {
                button.setText(text);
            }
        } else {
            button.setText(text);
        }
        button.setForeground(textColor);
        return button;
    }

    /**
     * Creates a GridBagConstraints that set the x,y coordinate of a component used for
     * adding such component to another component which uses the GridBagLayout.
     *
     * @param x Specifies the cell containing the leading edge of the component's
     *          display area, where the first cell in a row has {@code gridx=0}
     * @param y Specifies the cell at the top of the component's display area, where the
     *         topmost cell has {@code y = 0}
     * @return the GridBagConstraints object that will be used to add the component
     */
    public static GridBagConstraints createGridBagConstraints(int x, int y) {
        return GuiComponentFactory.createGridBagConstraints(x, y, 1, 1);
    }

    /**
     * Creates a GridBagConstraints that set the x,y coordinate and also the width
     * and height of a component used for adding such component to another component
     * which uses the GridBagLayout.
     *
     * @param x Specifies the cell containing the leading edge of the component's
     *          display area, where the first cell in a row has {@code gridx=0}
     * @param y Specifies the cell at the top of the component's display area, where the
     *         topmost cell has {@code y = 0}
     * @param height the number of cells in a row for the component's display area
     * @param width the number of cells in a column for the component's display area
     * @return the GridBagConstraints object that will be used to add the component
     */
    public static GridBagConstraints createGridBagConstraints(int x, int y, int width,
        int height) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;

        gbc.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = (x == 0) ? GridBagConstraints.BOTH
                : GridBagConstraints.HORIZONTAL;

        gbc.weightx = (x == 0) ? 0.1 : 1.0;
        return gbc;
    }

    /**
     * This function will create a flat design JCheckBox: transparent background,
     * white text with font size 18.
     *
     * @param text text appearing next to the check box
     * @param textColor the color of the text
     * @return a new transparent JCheckBox
     */
    static JCheckBox createCheckBox(String text, Color textColor) {
        LookAndFeel old = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        if (!text.startsWith("<html>")) {
            text = " " + text;
        }
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBorderPaintedFlat(true);
        checkBox.setFocusPainted(false);
        checkBox.setOpaque(false);
        checkBox.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        checkBox.setFocusable(false);
        checkBox.setFocusPainted(false);
        checkBox.setForeground(textColor);
        try {
            UIManager.setLookAndFeel(old);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        return checkBox;
    }

    /**
     * This function would create a JFileChooser for saving/loading feature.
     *
     * @param defaultName the default file name
     * @return a file chooser with txt as the default extension choice and "All types"
     *     option allowed
     */
    public static JFileChooser createFileChooser(String defaultName) {
        // save to
        JFileChooser fileChooser = new JFileChooser() {
            /**
             * Called by the UI when the user hits the Approve button
             * (labeled "Open" or "Save", by default). This can also be
             * called by the programmer.
             * This method causes an action event to fire
             * with the command string equal to
             * <code>APPROVE_SELECTION</code>.
             *
             * @see #APPROVE_SELECTION
             */
            @Override
            public void approveSelection() {
                if (getSelectedFile() == null) {
                    return;
                }
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    // overwrite confirmation
                    int result = JOptionPane.showConfirmDialog(this,
                        "The file already exists. Do you want to overwrite it?",
                        "File already exists",
                        JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                        default:
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Pure Text (*.txt)",
            "txt"));
        // set the default filename
        if (!new File(defaultName).isDirectory() && new File(defaultName).exists()) {
            fileChooser.setSelectedFile(new File(defaultName));
        }
        return fileChooser;
    }

    /**
     * Contains a scrollbar UI that modifies the standard swing's UI, making the
     * scrollbar UI translucent.
     *
     * @version <b>1.0</b>
     */
    private static final class TranslucentScrollBarUI extends BasicScrollBarUI {
        /**
         * Creates the decrease button used for this basic scroll bar.
         * @param orientation the orientation of the scroll bar
         * @return an transparent button
         */
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return GuiComponentFactory.createButton();
        }

        /**
         * Creates the increase button used for this basic scroll bar.
         * @param orientation the orientation of the scroll bar
         * @return an transparent button
         */
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return GuiComponentFactory.createButton();
        }

        /**
         * Paints the scrollbar track.
         * @param g Graphics object used for painting
         * @param c the component being painted
         * @param trackBounds bounding box for the thumb
         */
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // No track is needed
        }

        /**
         * Paints the scrollbar thumb.
         * @param g Graphics object used for painting
         * @param c the component being painted
         * @param thumbBounds bounding box for the thumb
         */
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            // Color
            if (super.isThumbRollover()) {
                // If the mouse is not currently over the thumb, make it opaque.
                g.setColor(Theme.WHITE_BUTTON_HOVER);
            } else {
                // If the mouse is not currently over the thumb, make it translucent.
                g.setColor(Theme.WHITE_BUTTON_TRANSLUCENT);
            }
            // Size
            if (super.scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                g.fillRect(thumbBounds.x, thumbBounds.y, 8, thumbBounds.height);
            } else {
                g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, 8);
            }
        }
    }

    /**
     * This function would create a JScrollPane with transparent background.
     *
     * @param insidePanel the panel that would stay inside the scroll pane
     * @param backgroundColor color for the background
     * @return a new JScrollPane
     */
    public static JScrollPane createSimpleScrollPanel(JComponent insidePanel,
        Color backgroundColor) {

        JScrollPane scrollPanel = new JScrollPane(insidePanel);
        scrollPanel.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        verticalScrollBar.setBackground(backgroundColor);
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUI(new TranslucentScrollBarUI());
        verticalScrollBar.setPreferredSize(new Dimension(8, 0));
        verticalScrollBar.setUnitIncrement(20);

        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        horizontalScrollBar.setBackground(backgroundColor);
        horizontalScrollBar.setOpaque(false);
        horizontalScrollBar.setUI(new TranslucentScrollBarUI());
        horizontalScrollBar.setPreferredSize(new Dimension(0, 8));

        scrollPanel.setBackground(backgroundColor);
        scrollPanel.setOpaque(false);
        scrollPanel.getViewport().setBackground(backgroundColor);
        scrollPanel.getViewport().setOpaque(true);
        scrollPanel.getViewport().setBounds(0, 0, 0, 0);
        scrollPanel.setBorder(BorderFactory.createEmptyBorder());

        return scrollPanel;
    }

    /**
     * This function will create a flat design JComboBox: transparent background,
     * dark popup menu and white text.
     *
     * @param hoverColor Color for background when the user moves the cursor over it
     * @param backgroundColor Color for background
     * @param textColor Color for text
     * @return a new JComboBox
     */
    public static JComboBox<String> createComboBox(Color hoverColor,
        Color backgroundColor,
        Color textColor) {
        JComboBox<String> choiceBox = new JComboBox<>();
        choiceBox.setUI(new BasicComboBoxUI() {
            /**
             * Creates a button which will be used as the control to show or hide
             * the popup portion of the combo box.
             *
             * @return a button which represents the popup control
             */
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = GuiComponentFactory.createButtonWithIcon(null,
                    "v", textColor);
                arrowButton.setBackground(hoverColor);
                return arrowButton;
            }
        });
        // General appearance
        ((JLabel) choiceBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        choiceBox.setForeground(textColor);
        choiceBox.setOpaque(false);
        choiceBox.setBackground(backgroundColor);
        choiceBox.setFont(new Font("Sans-serif", Font.PLAIN, 16));
        choiceBox.setFocusable(false);
        choiceBox.setFocusCycleRoot(false);
        // Possible choices appearance
        JList list = ((BasicComboPopup) choiceBox.getAccessibleContext()
            .getAccessibleChild(0)).getList();
        list.setSelectionBackground(hoverColor);
        list.setSelectionForeground(textColor);
        list.setBackground(backgroundColor);
        list.setForeground(textColor);
        choiceBox.setBorder(new LineBorder(hoverColor, 1, true));
        // Disable keys
        InputMap im = choiceBox
            .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke("DOWN"), "none");
        return choiceBox;
    }

    /**
     * This function wraps multiple pacman.components into one JPanel and let them equally
     * divided into areas.
     *
     * @param comps different pacman.components that are about to be added
     * @return a JPanel containing all pacman.components in comps
     */
    public static JPanel wrapInEquallyDividedPanel(Component... comps) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, comps.length, 5, 1));
        p.setOpaque(false);
        for (Component comp : comps) {
            p.add(comp);
        }
        return p;
    }

    /**
     * This function wraps multiple pacman.components into one JPanel and let them equally
     * divided into areas. This function does not resize vertically.
     *
     * @param comps different pacman.components that are about to be added
     * @return a JPanel containing all pacman.components in comps
     */
    public static JPanel wrapInEquallyDividedPanelFixedHeight(Component... comps) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(wrapInEquallyDividedPanel(comps), BorderLayout.SOUTH);
        return p;
    }

    /**
     * This function wraps multiple pacman.components into one JPanel and does not change the
     * way how they are divided into areas.
     *
     * @param comps different pacman.components that are about to be added
     * @return a JPanel containing all pacman.components in comps
     */
    public static JPanel wrapInPanel(Component... comps) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        int i = 0;
        for (Component comp: comps) {
            p.add(comp);
            if (i != comps.length - 1) {
                p.add(Box.createHorizontalStrut(10));
            }
            i++;
        }
        return p;
    }

    /**
     * This function creates a dialog.
     *
     * @param text the text on the dialog
     * @param relation the location of which this dialog is relative to
     * @param blank should there be empty area around the text
     * @return the dialog instance
     */
    public static JDialog createDialog(String text, Component relation, boolean blank) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setFocusable(false);
        dialog.setFocusableWindowState(false);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.SIDEBAR);

        // Add the message to the dialog
        JLabel toastLabel = GuiComponentFactory
            .createColoredLabel(text, 20, Theme.TEXT);
        toastLabel.setForeground(Theme.TEXT);
        JPanel panel = GuiComponentFactory.wrapInEquallyDividedPanel(toastLabel);
        if (blank) {
            panel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 70));
        } else {
            panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        }
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.setBounds(0, 0, panel.getPreferredSize().width + 20,
            panel.getPreferredSize().height + 10);
        if (relation != null) {
            dialog.setLocationRelativeTo(relation);
            dialog.setLocation(dialog.getX(),
                (int) (dialog.getY() + relation.getSize().height / 5.0 * 2));
        } else {
            dialog.setLocationRelativeTo(null);
            dialog.setLocation(dialog.getX(), dialog.getY() + 250);
        }

        return dialog;
    }

    /**
     * This function lets a JDialog fade away slowly.
     *
     * @param dialog the JDialog instance
     */
    public static void closeDialog(final JDialog dialog) {
        if (!dialog.isUndecorated()) {
            dialog.setUndecorated(true);
        }
        final Timer timer = new Timer(20, null);
        timer.setRepeats(true);
        timer.addActionListener(new ActionListener() {
            /**
             * Contains the original opacity of
             * the dialog.
             */
            private float opacity = 0.8f;

            /**
             * Invoked when an action occurs.
             *
             * @param e the action that timer
             *          changes
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                this.opacity -= 0.05f;
                dialog.setOpacity(Math.max(opacity, 0));
                if (this.opacity <= 0) {
                    timer.stop();
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        });
        timer.start();
    }

    /**
     * Creates a counter with a label that will be used in this application for
     * statistical data, such as tick number.
     *
     * @param stepCountLabel the label containing the number
     * @param name the name containing the purpose of this counter
     * @param textColor the color of text
     * @return a JPanel which contains the counter and all related pacman.components
     */
    public static JPanel createCounter(JLabel stepCountLabel, String name,
        Color textColor) {
        JPanel stepPanel = GuiComponentFactory
            .createPanel(SidebarLayoutInterface.DefaultSize.SIDEBAR_WIDTH,
                SidebarLayoutInterface.DefaultSize.STATS_HEIGHT, true);
        stepPanel.setLayout(new GridBagLayout());

        // Layout
        GridBagConstraints gbc = GuiComponentFactory.createGridBagConstraints(0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // number
        stepPanel.add(stepCountLabel, gbc);

        // label
        JLabel stepLabel = new JLabel(" " + name);
        stepLabel.setFont(new Font("sans-serif", Font.BOLD, 15));
        stepLabel.setForeground(textColor);
        gbc.gridy++;
        stepPanel.add(stepLabel, gbc);
        return stepPanel;
    }
}
