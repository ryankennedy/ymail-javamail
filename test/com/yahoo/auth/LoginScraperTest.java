package com.yahoo.auth;

import java.util.Map;

import org.junit.Test;
import org.apache.commons.httpclient.Cookie;
import junit.framework.TestCase;

public class LoginScraperTest {
    @Test
    public void login() {
        try {
            // TODO: Don't hardcode the user account.
            Cookie cookies[] = LoginScraper.login("rckenned_test", "testing");
            TestCase.assertTrue("Should be more than one cookie", cookies.length > 0);
        }
        catch(Exception e) {
            TestCase.fail(e.toString());
        }
    }
}