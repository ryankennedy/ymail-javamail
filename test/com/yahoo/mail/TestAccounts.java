package com.yahoo.mail;

import junit.framework.TestCase;

import javax.mail.*;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

public class TestAccounts {
    private static Properties properties = new Properties();

    static {
        try {
            InputStream propertyStream = TestAccounts.class.getResourceAsStream("/META-INF/test_accounts.properties");
            if(propertyStream == null) {
                System.err.println("Failed to locate resource META-INF/test_accounts.properties");
            }
            else {
                properties.load(propertyStream);
            }
        }
        catch(IOException e) {
            System.err.println("Failed to load test accounts properties file: " + e.toString());
        }
    }

    public static String getTestAccount() {
        return properties.getProperty("account");
    }

    public static Store getTestStore() {
        String testAccount = getTestAccount();
        try {
            return Session.getDefaultInstance(System.getProperties()).getStore(new URLName(testAccount));
        }
        catch(NoSuchProviderException e) {
            System.err.println(String.format("Failed to load JavaMail provider for test account (%s): %s", testAccount, e));
            return null;
        }
    }

    static Store getOpenStore() {
        Store store = getTestStore();
        try {
            store.connect();
        }
        catch(MessagingException e) {
            TestCase.fail("Failed to connect store: " + e.toString());
        }
        return store;
    }

    static void closeStore(Store store) {
        try {
            store.close();
        }
        catch(MessagingException e) {
            System.err.println("Failed to close store");
        }
    }
}