package epidemics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * This class contains utility methods that will be used in the simulation.
 *
 * @version 1.0
 */
public class EpidemicUtilities {

    /**
     * This method will random select some elements from a list and apply an action on
     * them.
     *
     * @param list   the list being queried
     * @param num    the number of selection; if {@code num > list.size()}, all elements
     *               will be selected
     * @param action the action performed on the list
     * @param <T>    the type of the element of the list
     * @return the number of items that were applied with the action
     */
    public static <T> int randomApply(List<T> list, int num,
        Consumer<? super T> action) {
        list = new ArrayList<>(list);
        if (num < list.size()) {
            // Use Fisher-Yates shuffle algorithm to get a random order list
            Collections.shuffle(list, ThreadLocalRandom.current());
        }
        // Select n elements and apply the action
        for (int i = 0; i < Math.min(num, list.size()); i++) {
            action.accept(list.get(i));
        }
        return Math.min(num, list.size());
    }

    /**
     * This method will random select one element from a list.
     *
     * @param list   the list being queried
     * @param <T>    the type of the element of the list
     * @return the number of items that were applied with the action
     */
    public static <T> T randomSelect(List<T> list) {
        list = new ArrayList<>(list);
        // Use Fisher-Yates shuffle algorithm to get a random order list
        Collections.shuffle(list, ThreadLocalRandom.current());
        // Select n elements and apply the action
        if (list.size() < 1) {
            return null;
        }
        return list.get(0);
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
        // match a number with optional '-' and decimal.
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
