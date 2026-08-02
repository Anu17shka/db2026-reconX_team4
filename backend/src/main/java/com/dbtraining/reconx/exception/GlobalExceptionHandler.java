package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail handleNotFound(TradeNotFoundException ex) {

        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        pd.setType(
                URI.create(
                  "https://reconx.dbtraining.com/errors/trade-not-found"
                )
        );

        pd.setTitle("Trade not found");

        pd.setProperty(
                "timestamp",
                Instant.now()
        );

        return pd;
    }


    @ExceptionHandler(DuplicateTradeRefException.class)
    public ProblemDetail handleDuplicate(
            DuplicateTradeRefException ex) {


        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );


        pd.setType(
                URI.create(
                 "https://reconx.dbtraining.com/errors/duplicate-trade-ref"
                )
        );


        pd.setTitle("Duplicate trade reference");


        return pd;
    }



    @ExceptionHandler(ReconException.class)
    public ProblemDetail handleRecon(
            ReconException ex) {


        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        ex.getMessage()
                );


        pd.setType(
                URI.create(
                  "https://reconx.dbtraining.com/errors/recon-failure"
                )
        );


        pd.setTitle("Reconciliation failure");


        pd.setProperty(
                "reconBreakId",
                ex.getReconBreakId()
        );


        return pd;
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex) {


        String message =
                ex.getBindingResult()
                  .getFieldErrors()
                  .stream()
                  .map(error ->
                      error.getField()
                      + ": "
                      + error.getDefaultMessage()
                  )
                  .collect(Collectors.joining("; "));


        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        message
                );


        pd.setTitle("Validation failed");


        return pd;
    }



    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(
            ConstraintViolationException ex) {


        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }




    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex) {


        log.error(
                "Unhandled exception",
                ex
        );


        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred — please contact support with the correlationId"
                );


        pd.setTitle("Internal server error");


        return pd;
    }

}