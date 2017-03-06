package com.pingunaut.wicketmessages.test;

public class TestPage {

    /**
     * class contains a referenced resource key. this one should be marked as "used" during export
     */
    public void someFakeMethodUsingResourceKey() {
        String myResourceKey = "countryCodeOnly";
        System.out.println(myResourceKey);
    }
}
