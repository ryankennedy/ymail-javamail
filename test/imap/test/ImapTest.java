package imap.test;

import org.junit.Test;

import javax.mail.*;
import java.util.Properties;

import junit.framework.TestCase;

public class ImapTest {
    @Test
    public void imapConnect() {
        Session session = Session.getDefaultInstance(new Properties());

        Store store = null;
        try {
            store = session.getStore(new URLName("imaps", "imap.gmail.com", 993, "/", "rckenned@gmail.com", "openhack09"));
            store.connect();

            Folder INBOX = store.getFolder("INBOX");
            TestCase.assertTrue(INBOX.exists());
            TestCase.assertEquals("INBOX", INBOX.getName());

            Folder inbox = store.getFolder("inbox");
            TestCase.assertTrue(inbox.exists());
            TestCase.assertEquals("INBOX", inbox.getName());
        }
        catch(NoSuchProviderException e) {
            TestCase.fail(e.toString());
        }
        catch(MessagingException e) {
            TestCase.fail(e.toString());
        }
        finally {
            if(store != null && store.isConnected()) {
                try {
                    store.close();
                }
                catch(MessagingException e) {
                    // Eat it.
                    System.err.println(e.toString());
                }
            }
        }
    }
}