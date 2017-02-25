package com.structurizr.onpremisesapi.web;

import com.structurizr.annotation.UsedBySoftwareSystem;
import com.structurizr.annotation.UsesComponent;
import com.structurizr.onpremisesapi.domain.UUID;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponent;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponentException;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * A simple implementation of the Structurizr web API, consisting of two operations to
 * get and put JSON workspace definitions:
 *
 *  - GET /workspace/{id}
 *  - PUT /workspace/{id}
 */
@UsedBySoftwareSystem(name = "Structurizr Client", description = "Gets and puts workspaces using")
public class ApiServlet extends HttpServlet {

    @UsesComponent(description = "Gets and puts workspace data using")
    private WorkspaceComponent workspaceComponent;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WorkspaceComponent workspaceComponent = WorkspaceComponent.create(config.getServletContext().getInitParameter("dataDirectory"));
        setWorkspaceComponent(workspaceComponent);
    }

    void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            addAccessControlAllowHeaders(response);

            long workspaceId = getWorkspaceId(request, response);
            if (workspaceId > 0) {
                String key = request.getParameter("key");
                String secret = request.getParameter("secret");

                if ((key == null || !UUID.isUUID(key))) {
                    send(new ApiError("A 36 character API key (UUID) must be specified using the parameter name 'key'."), response);
                    return;
                }

                if ((secret == null || !UUID.isUUID(secret))) {
                    send(new ApiError("A 36 character API secret (UUID) must be specified using the parameter name 'secret'."), response);
                    return;
                }

                if (workspaceComponent.createWorkspace(workspaceId, key, secret)) {
                    send(new ApiSuccessMessage("The key and secret for workspace " + workspaceId + " have been updated."), response);
                } else {
                    send(new ApiError("A key and secret pair for workspace " + workspaceId + " already exists."), response);
                }
            }
        } catch (WorkspaceComponentException e) {
            send(new ApiError(e), response);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addAccessControlAllowHeaders(response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            addAccessControlAllowHeaders(response);

            long workspaceId = getWorkspaceId(request, response);
            if (workspaceId > 0) {
                String resource = getResource(request);
                if (resource == null) {
                    if (isAuthorised(workspaceId, "GET", getPath(request, workspaceId), null, true, request, response)) {
                        String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId);

                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json; charset=utf-8");
                        send(new ApiDataResponse(workspaceAsJson), response);
                    }
                } else {
                    if (isImage(resource)) {
                        try {
                            RenderedImage image = workspaceComponent.getImage(workspaceId, resource);
                            if (image != null) {
                                String fileExtension = resource.substring(resource.lastIndexOf(".")+1);
                                response.setContentType(getMimeType(fileExtension));
                                OutputStream out = response.getOutputStream();
                                ImageIO.write(image, fileExtension, out);
                                out.close();
                                response.setStatus(HttpServletResponse.SC_OK);
                            } else {
                                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            }
                        } catch (WorkspaceComponentException e) {
                            e.printStackTrace();
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            }
        } catch (Exception e) {
            send(new ApiError(e), response);
        }
    }

    private boolean isImage(String name) {
        return  name != null && (
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".jpeg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".gif"));
    }

    private String getMimeType(String fileExtension) {
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "image/" + fileExtension;
        }
    }

    private String getPath(HttpServletRequest request, long workspaceId) {
        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        return contextPath + "workspace/" + workspaceId;
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            addAccessControlAllowHeaders(response);

            long workspaceId = getWorkspaceId(request, response);
            if (workspaceId > 0) {
                String workspaceAsJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                if (isAuthorised(workspaceId, "PUT", getPath(request, workspaceId), workspaceAsJson, false, request, response)) {
                    workspaceComponent.putWorkspace(workspaceId, workspaceAsJson);

                    send(new ApiSuccessMessage(), response);
                }
            }
        } catch (Exception e) {
            send(new ApiError(e), response);
        }
    }

    private long getWorkspaceId(HttpServletRequest request, HttpServletResponse response) {
        long workspaceId;
        try {
            String path = request.getPathInfo().substring(1); // remove the leading / character
            if (path.contains("/")) {
                String[] parts = path.split("/");
                workspaceId = Long.parseLong(parts[0]);
            } else {
                workspaceId = Long.parseLong(path);
            }

            if (workspaceId < 1) {
                send(new ApiError("Workspace ID must be greater than 1"), response);
                workspaceId = 0;
            }
        } catch (NumberFormatException e) {
            send(new ApiError("Workspace ID must be a number"), response);
            workspaceId = 0;
        }

        return workspaceId;
    }

    private String getResource(HttpServletRequest request) {
        String path = request.getPathInfo().substring(1); // remove the leading / character
        if (path.contains("/")) {
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                String resource = parts[1];
                if (resource != null && resource.trim().length() > 0) {
                    return resource;
                }
            }
        }

        return null;
    }

    private void send(ApiResponse apiResponse, HttpServletResponse response) {
        try {
            response.setStatus(apiResponse.getStatus());
            response.getWriter().write(apiResponse.toString());
        } catch (IOException e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }

    private void addAccessControlAllowHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "accept, origin, " +
                HttpHeaders.CONTENT_TYPE + ", " +
                HttpHeaders.CONTENT_MD5 + ", " +
                HttpHeaders.AUTHORIZATION + ", " +
                HttpHeaders.NONCE);
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT");
    }

    private boolean isAuthorised(long workspaceId, String httpMethod, String path, String content, boolean bypassHMacValidation, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String key = request.getParameter("key");
        String secret = request.getParameter("secret");
        if (UUID.isUUID(key) && UUID.isUUID(secret) && key.equals(workspaceComponent.getApiKey(workspaceId)) && secret.equals(workspaceComponent.getApiSecret(workspaceId))) {
            return true;
        }

        String authorizationHeaderAsString = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
            send(new ApiAuthorizationError("Authorization header must be provided"), response);
            return false;
        }

        String apiKey = workspaceComponent.getApiKey(workspaceId);

        HmacAuthorizationHeader hmacAuthorizationHeader = HmacAuthorizationHeader.parse(authorizationHeaderAsString);
        String apiKeyFromAuthorizationHeader = hmacAuthorizationHeader.getApiKey();

        if (!apiKeyFromAuthorizationHeader.equals(apiKey)) {
            send(new ApiAuthorizationError("Incorrect API key"), response);
            return false;
        } else {
            if (bypassHMacValidation) {
                // this makes the workspace accessible by only providing the API key
                return true;
            }
        }

        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        }

        String nonce = request.getHeader(HttpHeaders.NONCE);
        if (nonce == null || nonce.length() == 0) {
            send(new ApiAuthorizationError("Request header missing: " + HttpHeaders.NONCE), response);
            return false;
        }

        String contentMd5InRequest;
        String contentMd5Header = request.getHeader(HttpHeaders.CONTENT_MD5);
        if (contentMd5Header == null || contentMd5Header.length() == 0) {
            send(new ApiAuthorizationError("Request header missing: " + HttpHeaders.CONTENT_MD5), response);
            return false;
        }

        contentMd5InRequest = new String(Base64.getDecoder().decode(contentMd5Header));

        String apiSecret = workspaceComponent.getApiSecret(workspaceId);

        try {
            String generatedContentMd5 = new Md5Digest().generate(content);
            if (!contentMd5InRequest.equals(generatedContentMd5)) {
                // the content has been tampered with?
                send(new ApiAuthorizationError("MD5 hash doesn't match content"), response);
                return false;
            }
        } catch (Exception e) {
            send(new ApiError(e), response);
            return false;
        }

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode(apiSecret);
        try {
            String hmacInRequest = hmacAuthorizationHeader.getHmac();
            HmacContent hmacContent = new HmacContent(httpMethod, path, contentMd5InRequest, contentType, nonce);
            String generatedHmac = code.generate(hmacContent.toString());
            if (!hmacInRequest.equals(generatedHmac)) {
                send(new ApiAuthorizationError("Authorization header doesn't match"), response);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            send(new ApiError(e), response);
            return false;
        }
    }

}