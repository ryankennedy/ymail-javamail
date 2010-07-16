package com.yahoo.mail;

import junit.framework.TestCase;
import org.junit.Test;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

public class FolderTest {
    private static final String USER_FOLDER_NAME = "FolderTest_Folder";

    @Test
    public void getName() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        createUserFolder(store);

        for (Folder folder : store.getDefaultFolder().list()) {
            TestCase.assertNotNull(folder.getName());
            TestCase.assertEquals(folder, store.getDefaultFolder().getFolder(folder.getName()));
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getFullName() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        createUserFolder(store);

        for (Folder folder : store.getDefaultFolder().list()) {
            TestCase.assertNotNull(folder.getFullName());
            TestCase.assertEquals(folder, store.getFolder(folder.getFullName()));
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getParent() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        createUserFolder(store);

        TestCase.assertNull(store.getDefaultFolder().getParent());

        for (Folder folder : store.getDefaultFolder().list()) {
            TestCase.assertEquals(store.getDefaultFolder(), folder.getParent());
            TestCase.assertNotSame(store.getDefaultFolder(), folder.getParent());
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void exists() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        createUserFolder(store);
        TestCase.assertTrue(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).exists());

        deleteUserFolder(store);
        TestCase.assertFalse(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).exists());

        TestAccounts.closeStore(store);
    }

    @Test
    public void list() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getSeparator() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        // It's not important, there's no hierarchy. Just make sure it doesn't throw an exception.
        store.getDefaultFolder().getFolder("INBOX").getSeparator();

        for (Folder folder : store.getDefaultFolder().list()) {
            // It's not important, there's no hierarchy. Just make sure it doesn't throw an exception.
            folder.getSeparator();
        }

        TestAccounts.closeStore(store);
    }

    @Test
    public void getType() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        TestCase.assertEquals(Folder.HOLDS_FOLDERS, store.getDefaultFolder().getType());

        for (Folder folder : store.getDefaultFolder().list()) {
            TestCase.assertEquals(Folder.HOLDS_MESSAGES, folder.getType());
        }
        
        TestAccounts.closeStore(store);
    }

    @Test
    public void create() throws MessagingException {
        Store store = TestAccounts.getOpenStore();

        deleteUserFolder(store);

        TestCase.assertFalse(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).create(Folder.HOLDS_FOLDERS));
        TestCase.assertFalse(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES));
        TestCase.assertTrue(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).create(Folder.HOLDS_MESSAGES));
        TestCase.assertFalse(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).create(Folder.HOLDS_MESSAGES));

        TestCase.assertFalse(store.getDefaultFolder().getFolder(USER_FOLDER_NAME).getFolder("subfolder").create(Folder.HOLDS_MESSAGES));

        TestAccounts.closeStore(store);
    }

    @Test
    public void hasNewMessages() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getFolder() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void delete() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void renameTo() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void open() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void close() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void isOpen() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getPermanentFlags() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getMessageCount() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getMessage() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void appendMessages() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void expunge() {
        TestCase.fail("Not implemented");
    }

    @Test
    public void getFolderInfo() {
        TestCase.fail("Not implemented");
    }

    private void createUserFolder(Store store) throws MessagingException {
        if (!store.getDefaultFolder().getFolder(USER_FOLDER_NAME).exists()) {
            store.getDefaultFolder().getFolder(USER_FOLDER_NAME).create(Folder.HOLDS_MESSAGES);
        }
    }

    private void deleteUserFolder(Store store) throws MessagingException {
        if (store.getDefaultFolder().getFolder(USER_FOLDER_NAME).exists()) {
            store.getDefaultFolder().getFolder(USER_FOLDER_NAME).delete(true);
        }
    }
}
