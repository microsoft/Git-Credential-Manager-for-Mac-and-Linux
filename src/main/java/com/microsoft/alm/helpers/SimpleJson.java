// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A very simple JSON [de-]serializer that only handles a dictionary of scalars
 * (string, number, true, false, null).
 */
public class SimpleJson {

    enum State {
        START,
        PRE_KEY,
        KEY,
        PRE_VALUE,
        VALUE,
        NUMBER_VALUE,
        STRING_VALUE,
        STRING_VALUE_ESCAPE,
        STRING_VALUE_UNICODE,
        LITERAL_VALUE,
        POST_VALUE,
        END,
        ;
    }

    private SimpleJson() {
        // not intended to be used as an instance class
    }

    static void error(final char c, final State s) {
        final String message = "Unexpected character '" + c + "' at state " + s + ".";
        throw new IllegalArgumentException(message);
    }

    static boolean isDigit(final char c) {
        return Character.isDigit(c);
    }

    static boolean isHexDigit(final char c) {
        return Character.digit(c, 16) != -1;
    }

    static boolean isInsignificantWhitespace(final char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    static boolean isLeftCurlyBracket(final char c) {
        return c == '{';
    }

    static boolean isRightCurlyBracket(final char c) {
        return c == '}';
    }

    static boolean isColon(final char c) {
        return c == ':';
    }

    static boolean isComma(final char c) {
        return c == ',';
    }

    static boolean isPeriod(final char c) {
        return c == '.';
    }

    static boolean isDoubleQuote(final char c) {
        return c == '"';
    }

    static boolean isLiteralStart(final char c) {
        return c == 't' || c == 'f' || c == 'n';
    }

    static boolean isMinus(final char c) {
        return c == '-';
    }

    static boolean isExp(final char c) {
        return c == 'e' || c == 'E' || c == '-' || c == '+';
    }

    static boolean isEscape(final char c) {
        return c == '\\';
    }

    static Object decodeLiteral(final String input) {
        if ("true".equals(input)) {
            return true;
        }
        else if ("false".equals(input)) {
            return false;
        }
        else if ("null".equals(input)) {
            return null;
        }
        throw new IllegalArgumentException("Invalid literal '" + input + "'. Expected one of 'true', 'false' or 'null'.");
    }

    public static Map<String, Object> parse(final String input) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        final StringBuilder token = new StringBuilder();
        final StringBuilder unicodeHex = new StringBuilder();

        State state = State.START;
        String key = null;
        Object value;
        for (final char c : input.toCharArray()) {
            switch (state) {
                case START:
                    if (isLeftCurlyBracket(c)) {
                        state = State.PRE_KEY;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case PRE_KEY:
                    if (isDoubleQuote(c)) {
                        state = State.KEY;
                    }
                    else if (isRightCurlyBracket(c)) {
                        state = State.END;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case KEY:
                    if (isDoubleQuote(c)) {
                        key = token.toString();
                        token.setLength(0);
                        state = State.PRE_VALUE;
                    }
                    else {
                        token.append(c);
                    }
                    break;
                case PRE_VALUE:
                    if (isColon(c)) {
                        state = State.VALUE;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case VALUE:
                    if (isDoubleQuote(c)) {
                        state = State.STRING_VALUE;
                    }
                    else if (isMinus(c) || isDigit(c)) {
                        token.append(c);
                        state = State.NUMBER_VALUE;
                    }
                    else if (isLiteralStart(c)) {
                        token.append(c);
                        state = State.LITERAL_VALUE;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case NUMBER_VALUE:
                    if (isComma(c)) {
                        final String candidateDouble = token.toString();
                        token.setLength(0);
                        value = Double.parseDouble(candidateDouble);
                        result.put(key, value);
                        state = State.PRE_KEY;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        final String candidateDouble = token.toString();
                        token.setLength(0);
                        value = Double.parseDouble(candidateDouble);
                        result.put(key, value);
                        state = State.POST_VALUE;
                    }
                    else if (isRightCurlyBracket(c)) {
                        final String candidateDouble = token.toString();
                        token.setLength(0);
                        value = Double.parseDouble(candidateDouble);
                        result.put(key, value);
                        state = State.END;
                    }
                    else if (isDigit(c) || isExp(c) || isPeriod(c)) {
                        token.append(c);
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case STRING_VALUE:
                    if (isEscape(c)) {
                        state = State.STRING_VALUE_ESCAPE;
                    }
                    else if (isDoubleQuote(c)) {
                        value = token.toString();
                        token.setLength(0);
                        result.put(key, value);
                        state = State.POST_VALUE;
                    }
                    else {
                        token.append(c);
                    }
                    break;
                case STRING_VALUE_ESCAPE:
                    switch (c) {
                        case '"':
                        case '\\':
                        case '/':
                            token.append(c);
                            state = State.STRING_VALUE;
                            break;
                        case 'b':
                            token.append('\b');
                            state = State.STRING_VALUE;
                            break;
                        case 'f':
                            token.append('\f');
                            state = State.STRING_VALUE;
                            break;
                        case 'n':
                            token.append('\n');
                            state = State.STRING_VALUE;
                            break;
                        case 'r':
                            token.append('\r');
                            state = State.STRING_VALUE;
                            break;
                        case 't':
                            token.append('\t');
                            state = State.STRING_VALUE;
                            break;
                        case 'u':
                            state = State.STRING_VALUE_UNICODE;
                            break;
                        default:
                            error(c, state);
                    }
                    break;
                case STRING_VALUE_UNICODE:
                    if (isHexDigit(c)) {
                        unicodeHex.append(c);
                        if (unicodeHex.length() == 4) {
                            final String candidateHex = unicodeHex.toString();
                            unicodeHex.setLength(0);
                            final int codePoint = Integer.parseInt(candidateHex, 16);
                            final char[] chars = Character.toChars(codePoint);
                            token.append(chars);
                            state = State.STRING_VALUE;
                        }
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case LITERAL_VALUE:
                    switch (c) {
                        case 'a':
                        case 'e':
                        case 'l':
                        case 'r':
                        case 's':
                        case 'u':
                            token.append(c);
                            break;
                        case ',': {
                                final String candidateLiteral = token.toString();
                                token.setLength(0);
                                final Object literal = decodeLiteral(candidateLiteral);
                                result.put(key, literal);
                                state = State.POST_VALUE;
                            }
                            break;
                        case '}': {
                                final String candidateLiteral = token.toString();
                                token.setLength(0);
                                final Object literal = decodeLiteral(candidateLiteral);
                                result.put(key, literal);
                                state = State.END;
                            }
                            break;
                        default:
                            error(c, state);
                    }
                    break;
                case POST_VALUE:
                    if (isComma(c)) {
                        state = State.PRE_KEY;
                    }
                    else if (isRightCurlyBracket(c)) {
                        state = State.END;
                    }
                    else if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
                case END:
                    if (isInsignificantWhitespace(c)) {
                        continue;
                    }
                    else {
                        error(c, state);
                    }
                    break;
            }
        }

        return result;
    }

    public static String format(final Map<String, Object> input) {
        return null;
    }

}
