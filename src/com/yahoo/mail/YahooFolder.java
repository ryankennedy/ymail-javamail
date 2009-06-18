package com.yahoo.mail;

import com.yahoo.jsonrpc.Client;
import com.yahoo.jsonrpc.ClientException;
import com.yahoo.util.Base64;
import com.yahoo.util.NewlineOutputStream;
import com.yahoo.json.JsonArrayStringIterable;

import javax.mail.MessagingException;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.event.FolderEvent;
import javax.mail.event.ConnectionEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class YahooFolder extends javax.mail.Folder {
    private State state = State.CLOSED;
    private FolderInfo folderInfo;
    private List<Message> messages;

    public YahooFolder(javax.mail.Store store, FolderInfo info) {
        super(store);
        this.folderInfo = info;
    }

    public String getName() {
        return folderInfo.getName();
    }

    public String getFullName() {
        return folderInfo.getName();
    }

    public javax.mail.Folder getParent() throws MessagingException {
        return new DefaultFolder((YahooStore) getStore());
    }

    public boolean exists() throws MessagingException {
        return folderInfo.exists;
    }

    public javax.mail.Folder[] list(String s) throws MessagingException {
        return new javax.mail.Folder[0];
    }

    public char getSeparator() throws MessagingException {
        return '\u0000';
    }

    public int getType() throws MessagingException {
        return YahooFolder.HOLDS_MESSAGES;
    }

    public boolean create(int type) throws MessagingException {
        if(folderInfo.exists) {
            // Can't create a folder that already exists.
            return false;
        }
        else if(type != YahooFolder.HOLDS_MESSAGES) {
            // Can only create folders that hold messages.
            return false;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("name", folderInfo.getName());

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore) getStore()).getCookies());
            JSONObject response = client.call("CreateFolder", params);
            folderInfo.setId(response.getJSONObject("folderInfo").getString("fid"));
            folderInfo.setExists(true);

            notifyFolderListeners(FolderEvent.CREATED);

            return true;
        }
        catch(JSONException e) {
            throw new MessagingException("Failed to create folder, JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Failed to create folder, JSON-RPC error", e);
        }
    }

    public boolean hasNewMessages() throws MessagingException {
        throw new RuntimeException("Not implemented");
    }

    public javax.mail.Folder getFolder(String s) throws MessagingException {
        throw new MessagingException("Yahoo! Mail folders can only contain messages");
    }

    public boolean delete(boolean b) throws MessagingException {
        if(!folderInfo.exists) {
            // Can't delete a folder that doesn't exist
            return false;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("fid", folderInfo.getId());

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore) getStore()).getCookies());
            client.call("RemoveFolder", params);

            folderInfo.setId(null);
            folderInfo.setExists(false);

            notifyFolderListeners(FolderEvent.DELETED);

            return true;
        }
        catch(JSONException e) {
            throw new MessagingException("Failed to remove folder, JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Failed to remove folder, JSON-RPC error", e);
        }
    }

    public boolean renameTo(javax.mail.Folder folder) throws MessagingException {
        if(isOpen() || !folderInfo.exists) {
            // Can't rename an open folder or a folder that doesn't exist
            return false;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("fid", folderInfo.getId());
            request.put("name", folder.getName());

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore) getStore()).getCookies());
            JSONObject response = client.call("RenameFolder", params);

            folderInfo.setId(response.getJSONObject("folderInfo").getString("fid"));
            folderInfo.setName(response.getJSONObject("folderInfo").getString("name"));

            notifyFolderRenamedListeners(this);

            return true;
        }
        catch(JSONException e) {
            throw new MessagingException("Failed to rename folder, JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Failed to rename folder, JSON-RPC error", e);
        }
    }

    public void open(int mode) throws MessagingException {
        if(state != State.CLOSED) {
            throw new IllegalStateException("YahooFolder is not closed");
        }

        try {
            JSONObject request = new JSONObject();
            request.put("fid", folderInfo.getId());
            request.put("startMid", 0);
            request.put("numMid", folderInfo.getTotal());

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore) getStore()).getCookies());
            JSONObject response = client.call("ListMessages", params);

            List<Message> newMessages = new ArrayList<Message>(response.getInt("numMid"));
            for(String mid : new JsonArrayStringIterable(response.getJSONArray("mid"))) {
                newMessages.add(new YahooMessage(this, mid, newMessages.size() + 1, (YahooStore) getStore()));
            }

            messages = newMessages;

            if(mode == javax.mail.Folder.READ_ONLY) {
                state = State.OPEN_RO;
            }
            else if(mode == javax.mail.Folder.READ_WRITE) {
                state = State.OPEN_RW;
            }
            else {
                throw new MessagingException(String.format("Invalid folder open state: %d", mode));
            }

            notifyConnectionListeners(ConnectionEvent.OPENED);
        }
        catch(JSONException e) {
            throw new MessagingException("JSON problem", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Client problem", e);
        }
    }

    public void close(boolean expunge) throws MessagingException {
        if(state == State.CLOSED) {
            throw new IllegalStateException("YahooFolder is not open");
        }

        state = State.CLOSED;
        messages.clear();

        notifyConnectionListeners(ConnectionEvent.CLOSED);
    }

    public boolean isOpen() {
        return (state == State.OPEN_RO) || (state == State.OPEN_RW);
    }

    public Flags getPermanentFlags() {
        Flags flags = new Flags();
        flags.add(Flags.Flag.ANSWERED);
        flags.add(Flags.Flag.FLAGGED);
        flags.add(Flags.Flag.SEEN);
        flags.add(Flags.Flag.DRAFT);
        return flags;
    }

    public int getMessageCount() throws MessagingException {
        return (int) folderInfo.getTotal();
    }

    public Message getMessage(int i) throws MessagingException {
        if(state == State.CLOSED) {
            throw new IllegalStateException("YahooFolder must be open to retrieve messages");
        }

        if(i < 1 || i > messages.size()) {
            throw new IndexOutOfBoundsException(String.format("Message number (%d) out of range", i));
        }

        return messages.get(i - 1);
    }

    public void appendMessages(Message[] messages) throws MessagingException {
        try {
            for(Message message : messages) {
                ByteArrayOutputStream messageBody = new ByteArrayOutputStream();
                message.writeTo(new NewlineOutputStream(messageBody));
                String encoded = Base64.encodeBytes(messageBody.toByteArray(), Base64.DO_BREAK_LINES);

                try {
                    JSONObject request = new JSONObject();
                    request.put("fid", folderInfo.getId());
                    request.put("text", encoded);

                    JSONArray params = new JSONArray();
                    params.put(request);

                    Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", ((YahooStore) getStore()).getCookies());
                    client.call("SaveRawMessage", params);

                    notifyMessageAddedListeners(new Message[]{message});
                }
                catch(JSONException e) {
                    throw new MessagingException("Error building JSON request", e);
                }
                catch(ClientException e) {
                    throw new MessagingException("Error making SaveRawMessage call", e);
                }
            }
        }
        catch(IOException e) {
            throw new MessagingException("Error reading message body", e);
        }
    }

    public Message[] expunge() throws MessagingException {
        throw new RuntimeException("Not implemented");
    }

    public FolderInfo getFolderInfo() {
        return folderInfo;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof YahooFolder) && (((YahooFolder)obj).folderInfo.getId().equals(getFolderInfo().getId()));
    }

    @Override
    public String toString() {
        return String.format("YahooFolder <%s>", folderInfo.toString());
    }

    private enum State {
        CLOSED, OPEN_RO, OPEN_RW
    }

    static class FolderInfo {
        private String id = null;
        private String name = null;
        private long unread = -1;
        private long total = -1;
        private long size = -1;
        private boolean system = false;
        private boolean exists = false;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean exists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getUnread() {
            return unread;
        }

        public void setUnread(long unread) {
            this.unread = unread;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isSystem() {
            return system;
        }

        public void setSystem(boolean system) {
            this.system = system;
        }

        @Override
        public String toString() {
            return String.format("id = %s; name = %s; exists = %b; unread = %d; total = %d; size = %d; system = %b", id, name, exists, unread, total, size, system);
        }
    }
}