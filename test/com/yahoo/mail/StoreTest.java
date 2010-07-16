package com.yahoo.mail;

import org.junit.Test;

import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.event.FolderEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import junit.framework.TestCase;

public class StoreTest {
    @Test
    public void folderListener() throws MessagingException, TimeoutException, ExecutionException, InterruptedException {
        Store store = TestAccounts.getOpenStore();

        if(store.getFolder("added").exists()) {
            TestCase.assertTrue(store.getFolder("added").delete(true));
        }

        if(store.getFolder("renamed").exists()) {
            TestCase.assertTrue(store.getFolder("renamed").delete(true));
        }

        FutureFolderListener createdListener = new FutureFolderListener(FolderEvent.CREATED);
        store.addFolderListener(createdListener);

        FutureFolderListener renamedListener = new FutureFolderListener(FolderEvent.RENAMED);
        store.addFolderListener(renamedListener);

        FutureFolderListener deletedListener = new FutureFolderListener(FolderEvent.DELETED);
        store.addFolderListener(deletedListener);

        store.getFolder("added").create(Folder.HOLDS_MESSAGES);
        store.getFolder("added").renameTo(store.getFolder("renamed"));
        store.getFolder("renamed").delete(true);

        FolderEvent created = createdListener.get(50, TimeUnit.MILLISECONDS);
        FolderEvent renamed = renamedListener.get(50, TimeUnit.MILLISECONDS);
        FolderEvent deleted = deletedListener.get(50, TimeUnit.MILLISECONDS);

        TestCase.assertEquals("added", created.getFolder().getName());
        TestCase.assertEquals("renamed", renamed.getNewFolder().getName());
        TestCase.assertEquals("renamed", deleted.getFolder().getName());

        store.removeFolderListener(createdListener);
        store.removeFolderListener(renamedListener);
        store.removeFolderListener(deletedListener);

        createdListener.reset();

        store.getFolder("added").create(Folder.HOLDS_MESSAGES);
        try {
            createdListener.get(50, TimeUnit.MILLISECONDS);
            TestCase.fail("A created event should never have been sent to this listener");
        }
        catch(TimeoutException e) {
            // Expected
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getDefaultFolder() {
        Store store = TestAccounts.getOpenStore();

        try {
            Folder defaultFolder = store.getDefaultFolder();
            TestCase.assertTrue(defaultFolder.exists());
            TestCase.assertFalse(defaultFolder.isOpen());
            TestCase.assertNull(defaultFolder.getParent());
            TestCase.assertNotSame(defaultFolder, store.getDefaultFolder());
        }
        catch(MessagingException e) {
            TestCase.fail("Failed to operate on default folder: " + e.toString());
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getFolder() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        if(store.getFolder("test").exists()) {
            TestCase.assertTrue(store.getFolder("test").delete(false));
        }

        Folder testFolder = store.getFolder("test");
        TestCase.assertFalse(testFolder.exists());
        TestCase.assertNotSame(testFolder, store.getFolder("test"));

        TestCase.assertTrue(testFolder.create(Folder.HOLDS_MESSAGES));
        testFolder = store.getFolder("test");
        TestCase.assertTrue(testFolder.exists());
        TestCase.assertEquals(store.getDefaultFolder(), testFolder.getParent());

        TestAccounts.closeStore(store);
    }

    @Test
    public void getPersonalNamespaces() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        Folder personalNamespaces[] = store.getPersonalNamespaces();
        Folder inbox = store.getFolder("INBOX");
        if(inbox.exists()) {
            TestCase.assertTrue(folderExists(inbox, personalNamespaces));
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getSharedNamespaces() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        Folder sharedNamespaces[] = flattenFolders(store.getSharedNamespaces());
        Folder personalNamespaces[] = store.getPersonalNamespaces();
        for(Folder folder : sharedNamespaces) {
            TestCase.assertFalse(folderExists(folder, personalNamespaces));
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getUserNamespaces() {
        // TODO: This could be interesting for Yahoo! Small Business accounts.
        TestCase.fail("Not implemented");
    }

    private boolean folderExists(Folder folder, Folder[] folders) throws MessagingException {
        for(Folder current : folders) {
            if(current.equals(folder)) {
                return true;
            }
            else if(current.getType() == Folder.HOLDS_FOLDERS) {
                if(folderExists(folder, current.list())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Folder[] flattenFolders(Folder[] folders) throws MessagingException {
        List<Folder> folderList = new ArrayList<Folder>();
        for(Folder folder : folders) {
            folderList.add(folder);
            if(folder.getType() == Folder.HOLDS_FOLDERS) {
                folderList.addAll(Arrays.asList(flattenFolders(folder.list())));
            }
        }
        return folderList.toArray(new Folder[folderList.size()]);
    }
}