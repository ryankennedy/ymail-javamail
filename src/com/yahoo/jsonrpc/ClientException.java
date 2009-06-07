package com.yahoo.jsonrpc;

import org.json.JSONObject;

public class ClientException extends Exception {
    private JSONObject error = null;

    public ClientException() {
        super();
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }

    public ClientException(JSONObject error) {
        super();
        this.error = error;
    }

    @Override
    public String toString() {
        return this.getMessage() + "\n" + error.toString();
    }
}
