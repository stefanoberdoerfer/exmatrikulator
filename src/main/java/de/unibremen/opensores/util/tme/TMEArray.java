package de.unibremen.opensores.util.tme;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * TMEArray represents a TME array object.
 *
 * @author Sören Tempel
 */
public class TMEArray {
    /**
     * List holding the items.
     */
    private final List<Object> list;

    /**
     * Creates a new TMEArray.
     */
    public TMEArray() {
        this.list = new ArrayList<>();
    }

    /**
     * Adds an element to the end of the TMEArray.
     *
     * @param obj Object which should be added.
     * @throws NullPointerException If the given Object is null.
     */
    void add(final Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        list.add(obj);
    }

    /**
     * Returns the size of this list.
     *
     * @return Size of the list.
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if this list is empty.
     *
     * @return True if it is empty, false otherwise.
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Get the value object located at the given index.
     *
     * @param idx Index to use for lookup.
     * @return The object associated with the index.
     * @throws IndexOutOfBoundsException If the index is out of range.
     */
    public Object get(final int idx) {
        if (idx < 0 || idx >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(idx);
    }

    /**
     * Get the boolean value located at the given index.
     *
     * @param idx Index to use for lookup.
     * @throw IndexOutOfBoundsException If the index is out of range.
     * @throw NoSuchElementException If there is no boolean value at the given
     *     index.
     * @return The boolean associated with the index.
     */
    public boolean getBoolean(final int idx) {
        final Object obj = get(idx);
        if (obj.equals(Boolean.FALSE)) {
            return false;
        } else if (obj.equals(Boolean.TRUE)) {
            return true;
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the string value located at the given index.
     *
     * @param idx Index to use for lookup.
     * @throw IndexOutOfBoundsException If the index is out of range.
     * @throw NoSuchElementException If there is no string value at the given
     *     index.
     * @return The string associated with the index.
     */
    public String getString(final int idx) {
        final Object obj = get(idx);
        if (obj instanceof String) {
            return (String) obj;
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the int value located at the given index.
     *
     * @param idx Index to use for lookup.
     * @throw IndexOutOfBoundsException If the index is out of range.
     * @throw NoSuchElementException If there is no int value at the given
     *     index.
     * @return The int associated with the index.
     */
    public int getInt(final int idx) {
        final Object obj = get(idx);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the double value located at the given index.
     *
     * @param idx Index to use for lookup.
     * @throw IndexOutOfBoundsException If the index is out of range.
     * @throw NoSuchElementException If there is no double value at the given
     *     index.
     * @return The double associated with the index.
     */
    public double getDouble(final int idx) {
        final Object obj = get(idx);
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }

        throw new NoSuchElementException();
    }
}
