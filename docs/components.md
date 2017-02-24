# Components

The diagram below shows the components that make up the API Application.

![](embed:Components)

### ApiServlet

This is a simple Java Servlet that is mapped to a URL path of ```/workspace/*``` and listens for ```GET``` and ```PUT``` requests. A hash-based message authentication code scheme is used for securing the API, and the following headers must be present in the HTTP request.

- __Authorization__: The authorization header consists of the API key for the workspace being accessed along with a base64 encoded HMAC digest (see below), in the format ```APIkey:HMAC```.
- __Content-Type__: The content type of the request. This should be ```application/json; charset=utf-8``` when a ```PUT``` request is made.
- __Content-MD5__: The base64 encoded MD5 digest of the content being sent in the request.
- __Nonce__: A "number once" (e.g. an incrementing number, like a timestamp in milliseconds), which is used to detect reply attacks. *This API implementation currently ignores the supplied nonce.*

An ```OPTIONS``` request is also supported for preflighting, which returns the following headers.

- ```Access-Control-Allow-Origin: *```
- ```Access-Control-Allow-Headers: accept, origin, Content-Type, Content-MD5, Authorization, Nonce```
- ```Access-Control-Allow-Methods: GET, PUT```

The APIServlet will also serve images (".png", ".gif", ".jpg" and ".jpeg") that are stored in the workspace data directory.

### WorkspaceComponent

This component manages the data associated with a workspace. The included implementation simply stores data on the file system - see the [Data](#Data) section for more details.