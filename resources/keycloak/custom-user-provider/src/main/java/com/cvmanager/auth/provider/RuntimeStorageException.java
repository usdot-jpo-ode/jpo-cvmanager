package com.cvmanager.auth.provider;

public class RuntimeStorageException extends RuntimeException {

    private static final String PREFIX = "Database error: ";

    public RuntimeStorageException() {
        super(PREFIX);
    }

    public RuntimeStorageException(String message) {
        super(PREFIX + message);
    }

    public RuntimeStorageException(String message, Throwable cause) {
        super(PREFIX + message, cause);
    }

    public RuntimeStorageException(Throwable cause) {
        super(PREFIX, cause);
    }
}