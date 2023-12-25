package com.figaf.integration.tpm.exception;

public class MigParseException extends RuntimeException {

    public MigParseException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}
