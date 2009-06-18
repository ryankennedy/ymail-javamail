package com.yahoo.mail;

import javax.mail.event.FolderListener;
import javax.mail.event.FolderEvent;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureFolderListener implements FolderListener, Future<FolderEvent> {
    private int eventType;
    private FolderEvent event;
    private boolean done;

    public FutureFolderListener(int eventType) {
        this.eventType = eventType;
        this.done = false;
    }

    public void folderCreated(FolderEvent folderEvent) {
        checkEvent(folderEvent);
    }

    public void folderDeleted(FolderEvent folderEvent) {
        checkEvent(folderEvent);
    }

    public void folderRenamed(FolderEvent folderEvent) {
        checkEvent(folderEvent);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized FolderEvent get() throws InterruptedException, ExecutionException {
        while(!done) {
            wait();
        }
        return event;
    }

    public synchronized FolderEvent get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(!done) {
            wait(unit.toMillis(timeout));
        }

        if(!done) {
            throw new TimeoutException("Timeout waiting for folder event");
        }

        return event;
    }


    public synchronized void reset() {
        event = null;
        done = false;
    }

    private synchronized void checkEvent(FolderEvent folderEvent) {
        if(folderEvent.getType() == eventType) {
            done = true;
            event = folderEvent;
            this.notifyAll();
        }
    }
}