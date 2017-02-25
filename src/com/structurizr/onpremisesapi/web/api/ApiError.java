package com.structurizr.onpremisesapi.web.api;

class ApiError extends ApiMessageResponse {

    ApiError(String message) {
        super(message, 500);
    }

    ApiError(Exception e) {
        super(e.getMessage(), 500);
        e.printStackTrace();
    }

}
