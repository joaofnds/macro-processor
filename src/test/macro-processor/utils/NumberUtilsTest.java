package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static utils.NumberUtils.isNumeric;

class NumberUtilsTest {
    @Test
    void isNumericIsTrueWithANumericString() {
        assertTrue(isNumeric("10"));
    }

    @Test
    void isNumericIsTrueWithANumericStringWithSpaces() {
        assertTrue(isNumeric(" 10 "));
    }

    @Test
    void isNumericIsTrueWithSignalPrefix() {
        assertTrue(isNumeric("+10"));
        assertTrue(isNumeric("-10"));
    }

    @Test
    void isNumericIsFalseWithANonNumericString() {
        assertFalse(isNumeric("this is not a number"));
    }

    @Test
    void isNumericIsFalseWithANonNumericPrefix() {
        assertFalse(isNumeric("#10"));
    }
}