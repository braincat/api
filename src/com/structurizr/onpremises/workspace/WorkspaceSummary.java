package com.structurizr.onpremises.workspace;

public class WorkspaceSummary {

    private long id;
    private boolean key;
    private boolean secret;
    private boolean data;

    public WorkspaceSummary(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isKey() {
        return key;
    }

    void setKey(boolean key) {
        this.key = key;
    }

    public boolean isSecret() {
        return secret;
    }

    void setSecret(boolean secret) {
        this.secret = secret;
    }

    public boolean isData() {
        return data;
    }

    void setData(boolean data) {
        this.data = data;
    }
}
