package pacman.viewer;

import golgui.components.GuiComponentFactory;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import pacman.algorithms.AlgorithmFactory;
import pacman.util.PacmanTheme;
import pacman.util.StringUtilities;

/**
 * Contains a component that can let user choose which character to controll when
 * the game starts.
 *
 * @version 1.0
 */
public class AgentItemPanel extends JPanel {
    private final JRadioButton check;
    private final String name;
    private final JComboBox<String> algorithmChoices;
    private final JComboBox<String> iconChoices = null;  // Not supported yet

    /**
     * Creates a new AgentItemPanel instance.
     *
     * @param name the name of the agent
     * @param check JRadioButton object to select/unselect the agent item
     */
    public AgentItemPanel(String name, JRadioButton check) {
        super(new GridLayout(1, 3));
        this.setOpaque(false);
        boolean isGhost = !name.equalsIgnoreCase("pacman");
        // Not supported due to strict scheduling
        // this.iconChoices = createIconSelectionBox(isGhost);
        // this.add(iconChoices);
        this.name = name;
        JLabel nameLabel = new JLabel(StringUtilities.capitalize(name)
                + (isGhost ? " ghost" : ""));
        nameLabel.setForeground(PacmanTheme.WELCOME_TEXT);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        this.add(nameLabel);
        this.algorithmChoices = createAlgorithmSelectionBox();
        this.add(algorithmChoices);
        this.check = check;
        this.check.setOpaque(false);
        this.check.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(check);
    }

    /**
     * Creates a combo box to select the icon.
     *
     * @param isGhost if the agent is a ghost agent
     * @return the combo box created
     */
    private static JComboBox<String> createIconSelectionBox(boolean isGhost) {
        throw new UnsupportedOperationException("Changing agent icon is currently "
                + "not supported");
        /*
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
        */
    }

    /**
     * Creates a combo box to choose the algorithm the agent use.
     *
     * @return the created combo box
     */
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
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addCheckBoxActionListener(ActionListener l) {
        this.check.addActionListener(l);
    }

    /**
     * Adds an <code>ActionListener</code> to the algorithm checkbox..
     *
     * <p>The <code>ActionListener</code> will receive an <code>ActionEvent</code>
     * when a selection has been made. If the combo box is editable, then
     * an <code>ActionEvent</code> will be fired when editing has stopped.
     *
     * @param l  the <code>ActionListener</code> that is to be notified
     */
    public void addComboBoxActionListener(ActionListener l) {
        this.algorithmChoices.addActionListener(l);
    }

    /**
     * Gets the state of the button. True if the
     * toggle button is selected, false if it's not.
     *
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
     *
     * @return    a string representation of this component
     */
    @Override
    public String toString() {
        return "AgentItem {name=" + name + ", algorithm=" + getAlgorithmName()
                + ", selected=" + check.isSelected();
    }
}
