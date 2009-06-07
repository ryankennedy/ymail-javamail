package com.yahoo.mail;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before;

import javax.mail.*;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import java.util.Properties;
import java.util.Date;
import java.util.Enumeration;
import java.io.*;

public class StoreTest {
    private Session session = null;
    private Store store = createYmailStore(new URLName("ymail://rckenned_test:testing@us.mg0.mail.yahoo.com/"));

    @Before
    public void setUp() {
        try {
            store.connect();
            javax.mail.Folder folders[] = store.getDefaultFolder().list("%");
            for(javax.mail.Folder folder : folders) {
                if(!((YahooFolder) folder).getFolderInfo().isSystem()) {
                    folder.delete(true);
                }
            }
        }
        catch(MessagingException e) {
            StringWriter stackTrace = new StringWriter();
            PrintWriter writer = new PrintWriter(stackTrace);
            e.printStackTrace(writer);
            TestCase.fail("Failed to clean up mailbox prior to tests: " + e.toString() + "\n" + stackTrace.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void connect() {
        try {
            store.connect();
        }
        catch(MessagingException e) {
            TestCase.fail("Connect failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void listFolders() {
        try {
            store.connect();

            store.getDefaultFolder().list();
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void listFoldersByWildcard() {
        try {
            store.connect();

            store.getDefaultFolder().list("%");
            store.getDefaultFolder().list("Sen%");
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void getInbox() {
        try {
            store.connect();

            javax.mail.Folder inbox = store.getFolder("INBOX");
            TestCase.assertTrue(inbox.exists());
            TestCase.assertEquals("INBOX", inbox.getName());

            javax.mail.Folder doesntExist = store.getFolder("doesntexist");
            TestCase.assertFalse(doesntExist.exists());
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void createRemoveRename() {
        try {
            store.connect();

            javax.mail.Folder testFolder = store.getFolder("test_folder");
            TestCase.assertFalse(testFolder.exists());

            TestCase.assertTrue(testFolder.create(YahooFolder.HOLDS_MESSAGES));
            TestCase.assertTrue(((YahooFolder) testFolder).getFolderInfo().exists());
            TestCase.assertEquals("test%5ffolder", ((YahooFolder) testFolder).getFolderInfo().getId());

            TestCase.assertTrue(testFolder.renameTo(store.getFolder("test_folder2")));
            TestCase.assertTrue(testFolder.exists());
            TestCase.assertEquals("test%5ffolder2", ((YahooFolder) testFolder).getFolderInfo().getId());
            TestCase.assertEquals("test_folder2", testFolder.getName());

            TestCase.assertTrue(testFolder.delete(true));
            TestCase.assertFalse(testFolder.exists());
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void appendMessages() {
        try {
            store.connect();
            javax.mail.Folder inbox = store.getFolder("INBOX");

            MimeMessage simpleMessage = new MimeMessage(session);
            simpleMessage.setFrom(new InternetAddress("rckenned_test@yahoo.com", "Ryan Test"));
            simpleMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("bob@example.com", "Bob Example"));
            simpleMessage.setSentDate(new Date());
            simpleMessage.setSubject("this is a test text/plain message", "us-ascii");
            simpleMessage.setText("Wouldn't the world be a better place if Ryan had a million dollars?", "us-ascii", "plain");

            MimeMessage complexMessage = new MimeMessage(session);
            complexMessage.setFrom(new InternetAddress("rckenned_test@yahoo.com", "Ryan Test"));
            complexMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("bob@example.com", "Bob Example"));
            complexMessage.setSentDate(new Date());
            complexMessage.setSubject("this is a test multipart/alternative message", "us-ascii");

            MimeMultipart body = new MimeMultipart("alternative");
            MimeBodyPart plain = new MimeBodyPart();
            plain.setText("This is a plaintext message.", "us-ascii", "plain");
            MimeBodyPart html = new MimeBodyPart();
            html.setText("<b>This</b> <i>is</i> <strike>an</strike> HTML message.", "us-ascii", "html");
            body.addBodyPart(plain);
            body.addBodyPart(html);
            complexMessage.setContent(body);

            Message messages[] = {simpleMessage, complexMessage};

            inbox.appendMessages(messages);
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        catch(UnsupportedEncodingException e) {
            TestCase.fail("Encoding unsupported, wtf? - " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void hasNewMessages() {
        try {
            store.connect();
            javax.mail.Folder inbox = store.getFolder("INBOX");
            TestCase.assertFalse(inbox.hasNewMessages());
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    @Test
    public void open() {
        try {
            store.connect();
            javax.mail.Folder inbox = store.getFolder("INBOX");

            inbox.open(YahooFolder.READ_WRITE);
            TestCase.assertTrue(inbox.getMessageCount() > 0);
            Message message = inbox.getMessage(1);
            System.out.println("message.toString() = " + message.toString());
            System.out.println("message.getFrom()[0] = " + message.getFrom()[0]);
            System.out.println("message.getSubject() = " + message.getSubject());

            System.out.println("message.getReceivedDate() = " + message.getReceivedDate());
            System.out.println("message.getSentDate() = " + message.getSentDate());

            Address to[] = message.getRecipients(Message.RecipientType.TO);
            for(Address addr : to) {
                System.out.println(String.format("To: %s", addr));
            }

            Enumeration headers = message.getAllHeaders();
            while(headers.hasMoreElements()) {
                Header header = (Header)headers.nextElement();
                System.out.println(String.format("HEADER - %s: %s", header.getName(), header.getValue()));
            }
            inbox.close(false);
        }
        catch(MessagingException e) {
            TestCase.fail("Mailbox request failed: " + e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    TestCase.fail("Failed to close the store" + e.toString());
                }
            }
        }
    }

    private Store createYmailStore(URLName urlname) {
        session = Session.getDefaultInstance(new Properties());
//        session.addProvider(new Provider(Provider.Type.STORE, "ymail", "com.yahoo.mail.YahooStore", "Yahoo!", "1.0"));

        Store store = null;
        try {
            store = session.getStore(urlname);
            TestCase.assertEquals(YahooStore.class, store.getClass());
        }
        catch(NoSuchProviderException e) {
            TestCase.fail("Provider didn't register properly");
        }

        return store;
    }
}