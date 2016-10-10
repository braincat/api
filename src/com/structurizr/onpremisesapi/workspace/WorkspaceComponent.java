package com.structurizr.onpremisesapi.workspace;

import com.structurizr.annotation.Component;

/**
 * Responsible for managing workspace information (workspace definitions plus API keys and secrets).
 */
@Component
public interface WorkspaceComponent {

    public String getWorkspace(long workspaceId) throws WorkspaceComponentException;

    public void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException;

    public String getApiKey(long workspaceId) throws WorkspaceComponentException;

    public String getApiSecret(long workspaceId) throws WorkspaceComponentException;

}
