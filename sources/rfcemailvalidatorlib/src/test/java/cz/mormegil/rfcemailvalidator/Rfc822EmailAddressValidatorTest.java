package cz.mormegil.rfcemailvalidator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RunWith(Parameterized.class)
public class Rfc822EmailAddressValidatorTest {
    private final String str;
    private final boolean shouldBeValid;

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        final List<Object[]> result = new ArrayList<>();

        try (final InputStream inputStream = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("testcases.txt"))) {
            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;

                while (null != (line = bufferedReader.readLine())) {
                    final String str = line.substring(1);
                    final char flag = line.charAt(0);
                    result.add(new Object[]{str, flag == '+'});
                }
            }
        }

        return result;
    }

    public Rfc822EmailAddressValidatorTest(String str, boolean shouldBeValid) {
        this.str = str;
        this.shouldBeValid = shouldBeValid;
    }

    @Test
    public void validate() throws ParseException {
        final Rfc822EmailAddressValidator validator = new Rfc822EmailAddressValidator(str);
        if (shouldBeValid) {
            validator.validate();
        } else {
            Assert.assertThrows(ParseException.class, validator::validate);
        }
    }
}
