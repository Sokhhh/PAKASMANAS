package pacman.viewer;

import golgui.components.GuiComponentFactory;
import pacman.agents.GhostAgent;
import pacman.algorithms.AlgorithmFactory;
import pacman.util.PacmanTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class AgentItemPanel extends JPanel {
    private final JRadioButton check;
    private final String name;
    private final JComboBox<String> algorithmChoices;
    private final JComboBox<String> iconChoices;
    private final JLabel nameLabel;

    public AgentItemPanel(String name, JRadioButton check) {
        super(new GridLayout(1, 3));
        this.setOpaque(false);
        boolean isGhost = !name.equalsIgnoreCase("pacman");
        this.iconChoices = createIconSelectionBox(isGhost);
        // this.add(iconChoices);
        this.name = name;
        this.nameLabel =
                new JLabel(name.substring(0, 1).toUpperCase() + name.substring(1)
                        + (isGhost ? " ghost" : ""));
        this.nameLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        this.nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        this.add(nameLabel);
        this.algorithmChoices = createAlgorithmSelectionBox();
        this.add(algorithmChoices);
        this.check = check;
        this.check.setOpaque(false);
        this.check.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(check);
    }

    private static JComboBox<String> createIconSelectionBox(boolean isGhost) {
        JComboBox<String> ret =
                GuiComponentFactory.createComboBox(PacmanTheme.BUTTON_HOVERED,
                        PacmanTheme.WELCOME_BACKGROUND, PacmanTheme.WELCOME_TEXT);
        if (isGhost) {
            for (String name: GhostAgent.NAMES) {
                ret.addItem(name + " ghost");
            }
        } else {
            ret.addItem("default pacman");
        }
        ret.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return ret;
    }

    private static JComboBox<String> createAlgorithmSelectionBox() {
        JComboBox<String> ret =
                GuiComponentFactory.createComboBox(PacmanTheme.BUTTON_HOVERED,
                        PacmanTheme.WELCOME_BACKGROUND, PacmanTheme.WELCOME_TEXT);
        // Items
        for (String algorithm: AlgorithmFactory.SupportedAlgorithms.values.keySet()) {
            ret.addItem(algorithm);
        }
        ret.setSelectedItem(AlgorithmFactory.SupportedAlgorithms.GREEDY_ALGORITHM);
        ret.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return ret;
    }

    /**
     * Sets the state of the check box.
     *
     * @param b  true if the button is selected, otherwise false
     */
    public void setSelected(boolean b) {
        this.check.setSelected(b);
    }

    /**
     * Adds an <code>ActionListener</code> to the checkbox.
     * @param l the <code>ActionListener</code> to be added
     */
    public void addCheckBoxActionListener(ActionListener l) {
        this.check.addActionListener(l);
    }

    /**
     * Gets the state of the button. True if the
     * toggle button is selected, false if it's not.
     * @return true if the toggle button is selected, otherwise false
     */
    public boolean isSelected() {
        return this.check.isSelected();
    }

    /**
     * Gets the name of the agent.
     *
     * @return the name of the agent
     */
    public String getAgentName() {
        return name;
    }

    /**
     * Gets the name of selected algorithm.
     *
     * @return the name of selected algorithm
     */
    public String getAlgorithmName() {
        return (String) algorithmChoices.getSelectedItem();
    }

    /**
     * Returns a string representation of this component and its values.
     * @return    a string representation of this component
     */
    @Override
    public String toString() {
        return "AgentItem {name=" + name + ", algorithm=" + getAlgorithmName()
                + ", selected=" + check.isSelected();
    }
}
