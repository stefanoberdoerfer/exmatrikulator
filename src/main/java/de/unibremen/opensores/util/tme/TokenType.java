package de.unibremen.opensores.util.tme;

/**
 * Enumeration representing valid token types.
 *
 * @author Sören Tempel
 */
enum TokenType {
    /**
     * End of file.
     */
    EOF,

    /**
     * The newline character.
     */
    NEWLINE,

    /**
     * Identifier (can contain almost any char).
     */
    IDENTIFIER,

    /**
     * Natural number.
     */
    NUMBER,

    /**
     * Decimal number.
     */
    DECIMAL,

    /**
     * Assign character '='.
     */
    ASSIGN,

    /**
     * Comma character ','.
     */
    COMMA,

    /**
     * Opening curly brackets '{'.
     */
    LEFTCURLY,

    /**
     * Closing curly brackets '}'.
     */
    RIGHTCURLY,
}
