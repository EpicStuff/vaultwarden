# Why an frp https2http tunnel can fail on the first load

When you front an HTTP-only Vaultwarden instance with frp's `https2http` plugin, the tunnel terminates TLS at the frp server and forwards the request to the backend in plain HTTP. A cold or recently-idle tunnel can sometimes leak a backend HTTP response into the client-side TLS handshake, which produces the `ERR_SSL_PROTOCOL_ERROR` or `Unable to parse TLS packet header` error you see on a first page load.

## What is happening

* The browser starts a TLS handshake on port 443. Before the handshake finishes, the frp client opens or resumes the TCP connection to the HTTP backend.
* If the backend replies immediately (for example with a 301/302 redirect or a health-check response) while frp is still framing the TLS session, those plain HTTP bytes are relayed to the browser.
* Because the browser is still expecting TLS handshake bytes, it interprets the plain HTTP data as a malformed TLS record and reports `ERR_SSL_PROTOCOL_ERROR`. The Android app logs the same condition as `SSLException: Unable to parse TLS packet header`.
* On refresh, the previous attempt has warmed the tunnel and the backend connection is already synchronized with the TLS session, so the handshake completes and the page loads normally.

## Mitigations

* Keep the tunnel warm by sending a lightweight HTTPS probe on an interval so the backend connection is already established before user traffic arrives.
* Add a small retry or backoff on TLS packet header failures in clients (as the mobile app interceptor now does) to automatically replay the first request.
* Ensure the backend does not emit immediate redirects or banners before the TLS layer finishes; for example, disable eager HTTP banners and prefer late responses behind the proxy.
