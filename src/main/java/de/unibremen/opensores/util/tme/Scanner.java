package de.unibremen.opensores.util.tme;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Scanner which can be used to tokenize TME files.
 *
 * @author Sören Tempel
 */
class Scanner {
    /**
     * Integer value for EOF.
     */
    private static final int EOF = -1;

    /**
     * Queue containing scanned items.
     */
    private BlockingQueue<Token> tokens;

    /**
     * The line of text being scanned currently.
     */
    private String input;

    /**
     * The next lexing function to enter.
     */
    private State state;

    /**
     * Current line number in the input.
     */
    private int line;

    /**
     * Current position in the input.
     */
    private int pos;

    /**
     * Start position of this item.
     */
    private int start;

    /**
     * Width of last read character.
     */
    private int width;

    /**
     * FunctionalInterface to represent State.
     */
    @FunctionalInterface
    private interface State {
        public State lex(final Scanner sc);
    }

    /**
     * Creates a new Lexer for the given data.
     *
     * @parma data Data which should be tokenized.
     */
    public Scanner(final String data) {
        line = 1;
        input = data;

        tokens = new LinkedBlockingQueue<>();
        new Thread(this::run).start();
    }

    /**
     * Returns the next token.
     *
     * @return Next token.
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws NoSuchElementException If there is no next token.
     */
    public Token nextToken() throws InterruptedException {
        if (tokens == null) {
            throw new NoSuchElementException();
        }

        final Token t = tokens.take();
        if (t.getType() == TokenType.EOF) {
            tokens = null;
        }

        return t;
    }

    /**
     * Returns the next character.
     *
     * @return Next UTF32 character.
     */
    private int next() {
        if (pos >= input.length()) {
            width = 0;
            return EOF;
        }

        final int c = input.codePointAt(pos);
        width = Character.charCount(c);

        pos += width;
        return c;
    }

    /**
     * Returns the next character without advancing the cusor.
     *
     * @return Next UTF32 character.
     */
    private int peek() {
        final int c = next();
        backup();
        return c;
    }

    /**
     * Resets the cursor position.
     */
    private void backup() {
        pos -= width;
    }

    /**
     * Ignores the currently processed token.
     */
    private void ignore() {
        start = pos;
    }

    /**
     * Emits a new token and makes it available to the caller.
     *
     * @param tt TokenType to emit.
     */
    private void emit(final TokenType tt) {
        if (tt == TokenType.NEWLINE) {
            line++;
        }

        final String s = input.substring(start, pos);
        tokens.add(new Token(tt, line, s));
        start = pos;
    }

    /**
     * Starts the tokenizer thread.
     */
    private void run() {
        for (state = this::lexAny; state != null;) {
            state = state.lex(this);
        }

        tokens.add(new Token(TokenType.EOF, line, "EOF"));
    }

    /**
     * Returns true if the given code point is a separation character.
     *
     * @param cp Code point which should be checked.
     * @return True if it is, false otherwise.
     */
    private boolean isSep(final int cp) {
        return cp == EOF || cp == '=' || cp == ',' || cp == '}' || cp == '{'
            || Character.isSpaceChar(cp) || Character.isWhitespace(cp);
    }

    /**
     * Lexes a number.
     *
     * @param sc Scanner used for lexing.
     * @return New State after lexing the number.
     */
    private State lexNumber(final Scanner sc) {
        boolean isDecimal = false;
        while (true) {
            final int c = sc.peek();
            if (Character.isDigit(c)) {
                sc.next();
                continue;
            }

            if (c == '.' && !isDecimal) {
                sc.next();
                if (Character.isDigit(sc.peek())) {
                    isDecimal = true;
                    continue;
                } else {
                    sc.backup();
                }
            }

            if (isSep(c)) {
                sc.emit((isDecimal) ? TokenType.DECIMAL : TokenType.NUMBER);
            } else {
                return this::lexIdentifier;
            }

            break;
        }

        return this::lexAny;
    }

    /**
     * Lexes an identifier.
     *
     * @param sc Scanner used for lexing.
     * @return New State after lexing the number.
     */
    private State lexIdentifier(final Scanner sc) {
        while (true) {
            final int c = sc.peek();
            if (isSep(c)) {
                sc.emit(TokenType.IDENTIFIER);
                break;
            } else {
                sc.next();
            }
        }

        return this::lexAny;
    }

    /**
     * Tries to lex any given character.
     *
     * @param sc Scanner used for lexing.
     * @return New State after lexing the number.
     */
    private State lexAny(final Scanner sc) {
        final int c = sc.next();
        if (c == EOF) {
            return null;
        } else if (c == '\n') {
            sc.emit(TokenType.NEWLINE);
            return this::lexAny;
        } else if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
            sc.ignore();
            return this::lexAny;
        } else if (c == '=') {
            sc.emit(TokenType.ASSIGN);
            return this::lexAny;
        } else if (c == ',') {
            sc.emit(TokenType.COMMA);
            return this::lexAny;
        } else if (c == '{') {
            sc.emit(TokenType.LEFTCURLY);
            return this::lexAny;
        } else if (c == '}') {
            sc.emit(TokenType.RIGHTCURLY);
            return this::lexAny;
        } else if (Character.isDigit(c)) {
            return this::lexNumber;
        } else {
            return this::lexIdentifier;
        }
    }
}
