package com.dbtraining.reconx.exception;

public class ReconException extends RuntimeException {

    private Long reconBreakId;


    public ReconException(String message) {
        super(message);
    }


    public ReconException(String message, Long reconBreakId) {
        super(message);
        this.reconBreakId = reconBreakId;
    }


    public ReconException(String message, Throwable cause) {
        super(message, cause);
    }


    public Long getReconBreakId() {
        return reconBreakId;
    }
}