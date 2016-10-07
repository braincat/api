package com.structurizr.workspace;

public interface WorkspaceComponent {

    public String getWorkspace(long workspaceId) throws WorkspaceComponentException;

    public void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException;

    public String getApiKey(long workspaceId) throws WorkspaceComponentException;

    public String getApiSecret(long workspaceId) throws WorkspaceComponentException;

}
