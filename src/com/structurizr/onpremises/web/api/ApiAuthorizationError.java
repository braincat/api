package com.structurizr.onpremises.web.api;

class ApiAuthorizationError extends ApiMessageResponse {

    ApiAuthorizationError(String message) {
        super(message, 401);
    }

}
