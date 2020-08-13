package golgui.utils;

import java.awt.Color;
import java.util.HashMap;

/**
 * UI theme defined for the Game Of Life application.
 *
 * @version <b>1.0</b>
 */
public class Theme {
    /** Contains the default color of sidebar. **/
    public static final Color DEFAULT = new Color(16, 110, 190);
    /** Contains the background color of the side bar panel. **/
    public static Color SIDEBAR = new Color(16, 110, 190);
    /** Contains the highlighted color of the side bar panel (usually darker). **/
    public static Color SIDEBAR_HIGHLIGHT = SIDEBAR.darker();
    /** Contains the toolbar button edge color when the mouse is hovering over. **/
    public static Color TOOLBAR_BUTTON_EDGE = new Color(120, 168, 209);
    /** Contains the toolbar button background color when the mouse is hovering over. **/
    public static Color TOOLBAR_BUTTON_HOVER = new Color(68, 135, 190);
    /** Contains the color that the text in this application uses. */
    public static Color TEXT = Color.WHITE;
    /** Contains a color that a light color button uses. */
    public static Color WHITE_BUTTON_TRANSLUCENT = new Color(255, 255, 255, 50);
    /**
     * Contains a color that a light color button uses when the user's mouse is
     * hovering over.
     */
    public static Color WHITE_BUTTON_HOVER = new Color(255, 255, 255, 100);
    /** Contains the color that represents a dead cell. **/
    public static Color DEAD = new Color(250, 250, 250);
    /** Contains the color that represents an alive cell. **/
    public static Color ALIVE = new Color(138, 186, 224);
    /** Contains the color a cell border when the mouse is hovering over.  **/
    public static Color HOVER = new Color(181, 181, 181);
    /** A flag to indicate whether the sidebar background is a relatively light theme. */
    public static boolean IS_LIGHT_THEME = isLightColor(SIDEBAR);


    // ==================================================================================
    //                     SOME COLOR RELATED ALGORITHMS
    // ==================================================================================

    /**
     * This function gets a contrast color of an existing color.
     *
     * @param old old color
     * @return the contrast of old color
     */
    public static Color getContrastColor(Color old) {
        return new Color(255 - old.getRed(), 255 - old.getGreen(), 255 - old.getBlue());
    }

    /**
     * This function checks if a dark font is needed based on the color of the
     * background. This algorithm is inspired from https://stackoverflow
     * .com/questions/1855884/determine-font-color-based-on-background-color
     * /1855903#1855903 (Author: Gacek, answered Dec 6 2009 at 17:09)
     *
     * @param background the background color
     * @return {@code true} if the font color should be a dark one (the background is a
     *      light color) and {@code false} if the font color should be a light color
     */
    public static boolean isLightColor(Color background) {
        // Calculate the perceptive luminance (aka luma)
        double luma =
            ((0.299 * background.getRed()) + (0.587 * background.getGreen()) + (0.114
                * background.getBlue())) / 255;

        // Return black for bright colors, white for dark colors
        return luma > 0.6;
    }

    /**
     * This function combines a translucent foreground color with a background color,
     * producing a new color blended between the two, using the Alpha blending
     * algorithm. If the foreground color is completely transparent, the blended
     * color will be the background color. Conversely, if it is completely opaque, the
     * blended color will be the foreground color.
     *
     * @param background the background color
     * @param opacity the opacity of foreground color
     * @return the blended color between the foreground and background
     */
    public static Color addTint(Color background, double opacity) {
        Color foreground;
        if (Theme.isLightColor(background)) {
            foreground = Color.BLACK;
        } else {
            foreground = Color.WHITE;
        }
        opacity = 255 * opacity;
        // Alpha blending algorithm:
        // out[rgb] = (src[rgb] * src[a] + dst[rgb] * dst[a] * (1 - src[a])) / out[a]
        int outRed =
            (int) ((background.getRed() * opacity + foreground.getRed() * (255 - opacity))
                / 255);
        int outGreen =
            (int) ((background.getGreen() * opacity + foreground.getGreen() * (255
                - opacity)) / 255);
        int outBlue =
            (int) (
                (background.getBlue() * opacity + foreground.getBlue() * (255 - opacity))
                    / 255);
        return new Color(outRed, outGreen, outBlue);
    }

    // ==================================================================================
    //                                    MODIFIERS
    // ==================================================================================

    /**
     * This function changes the side bar UI color.
     *
     * <p>- requires: None
     *
     * <p>- modifies: {@link #SIDEBAR},
     * {@link #SIDEBAR_HIGHLIGHT},
     * {@link #IS_LIGHT_THEME}
     * {@link #TOOLBAR_BUTTON_EDGE}
     * {@link #TOOLBAR_BUTTON_HOVER}
     * {@link #TEXT}
     * {@link #WHITE_BUTTON_TRANSLUCENT}
     * {@link #WHITE_BUTTON_HOVER}
     *
     * <p>- effects: {@link #SIDEBAR} = sideBar, all other colors are changed based on
     * the side bar color
     *
     * @param sideBar the new color for the side bar.
     */
    public static void applySideBarColor(Color sideBar) {
        Theme.SIDEBAR = sideBar;
        Theme.SIDEBAR_HIGHLIGHT = (sideBar.equals(Color.BLACK)) ? new Color(45, 45, 45) :
            sideBar.darker();
        Theme.IS_LIGHT_THEME = Theme.isLightColor(sideBar);
        Theme.TOOLBAR_BUTTON_EDGE = addTint(sideBar, 0.6);
        Theme.TOOLBAR_BUTTON_HOVER = addTint(sideBar, 0.8);
        Theme.TEXT = Theme.IS_LIGHT_THEME ? Color.BLACK : Color.WHITE;
        Theme.WHITE_BUTTON_TRANSLUCENT = Theme.IS_LIGHT_THEME ? new Color(0,
            0, 0, 50) : new Color(255, 255, 255, 50);
        Theme.WHITE_BUTTON_HOVER = Theme.IS_LIGHT_THEME ? new Color(0,
            0, 0, 100) : new Color(255, 255, 255, 100);
    }

    /**
     * This function changes the color of alive cells.
     *
     * <p>- requires: None
     *
     * <p>- modifies: {@link #ALIVE}
     *
     * <p>- effects: {@link #ALIVE} = aliveCell
     *
     * @param aliveCell the new color for the alive cells.
     */
    public static void applyAliveCellColor(Color aliveCell) {
        Theme.ALIVE = addTint(aliveCell,  Theme.isLightColor(aliveCell) ? 0.7 : 0.5);
    }

    /**
     * This function changes the color of dead cells.
     *
     * <p>- requires: None
     *
     * <p>- modifies: {@link #ALIVE}
     *
     * <p>- effects: {@link #ALIVE} = aliveCell
     *
     * @param deadCell the new color for the dead cells.
     */
    public static void applyDeadCellColor(Color deadCell) {
        Theme.DEAD = deadCell;
    }

    /**
     * This function changes the colors.
     *
     * <p>- requires: None
     *
     * <p>- modifies: any item matching the name
     *
     * <p>- effects: color of that item = newColor
     *
     * @param name the name of what the color belongs to
     * @param newColor the new color for that item
     */
    public static void change(String name, Color newColor) {
        switch (name) {
            case "SIDEBAR":
                Theme.applySideBarColor(newColor);
                break;
            case "SIDEBAR_HIGHLIGHT":
                Theme.SIDEBAR_HIGHLIGHT = newColor;
                break;
            case "TOOLBAR_BUTTON_EDGE":
                Theme.TOOLBAR_BUTTON_EDGE = newColor;
                break;
            case "TOOLBAR_BUTTON_HOVER":
                Theme.TOOLBAR_BUTTON_HOVER = newColor;
                break;
            case "TEXT":
                Theme.TEXT = newColor;
                break;
            case "WHITE_BUTTON_TRANSLUCENT":
                Theme.WHITE_BUTTON_TRANSLUCENT = newColor;
                break;
            case "WHITE_BUTTON_HOVER":
                Theme.WHITE_BUTTON_HOVER = newColor;
                break;
            case "DEAD":
                Theme.DEAD = newColor;
                break;
            case "ALIVE":
                Theme.ALIVE = newColor;
                break;
            case "HOVER":
                Theme.HOVER = newColor;
                break;
            default:
                break;
        }
    }

    // ==================================================================================
    //                                 CELL COLORS
    // ==================================================================================

    /**
     * This function creates a map which will contain singletons for Colors of different
     * ages.
     *
     * <p>- requires: {@link #ALIVE} = null, {@link #DEAD} = null
     *
     * <p>- modifies: None
     *
     * <p>- effects: None
     *
     * @return a map which will contain singletons for Colors of different ages.
     */
    public static HashMap<Integer, Color> getInitialAliveColorMap() {
        HashMap<Integer, Color> map = new HashMap<>();
        map.put(0, Theme.DEAD);
        map.put(1, Theme.ALIVE);
        return map;
    }
}
