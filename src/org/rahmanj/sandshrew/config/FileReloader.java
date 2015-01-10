package org.rahmanj.sandshrew.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileStore;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Standard file watcher routine
 *
 * Created by jprahman on 1/9/15.
 */
public class FileReloader implements Runnable {


    /**
     * Construct a new {@link FileReloader} instance
     *
     * @param file {@link Path} to the file to watch
     * @param callback The {@link FileChangedHandler} to invoke when the file changes
     * @param errorCallback The {@link FileChangedErrorHandler} to invoke if an error occurs
     * @param executor The {@link ScheduledExecutorService} to run the {@link FileReloader} on in the background
     * @param period The delay (in seconds) between successive checks on the file
     */
    public FileReloader(Path file, FileChangedHandler callback, FileChangedErrorHandler errorCallback, ScheduledExecutorService executor, long period) {
        if (executor == null) {
            throw new NullPointerException("Null executor");
        }

        if (file == null) {
            throw new NullPointerException("Null path");
        }

        if (callback == null) {
            throw new NullPointerException("null callback");
        }

        if (errorCallback == null) {
            throw new NullPointerException("null error callback");
        }

        _callback = callback;
        _filePath = file;
        _executorService = executor;
        _lastAttrs = null;

        // Kick-off
        _future = _executorService.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Construct a new {@link FileReloader} instance
     *
     * @param file {@link Path} to the file to watch
     * @param callback The {@link FileChangedHandler} to invoke when the file changes
     * @param errorCallback The {@link FileChangedErrorHandler} to invoke if an error occurs
     * @param executor The {@link ScheduledExecutorService} to run the {@link FileReloader} on in the background
     */
    public FileReloader(Path file, FileChangedHandler callback, FileChangedErrorHandler errorCallback, ScheduledExecutorService executor) {
        this(file, callback, errorCallback, executor, 2);
    }

    @Override
    public void run() {

        PosixFileAttributeView view = Files.getFileAttributeView(_filePath, PosixFileAttributeView.class);

        try {
            PosixFileAttributes attrs = view.readAttributes();

            // Fire the callback if either
            if (_lastAttrs == null || _lastAttrs.lastModifiedTime().compareTo(attrs.lastModifiedTime()) < 0) {
                _callback.fileChanged(_filePath);
            }
            _lastAttrs = attrs;
        } catch (IOException e) {
            _errorCallback.fileAccessError(e);
        }
    }

    /**
     * Cancel further execution of the {@link FileReloader}
     */
    public void cancel() {
        _future.cancel(false);
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
