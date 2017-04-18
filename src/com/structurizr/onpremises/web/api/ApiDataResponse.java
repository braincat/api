package com.structurizr.onpremises.web.api;

class ApiDataResponse extends ApiResponse {

    private String data;

    ApiDataResponse(String data) {
        super(200);

        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}
