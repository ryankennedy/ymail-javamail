package com.yahoo.auth;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class LoginScraper {
    private static final String LOGIN_URL = "https://login.yahoo.com/config/login";
    private static Map<String, Cookie[]> LOGIN_CACHE = new HashMap<String, Cookie[]>();

    public static Cookie[] login(String username, String password) throws IOException {
        String key = username + "," + password;
        if(LOGIN_CACHE.containsKey(key)) {
            return LOGIN_CACHE.get(key);
        }

        HttpClient client = new HttpClient();
        client.setState(new HttpState());

        PostMethod post = new PostMethod(LOGIN_URL);
        post.addParameter("login", username);
        post.addParameter("passwd", password);

        int status = client.executeMethod(post);
//        if(status != HttpStatus.SC_OK) {
//            throw new Exception("Failed to login, HTTP status " + status);
//        }

        Cookie cookies[] = client.getState().getCookies();
        LOGIN_CACHE.put(key, cookies);
        return cookies;
    }
}