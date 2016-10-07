package com.structurizr.api;

import com.structurizr.workspace.FileSystemWorkspaceComponent;
import com.structurizr.workspace.WorkspaceComponent;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * A simple implementation of the Structurizr web API, consisting of two operations to
 * get and put JSON workspace definitions:
 *
 *  - GET /workspace/{id}
 *  - PUT /workspace/{id}
 */
public class ApiServlet extends HttpServlet {

    private WorkspaceComponent workspaceComponent;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        setWorkspaceComponent(new FileSystemWorkspaceComponent(new File(config.getServletContext().getInitParameter("dataDirectory"))));
    }

    void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
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
                if (isAuthorised(workspaceId, "GET", "/workspace/" + workspaceId, null, request, response)) {
                    String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId);

                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json; charset=utf-8");
                    send(new ApiDataResponse(workspaceAsJson), response);
                }
            }
        } catch (Exception e) {
            send(new ApiError(e), response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            addAccessControlAllowHeaders(response);

            long workspaceId = getWorkspaceId(request, response);
            if (workspaceId > 0) {
                String workspaceAsJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                if (isAuthorised(workspaceId, "PUT", "/workspace/" + workspaceId, workspaceAsJson, request, response)) {
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
            workspaceId = Long.parseLong(request.getPathInfo().substring(1)); // remove the leading / character

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

    private boolean isAuthorised(long workspaceId, String httpMethod, String path, String content, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authorizationHeaderAsString = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
            send(new ApiAuthorizationError("Authorization header must be provided"), response);
            return false;
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

        String apiKey = workspaceComponent.getApiKey(workspaceId);
        HmacAuthorizationHeader hmacAuthorizationHeader = HmacAuthorizationHeader.parse(authorizationHeaderAsString);
        String apiKeyFromAuthorizationHeader = hmacAuthorizationHeader.getApiKey();
        String hmacInRequest = hmacAuthorizationHeader.getHmac();
        contentMd5InRequest = new String(Base64.getDecoder().decode(contentMd5Header));

        if (!apiKeyFromAuthorizationHeader.equals(apiKey)) {
            send(new ApiAuthorizationError("Incorrect API key"), response);
            return false;
        }

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