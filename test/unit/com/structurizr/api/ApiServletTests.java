package com.structurizr.api;

import com.structurizr.workspace.WorkspaceComponent;
import com.structurizr.workspace.WorkspaceComponentException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.*;

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
    public void test_doGet_ReturnsAnApiError_WhenNoNonceHeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "123");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Request header missing: Nonce\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenNoContentMd5HeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "123");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Request header missing: Content-MD5\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "otherkey:f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Incorrect API key\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenTheContentMd5HeaderDoesNotMatchTheHashOfTheContent() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZmM1ZTAzOGQzOGE1NzAzMjA4NTQ0MWU3ZmU3MDEwYjA=");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"MD5 hash doesn't match content\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsAnApiError_WhenTheHmacPartOfTheAuthorizationHeaderIsIncorrectlySpecified() throws Exception {
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");
        apiServlet.doGet(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Authorization header doesn't match\"}", response.getContent());
    }

    @Test
    public void test_doGet_ReturnsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        workspaceComponent.putWorkspace(1, "json");
        request.setPathInfo("/1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:NzdiN2M0MjAyNjA3MmJhYWZkYzUzZTgwZWJhNzRmYzE1YmIyYjE4NjBhZTdmODYxMDJhZThlODRkZjM1MTExYw==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");
        apiServlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("json", response.getContent());

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("accept, origin, Content-Type, Content-MD5, Authorization, Nonce", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("GET, PUT", response.getHeader("Access-Control-Allow-Methods"));
    }

    @Test
    public void test_doPut_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() throws Exception {
        request.setPathInfo("/1");
        apiServlet.doPut(request, response);
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Authorization header must be provided\"}", response.getContent());
    }

    @Test
    public void test_doPut_PutsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        assertNull("json", workspaceComponent.getWorkspace(1));

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

}

class MockHttpServletRequest implements HttpServletRequest {

    private String pathInfo;
    private Map<String,String> headers = new HashMap<>();
    private StringReader stringReader = new StringReader("json");

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
        return null;
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

    @Override
    public String getParameter(String s) {
        return null;
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

    String getContent() {
        return stringWriter.toString();
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
    public void sendError(int i) throws IOException {

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
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
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
    public void setContentType(String s) {

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

}
