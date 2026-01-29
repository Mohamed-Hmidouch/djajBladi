package org.example.djajbladibackend.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Wraps the response so X-Cache-Status is set before the first byte is written (before commit).
 */
public class CacheStatusResponseWrapper extends HttpServletResponseWrapper {

    private boolean headerSet;
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public CacheStatusResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    private void setCacheStatusHeaderIfNeeded() {
        if (headerSet) return;
        headerSet = true;
        String status = CacheHitTrackingCache.getStatus();
        if (status != null) {
            setHeader("X-Cache-Status", status);
        }
        CacheHitTrackingCache.clearStatus();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) throw new IllegalStateException("getWriter() already called");
        if (outputStream == null) {
            setCacheStatusHeaderIfNeeded();
            outputStream = getResponse().getOutputStream();
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) throw new IllegalStateException("getOutputStream() already called");
        if (writer == null) {
            setCacheStatusHeaderIfNeeded();
            writer = new PrintWriter(new OutputStreamWriter(getResponse().getOutputStream(), StandardCharsets.UTF_8));
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) writer.flush();
        if (outputStream != null) outputStream.flush();
    }
}
