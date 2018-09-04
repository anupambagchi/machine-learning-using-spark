package com.anupambagchi.test.exercise;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.anupambagchi.trials.PalindromeChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestPalindromeChecker {

    @Before
    public void setup() {
    }

    @Test
    public void testPalindrome() throws IOException {
        // An example input field
        String s1 = "ABCDEDCBA";
        String s2 = "ABCDBCA";

        PalindromeChecker P1 = new PalindromeChecker(s1);
        assertTrue("Checking palindrome for " + P1.getTheString(), P1.isPalindrome());

        PalindromeChecker P2 = new PalindromeChecker(s2);
        assertFalse("Checking palindrome for " + P2.getTheString(), P2.isPalindrome());

    }

    @AfterClass
    public static void testCleanup() {
        // Teardown for data used by the unit tests
    }

    /*
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionIsThrown() {
        // MyClass tester = new MyClass();
        // tester.multiply(1000, 5);
    }
    */


}
