package pacman.agents;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import pacman.model.Direction;


/**
 * This is a component that defines the behavior of a user controlled character
 * (ghost/pacman) in the game with moving animations.
 *
 * @version 1.0
 */
public interface KeyboardControlledAgent {
    /**
     * Binds the arrow keys with moving shortcuts.
     *
     * @param component the AbstractAgent instance
     */
    default void setArrowKeyToControl(AbstractAgent component) {
        setKeyBindings(component, Direction.LEFT, KeyEvent.VK_LEFT);
        setKeyBindings(component, Direction.RIGHT, KeyEvent.VK_RIGHT);
        setKeyBindings(component, Direction.DOWN, KeyEvent.VK_DOWN);
        setKeyBindings(component, Direction.UP, KeyEvent.VK_UP);
    }

    /**
     * Binds a hotkey with the action of changing the direction.
     *
     * @param agent        the agent component
     * @param newDirection the new direction specified by the user
     * @param keyCode      an int specifying the numeric code for a keyboard key
     */
    default void setKeyBindings(AbstractAgent agent, Direction newDirection,
                                int keyCode) {
        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(keyCode, 0, false);

        InputMap inputMap = agent.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyCodePressed, keyCodePressed.toString());

        ActionMap actionMap = agent.getActionMap();
        actionMap.put(keyCodePressed.toString(),
                agent.new NewDirectionAction(newDirection));
    }

    /**
     * Removes the binding between the arrow keys and moving shortcuts.
     *
     * @param component the AbstractAgent instance
     */
    default void disableArrowKeyToControl(AbstractAgent component) {
        removeKeyBindings(component, KeyEvent.VK_LEFT);
        removeKeyBindings(component, KeyEvent.VK_RIGHT);
        removeKeyBindings(component, KeyEvent.VK_DOWN);
        removeKeyBindings(component, KeyEvent.VK_UP);
    }

    /**
     * Removes a hotkey with the action of changing the direction.
     *
     * @param component the AbstractAgent instance
     * @param keyCode   an int specifying the numeric code for a keyboard key
     */
    default void removeKeyBindings(AbstractAgent component, int keyCode) {
        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(keyCode, 0, false);

        InputMap inputMap = component.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.remove(keyCodePressed);

        ActionMap actionMap = component.getActionMap();
        actionMap.remove(keyCodePressed.toString());
    }

    /**
     * Let AI takes over from the user.
     *
     * @return {@code true} if the user is no longer in control and {@code false}
     *      otherwise
     */
    boolean aiTakeOver();
}
