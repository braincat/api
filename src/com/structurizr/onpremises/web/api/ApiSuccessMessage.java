package com.structurizr.onpremises.web.api;

class ApiSuccessMessage extends ApiMessageResponse {

    ApiSuccessMessage() {
        super("OK", 200);
    }

    ApiSuccessMessage(String message) {
        super(message, 200);
    }

}
