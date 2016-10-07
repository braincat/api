package com.structurizr.api;

class ApiAuthorizationError extends ApiMessageResponse {

    ApiAuthorizationError(String message) {
        super(message, 401);
    }

}
