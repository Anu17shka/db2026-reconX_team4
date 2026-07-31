// package com.dbtraining.reconx.exception;

// /** TICKET-ADV025 — 409 Conflict: tradeRef already exists. */
// public class DuplicateTradeRefException extends ReconException {
//     public DuplicateTradeRefException(String tradeRef) {
//         super("Duplicate tradeRef: " + tradeRef);
//     }
// }

package com.dbtraining.reconx.exception;

public class DuplicateTradeRefException extends ReconException {

    public DuplicateTradeRefException(String message) {
        super(message);
    }

    public DuplicateTradeRefException(String message, Throwable cause) {
        super(message, cause);
    }
}