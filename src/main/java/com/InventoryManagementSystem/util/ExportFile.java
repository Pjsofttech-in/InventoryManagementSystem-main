package com.InventoryManagementSystem.util;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** A generated export, ready to be streamed to the browser as a download. */
public record ExportFile(byte[] content, MediaType mediaType, String fileName) {

    public ResponseEntity<byte[]> toResponse() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .cacheControl(CacheControl.noStore())
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }
}
