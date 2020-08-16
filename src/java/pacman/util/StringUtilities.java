package pacman.util;

/**
 * Contains operations on {@link String} that jdk does not provide. Operations in
 * this class is null safe.
 *
 * @version 1.0
 */
public class StringUtilities {
    /**
     * Change a normal string to a HTML formatted string that can be placed in
     * JLabels.
     *
     * @param str the original string
     * @return the HTML formatted string
     */
    public static String makeHTML(String str) {
        if (str == null) {
            return null;
        }
        return "<html>" + str.replaceAll(System.lineSeparator(), "<br>").replaceAll("\n", "<br>")
                + "</html>";
    }

    /**
     * Checks if a string is an integer.
     *
     * @param str the string being queried
     * @return {@code true} if the string is an integer
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (str.chars().allMatch(Character::isDigit)) {
            return true;
        } else {
            if (str.charAt(0) == '-') {
                return isInteger(str.substring(1));
            } else {
                return false;
            }
        }
    }

    /**
     * Checks if a string is a double.
     *
     * @param str the string being queried
     * @return {@code true} if the string is a double
     */
    public static boolean isDouble(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // match a number with optional '-' and decimal.
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Capitalizes a String changing the first letter to title case as per
     * {@code Character.toUpperCase(char)}. No other letters are changed.
     *
     * <p>Examples:
     * <pre>{@code
     *  StringUtilities.capitalize(null)  = null
     *  StringUtilities.capitalize("")    = ""
     *  StringUtilities.capitalize("cat") = "Cat"
     *  StringUtilities.capitalize("cAt") = "CAt"
     * }</pre>
     *
     * @param str the string to be capitalized
     * @return the capitalized String, null if null String input
     */
    public static String capitalize(String str) {
        if (str == null) {
            return null;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
