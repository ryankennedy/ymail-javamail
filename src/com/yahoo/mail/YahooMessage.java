package com.yahoo.mail;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.activation.DataHandler;
import java.util.Date;
import java.util.Enumeration;
import java.io.*;
import java.text.ParseException;

import com.yahoo.jsonrpc.Client;
import com.yahoo.jsonrpc.ClientException;

public class YahooMessage extends MimeMessage {
    private String messageId;
    private YahooStore store;
    private JSONObject message;
    private InternetHeaders headers;

    public YahooMessage(YahooFolder folder, String messageId, int messageNumber, YahooStore store) {
        super(folder, messageNumber);
        this.messageId = messageId;
        this.store = store;
    }

    public Address[] getFrom() throws MessagingException {
        loadMessage();
        try {
            String email = message.getJSONObject("from").getString("email");
            String name = message.getJSONObject("from").getString("name");
            return new InternetAddress[]{new InternetAddress(email, name)};
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException("This should never happen");
        }
    }

    public void setFrom() throws MessagingException {
        throw new IllegalWriteException();
    }

    public void setFrom(Address address) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void addFrom(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException();
    }

    public Address[] getRecipients(RecipientType recipientType) throws MessagingException {
        loadMessage();

        try {
            JSONArray recipients;
            if(recipientType == RecipientType.TO && message.has("to")) {
                recipients = message.getJSONArray("to");
            }
            else if(recipientType == RecipientType.CC && message.has("cc")) {
                recipients = message.getJSONArray("cc");
            }
            else if(recipientType == RecipientType.BCC && message.has("bcc")) {
                recipients = message.getJSONArray("bcc");
            }
            else {
                recipients = new JSONArray();
            }

            Address addresses[] = new Address[recipients.length()];
            for(int i = 0; i < recipients.length(); i++) {
                JSONObject jsonAddress = recipients.getJSONObject(i);
                addresses[i] = new InternetAddress(jsonAddress.getString("email"), jsonAddress.getString("name"));
            }

            return addresses;
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
        catch(UnsupportedEncodingException e) {
            throw new MessagingException("Encoding exception", e);
        }
    }

    public void setRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void addRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException();
    }

    public String getSubject() throws MessagingException {
        loadMessage();
        try {
            return message.getString("subject");
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
    }

    public void setSubject(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public Date getSentDate() throws MessagingException {
        loadHeaders();

        MailDateFormat format = new MailDateFormat();
        String date = headers.getHeader("Date")[0];
        try {
            return format.parse(date);
        }
        catch(ParseException e) {
            throw new MessagingException(String.format("Error parsing date: %s", date), e);
        }
    }

    public void setSentDate(Date date) throws MessagingException {
        throw new IllegalWriteException();
    }

    public Date getReceivedDate() throws MessagingException {
        loadMessage();
        try {
            return new Date(message.getLong("receivedDate") * 1000);
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
    }

    public Flags getFlags() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFlags(Flags flags, boolean b) throws MessagingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message reply(boolean b) throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException();
    }

    public int getSize() throws MessagingException {
        return -1;
    }

    public int getLineCount() throws MessagingException {
        return -1;
    }

    public String getContentType() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isMimeType(String s) throws MessagingException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDisposition() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDisposition(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public String getDescription() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDescription(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public String getFileName() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFileName(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataHandler getDataHandler() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getContent() throws IOException, MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDataHandler(DataHandler dataHandler) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void setContent(Object o, String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void setText(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void setContent(Multipart multipart) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void writeTo(OutputStream outputStream) throws IOException, MessagingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getHeader(String s) throws MessagingException {
        loadHeaders();
        return headers.getHeader(s);
    }

    public void setHeader(String s, String s1) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void addHeader(String s, String s1) throws MessagingException {
        throw new IllegalWriteException();
    }

    public void removeHeader(String s) throws MessagingException {
        throw new IllegalWriteException();
    }

    public Enumeration getAllHeaders() throws MessagingException {
        loadHeaders();
        return headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] strings) throws MessagingException {
        loadHeaders();
        return headers.getMatchingHeaderLines(strings);
    }

    public Enumeration getNonMatchingHeaders(String[] strings) throws MessagingException {
        loadHeaders();
        return headers.getNonMatchingHeaders(strings);
    }

    private void loadMessage() throws MessagingException {
        if(message != null) {
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("fid", ((YahooFolder)getFolder()).getFolderInfo().getId());
            request.put("mid", messageId);

            JSONArray params = new JSONArray();
            params.put(request);

            Client client = new Client("http://us.mg0.mail.yahoo.com/ws/mail/v1.1/jsonrpc?appid=Ryan", store.getCookies());
            JSONObject response = client.call("GetMessage", params);
            message = response.getJSONArray("message").getJSONObject(0);
        }
        catch(JSONException e) {
            throw new MessagingException("JSON error", e);
        }
        catch(ClientException e) {
            throw new MessagingException("Client error", e);
        }
    }

    private void loadHeaders() throws MessagingException {
        if(headers != null) {
            return;
        }

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

            headers = new InternetHeaders(new ByteArrayInputStream(headerString.getBytes()));
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