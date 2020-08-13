package pacman.components;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import pacman.model.Direction;

public interface KeyboardControlledAgent {
    default void setArrowKeyToControl(AbstractAgent component) {
        setKeyBindings(component, Direction.LEFT, KeyEvent.VK_LEFT);
        setKeyBindings(component, Direction.RIGHT, KeyEvent.VK_RIGHT);
        setKeyBindings(component, Direction.DOWN, KeyEvent.VK_DOWN);
        setKeyBindings(component, Direction.UP, KeyEvent.VK_UP);
    }

    /**
     * Binds a hotkey with the action of changing the direction.
     *
     * @param newDirection the new direction specified by the user
     * @param keyCode      an int specifying the numeric code for a keyboard key
     */
    default void setKeyBindings(AbstractAgent component, Direction newDirection,
                                int keyCode) {
        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(keyCode, 0, false);

        InputMap inputMap = component.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyCodePressed, keyCodePressed.toString());

        ActionMap actionMap = component.getActionMap();
        actionMap.put(keyCodePressed.toString(),
                component.new NewDirectionAction(newDirection));
    }

    default void disableArrowKeyToControl(AbstractAgent component) {
        removeKeyBindings(component, KeyEvent.VK_LEFT);
        removeKeyBindings(component, KeyEvent.VK_RIGHT);
        removeKeyBindings(component, KeyEvent.VK_DOWN);
        removeKeyBindings(component, KeyEvent.VK_UP);
    }

    default void removeKeyBindings(AbstractAgent component,
                                   int keyCode) {
        KeyStroke keyCodePressed = KeyStroke.getKeyStroke(keyCode, 0, false);

        InputMap inputMap = component.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.remove(keyCodePressed);

        ActionMap actionMap = component.getActionMap();
        actionMap.remove(keyCodePressed.toString());
    }

    boolean aiTakeOver();
}
