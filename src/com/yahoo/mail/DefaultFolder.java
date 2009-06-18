package com.yahoo.mail;

import com.yahoo.jsonrpc.Client;
import com.yahoo.jsonrpc.ClientException;
import com.yahoo.json.JsonArrayObjectIterable;

import javax.mail.*;
import javax.mail.Folder;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.ArrayList;

class DefaultFolder extends javax.mail.Folder {
    private State state = State.CLOSED;

    public DefaultFolder(YahooStore store) {
        super(store);
    }

    public String getName() {
        return "";
    }

    public String getFullName() {
        return "";
    }

    public Folder getParent() throws MessagingException {
        return null;
    }

    public boolean exists() throws MessagingException {
        return true;
    }

    public Folder[] list(String pattern) throws MessagingException {
        Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore)getStore()).getCookies());
        JSONArray parameters = new JSONArray();
        parameters.put(new JSONObject());

        try {
            JSONObject folders = client.call("ListFolders", parameters);

            List<Folder> folderList = new ArrayList<Folder>(folders.getInt("numberOfFolders"));
            for(JSONObject jsonFolder : new JsonArrayObjectIterable(folders.getJSONArray("folder"))) {
                YahooFolder.FolderInfo info = jsonFolderToFolderInfo(jsonFolder);
                info.setExists(true);

                if(nameMatches(pattern, info.getName())) {
                    folderList.add(new YahooFolder(getStore(), info));
                }
            }

            return folderList.toArray(new Folder[folderList.size()]);
        }
        catch(ClientException e) {
            throw new MessagingException("Failed to get folder list", e);
        }
        catch(JSONException e) {
            throw new MessagingException("Failed to handle JSON", e);
        }
    }

    private YahooFolder.FolderInfo jsonFolderToFolderInfo(JSONObject jsonFolder) throws JSONException {
        YahooFolder.FolderInfo info = new YahooFolder.FolderInfo();
        String name = jsonFolder.getJSONObject("folderInfo").getString("name");
        info.setName(name.equalsIgnoreCase("INBOX") ? "INBOX" : name);
        info.setId(jsonFolder.getJSONObject("folderInfo").getString("fid"));
        info.setUnread(jsonFolder.getLong("unread"));
        info.setTotal(jsonFolder.getLong("total"));
        info.setSize(jsonFolder.getLong("size"));
        info.setSystem(jsonFolder.getBoolean("isSystem"));
        return info;
    }

    public char getSeparator() throws MessagingException {
        return '\u0000';
    }

    public int getType() throws MessagingException {
        return Folder.HOLDS_FOLDERS;
    }

    public boolean create(int i) throws MessagingException {
        return false;
    }

    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    public Folder getFolder(String name) throws MessagingException {
        Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore)getStore()).getCookies());
        JSONArray parameters = new JSONArray();
        parameters.put(new JSONObject());

        try {
            JSONObject folders = client.call("ListFolders", parameters);

            Folder folder = null;
            for(JSONObject jsonFolder : new JsonArrayObjectIterable(folders.getJSONArray("folder"))) {
                if(jsonFolder.getJSONObject("folderInfo").getString("name").equals(name)) {
                    YahooFolder.FolderInfo info = jsonFolderToFolderInfo(jsonFolder);
                    info.setExists(true);
                    folder = new YahooFolder(getStore(), info);
                }
                else if(name.equalsIgnoreCase("INBOX") && jsonFolder.getJSONObject("folderInfo").getString("name").equalsIgnoreCase(name)) {
                    YahooFolder.FolderInfo info = jsonFolderToFolderInfo(jsonFolder);
                    info.setName("INBOX");
                    info.setId(jsonFolder.getJSONObject("folderInfo").getString("fid"));
                    info.setExists(true);
                    folder = new YahooFolder(getStore(), info);
                }
            }

            if(folder == null) {
                YahooFolder.FolderInfo info = new YahooFolder.FolderInfo();
                info.setName(name);
                info.setExists(false);
                folder = new YahooFolder(getStore(), info);
            }
            
            return folder;
        }
        catch(ClientException e) {
            throw new MessagingException("Failed to get folder list", e);
        }
        catch(JSONException e) {
            throw new MessagingException("Failed to handle JSON", e);
        }
    }

    public boolean delete(boolean b) throws MessagingException {
        return false;
    }

    public boolean renameTo(Folder folder) throws MessagingException {
        return false;
    }

    public void open(int i) throws MessagingException {
        if(state != State.CLOSED) {
            throw new IllegalStateException("Folder is not closed");
        }

        if(i == Folder.READ_ONLY) {
            state = State.OPEN_RO;
        }
        else if(i == Folder.READ_WRITE) {
            state = State.OPEN_RW;
        }
        else {
            throw new MessagingException(String.format("Invalid folder open state: %d", i));
        }
    }

    public void close(boolean b) throws MessagingException {
        if(state == State.CLOSED) {
            throw new IllegalStateException("Folder is not open");
        }

        state = State.CLOSED;
    }

    public boolean isOpen() {
        return (state == State.OPEN_RO) || (state == State.OPEN_RW);
    }

    public Flags getPermanentFlags() {
        return new Flags();
    }

    public int getMessageCount() throws MessagingException {
        return 0;
    }

    public Message getMessage(int i) throws MessagingException {
        throw new IndexOutOfBoundsException("The default Yahoo! Mail folder contains no messages.");
    }

    public void appendMessages(Message[] messages) throws MessagingException {
        throw new MessagingException("The default Yahoo! Mail folder contains no messages.");
    }

    public Message[] expunge() throws MessagingException {
        return new Message[0];
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultFolder;
    }

    private boolean nameMatches(String pattern, String name) {
        if(pattern.contains("%") || pattern.contains("*")) {
            pattern = pattern.replace("%", ".*").replace("*", ".*");
            return name.matches(pattern);
        }
        else if(pattern.equalsIgnoreCase("INBOX")) {
            return name.equalsIgnoreCase("INBOX");
        }
        else {
            return name.equals(pattern);
        }
    }

    private enum State { CLOSED, OPEN_RO, OPEN_RW }
}