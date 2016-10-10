package com.structurizr.onpremisesapi.web;

class ApiAuthorizationError extends ApiMessageResponse {

    ApiAuthorizationError(String message) {
        super(message, 401);
    }

}
