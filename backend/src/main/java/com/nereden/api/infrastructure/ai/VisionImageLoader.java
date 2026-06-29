package com.nereden.api.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@Component
public class VisionImageLoader {

    private final Path uploadPath;
    private final RestClient restClient;

    public VisionImageLoader(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.restClient = RestClient.create();
    }

    public LoadedImage load(String imageUrl) {
        final String filename = extractFilename(imageUrl);
        if (filename != null) {
            final Path localFile = uploadPath.resolve(filename);
            if (Files.exists(localFile)) {
                try {
                    final byte[] bytes = Files.readAllBytes(localFile);
                    return new LoadedImage(detectMimeType(filename), Base64.getEncoder().encodeToString(bytes));
                } catch (Exception ex) {
                    log.warn("Local image read failed for {}, falling back to HTTP", filename, ex);
                }
            }
        }

        try {
            final byte[] bytes = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException("Görsel indirilemedi.");
            }
            return new LoadedImage(detectMimeType(imageUrl), Base64.getEncoder().encodeToString(bytes));
        } catch (Exception ex) {
            log.error("Image download failed for {}", imageUrl, ex);
            throw new IllegalStateException("Görsel yüklenemedi.");
        }
    }

    private String extractFilename(String imageUrl) {
        final int filesIndex = imageUrl.indexOf("/files/");
        if (filesIndex < 0) {
            return null;
        }
        final String filename = imageUrl.substring(filesIndex + "/files/".length());
        return filename.contains("/") ? null : filename;
    }

    private String detectMimeType(String value) {
        final String lower = value.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    public record LoadedImage(String mimeType, String base64Data) {}
}
