package de.unibremen.opensores.util.tme;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import org.junit.Test;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.text.ParseException;

/**
 * Unit tests for the Parser class.
 *
 * @author Sören Tempel.
 */
public class ParserTest {
    /**
     * Test for node without any key/value pairs.
     */
    @Test
    public void testParseEmptyNode() {
        final Parser p = parse("bar 23 {\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals("bar", o.getName());
        assertEquals(23, o.getId());
    }

    /**
     * Test for node with string values.
     */
    @Test
    public void testParseStringValues() {
        final Parser p = parse("foo 42 {\n bar=foo\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals("foo", o.getString("bar"));
    }

    /**
     * Test for node with URL encoded strings.
     */
    @Test
    public void testParseEncodedStringValues() {
        final Parser p = parse("baz 23 {\n a=%C3%9Cbungsblatt \n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals("Übungsblatt", o.getString("a"));
    }

    /**
     * Test for node with boolean values.
     */
    @Test
    public void testParseBooleanValues() {
        final Parser p = parse("foo 42 {\n bar=false\n baz=true\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals(false, o.getBoolean("bar"));
        assertEquals(true, o.getBoolean("baz"));
    }

    /**
     * Test for node with number values.
     */
    @Test
    public void testParseNumberValues() {
        final Parser p = parse("foo 42 {\n bar=23.42\n baz=1337\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals(23.42, o.getDouble("bar"), 0);
        assertEquals(23, o.getInt("bar"));

        assertEquals(1337, o.getDouble("baz"), 0);
        assertEquals(1337, o.getInt("baz"));
    }

    /**
     * Test for node with empty and null values.
     */
    @Test
    public void testParseEmptyValues() {
        final Parser p = parse("foo 42 {\n bar=\n baz=<null>\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertFalse(o.has("bar"));
        assertFalse(o.has("baz"));
    }

    /**
     * Test for node with list values.
     */
    @Test
    public void testParseListValues() {
        final Parser p = parse("foo 42 {\n bar={1, 2, 3}\n baz={foo, bar}\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        final TMEArray a1 = o.getArray("bar");
        assertEquals(3, a1.size());

        assertEquals(1, a1.getInt(0));
        assertEquals(1, a1.getDouble(0), 0);

        assertEquals(2, a1.getInt(1));
        assertEquals(2, a1.getDouble(1), 0);

        assertEquals(3, a1.getInt(2));
        assertEquals(3, a1.getDouble(2), 0);

        final TMEArray a2 = o.getArray("baz");
        assertEquals(2, a2.size());

        assertEquals("foo", a2.getString(0));
        assertEquals("bar", a2.getString(1));
    }

    /**
     * Test for node with mixed values.
     */
    @Test
    public void testPareMixedValues() {
        final Parser p = parse("foo 23 {\n a=a \n b=true \n c=1 \n d=2.3 \n e={foo} \n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(1, objs.size());
        final TMEObject o = objs.get(0);

        assertEquals("a", o.getString("a"));
        assertEquals(true, o.getBoolean("b"));
        assertEquals(1, o.getInt("c"));
        assertEquals(2.3, o.getDouble("d"), 0);

        final TMEArray a = o.getArray("e");
        assertEquals(1, a.size());
        assertEquals("foo", a.getString(0));
    }

    /**
     * Test for node with empty lists.
     */
    @Test
    public void testParseEmptyList() {
        final Parser p = parse("foo 23 {\n a={}\n}");
        final List<TMEObject> objs = p.getTMEObjects();

        final TMEObject o = objs.get(0);
        final TMEArray  a = o.getArray("a");

        assertTrue(a.isEmpty());
    }

    /**
     * Test with multiple nodes.
     */
    @Test
    public void testParseMultipleNodes() {
        final Parser p = parse("bar 42 {\n a=a \n} \n baz 23 {\n a=b \n}");
        final List<TMEObject> objs = p.getTMEObjects();

        assertEquals(2, objs.size());
        final TMEObject o1 = objs.get(0);
        final TMEObject o2 = objs.get(1);

        assertEquals("a", o1.getString("a"));
        assertEquals("b", o2.getString("a"));
    }

    /**
     * Test invalid TME markup.
     */
    @Test
    public void testInvalidTMEMarkup() {
        final Map<String, String> map = new HashMap<>();
        map.put("bar 42 {\n} fail", "Expected newline or EOF");
        map.put("bar foo {\n}", "Missing node ID");
        map.put("42 23 {\n}", "Missing node name");
        map.put("foo 23 [\n]", "Expected opening curly brackets");
        map.put("bar 23 {}", "Expected newline character");
        map.put("baz 42 {\n a-c \n}", "Missing assign character");
        map.put("bar 23 {\n 23=42 \n}", "Expected key name");
        map.put("baz 99 {\n bar=23}", "Expected newline character");
        map.put("lel 12 {\n foo== \n}", "Unexpected key value");
        map.put("kek 13 {\n a={1,} \n}", "Missing value after seperator");
        map.put("kek 13 {\n a={] \n}", "Expected ',', '=' or '}'");

        for (final Map.Entry<String, String> t : map.entrySet()) {
            try {
                new Parser(t.getKey());
            } catch (final ParseException e) {
                assertEquals(t.getValue(), e.getMessage());
                continue;
            } catch (final InterruptedException e) {
                fail();
            }

            fail();
        }
    }

    /**
     * Helper method for creating a new Parser.
     */
    private Parser parse(final String s) {
        final Parser p;
        try {
            p = new Parser(s);
        } catch (final InterruptedException|ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        return p;
    }
}
