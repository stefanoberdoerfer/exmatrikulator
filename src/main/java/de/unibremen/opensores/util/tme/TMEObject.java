package de.unibremen.opensores.util.tme;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * TMEObject represents a single TME node.
 *
 * @author Sören Tempel.
 */
public class TMEObject {
    /**
     * TMEObject name.
     */
    final String name;

    /**
     * TMEObject id.
     */
    final int id;

    /**
     * Key value map.
     */
    final Map<String, Object> map;

    /**
     * Creates a new TMEObject.
     *
     * @param name TMEObject name.
     * @param id TMEObject id.
     */
    TMEObject(final String name, final int id) {
        this.name = name;
        this.id = id;
        this.map = new HashMap<>();
    }

    /**
     * Put a key/value pair in the TMEObject.
     *
     * @param key String which should be used as the key.
     * @param value The associated key value.
     * @throws NullPointerException if the key or the value is null.
     */
    void put(final String key, final Object value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }

        map.put(key, value);
    }

    /**
     * Whether the given key is present in the object.
     *
     * @param key Key which should be looked up.
     * @return True if it is, false otherwise.
     */
    public boolean has(final String key) {
        return map.containsKey(key);
    }

    /**
     * Getter for the name attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the id attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key Key which should be looked up.
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public Object get(final String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final Object val = map.get(key);
        if (val == null) {
            throw new NoSuchElementException();
        }

        return val;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key Key which should be looked up
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public boolean getBoolean(final String key) {
        final Object obj = get(key);
        if (obj.equals(Boolean.FALSE)) {
            return false;
        } else if (obj.equals(Boolean.TRUE)) {
            return true;
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the string value associated with a key.
     *
     * @param key Key which should be looked up
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public String getString(final String key) {
        final Object obj = get(key);
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).toString();
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the int value associated with a key.
     *
     * @param key Key which should be looked up
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public int getInt(final String key) {
        final Object obj = get(key);
        if (obj instanceof Number) {
            return ((Number)obj).intValue();
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key Key which should be looked up
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public double getDouble(final String key) {
        final Object obj = get(key);
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }

        throw new NoSuchElementException();
    }

    /**
     * Get the array value associated with a key.
     *
     * @param key Key which should be looked up
     * @throw NullPointerException If the specified key is null.
     * @throw NoSuchElementException If the requested key doesn't exist.
     * @return The object associated with the key.
     */
    public TMEArray getArray(final String key) {
        final Object obj = get(key);
        if (obj instanceof TMEArray) {
            return (TMEArray)obj;
        }

        throw new NoSuchElementException();
    }
}
