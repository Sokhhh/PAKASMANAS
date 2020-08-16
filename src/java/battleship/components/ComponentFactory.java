package battleship.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * This is a factory object that creates basic components of the application.
 *
 * @version <b>1.0</b>
 */
public class ComponentFactory {
    /**
     * Method that hides the constructor since the factory does not need initialization.
     */
    private ComponentFactory() {
        // Ignored
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
     * This function creates a transparent background.
     *
     * @param text the text label on the button
     * @return a new JButton object that has a transparent background
     */
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);

        button.setBackground(new Color(0, 0, 0, 125));
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 225), 2));

        // Add hovering effect
        SwingUtilities.invokeLater(() -> button.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse enters a component.
             *
             * @param evt the mouse enter event
             */
            @Override
            public void mouseEntered(MouseEvent evt) {
                // button.setBorderPainted(true);
                // button.setOpaque(true);
            }

            /**
             * Invoked when the mouse exits a component.
             *
             * @param evt the mouse exit event
             */
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.repaint();
            }
        }));
        return button;
    }


    /**
     * This function wraps multiple components into one JPanel and let them equally
     * divided into areas.
     *
     * @param comps different components that are about to be added
     * @return a JPanel containing all components in comps
     */
    public static JPanel wrapInEquallyDividedPanel(Component... comps) {
        return wrapInEquallyDividedPanel(5, comps);
    }

    /**
     * This function wraps multiple components into one JPanel and let them equally
     * divided into areas.
     *
     * @param hgap   the horizontal gap
     * @param comps different components that are about to be added
     * @return a JPanel containing all components in comps
     */
    public static JPanel wrapInEquallyDividedPanel(int hgap, Component... comps) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, comps.length, hgap, 1));
        p.setOpaque(false);
        for (Component comp : comps) {
            p.add(comp);
        }
        return p;
    }

    /**
     * This function wraps multiple components into one JPanel and does not change the
     * size or alignment.
     *
     * @param comps different components that are about to be added
     * @return a JPanel containing all components in comps
     */
    public static JComponent wrapInLeftAlign(JComponent... comps) {
        Box b = Box.createHorizontalBox();
        for (JComponent component : comps) {
            b.add(component);
        }
        b.add(Box.createHorizontalGlue());
        b.setOpaque(false);
        return b;
    }
}
