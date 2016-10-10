package com.structurizr.onpremisesapi.web;

abstract class ApiResponse {

    private int status;

    protected ApiResponse(int status) {
        this.status = status;
    }

    int getStatus() {
        return status;
    }

    @Override
    public abstract String toString();

}
