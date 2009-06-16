package com.yahoo.mail;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetHeaders;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.io.*;

import com.yahoo.jsonrpc.Client;
import com.yahoo.jsonrpc.ClientException;

public class YahooMessage extends MimeMessage {
    private String messageId;
    private YahooStore store;
    private boolean messageLoaded = false;
    private boolean headersLoaded = false;
    private Date receivedDate;
    private String mimeMessageId;

    public YahooMessage(YahooFolder folder, String messageId, int messageNumber, YahooStore store) {
        super(folder, messageNumber);

        // TODO: take in the flags

        this.messageId = messageId;
        this.store = store;
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        loadMessage();
        return receivedDate;
    }

    @Override
    public String getSubject() throws MessagingException {
        loadMessage();
        return super.getSubject();
    }

    @Override
    public Address[] getFrom() throws MessagingException {
        loadMessage();
        return super.getFrom();
    }

    @Override
    public String getMessageID() throws MessagingException {
        loadMessage();
        return mimeMessageId;
    }

    @Override
    public String[] getHeader(String s) throws MessagingException {
        loadHeaders();
        return super.getHeader(s);
    }

    @Override
    public String getHeader(String s, String s1) throws MessagingException {
        loadHeaders();
        return super.getHeader(s, s1);
    }

    @Override
    public Enumeration getAllHeaders() throws MessagingException {
        loadHeaders();
        return super.getAllHeaders();
    }

    private void loadMessage() throws MessagingException {
        if(messageLoaded) {
            return;
        }

        if(headers == null) {
            headers = new InternetHeaders();
        }

        try {
            JSONObject request = new JSONObject();
            request.put("fid", ((YahooFolder)getFolder()).getFolderInfo().getId());
            request.put("mid", messageId);

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", store.getCookies());
            JSONObject response = client.call("GetMessage", params);
            JSONObject message = response.getJSONArray("message").getJSONObject(0);

            // receivedDate
            if(message.has("receivedDate")) {
                receivedDate = new Date(message.getLong("receivedDate") * 1000);
            }
            else {
                receivedDate = null;
            }

            // subject
            if(!headersLoaded && message.has("subject")) {
                setSubject(message.getString("subject"));
            }

            // from
            if(!headersLoaded && message.has("from")) {
                try {
                    setFrom(new InternetAddress(message.getJSONObject("from").getString("email"),
                            message.getJSONObject("from").getString("name"), "utf-8"));
                }
                catch(UnsupportedEncodingException e) {
                    // TODO: handle it
                }
            }

            // replyto
            if(!headersLoaded && message.has("replyto")) {
                try {
                    JSONArray jsonReplyTos = message.getJSONArray("replyto");
                    List<Address> replyTos = new ArrayList<Address>(jsonReplyTos.length());
                    for(int i = 0; i < jsonReplyTos.length(); i++) {
                        JSONObject address = jsonReplyTos.getJSONObject(i);
                        replyTos.add(new InternetAddress(address.getString("email"), address.getString("name"), "utf-8"));
                    }
                    setReplyTo(replyTos.toArray(new Address[replyTos.size()]));
                }
                catch(UnsupportedEncodingException e) {
                    // TODO: handle it
                }
            }

            // to
            if(!headersLoaded && message.has("to")) {
                setRecipients(RecipientType.TO, recipientsToAddresses(message.getJSONArray("to")));
            }

            // cc
            if(!headersLoaded && message.has("cc")) {
                setRecipients(RecipientType.CC, recipientsToAddresses(message.getJSONArray("cc")));
            }

            // bcc
            if(!headersLoaded && message.has("bcc")) {
                setRecipients(RecipientType.BCC, recipientsToAddresses(message.getJSONArray("bcc")));
            }

            // part
            parseJsonParts(message.getJSONArray("part"));

            // messageId
            if(!headersLoaded && message.has("messageId")) {
                mimeMessageId = message.getString("messageId");
            }

            messageLoaded = true;
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Client error", e);
        }
    }

    private void parseJsonParts(JSONArray jsonArray) throws JSONException, MessagingException {
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject part = jsonArray.getJSONObject(i);
            if(part.getString("partId").equals("TEXT")) {
                if(part.getString("type").equals("multipart")) {
                    // TODO: Set the datahandler with a multipart data source.
                }
                else if(part.getString("type").equals("message")) {
                    // TODO: Set the datahandler with a message data source.
                }
                else if(part.has("text") && part.getString("type").equals("text")) {
                    // TODO: Do I need to worry about the charset here? What charset will be used to decode the bytes?
                    content = part.getString("text").getBytes();
                }
                else {
                    // TODO: This must be some binary type that snuck in as the top level type...set the data handler with a "binary" data source.
                    throw new MessagingException(String.format("Not currently handling this type: %s/%s", part.getString("type"), part.getString("subtype")));
                }
            }
        }
    }

    private Address[] recipientsToAddresses(JSONArray recipients) {
        List<Address> addresses = new ArrayList<Address>(recipients.length());
        for(int i = 0; i < recipients.length(); i++) {
            try {
                JSONObject jsonAddress = recipients.getJSONObject(i);
                addresses.add(new InternetAddress(jsonAddress.getString("email"), jsonAddress.getString("name")));
            }
            catch(JSONException e) {
                // TODO: handle it
            }
            catch(UnsupportedEncodingException e) {
                // TODO: handle it
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    private void loadHeaders() throws MessagingException {
        if(headersLoaded) {
            return;
        }

        headers = new InternetHeaders();

        try {
            JSONObject request = new JSONObject();
            request.put("fid", ((YahooFolder)getFolder()).getFolderInfo().getId());

            JSONArray messageIds = new JSONArray();
            messageIds.put(messageId);
            request.put("mid", messageIds);

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", store.getCookies());
            JSONObject response = client.call("GetMessageRawHeader", params);
            String headerString = response.getJSONArray("rawheaders").getString(0);

            headers.load(new ByteArrayInputStream(headerString.getBytes()));

            headersLoaded = true;
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Client error", e);
        }
    }

    @Override
    public String toString() {
        return String.format("YahooMessage <fid = %s, mid = %s>", ((YahooFolder)getFolder()).getFolderInfo().getId(), messageId);
    }
}