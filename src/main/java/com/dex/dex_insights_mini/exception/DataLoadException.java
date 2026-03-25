package com.dex.dex_insights_mini.exception;

public class DataLoadException extends RuntimeException {
    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

