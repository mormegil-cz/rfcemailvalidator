package cz.mormegil.rfcemailvalidator;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

/**
 * Validator for e-mail address strictly according to RFC 822. It verifies the supplied string matches the “addr-spec”
 * production in the specification.
 *
 * @see <a href="https://tools.ietf.org/html/rfc822#section-6.1">RFC 822, section 6.1</a>
 */
public class Rfc822EmailAddressValidator {
    /**
     * The string to be validated
     */
    private final String str;

    /**
     * Current position in the validated string
     */
    private int pos;

    /**
     * Characters in the “specials” set.
     *
     * @see <a href="https://tools.ietf.org/html/rfc822#section-3.3">RFC 822, section 3.3</a>
     */
    private static final HashSet<Character> SPECIALS = new HashSet<>(Arrays.asList('(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '.', '[', ']'));

    /**
     * Constructor. Prepares to validate the supplied string
     *
     * @param str String to be validated
     */
    public Rfc822EmailAddressValidator(String str) {
        this.str = Objects.requireNonNull(str);
    }

    /**
     * Do the validation of the string passed in the constructor. If the e-mail address is not valid,
     * throws an exception, otherwise, returns normally.
     *
     * @throws ParseException if the string is not a valid e-mail address
     */
    public void validate() throws ParseException {
        pos = 0;
        parseAddrSpec();
    }

    // region --- Generic parsing utility methods ---

    private char peekChar() throws ParseException {
        if (pos >= str.length()) {
            return '\0';
        }
        final char c = str.charAt(pos);
        if (c == '\0') {
            throw new ParseException("Embedded NUL found", pos);
        }
        return c;
    }

    private String charNameOrEof(char currentChar) {
        return currentChar == '\0' ? "end of input" : "'" + currentChar + "'";
    }

    private void consumeChar(char expectedChar) throws ParseException {
        final char currentChar = peekChar();
        if (currentChar != expectedChar) {
            throw new ParseException(String.format("'%c' expected but %s found", expectedChar, charNameOrEof(currentChar)), pos);
        }
        ++pos;
    }

    // endregion

    // region --- The individual production rules from the RFC ---

    private void parseChar() throws ParseException {
        if (pos >= str.length()) {
            throw new ParseException("Unexpected end of input reached", pos);
        }
        ++pos;
    }

    private void parseAddrSpec() throws ParseException {
        parseLocalPart();
        consumeChar('@');
        parseDomain();
    }

    private void parseLocalPart() throws ParseException {
        parseWord();
        while (peekChar() == '.') {
            consumeChar('.');
            parseWord();
        }
    }

    private void parseWord() throws ParseException {
        if (peekChar() == '"') {
            parseQuotedString();
        } else {
            parseAtom();
        }
    }

    private void parseQuotedString() throws ParseException {
        consumeChar('"');
        while (peekChar() != '"') {
            if (peekChar() == '\\') {
                parseQuotedPair();
            } else {
                parseQtext();
            }
        }
        consumeChar('"');
    }

    private void parseQuotedPair() throws ParseException {
        consumeChar('\\');
        parseChar();
    }

    private void parseQtext() throws ParseException {
        final char currentChar = peekChar();
        if (currentChar == '\0' || currentChar == '"' || currentChar == '\\' || currentChar == '\015') {
            throw new ParseException(String.format("Unexpected %s where qtext expected", charNameOrEof(currentChar)), pos);
        }
        ++pos;
    }

    private boolean isAtomChar(char c) {
        return !(SPECIALS.contains(c) || c <= ' ');
    }

    private void parseAtom() throws ParseException {
        final char currentChar = peekChar();
        if (!isAtomChar(currentChar)) {
            throw new ParseException(String.format("Unexpected %s where atom expected", charNameOrEof(currentChar)), pos);
        }
        while (isAtomChar(peekChar())) {
            ++pos;
        }
    }

    private void parseDomain() throws ParseException {
        parseSubdomain();
        while (peekChar() == '.') {
            consumeChar('.');
            parseSubdomain();
        }
    }

    private void parseSubdomain() throws ParseException {
        if (peekChar() == '[') {
            parseDomainLiteral();
        } else {
            parseDomainRef();
        }
    }

    private void parseDomainRef() throws ParseException {
        parseAtom();
    }

    private void parseDomainLiteral() throws ParseException {
        consumeChar('[');
        while (peekChar() != ']') {
            if (peekChar() == '\\') {
                parseQuotedPair();
            } else {
                parseDtext();
            }
        }
        consumeChar(']');
    }

    private void parseDtext() throws ParseException {
        final char currentChar = peekChar();
        if (currentChar == '\0' || currentChar == '[' || currentChar == ']' || currentChar == '\\' || currentChar == '\015') {
            throw new ParseException(String.format("Unexpected %s where dtext expected", charNameOrEof(currentChar)), pos);
        }
        ++pos;
    }

    // endregion
}
