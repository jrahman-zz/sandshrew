package org.rahmanj.sandshrew.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Standard file watcher routine
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class FileWatcher implements Runnable {


    /**
     * Construct a new {@link FileWatcher} instance
     *
     * @param file {@link Path} to the file to watch
     * @param callback The {@link FileChangedHandler} to invoke when the file changes
     * @param errorCallback The {@link FileChangedErrorHandler} to invoke if an error occurs
     * @param executor The {@link ScheduledExecutorService} to run the {@link FileWatcher} on in the background
     * @param period The delay (in seconds) between successive checks on the file
     */
    public FileWatcher(Path file, FileChangedHandler callback, FileChangedErrorHandler errorCallback, ScheduledExecutorService executor, long period) {
        if (executor == null) {
            throw new NullPointerException("Null executor");
        }

        if (file == null) {
            throw new NullPointerException("Null path");
        }

        if (callback == null) {
            throw new NullPointerException("Null callback");
        }

        if (errorCallback == null) {
            throw new NullPointerException("Null error callback");
        }

        _callback = callback;
        _filePath = file;
        _executorService = executor;
        _lastAttrs = null;

        // Kick-off
        _future = _executorService.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Construct a new {@link FileWatcher} instance
     *
     * @param file {@link Path} to the file to watch
     * @param callback The {@link FileChangedHandler} to invoke when the file changes
     * @param errorCallback The {@link FileChangedErrorHandler} to invoke if an error occurs
     * @param executor The {@link ScheduledExecutorService} to run the {@link FileWatcher} on in the background
     */
    public FileWatcher(Path file, FileChangedHandler callback, FileChangedErrorHandler errorCallback, ScheduledExecutorService executor) {
        this(file, callback, errorCallback, executor, 2);
    }

    @Override
    public void run() {

        PosixFileAttributeView view = Files.getFileAttributeView(_filePath, PosixFileAttributeView.class);

        try {
            PosixFileAttributes attrs = view.readAttributes();

            // Fire the callback if either first invocation OR new file
            if (_lastAttrs == null || _lastAttrs.lastModifiedTime().compareTo(attrs.lastModifiedTime()) < 0) {
                _callback.fileChanged(_filePath);
            }
            _lastAttrs = attrs;
        } catch (IOException e) {
            _errorCallback.fileAccessError(e);
        }
    }

    /**
     * Cancel further execution of the {@link FileWatcher}
     *
     * @return True if the {@link FileWatcher} was successfully cancelled, false otherwise
     */
    public boolean cancel() {
        return _future.cancel(false);
    }


    /**
     * Last file attributes view we saw
     */
    private PosixFileAttributes _lastAttrs;

    /**
     *
     */
    private FileChangedHandler _callback;

    /**
     *
     */
    private FileChangedErrorHandler _errorCallback;

    /**
     *
     */
    private Path _filePath;

    /**
     *
     */
    private ScheduledExecutorService _executorService;

    /**
     *
     */
    private ScheduledFuture _future;

}
