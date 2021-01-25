package cz.mormegil.rfcemailvalidator.tools;

import cz.mormegil.rfcemailvalidator.Rfc822EmailAddressValidator;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * A utility program generating random examples of valid e-mail addresses
 */
public class ExampleGenerator {
    private static final HashSet<Character> SPECIALS = new HashSet<>(Arrays.asList('(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '.', '[', ']'));

    private static final Random rng = new Random();
    private static final StringBuilder builder = new StringBuilder();

    /**
     * Program entry point
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        final int count;
        switch (args.length) {
            case 0:
                count = 10;
                break;
            case 1:
                count = Integer.parseInt(args[0]);
                break;
            default:
                System.out.println("Usage: ExampleGenerator [count]");
                return;
        }

        for (int i = 0; i < count; ++i) {
            final String randomAddress = generateRandomAddress();
            final Rfc822EmailAddressValidator validator = new Rfc822EmailAddressValidator(randomAddress);
            try {
                validator.validate();
            } catch (ParseException e) {
                System.out.println("*** Address '" + randomAddress + "' reported as invalid: " + e);
            }
            System.out.println(randomAddress);
        }
    }

    private static String generateRandomAddress() {
        builder.setLength(0);
        generateAddrSpec();
        return builder.toString();
    }

    private static void generateAddrSpec() {
        generateLocalPart();
        builder.append('@');
        generateDomain();
    }

    private static void generateLocalPart() {
        generateWord();
        int length = rng.nextInt(3);
        for (int i = 0; i < length; ++i) {
            builder.append('.');
            generateWord();
        }
    }

    private static void generateWord() {
        if (rng.nextInt(5) == 0) {
            generateQuotedString();
        } else {
            generateAtom();
        }
    }

    private static void generateQuotedString() {
        builder.append('"');
        final int length = rng.nextInt(10);
        for (int i = 0; i < length; ++i) {
            if (rng.nextInt(20) == 0) {
                generateQuotedPair();
            } else {
                generateQtext();
            }
        }
        builder.append('"');
    }

    private static void generateQuotedPair() {
        builder.append('\\');
        generateChar();
    }

    private static char randomChar() {
        return (char) (rng.nextInt(127 - 32 + 1) + 32); // OK, we could generate controls, but...
    }

    private static void generateChar() {
        builder.append(randomChar());
    }

    private static void generateQtext() {
        char generatedChar;
        do {
            generatedChar = randomChar();
        } while (generatedChar == '"' || generatedChar == '\\');
        builder.append(generatedChar);
    }

    private static void generateAtomChar() {
        char generatedChar;
        do {
            generatedChar = randomChar();
        } while (generatedChar == ' ' || SPECIALS.contains(generatedChar));
        builder.append(generatedChar);
    }

    private static void generateAtom() {
        final int length = rng.nextInt(10) + 1;
        for (int i = 0; i < length; ++i) {
            generateAtomChar();
        }
    }

    private static void generateDomain() {
        generateSubdomain();
        final int length = rng.nextInt(5);
        for (int i = 0; i < length; ++i) {
            builder.append('.');
            generateSubdomain();
        }
    }

    private static void generateSubdomain() {
        if (rng.nextInt(10) == 0) {
            generateDomainLiteral();
        } else {
            generateDomainRef();
        }
    }

    private static void generateDomainRef() {
        generateAtom();
    }

    private static void generateDomainLiteral() {
        builder.append('[');
        final int length = rng.nextInt(20);
        for (int i = 0; i < length; ++i) {
            if (rng.nextInt(20) == 0) {
                generateQuotedPair();
            } else {
                generateDtext();
            }
        }
        builder.append(']');
    }

    private static void generateDtext() {
        char generatedChar;
        do {
            generatedChar = randomChar();
        } while (generatedChar == '[' || generatedChar == ']' || generatedChar == '\\');
        builder.append(generatedChar);
    }
}
