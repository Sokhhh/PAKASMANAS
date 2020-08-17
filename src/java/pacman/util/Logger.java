package pacman.util;

import java.io.PrintStream;
import java.util.IllegalFormatException;

/**
 * This is an utility to print logs.
 *
 * @version 1.0
 */
public class Logger {
    /** Contains a flag that if log should be printed. */
    public static boolean LOG_ENABLED = true;
    /** Contains a flag that if stack trace should be printed. */
    public static boolean LOG_DETAIL = true;

    /** Contains the stream for outputting the log. */
    public static PrintStream logger = System.out;

    /** Prints in normal text color in console. */
    public static final String ANSI_RESET = "\u001B[0m";

    /** Prints in green. */
    public static final String ANSI_GREEN = "\u001B[32m";

    /** Prints in purple. */
    public static final String ANSI_PURPLE = "\u001B[35m";

    /** Prints in yello. */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * This function prints log to the console with the information of function callee.
     *
     * @param logs strings of log detail
     */
    public static void println(Object... logs) {
        if (LOG_ENABLED) {
            if (LOG_DETAIL) {
                logger.print("[LOG] " + Thread.currentThread().getStackTrace()[2] + ": ");
            }
            for (Object log : logs) {
                logger.print(log.toString() + " ");
            }
            logger.println();
        }
    }

    /**
     * This function prints a formatted string to the log stream using the specified
     * format string and arguments, and terminates the current line by writing the
     * line separator string. The line separator string is defined by the system
     * property <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     *
     * @param  format
     *         A format string as described in <a
     *         href="../pacman.util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../pacman.util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../pacman.util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     */
    public static void printlnf(String format, Object ... args)
        throws IllegalFormatException {
        if (LOG_ENABLED) {
            if (LOG_DETAIL) {
                logger.print("[LOG] " + Thread.currentThread().getStackTrace()[2] + ": ");
            }
            logger.printf(format, args);
            logger.println();
        }
    }

    /**
     * Prints error message.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../pacman.util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../pacman.util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../pacman.util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     */
    public static void err(String format, Object ... args)
        throws IllegalFormatException {
        if (LOG_ENABLED) {
            if (LOG_DETAIL) {
                System.err.print("[LOG] " + Thread.currentThread().getStackTrace()[2]
                    + ": ");
            }
            System.err.printf(format, args);
            System.err.println();
        }
    }

    /**
     * This function prints a formatted string to the log stream using the specified
     * format string and arguments, and terminates the current line by writing the
     * line separator string. The line separator string is defined by the system
     * property <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     *
     * @param  color  the color of the text
     * @param  format
     *         A format string as described in <a
     *         href="../pacman.util/Formatter.html#syntax">Format string syntax</a>
     * @param stack the number of stack need to be printed
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../pacman.util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../pacman.util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     */
    public static void printColor(String color, int stack, String format, Object ... args)
        throws IllegalFormatException {
        if (LOG_ENABLED) {
            if (LOG_DETAIL) {
                logger.print("[LOG] " + Thread.currentThread().getStackTrace()[2 + stack]
                    + ": " + color);
            }
            logger.printf(format, args);
            logger.println(ANSI_RESET);
        }
    }
}
