package com.eeum.domain.report.exception;

public class AlreadyDeletedException extends RuntimeException {

    public AlreadyDeletedException() {
        super("That Entity is already soft deleted.");
    }
}
