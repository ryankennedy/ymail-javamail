package com.yahoo.mail;

import com.yahoo.auth.LoginScraper;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.URLName;
import javax.mail.Session;
import java.io.IOException;

import org.apache.commons.httpclient.Cookie;

public class YahooStore extends javax.mail.Store {
    private int port;
    private String username;
    private String password;
    private Cookie cookies[];

    public YahooStore(Session session, URLName urlname) {
        super(session, urlname);        
    }

    @Override
    protected boolean protocolConnect(String hostname, int port, String username, String password) throws MessagingException {
        if(isConnected()) {
            return true;
        }

        if((hostname == null) || (username == null) || (password == null)) {
            return false;
        }

        this.port = port;
        this.username = username;
        this.password = password;

        if(this.port < 0) {
            this.port = 80;
        }

        try {
            this.cookies = LoginScraper.login(this.username, this.password);
            return true;
        }
        catch(IOException e) {
            throw new MessagingException("Error getting Yahoo! cookies", e);
        }
    }

    @Override
    public void close() throws MessagingException {
        if(isConnected()) {
            this.port = 0;
            this.username = null;
            this.password = null;
            this.cookies = null;
            super.close();
        }
    }

    public Folder getDefaultFolder() throws MessagingException {
        if(!isConnected()) {
            throw new IllegalStateException("Store is not connected");
        }

        return new DefaultFolder(this);
    }

    public Folder getFolder(String s) throws MessagingException {
        if(!isConnected()) {
            throw new IllegalStateException("Store is not connected");
        }

        return new DefaultFolder(this).getFolder(s);
    }

    public Folder getFolder(URLName urlName) throws MessagingException {
        if(!isConnected()) {
            throw new IllegalStateException("Store is not connected");
        }

        return new DefaultFolder(this).getFolder(urlName.getFile());
    }

    Cookie[] getCookies() {
        return this.cookies;
    }
}