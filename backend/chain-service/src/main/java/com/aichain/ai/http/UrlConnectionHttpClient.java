package com.aichain.ai.http;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * LangChain4j transport based on the classic URLConnection API.
 * This avoids the Windows selector used by the JDK and Apache async clients.
 */
public final class UrlConnectionHttpClient implements HttpClient {

    private final Duration connectTimeout;
    private final Duration readTimeout;

    private UrlConnectionHttpClient(Duration connectTimeout, Duration readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public static HttpClientBuilder builder() {
        return new Builder();
    }

    @Override
    public SuccessfulHttpResponse execute(HttpRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = open(request);
            int status = connection.getResponseCode();
            byte[] body = readAll(status >= 400 ? connection.getErrorStream() : connection.getInputStream());
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("AI provider returned HTTP " + status + ": "
                        + new String(body, StandardCharsets.UTF_8));
            }
            return response(connection, status, body);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new IllegalStateException(e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
        CompletableFuture.runAsync(() -> stream(request, parser, listener));
    }

    private void stream(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
        HttpURLConnection connection = null;
        try {
            connection = open(request);
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                byte[] error = readAll(connection.getErrorStream());
                throw new IllegalStateException("AI provider returned HTTP " + status + ": "
                        + new String(error, StandardCharsets.UTF_8));
            }
            listener.onOpen(response(connection, status, new byte[0]));
            parser.parse(connection.getInputStream(), listener);
            listener.onClose();
        } catch (Throwable error) {
            listener.onError(error);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private HttpURLConnection open(HttpRequest request) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(request.url()).openConnection();
        connection.setRequestMethod(request.method().name());
        connection.setConnectTimeout((int) connectTimeout.toMillis());
        connection.setReadTimeout((int) readTimeout.toMillis());
        request.headers().forEach((name, values) -> values.forEach(value -> connection.addRequestProperty(name, value)));
        if (request.body() != null) {
            connection.setDoOutput(true);
            byte[] bytes = request.body().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
        }
        return connection;
    }

    private static SuccessfulHttpResponse response(HttpURLConnection connection, int status, byte[] body) {
        Map<String, List<String>> rawHeaders = connection.getHeaderFields();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        if (rawHeaders != null) {
            rawHeaders.forEach((name, values) -> {
                if (name != null) headers.put(name, values);
            });
        }
        return SuccessfulHttpResponse.builder()
                .statusCode(status)
                .headers(headers)
                .body(body)
                .build();
    }

    private static byte[] readAll(InputStream input) throws Exception {
        if (input == null) return new byte[0];
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private static final class Builder implements HttpClientBuilder {
        private Duration connectTimeout = Duration.ofSeconds(15);
        private Duration readTimeout = Duration.ofSeconds(120);

        @Override public Duration connectTimeout() { return connectTimeout; }
        @Override public HttpClientBuilder connectTimeout(Duration value) { connectTimeout = value; return this; }
        @Override public Duration readTimeout() { return readTimeout; }
        @Override public HttpClientBuilder readTimeout(Duration value) { readTimeout = value; return this; }
        @Override public HttpClient build() { return new UrlConnectionHttpClient(connectTimeout, readTimeout); }
    }
}
