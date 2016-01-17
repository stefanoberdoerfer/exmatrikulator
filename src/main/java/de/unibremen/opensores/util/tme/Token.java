package de.unibremen.opensores.util.tme;

/**
 * Token represents a single input token.
 */
class Token {
    /**
     * Type of this token.
     */
    private final TokenType type;

    /**
     * Line number this token appears on.
     */
    private final int line;

    /**
     * Text of this token item.
     */
    private final String text;

    /**
     * Creates a new input token.
     *
     * @param type Type of the token.
     * @param line Line of the token.
     * @param text Text of the token.
     */
    Token(final TokenType type, final int line, final String text) {
        this.type = type;
        this.line = line;
        this.text = text;
    }

    /**
     * Returns the type of the token.
     *
     * @return Type of the token.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the line number the token is located at.
     *
     * @return Line number of the token.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the text of this token.
     *
     * @return Text of the token.
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return getText();
    }
}
