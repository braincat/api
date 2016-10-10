package com.structurizr.onpremisesapi.web;

abstract class ApiMessageResponse extends ApiResponse {

    private String message;

    protected ApiMessageResponse(String message, int status) {
        super(status);

        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("{\"message\":\"%s\"}", message);
    }

}
