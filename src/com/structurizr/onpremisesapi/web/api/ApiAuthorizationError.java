package com.structurizr.onpremisesapi.web.api;

class ApiAuthorizationError extends ApiMessageResponse {

    ApiAuthorizationError(String message) {
        super(message, 401);
    }

}
