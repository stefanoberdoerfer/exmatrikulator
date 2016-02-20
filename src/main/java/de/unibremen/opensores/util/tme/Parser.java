package de.unibremen.opensores.util.tme;

import java.util.List;
import java.util.ArrayList;
import java.text.ParseException;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * Parser which can be used to parse TME files created by jgradebook.
 *
 * @author Sören Tempel
 */
public class Parser {
    /**
     * Lexer used to tokenize the input.
     */
    private final Scanner lexer;

    /**
     * TMEObjects parsed so far.
     */
    private final List<TMEObject> nodes;

    /**
     * Next token.
     */
    private Token peekToken;

    /**
     * Creates a new parser object.
     *
     * @param data Data which should be parsed.
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    public Parser(final String data) throws InterruptedException,
           ParseException {
        this.nodes = new ArrayList<>();
        this.lexer = new Scanner(data);

        this.peekToken = null;
        parse();
    }

    /**
     * Returns the next token and advances the cursor position.
     *
     * @return Next token.
     * @throws InterruptedException If thread was interrupted during execution.
     */
    private Token next() throws InterruptedException {
        Token t = peekToken;
        if (t != null) {
            peekToken = null;
        } else {
            t = lexer.nextToken();
        }

        return t;
    }

    /**
     * Returns the next token without advancing the cursor position.
     *
     * @return Next token.
     * @throws InterruptedException If thread was interrupted during execution.
     */
    private Token peek() throws InterruptedException {
        final Token t = peekToken;
        if (t != null) {
            return t;
        }

        peekToken = lexer.nextToken();
        return peekToken;
    }

    /**
     * Return a list of parsed nodes.
     *
     * @return List of parsed TMEObjects.
     */
    public List<TMEObject> getTMEObjects() {
        return nodes;
    }

    /**
     * Starts the parser and adds new nodes to the list.
     *
     * <p>
     * Simplified pseudo EBNF:
     * </p>
     *
     * <pre>
     * TME = { node, "\n" | EOF};
     * </pre>
     *
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    private void parse() throws InterruptedException, ParseException {
        for (Token t = peek(); t.getType() != TokenType.EOF; t = peek()) {
            nodes.add(parseNode());

            final Token nt = peek();
            switch (nt.getType()) {
                case NEWLINE:
                    next();
                    break;
                case EOF:
                    break;
                default:
                    throw new ParseException("Expected newline or EOF",
                            nt.getLine());
            }
        }
    }

    /**
     * Parses a single node and returns it.
     *
     * <p>
     * Simplified pseudo EBNF:
     * </p>
     *
     * <pre>
     * ID   = number;
     * name = string;
     * node = name, " ", ID, " ", "{", "\n", { assignment }, "}";
     * </pre>
     *
     * @return Parsed TMEObject.
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    private TMEObject parseNode() throws InterruptedException, ParseException {
        final Token nameToken = next();
        if (nameToken.getType() != TokenType.IDENTIFIER) {
            throw new ParseException("Missing node name", nameToken.getLine());
        }

        final Token idToken = next();
        if (idToken.getType() != TokenType.NUMBER) {
            throw new ParseException("Missing node ID", idToken.getLine());
        }

        final Token bracketToken = next();
        if (bracketToken.getType() != TokenType.LEFTCURLY) {
            throw new ParseException("Expected opening curly brackets",
                    bracketToken.getLine());
        }

        final Token sepToken = next();
        if (sepToken.getType() != TokenType.NEWLINE) {
            throw new ParseException("Expected newline character",
                   sepToken.getLine());
        }

        Token t = peek();
        TMEObject node = new TMEObject(nameToken.getText(),
                Integer.parseInt(idToken.getText()));

        while (t.getType() != TokenType.RIGHTCURLY) {
            final TokenType tt = t.getType();
            if (tt == TokenType.EOF) {
                throw new ParseException("Unexpected EOF", t.getLine());
            }

            node = parseAssign(node);
            t = peek();
        }

        // Consuming closing bracket.
        next();

        return node;
    }

    /**
     * Parses an assignment and returns a new node with the assigned values. If
     * a key is set but no value has been set for the key the node is not
     * modified at all. Because we can't figure out the type of the key in this
     * case.
     *
     * <p>
     * Simplified pseudo EBNF:
     * </p>
     *
     * <pre>
     * key        = string;
     * assignment = key, "=", [ value ], "\n";
     * </pre>
     *
     * @param node Outer node for this assignment.
     * @return New Node with assigned values.
     *
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    private TMEObject parseAssign(final TMEObject node) throws
            InterruptedException, ParseException {
        final Token keyToken = next();
        if (keyToken.getType() != TokenType.IDENTIFIER) {
            throw new ParseException("Expected key name", keyToken.getLine());
        }

        final Token assignToken = next();
        if (assignToken.getType() != TokenType.ASSIGN) {
            throw new ParseException("Missing assign character",
                    assignToken.getLine());
        }

        final Token valToken = next();
        final TokenType valType = valToken.getType();
        if (valType == TokenType.NEWLINE) {
            return node; // Key value is empty
        } else {
            final Object val = parseValue(valToken);
            if (val != null) {
                // <null> is the same as an empty value.
                node.put(keyToken.getText(), val);
            }
        }

        final Token sepToken = next();
        if (sepToken.getType() != TokenType.NEWLINE) {
            throw new ParseException("Expected newline character",
                    sepToken.getLine());
        }

        return node;
    }

    /**
     * Parses a value.
     *
     * <p>
     * Simplified pseudo EBNF:
     * </p>
     *
     * <pre>
     * boolean = "true" | "false";
     * string  = { letter };
     * number  = { digit }, [ ".", { digit } ];
     * hash    = "{", { value, "=", value, }, "}";
     * null    = "&lt;null&gt;";
     * value   = number | string | boolean | array | hash | null;
     * </pre>
     *
     * @param val Value Token which should be parsed.
     * @return Object representing the value.
     *
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    private Object parseValue(final Token val) throws ParseException,
            InterruptedException {
        String v = val.getText();
        switch (val.getType()) {
            case IDENTIFIER:
                try {
                    v = URLDecoder.decode(val.getText(), "utf-8");
                } catch (final UnsupportedEncodingException e) {
                    // XXX is there a better way to handle this?
                    v = val.getText();
                }

                if (v.equals("false") || v.equals("true")) {
                    return Boolean.valueOf(v);
                } else if (v.equals("<null>")) {
                    return null;
                }

                return v;
            case NUMBER:
                return Integer.valueOf(v);
            case DECIMAL:
                return Double.valueOf(v);
            case LEFTCURLY:
                return parseArray(val);
            default:
                throw new ParseException("Unexpected key value", val.getLine());
        }
    }

    /**
     * Parses an array.
     *
     * <p>
     * Simplified pseudo EBNF:
     * </p>
     *
     * <pre>
     * array = "{", { value, "," | "=" }, "}";
     * </pre>
     *
     * @param val Value Token which should be parsed.
     * @return TMEArray containing the array values.
     *
     * @throws InterruptedException If thread was interrupted during execution.
     * @throws ParseException If the input couldn't be parsed.
     */
    private TMEArray parseArray(final Token val)
        throws ParseException, InterruptedException {
        if (val.getType() != TokenType.LEFTCURLY) {
            throw new ParseException("Expected opening curly brackets",
                    val.getLine());
        }

        Token t = next();
        final TMEArray array = new TMEArray();

        while (t.getType() != TokenType.RIGHTCURLY) {
            final Object v = parseValue(t);

            t = next();
            switch (t.getType()) {
                case COMMA:
                case ASSIGN:
                    final Token nt = peek();
                    if (nt.getType() == TokenType.RIGHTCURLY) {
                        throw new ParseException("Missing value after seperator",
                                nt.getLine());
                    }

                    break;
                case RIGHTCURLY:
                    break;
                default:
                    throw new ParseException("Expected ',', '=' or '}'", t.getLine());
            }

            array.add(v);
            if (t.getType() != TokenType.RIGHTCURLY) {
                t = next();
            }
        }

        // Closing bracket is consumed in parseAssign.
        return array;
    }
}
