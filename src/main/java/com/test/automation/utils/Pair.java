package com.test.automation.utils;


/**
 * Replacement class for javac Pair class.
 * @param <T>
 * @param <U>
 */
public class Pair<T, U>
{
    public T fst;
    public U snd;

    public Pair(T first, U second) {
        this.fst = first;
        this.snd = second;

    }
    // Return a map entry (key-value pair) from the specified values
    public static <T, U> Pair<T, U> of(T first, U second)
    {
        return new Pair<>(first, second);
    }

    public String toString() {
        return "Pair (" + fst + "," + snd + ")";
    }

}
