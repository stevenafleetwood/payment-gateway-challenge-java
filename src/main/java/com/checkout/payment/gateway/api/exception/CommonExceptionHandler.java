package com.checkout.payment.gateway.api.exception;

import com.checkout.payment.gateway.api.resources.ErrorResponse;
import com.checkout.payment.gateway.exception.EventProcessingException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException ex) {
    log.debug("Invalid request received", ex);

    String errors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
    return new ResponseEntity<>(new ErrorResponse(
        "Request was invalid: " + errors),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(PaymentRejectedException.class)
  public ResponseEntity<ErrorResponse> handleException(PaymentRejectedException ex) {
    log.debug("Payment submission was rejected", ex);

    String errors = ex.getErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
    return new ResponseEntity<>(new ErrorResponse(
        "Payment was Rejected as invalid information was supplied: " + errors),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleException(PaymentNotFoundException ex) {
    log.debug("Request for non-existent payment received: ", ex);
    return new ResponseEntity<>(new ErrorResponse(
        "Request was invalid: No payment found with id: " + ex.getId()),
        HttpStatus.NOT_FOUND
    );
  }

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    log.error("EventProcessingException happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Internal error"),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Unhandled exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Internal error"),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
