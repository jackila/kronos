package org.kronos.utils;

import java.util.Arrays;

/**
 * @Author: jackila
 * @Date: 21:53 2022/12/26
 */
public class Utils {
    /**
     * Get the key from the given args. Keys have to start with '-' or '--'. For example, --key1
     * value1 -key2 value2.
     *
     * @param args all given args.
     * @param index the index of args to be parsed.
     * @return the key of the given arg.
     */
    public static String getKeyFromArgs(String[] args, int index) {
        String key;
        if (args[index].startsWith("--")) {
            key = args[index].substring(2);
        } else if (args[index].startsWith("-")) {
            key = args[index].substring(1);
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "Error parsing arguments '%s' on '%s'. Please prefix keys with -- or -.",
                            Arrays.toString(args), args[index]));
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "The input " + Arrays.toString(args) + " contains an empty argument");
        }

        return key;
    }
}
