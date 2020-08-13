package golgui.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.NumberFormatter;

/**
 * This is a extended NumberFormatter that allows only natural numbers and empty
 * strings ("") as valid input.
 *
 * @version <b>1.0</b>
 */
public class NumberAndSpaceFormatter extends NumberFormatter {

    /**
     * Stands for a policy that user can input empty string but the empty string will
     * automatically changed to 0.
     */
    public static final int ANY_INPUT_ALLOWED = 0;

    /**
     * Stands for a policy that user can input empty string and the empty string will stay
     * as empty string.
     */
    public static final int EMPTY_STRING_BECOME_ZERO = 1;

    /**
     * Stands for a policy that the user is allowed to input anything.
     */
    public static final int EMPTY_STRING_AND_NUM_ALLOWED = 2;

    /**
     * Stands for a policy that user can input empty string but the empty string will
     * automatically changed to 1. And input of 0 is not allowed.
     */
    public static final int ZERO_NOT_ALLOWED = 3;

    /**
     * {@code true} if the empty string stays as empty string, and {@code false} if empty
     * string will be automatically changed to "0".
     */
    private final int emptyStringPolicy;

    /**
     * Creates a NumberAndSpaceFormatter with the specified Format instance, and sets
     * allowed number to only natural numbers (0 included).
     *
     * @param format            Format used to dictate legal values
     * @param emptyStringPolicy a flag stands for the policy that how the formatter treats
     *                          an empty string
     */
    public NumberAndSpaceFormatter(NumberFormat format, int emptyStringPolicy) {
        super(format);
        if (emptyStringPolicy == ANY_INPUT_ALLOWED) {
            throw new RuntimeException("You don't need a Formatter if you allow any "
                + "input.");
        }
        this.emptyStringPolicy = emptyStringPolicy;
        this.setValueClass(Integer.class);
        if (emptyStringPolicy == ZERO_NOT_ALLOWED) {
            this.setMinimum(1);
        } else {
            this.setMinimum(0);
        }
        this.setMaximum(Integer.MAX_VALUE);
        this.setAllowsInvalid(false);
        this.setCommitsOnValidEdit(true);
    }

    /**
     * Returns the <code>Object</code> representation of the
     * <code>String</code> <code>text</code>.
     *
     * @param text <code>String</code> to convert
     * @return <code>Object</code> representation of text
     * @throws ParseException if there is an error in the conversion
     */
    @Override
    public Object stringToValue(final String text) throws ParseException {
        try {
            return super.stringToValue(text);
        } catch (ParseException e) {
            if (text.equals("")) { // Allow for an empty string
                if (emptyStringPolicy == ZERO_NOT_ALLOWED) {
                    return super.stringToValue("1");
                } else if (emptyStringPolicy == EMPTY_STRING_BECOME_ZERO) {
                    return super.stringToValue("0");
                } else {
                    return "";
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns a String representation of the Object
     * <code>value</code>. This invokes <code>format</code> on the
     * current <code>Format</code>.
     *
     * @param value Value to convert
     * @return String representation of value
     * @throws ParseException if there is an error in the conversion
     */
    public String valueToString(Object value) throws ParseException {
        try {
            return super.valueToString(value);
        } catch (ParseException | IllegalArgumentException e) {
            if (value instanceof String && value.equals("")) {
                return "";   // Allow for an empty string
            } else {
                throw e;
            }
        }
    }
}