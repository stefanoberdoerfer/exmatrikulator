package de.unibremen.opensores.util.tme;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import org.junit.Test;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Unit tests for the scanner class.
 */
public class ScannerTest {
    /**
     * Test next method with different line numbers.
     */
    @Test
    public void testNextLineNumbers() {
        final Scanner s = new Scanner("foo\nbar");

        final Token t1 = next(s);
        assertEquals(1, t1.getLine());

        final Token t2 = next(s);
        assertEquals(2, t2.getLine());

        final Token t3 = next(s);
        assertEquals(2, t2.getLine());
    }

    /**
     * Positive test for nextToken method.
     */
    @Test
    public void testNextTokenPositive() {
        final Scanner s = new Scanner("μ");
        assertEquals(next(s).getText(), "μ");
        assertEquals(next(s).getText(), "EOF");
    }

    /**
     * Negative test for the nextToken method.
     */
    @Test(expected=NoSuchElementException.class)
    public void testNextTokenNegative() {
        final Scanner s = new Scanner("");
        assertEquals(next(s).getText(), "EOF");
        next(s);
    }

    /**
     * Test next method with mixed values.
     */
    @Test
    public void testNextMixedValues() {
        final Map<String, String[]> map = new HashMap<>();
        map.put("1337 foo 2.3", new String[] {"1337", "foo", "2.3"});
        map.put("true ,    23", new String[] {"true", ",", "23"});
        map.put("\n\r\r\n", new String[] {"\n", "\n"});
        map.put("= foo ==", new String[] {"=", "foo", "=", "="});
        map.put("=foo=", new String[] {"=", "foo", "="});
        map.put(",,foo,,", new String[] {",", ",", "foo", ",", ","});
        map.put("{foo}", new String[] {"{", "foo", "}"});

        for (final Map.Entry<String, String[]> e : map.entrySet()) {
            final Scanner sc = new Scanner(e.getKey());
            for (final String s : e.getValue()) {
                final Token t = next(sc);
                assertEquals(s, t.getText());
            }

            assertEquals(TokenType.EOF, next(sc).getType());
        }
    }

    /**
     * Test next method with different token types.
     */
    @Test
    public void testNextMixedTypes() {
        final Map<String, TokenType[]> map = new HashMap<>();
        map.put("1337 f", new TokenType[] {TokenType.NUMBER, TokenType.IDENTIFIER});
        map.put("2.3 ,=", new TokenType[] {TokenType.DECIMAL, TokenType.COMMA, TokenType.ASSIGN});
        map.put("2. 2f", new TokenType[] {TokenType.IDENTIFIER, TokenType.IDENTIFIER});
        map.put("{,f", new TokenType[] {TokenType.LEFTCURLY, TokenType.COMMA, TokenType.IDENTIFIER});
        map.put("}2", new TokenType[] {TokenType.RIGHTCURLY, TokenType.NUMBER});
        map.put("\n()\n", new TokenType[] {TokenType.NEWLINE, TokenType.IDENTIFIER, TokenType.NEWLINE});
        map.put("", new TokenType[] {TokenType.EOF});

        for (final Map.Entry<String, TokenType[]> e : map.entrySet()) {
            final Scanner sc = new Scanner(e.getKey());
            for (final TokenType t : e.getValue()) {
                final Token c = next(sc);
                assertEquals(t, c.getType());
            }
        }
    }

    /**
     * Helper method for invoking Scanner.nextToken.
     */
    private Token next(final Scanner s) {
        final Token t;
        try {
            t = s.nextToken();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        return t;
    }
}
