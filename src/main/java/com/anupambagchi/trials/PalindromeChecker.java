package com.anupambagchi.trials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// import org.apache.commons.lang.StringUtils;

/**
 *
 */

public class PalindromeChecker {

    /**
     */
    private String theString;

    public PalindromeChecker(String str) {
        theString = str;
    }

    /**
     * A simple string reverse
     * @return
     */
    String reverseString() {
        char[] charArray = theString.toCharArray();
        int counter = 0;
        int strLength = theString.length();

        while (counter < strLength / 2) {
            char temp = charArray[counter];
            charArray[counter] = charArray[strLength - counter - 1];
            charArray[strLength - counter - 1] = temp;
            counter++;
        }
        return new String(charArray);
    }

    public boolean isPalindrome() {
        return theString.equals(reverseString());  // uses local function to revere string
        // return StringUtils.reverse(theString).equals(theString);  // uses the Apache Commons
    }

    public String getTheString() {
        return theString;
    }

    /**
     * This is the main entry point for a this program.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String inputString = null;
        try {
            System.out.print("Please type in a string to find its palindrome : ");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            inputString = bufferRead.readLine();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        PalindromeChecker aString = new PalindromeChecker(inputString);
        boolean isPalindrome = aString.isPalindrome();
        System.out.println("The original string: " + aString.getTheString());
        System.out.println("The reversed string: " + aString.reverseString());
        if (isPalindrome)
            System.out.println("\nYOUR STRING IS A PALINDROME !!");
        else
            System.out.println("\nThe string is not a palindrome.");
    }
}
