package com.bbc.km.controller;

import com.bbc.km.exception.PlateNotEmptyException;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ErrorResponse> handleGenericException(Exception exception, WebRequest request) {
        return this._buildError(exception);
    }

    @ExceptionHandler(PlateNotEmptyException.class)
    public ResponseEntity<ErrorResponse> handlePlateNotEmptyException(PlateNotEmptyException exception,
                                                                      WebRequest request) {
        return _buildError(exception);
    }

    private ResponseEntity<ErrorResponse> _buildError(Exception exception) {

        HttpStatus httpCode = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage;

        if (exception.getClass().isAnnotationPresent(ResponseStatus.class)) {
            httpCode = exception.getClass().getAnnotation(ResponseStatus.class).code();
        }

        if (exception.getMessage() == null) {
            errorMessage = exception.getClass().getName();
        } else {
            errorMessage = exception.getMessage();
        }

        final ErrorResponse err = new ErrorResponse(
                LocalDateTime.now().toString(),
                1,
                httpCode.value(),
                httpCode.getReasonPhrase(),
                errorMessage
        );

        return ResponseEntity.status(httpCode).body(err);
    }

    public static class ErrorResponse {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
        private String timestamp;
        private Integer errorCode;
        private Integer httpCode;
        private String message;
        private String cause;

        public ErrorResponse() {
        }

        public ErrorResponse(String timestamp, Integer errorCode, Integer httpCode, String message, String cause) {
            this.timestamp = timestamp;
            this.errorCode = errorCode;
            this.httpCode = httpCode;
            this.message = message;
            this.cause = cause;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(Integer errorCode) {
            this.errorCode = errorCode;
        }

        public Integer getHttpCode() {
            return httpCode;
        }

        public void setHttpCode(Integer httpCode) {
            this.httpCode = httpCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }
    }
}
