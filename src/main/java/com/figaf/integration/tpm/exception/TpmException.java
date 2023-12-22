package com.figaf.integration.tpm.exception;

public class TpmException extends Exception {

    public TpmException(String errorMsg) {
        super(errorMsg);
    }

    public TpmException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}
