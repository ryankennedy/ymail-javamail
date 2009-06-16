package com.yahoo.jsonrpc;

import org.json.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Client {
    private String url;

    // TODO: Is HttpClient threadsafe?
    private HttpClient client;

    public Client(String url, org.apache.commons.httpclient.Cookie cookies[]) {
        this.url = url;
        client = new HttpClient();

        HttpState state = new HttpState();
        state.addCookies(cookies);
        client.setState(state);
    }

    public JSONObject call(String method, JSONArray parameters) throws ClientException {
        JSONObject request;
        try {
            request = new JSONObject();
            request.put("method", method);
            request.put("params", parameters);
        }
        catch(JSONException e) {
            throw new ClientException("Error constructing JSON", e);
        }

        PostMethod post = new PostMethod(this.url);
        post.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        post.getParams().setParameter("http.protocol.single-cookie-header", true);
        try {
            post.setRequestEntity(new StringRequestEntity(request.toString(), "application/json", "utf-8"));
        }
        catch(UnsupportedEncodingException e) {
            // This shouldn't happen...utf-8 is always supported.
            throw new RuntimeException("WTF? UTF-8 isn't accepted?");
        }

        try {
            int status = client.executeMethod(post);

            JSONObject response;
            if(post.getResponseHeader("Content-Type").getValue().equals("application/json")) {
                try {
                    response = new JSONObject(new JSONTokener(new InputStreamReader(post.getResponseBodyAsStream())));
                }
                catch(JSONException e) {
                    throw new ClientException("Error parsing JSON", e);
                }
                catch(IOException e) {
                    throw new ClientException("Error reading response body");
                }
            }
            else {
                throw new ClientException("Response is not JSON");
            }

            if(status != HttpStatus.SC_OK) {
                if(response.getJSONObject("error").getString("code").startsWith("Client.ClientRedirect")) {
                    this.url = response.getJSONObject("error").getJSONObject("detail").getString("url");
                    return this.call(method, parameters);
                }
                throw new ClientException(response.getJSONObject("error"));
            }

            return response.getJSONObject("result");
        }
        catch(IOException e) {
            throw new ClientException("Failed to make HTTP request", e);
        }
        catch(JSONException e) {
            throw new ClientException("Failed to parse JSON", e);
        }
    }
}