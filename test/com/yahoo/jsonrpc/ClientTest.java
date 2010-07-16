package com.yahoo.jsonrpc;

import org.junit.Test;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.httpclient.Cookie;
import junit.framework.TestCase;

import java.io.IOException;

import com.yahoo.auth.LoginScraper;

public class ClientTest {
    @Test
    public void call() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

        Cookie cookies[] = null;
        try {
            // TODO: Don't hardcode the user account.
            cookies = LoginScraper.login("rckenned_test", "testing");
        }
        catch(IOException e) {
            TestCase.fail("Error authenticating user" + e.toString());
        }

        Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", cookies);
        JSONArray parameters = new JSONArray();
        parameters.put(new JSONObject());
        try {
            JSONObject response = client.call("ListFolders", parameters);
        }
        catch(ClientException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            TestCase.fail("JSON-RPC call failed");
        }
    }
}