package com.bbc.km.exception;

import com.bbc.km.controller.ErrorHandlerController;

public class PlateOffException extends RuntimeException {

    private final ErrorHandlerController.ErrorInfo info;

    public PlateOffException(String message, ErrorHandlerController.ErrorInfo info) {
        super(message);
        this.info = info;
    }

    public ErrorHandlerController.ErrorInfo getInfo() {
        return info;
    }
}
