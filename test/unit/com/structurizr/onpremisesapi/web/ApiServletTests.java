package com.structurizr.onpremisesapi.web;

import com.structurizr.onpremisesapi.workspace.WorkspaceComponent;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponentException;
import com.structurizr.onpremisesapi.workspace.WorkspaceSummary;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiServletTests {

    private ApiServlet apiServlet;
    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private WorkspaceComponent workspaceComponent = new MockWorkspaceComponent();

    @Before
    public void setUp() throws Exception {
        apiServlet = new ApiServlet();
        apiServlet.init(new ServletConfig() {
            @Override
            public String getServletName() {
                return "ApiServlet";
            }

            @Override
            public ServletContext getServletContext() {
                return new MockServletContext();
            }

            @Override
            public String getInitParameter(String s) {
                return null;
            }

            @Override
            public Enumeration getInitParameterNames() {
                return null;
            }
        });

        apiServlet.setWorkspaceComponent(workspaceComponent);
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() throws Exception {
        request.setPathInfo("/-1");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Workspace ID must be greater than 1\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenANonNumericWorkspaceIdIsSpecified() throws Exception {
        request.setPathInfo("/abc");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Workspace ID must be a number\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAKeyIsNotSpecified() throws Exception {
        request.setPathInfo("/1234");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API key (UUID) must be specified using the parameter name 'key'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAnEmptyKeyIsSpecified() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API key (UUID) must be specified using the parameter name 'key'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAnInvalidKeyIsSpecified() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "key");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API key (UUID) must be specified using the parameter name 'key'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenASecretIsNotSpecified() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API secret (UUID) must be specified using the parameter name 'secret'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAnEmptySecretIsSpecified() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        request.setParameter("secret", "");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API secret (UUID) must be specified using the parameter name 'secret'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAnInvalidSecretIsSpecified() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        request.setParameter("secret", "secret");
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"A 36 character API secret (UUID) must be specified using the parameter name 'secret'\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenTheWorkspaceCouldNotBeCreated() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        request.setParameter("secret", "670a738a-d334-4f25-a5b4-003c2b5afe95");
        apiServlet.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public boolean createWorkspace(long workspaceId, String key, String secret) throws WorkspaceComponentException {
                return false;
            }
        });
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Workspace 1234 already exists\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiError_WhenAWorkspaceComponentExceptionIsThrown() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        request.setParameter("secret", "670a738a-d334-4f25-a5b4-003c2b5afe95");
        apiServlet.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public boolean createWorkspace(long workspaceId, String key, String secret) throws WorkspaceComponentException {
                throw new WorkspaceComponentException("Some message");
            }
        });
        apiServlet.doPost(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Some message\"}", response.getContent());
    }

    @Test
    public void test_doPost_ReturnsAnApiSuccessMessage_WhenTheWorkspaceCouldBeCreated() throws Exception {
        request.setPathInfo("/1234");
        request.setParameter("key", "f76ea707-c778-42f3-8c10-359bb4dfc68f");
        request.setParameter("secret", "670a738a-d334-4f25-a5b4-003c2b5afe95");
        apiServlet.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public boolean createWorkspace(long workspaceId, String key, String secret) throws WorkspaceComponentException {
                return true;
            }
        });
        apiServlet.doPost(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("{\"message\":\"OK\"}", response.getContent());
    }

    @Test
    public void test_doOptions_AddsAccessControlHeaders() throws Exception {
        apiServlet.doOptions(request, response);

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("accept, origin, Content-Type, Content-MD5, Authorization, Nonce", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("GET, PUT", response.getHeader("Access-Control-Allow-Methods"));
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() throws Exception {
        request.setPathInfo("/-1");
        apiServlet.doGet(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Workspace ID must be greater than 1\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenANonNumericWorkspaceIdIsSpecified() throws Exception {
        request.setPathInfo("/abc");
        apiServlet.doGet(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Workspace ID must be a number\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Authorization header must be provided\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecifiedAndTheUrlHasATrailingSlash() throws Exception {
        request.setPathInfo("/1/");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Authorization header must be provided\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "123");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");
        apiServlet.doGet(request, response);
        assertEquals(500, response.getStatus());
        assertEquals("{\"message\":\"Invalid authorization header\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsTheWorkspace_WhenApiKeyIsCorrectlySpecified() throws Exception {
        workspaceComponent.putWorkspace(1, "json");
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NzdiN2M0MjAyNjA3MmJhYWZkYzUzZTgwZWJhNzRmYzE1YmIyYjE4NjBhZTdmODYxMDJhZThlODRkZjM1MTExYw==");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("json", response.getContent());

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("accept, origin, Content-Type, Content-MD5, Authorization, Nonce", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("GET, PUT", response.getHeader("Access-Control-Allow-Methods"));
    }

    @Test
    public void test_doPut_ReturnsAnApiError_WhenNoNonceHeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        request.setContent("json");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
        apiServlet.doPut(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Request header missing: Nonce\"}", response.getContent());
    }

    @Test
    public void test_doPut_ReturnsAnApiError_WhenNoContentMd5HeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        request.setContent("json");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        apiServlet.doPut(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Request header missing: Content-MD5\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "otherkey:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Incorrect API key\"}", response.getContent());
    }

    @Test
    public void test_doPut_ReturnsAnApiError_WhenTheContentMd5HeaderDoesNotMatchTheHashOfTheContent() throws Exception {
        request.setPathInfo("/1");
        request.setContent("json");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZmM1ZTAzOGQzOGE1NzAzMjA4NTQ0MWU3ZmU3MDEwYjA=");
        apiServlet.doPut(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"MD5 hash doesn't match content\"}", response.getContent());
    }

    @Test
    public void test_doPut_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() throws Exception {
        request.setContent("json");
        request.setPathInfo("/1");
        apiServlet.doPut(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Authorization header must be provided\"}", response.getContent());
    }

    @Test
    public void test_doPut_PutsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        assertNull("json", workspaceComponent.getWorkspace(1));
        request.setContent("json");
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, Base64.getEncoder().encodeToString(new Md5Digest().generate("json").getBytes()));
        apiServlet.doPut(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("json", workspaceComponent.getWorkspace(1));

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("accept, origin, Content-Type, Content-MD5, Authorization, Nonce", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("GET, PUT", response.getHeader("Access-Control-Allow-Methods"));
    }

    @Test
    public void test_doGet_ReturnsANotFoundError_WhenAResourceIsRequestedButItDoesNotExist() throws Exception {
        apiServlet.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public RenderedImage getImage(long workspaceId, String name) throws WorkspaceComponentException {
                return null;
            }
        });

        request.setPathInfo("/1/image.png");
        apiServlet.doGet(request, response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void test_doGet_ReturnsANotFoundError_WhenANonImageResourceIsRequested() throws Exception {
        request.setPathInfo("/1/image.txt");
        apiServlet.doGet(request, response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void test_doGet_ReturnsAPngImage_WhenAnImageResourceIsRequestedAndItDoesExist() throws Exception {
        request.setPathInfo("/1/image.png");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(6631, response.getContentAsBytes().length);
        assertEquals("image/png", response.getContentType());
    }

    @Test
    public void test_doGet_ReturnsAJpegImage_WhenAnImageResourceIsRequestedAndItDoesExist() throws Exception {
        request.setPathInfo("/1/image.jpeg");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(7772, response.getContentAsBytes().length);
        assertEquals("image/jpeg", response.getContentType());
    }

    @Test
    public void test_doGet_ReturnsAJpgImage_WhenAnImageResourceIsRequestedAndItDoesExist() throws Exception {
        request.setPathInfo("/1/image.jpg");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(7772, response.getContentAsBytes().length);
        assertEquals("image/jpeg", response.getContentType());
    }

    @Test
    public void test_doGet_ReturnsAGifImage_WhenAnImageResourceIsRequestedAndItDoesExist() throws Exception {
        request.setPathInfo("/1/image.gif");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(4593, response.getContentAsBytes().length);
        assertEquals("image/gif", response.getContentType());
    }

}

class MockHttpServletRequest implements HttpServletRequest {

    private String pathInfo;
    private Map<String,String> headers = new HashMap<>();
    private Map<String,String> parameters = new HashMap<>();
    private StringReader stringReader;

    void setContent(String content) {
        stringReader = new StringReader(content);
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return null;
    }

    void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return "/";
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    void setParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    @Override
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(stringReader);
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

}

class MockHttpServletResponse implements HttpServletResponse {

    private Map<String,String> headers = new HashMap<>();
    private int status;
    private StringWriter stringWriter = new StringWriter();
    private PrintWriter printWriter = new PrintWriter(stringWriter);
    private List<Integer> bytes = new ArrayList<>();
    private String contentType;

    String getContent() {
        return stringWriter.toString();
    }

    Integer[] getContentAsBytes() {
        return bytes.toArray(new Integer[0]);
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int status) throws IOException {
        this.status = status;
    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setHeader(String s, String s2) {

    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    int getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                bytes.add(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

}

class MockServletContext implements ServletContext {

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public ServletContext getContext(String s) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String s) {
        return null;
    }

    @Override
    public Set getResourcePaths(String s) {
        return null;
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }

    @Override
    public Servlet getServlet(String s) throws ServletException {
        return null;
    }

    @Override
    public Enumeration getServlets() {
        return null;
    }

    @Override
    public Enumeration getServletNames() {
        return null;
    }

    @Override
    public void log(String s) {

    }

    @Override
    public void log(Exception e, String s) {

    }

    @Override
    public void log(String s, Throwable throwable) {

    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(String s) {
        return "build/ApiServletTests";
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public String getServletContextName() {
        return null;
    }

}

class MockWorkspaceComponent implements WorkspaceComponent {

    private Map<Long,String> workspaces = new HashMap<>();

    @Override
    public Collection<WorkspaceSummary> getWorkspaces() {
        return null;
    }

    @Override
    public boolean createWorkspace(long workspaceId, String key, String secret) throws WorkspaceComponentException {
        return false;
    }

    @Override
    public String getWorkspace(long workspaceId) throws WorkspaceComponentException {
        return workspaces.get(workspaceId);
    }

    @Override
    public void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException {
        workspaces.put(workspaceId, json);
    }

    @Override
    public String getApiKey(long workspaceId) throws WorkspaceComponentException {
        return "key";
    }

    @Override
    public String getApiSecret(long workspaceId) throws WorkspaceComponentException {
        return "secret";
    }

    @Override
    public RenderedImage getImage(long workspaceId, String name) throws WorkspaceComponentException {
        try {
            return ImageIO.read(new File("test/unit/com/structurizr/onpremisesapi", name));
        } catch (IOException e) {
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

}
