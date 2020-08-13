package golgui.viewer;

import gol.viewer.UserInteraction;

/**
 * This interface defines the general behavior of an application which uses my sidebar
 * layout.
 *
 * @version 1.0
 */
public interface SidebarLayoutInterface extends UserInteraction {
    /**
     * Contains the information regarding the size of different pacman.components.
     */
    final class DefaultSize {

        /**
         * Contains the width of the side bar.
         */
        public static final int SIDEBAR_WIDTH = 275;
        /**
         * Contains the width of a panel in the side bar.
         */
        public static final int SIDEBAR_INNER_WIDTH = 245;
        /**
         * Contains the height of the side bar.
         */
        public static final int SIDEBAR_HEIGHT = 800;
        /**
         * Contains the height of the stats panel.
         */
        public static final int STATS_HEIGHT = 75;
        /**
         * Contains the height of the toolbar.
         */
        public static final int TOOLBAR_HEIGHT = 50;
        /**
         * Contains the value for an unset width/height.
         */
        public static final int UNSET = 0;
    }

    /**
     * Contains all available side bar panels that could be shown at the lower part of
     * the side bar.
     */
    final class SidebarPanelOptions {

        /**
         * Represents the settings panel that allows a user to set different application
         * parameters.
         */
        public static final String SETTINGS = "SETTINGS";

        /**
         * Represents a tutorial panel that tells the user information regarding the
         * general information of the application.
         */
        public static final String GENERAL = "GENERAL";

        /**
         * Represents a tutorial panel that shows the simulation statistics as
         * simulation progresses.
         */
        public static final String STATISTICS = "STATISTICS";

        /**
         * Represents a panel that introduces the application to the user.
         */
        public static final String INTRODUCTION = "INTRODUCTION";

        /**
         * Represents a panel  that tells the user information regarding the hot keys.
         */
        public static final String HOTKEY = "HOTKEY";

        /**
         * Represents a board that call allow the user to edit the grid.
         */
        public static final String EDITGRID = "EDITGRID";
    }

    /**
     * Contains all available settings panels that could be shown at the lower part of
     * the side bar.
     */
    final class SettingsPanelOptions {

        /**
         * Contains file input/output.
         */
        public static int FILE_SETTINGS = 0;

        /**
         * Contains startup behavior.
         */
        public static int STARTUP_SETTINGS = 1;

        /**
         * Contains theme settings.
         */
        public static int THEME_SETTINGS = 2;

        /**
         * Contains other configurations.
         */
        public static int ADVANCED_SETTINGS = 3;
    }

    /**
     * This function let the lower part of the side bar show a specified panel.
     *
     * @param panelName the name of the panel. Should be one of
     *      {@link SidebarPanelOptions}
     */
    void showSideBarPanel(String panelName);

    /**
     * This function let the lower part of the side bar show a specified settings panel.
     *
     * @param tabName the name of the panel. Should be one of
     *       {@link SettingsPanelOptions}
     */
    void showSettingsPanel(int tabName);

    /**
     * This function saves all settings to a local file.
     *
     * @param message if a message is needed to show the result of saving
     */
    void saveSettings(final boolean message);

    /**
     * This function reloads and reapplies the settings.
     */
    void reloadSettings();

    /**
     * This function resets and reapplies the settings.
     */
    void resetSettings();
}
