package com.yahoo.mail;

import org.junit.Test;

import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.event.FolderListener;
import javax.mail.event.FolderEvent;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

public class StoreTest {
    @Test
    public void addFolderListener() {
        Store store = TestAccounts.getTestStore();
        try {
            store.connect();
        }
        catch(MessagingException e) {
            TestCase.fail("Failed to connect store: " + e.toString());
        }

        try {
            if(store.getFolder("added").exists()) {
                store.getFolder("added").delete(true);
            }
            if(store.getFolder("renamed").exists()) {
                store.getFolder("renamed").delete(true);
            }
        }
        catch(MessagingException e) {
            TestCase.fail("Failed to clean up test mailbox prior to testing");
        }

        final List<String> created = new ArrayList<String>();
        final List<String> deleted = new ArrayList<String>();
        final List<String> renamed = new ArrayList<String>();

        FolderListener listener = new FolderListener() {
            public void folderCreated(FolderEvent folderEvent) {
                created.add(folderEvent.getFolder().getName());
            }

            public void folderDeleted(FolderEvent folderEvent) {
                deleted.add(folderEvent.getFolder().getName());
            }

            public void folderRenamed(FolderEvent folderEvent) {
                renamed.add(folderEvent.getNewFolder().getName());
            }
        };

        store.addFolderListener(listener);

        try {
            store.getFolder("added").create(Folder.HOLDS_MESSAGES);
            store.getFolder("added").renameTo(store.getFolder("renamed"));
            store.getFolder("renamed").delete(true);

            // TODO: Replace with some of the Java futures stuff so we don't have to sleep.
            try {
                // Sleep for 5 seconds to wait for all the events to be delivered to the listeners.
                Thread.sleep(5000);
            }
            catch(InterruptedException e) { }

            TestCase.assertTrue("FolderListener.folderCreated() doesn't appear to have fired: " + created, created.contains("added"));
            TestCase.assertTrue("FolderListener.folderRenamed() doesn't appear to have fired: " + renamed, renamed.contains("renamed"));
            TestCase.assertTrue("FolderListener.folderDeleted() doesn't appear to have fired: " + deleted, deleted.contains("renamed"));
        }
        catch(MessagingException e) {
            TestCase.fail("Failed folder operation: " + e.toString());
        }
        finally {
            store.removeFolderListener(listener);
            try {
                store.close();
            }
            catch(MessagingException e) {
                System.err.println("Failed to close store");
            }
        }
    }

    @Test
    public void addStoreListener() {

    }

    @Test
    public void getDefaultFolder() {

    }

    @Test
    public void getFolder() {

    }

    @Test
    public void getPersonalNamespaces() {

    }

    @Test
    public void getSharedNamespaces() {

    }

    @Test
    public void getUserNamespaces() {

    }

    @Test
    public void removeFolderListener() {

    }

    @Test
    public void removeStoreListener() {

    }
}