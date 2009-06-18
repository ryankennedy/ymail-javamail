package com.yahoo.mail;

import org.junit.Test;

import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.event.FolderEvent;
import java.util.concurrent.TimeUnit;

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

        FutureFolderListener createdListener = new FutureFolderListener(FolderEvent.CREATED);
        store.addFolderListener(createdListener);

        FutureFolderListener renamedListener = new FutureFolderListener(FolderEvent.RENAMED);
        store.addFolderListener(renamedListener);

        FutureFolderListener deletedListener = new FutureFolderListener(FolderEvent.DELETED);
        store.addFolderListener(deletedListener);

        try {
            store.getFolder("added").create(Folder.HOLDS_MESSAGES);
            store.getFolder("added").renameTo(store.getFolder("renamed"));
            store.getFolder("renamed").delete(true);

            FolderEvent created = null;
            FolderEvent renamed = null;
            FolderEvent deleted = null;
            try {
                created = createdListener.get(50, TimeUnit.MILLISECONDS);
                renamed = renamedListener.get(50, TimeUnit.MILLISECONDS);
                deleted = deletedListener.get(50, TimeUnit.MILLISECONDS);
            }
            catch(Exception e) {
                TestCase.fail("Exception waiting for folder events: " + e.toString());
            }

            TestCase.assertEquals("FolderListener.folderCreated() doesn't appear to have fired", "added", created.getFolder().getName());
            TestCase.assertEquals("FolderListener.folderRenamed() doesn't appear to have fired", "renamed", renamed.getNewFolder().getName());
            TestCase.assertEquals("FolderListener.folderDeleted() doesn't appear to have fired", "renamed", deleted.getFolder().getName());
        }
        catch(MessagingException e) {
            TestCase.fail("Failed folder operation: " + e.toString());
        }
        finally {
            store.removeFolderListener(createdListener);
            store.removeFolderListener(renamedListener);
            store.removeFolderListener(deletedListener);
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