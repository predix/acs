package com.ge.predix.acs.encryption;

@SuppressWarnings("serial")
public class CipherInitializationFailureException extends RuntimeException {

    public CipherInitializationFailureException(final Throwable cause) {
        super(cause);
    }
}
