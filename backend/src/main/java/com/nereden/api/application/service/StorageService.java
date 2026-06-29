package com.nereden.api.application.service;

import com.nereden.api.application.dto.storage.UploadImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private final Path uploadPath;
    private final String baseUrl;

    public StorageService(
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.base-url}") String baseUrl
    ) throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.replaceAll("/$", "");
        Files.createDirectories(this.uploadPath);
    }

    public UploadImageResponse uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz.");
        }

        final String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Desteklenmeyen dosya formatı.");
        }

        final String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        final String key = UUID.randomUUID() + extension;
        final Path target = uploadPath.resolve(key);

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException ex) {
            log.error("File upload failed", ex);
            throw new IllegalStateException("Dosya yüklenemedi.");
        }

        return UploadImageResponse.builder()
                .key(key)
                .url(baseUrl + "/" + key)
                .build();
    }
}
