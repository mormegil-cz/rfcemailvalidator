package cz.mormegil.rfcemailvalidator.tools;

import cz.mormegil.rfcemailvalidator.Rfc822EmailAddressValidator;
import emailvalidator4j.validator.WarningsNotAllowed;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressCriteria;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A utility program to generate comparison table of various e-mail address validators
 */
public class CompareValidators {
    private static final Map<String, Predicate<String>> TESTED_VALIDATORS;
    private static final String SUCCEED_MARK = "✅";
    private static final String FAIL_MARK = "❌";

    static {
        TESTED_VALIDATORS = new LinkedHashMap<>(9);
        TESTED_VALIDATORS.put("Java", CompareValidators::javaValidator);
        TESTED_VALIDATORS.put("HTML5", CompareValidators::html5Regex);
        TESTED_VALIDATORS.put("5322", CompareValidators::rfc5322Regex);
        TESTED_VALIDATORS.put("CoVa", CompareValidators::commonsValidation);
        TESTED_VALIDATORS.put("EV4j", CompareValidators::eguilasEmailValidator4j);
        TESTED_VALIDATORS.put("EV4j!", CompareValidators::eguilasEmailValidator4jNoWarnings);
        TESTED_VALIDATORS.put("2822", CompareValidators::bottemaEmailAddressValidatorRec);
        TESTED_VALIDATORS.put("2822+", CompareValidators::bottemaEmailAddressValidatorCompliant);
        TESTED_VALIDATORS.put("852V", CompareValidators::rfc852Parser);
    }

    private static boolean javaValidator(String str) {
        try {
            final InternetAddress emailAddr = new InternetAddress(str);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    private static boolean commonsValidation(String str) {
        return org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(str);
    }

    private static boolean eguilasEmailValidator4j(String str) {
        final emailvalidator4j.EmailValidator validator = new emailvalidator4j.EmailValidator();
        return validator.isValid(str);
    }

    private static boolean eguilasEmailValidator4jNoWarnings(String str) {
        final emailvalidator4j.EmailValidator validator = new emailvalidator4j.EmailValidator(Collections.singletonList(new WarningsNotAllowed()));
        return validator.isValid(str);
    }

    private static boolean bottemaEmailAddressValidatorRec(String str) {
        return org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator.isValid(str, EmailAddressCriteria.RECOMMENDED);
    }

    private static boolean bottemaEmailAddressValidatorCompliant(String str) {
        return org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator.isValid(str, EmailAddressCriteria.RFC_COMPLIANT);
    }

    private static final Pattern RE_EMAIL_HTML5 = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    // https://stackoverflow.com/questions/53299385/email-id-validation-according-to-rfc5322-and-https-en-wikipedia-org-wiki-email
    private static final Pattern RE_EMAIL_RFC5322 = Pattern.compile("(?im)^(?=.{1,64}@)(?:(\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"@)|((?:[0-9a-z](?:\\.(?!\\.)|[-!#\\$%&'\\*\\+/=\\?\\^`\\{\\}\\|~\\w])*)?[0-9a-z]@))(?=.{1,255}$)(?:(\\[(?:\\d{1,3}\\.){3}\\d{1,3}\\])|((?:(?=.{1,63}\\.)[0-9a-z][-\\w]*[0-9a-z]*\\.)+[a-z0-9][\\-a-z0-9]{0,22}[a-z0-9])|((?=.{1,63}$)[0-9a-z][-\\w]*))$");

    private static boolean html5Regex(String str) {
        return RE_EMAIL_HTML5.matcher(str).matches();
    }

    private static boolean rfc5322Regex(String str) {
        return RE_EMAIL_RFC5322.matcher(str).matches();
    }

    private static boolean rfc852Parser(String str) {
        final Rfc822EmailAddressValidator parser = new Rfc822EmailAddressValidator(str);
        try {
            parser.validate();
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Program entry point
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) throws IOException {
        final InputStream in;
        switch (args.length) {
            case 0:
                in = System.in;
                break;
            case 1:
                in = new FileInputStream(args[0]);
                break;
            default:
                System.out.println("Usage: CompareValidators [input.txt]");
                return;
        }

        System.out.print("| String");
        for (final Map.Entry<String, Predicate<String>> validator : TESTED_VALIDATORS.entrySet()) {
            System.out.print(" | " + validator.getKey());
        }
        System.out.println(" |");
        System.out.print("|----");
        for (int i = 0; i < TESTED_VALIDATORS.size(); ++i) {
            System.out.print("|----");
        }
        System.out.println("|");

        try (BufferedReader input = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = input.readLine()) != null) {
                System.out.print("| ``");
                System.out.print(line.substring(0, Math.min(line.length(), 70)).replace("|", "\\|"));
                System.out.print("``");
                for (final Map.Entry<String, Predicate<String>> validator : TESTED_VALIDATORS.entrySet()) {
                    System.out.print(" | " + (validator.getValue().test(line) ? SUCCEED_MARK : FAIL_MARK));
                }
                System.out.println(" |");
            }
        }
    }
}
