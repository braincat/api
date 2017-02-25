package com.structurizr.onpremisesapi.web;

class ApiSuccessMessage extends ApiMessageResponse {

    ApiSuccessMessage() {
        super("OK", 200);
    }

    ApiSuccessMessage(String message) {
        super(message, 200);
    }

}
