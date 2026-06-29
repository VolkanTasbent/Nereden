package com.nereden.api.infrastructure.market;

import com.nereden.api.infrastructure.ai.VisionImageLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LensPublicImageResolver {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; Nereden/1.0)";

    private final VisionImageLoader imageLoader;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String resolvePublicUrl(String imageUrl) {
        if (isPublicImageUrl(imageUrl)) {
            return imageUrl;
        }

        try {
            final VisionImageLoader.LoadedImage image = imageLoader.load(imageUrl);
            final byte[] bytes = Base64.getDecoder().decode(image.base64Data());
            final String filename = "analysis" + extensionForMime(image.mimeType());
            final String mimeType = image.mimeType() != null ? image.mimeType() : "image/jpeg";

            for (UploadTarget target : List.of(
                    new UploadTarget(
                            "https://litterbox.catbox.moe/resources/internals/upload.php",
                            "fileToUpload",
                            filename,
                            mimeType,
                            "reqtype", "fileupload",
                            "time", "1h"
                    ),
                    new UploadTarget(
                            "https://catbox.moe/user/api.php",
                            "fileToUpload",
                            filename,
                            mimeType,
                            "reqtype", "fileupload",
                            null, null
                    )
            )) {
                final String publicUrl = upload(target, bytes);
                if (publicUrl != null) {
                    log.info("Published local image for Google Lens via {}: {}", target.endpoint(), publicUrl);
                    return publicUrl;
                }
            }

            log.warn("Temporary image upload did not return a public URL");
            return null;
        } catch (Exception ex) {
            log.warn("Could not publish local image for Google Lens", ex);
            return null;
        }
    }

    private String upload(UploadTarget target, byte[] bytes) {
        try {
            final String boundary = "----NeredenBoundary" + UUID.randomUUID();
            final byte[] body = buildMultipartBody(boundary, target, bytes);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(target.endpoint()))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.debug("Image upload failed for {}: HTTP {}", target.endpoint(), response.statusCode());
                return null;
            }

            return normalizeUploadedUrl(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.debug("Image upload failed for {}: {}", target.endpoint(), ex.getMessage());
            return null;
        }
    }

    private byte[] buildMultipartBody(String boundary, UploadTarget target, byte[] fileBytes) {
        final String lineBreak = "\r\n";
        final StringBuilder text = new StringBuilder();

        appendField(text, boundary, lineBreak, target.extraFieldName1(), target.extraFieldValue1());
        appendField(text, boundary, lineBreak, target.extraFieldName2(), target.extraFieldValue2());

        text.append("--").append(boundary).append(lineBreak);
        text.append("Content-Disposition: form-data; name=\"")
                .append(target.fileFieldName())
                .append("\"; filename=\"")
                .append(target.filename())
                .append("\"")
                .append(lineBreak);
        text.append("Content-Type: ").append(target.mimeType()).append(lineBreak).append(lineBreak);

        final byte[] prefix = text.toString().getBytes(StandardCharsets.UTF_8);
        final byte[] suffix = (lineBreak + "--" + boundary + "--" + lineBreak).getBytes(StandardCharsets.UTF_8);
        final byte[] body = new byte[prefix.length + fileBytes.length + suffix.length];
        System.arraycopy(prefix, 0, body, 0, prefix.length);
        System.arraycopy(fileBytes, 0, body, prefix.length, fileBytes.length);
        System.arraycopy(suffix, 0, body, prefix.length + fileBytes.length, suffix.length);
        return body;
    }

    private void appendField(
            StringBuilder text,
            String boundary,
            String lineBreak,
            String name,
            String value
    ) {
        if (name == null || value == null) {
            return;
        }
        text.append("--").append(boundary).append(lineBreak);
        text.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(lineBreak);
        text.append(lineBreak).append(value).append(lineBreak);
    }

    private String normalizeUploadedUrl(String response) {
        if (response == null) {
            return null;
        }
        final String trimmed = response.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed.split("\\s+")[0];
        }
        return null;
    }

    private boolean isPublicImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        final String lower = imageUrl.toLowerCase();
        return !lower.contains("localhost")
                && !lower.contains("127.0.0.1")
                && !lower.contains("10.0.2.2")
                && (lower.startsWith("http://") || lower.startsWith("https://"));
    }

    private String extensionForMime(String mimeType) {
        if ("image/png".equals(mimeType)) {
            return ".png";
        }
        if ("image/webp".equals(mimeType)) {
            return ".webp";
        }
        return ".jpg";
    }

    private record UploadTarget(
            String endpoint,
            String fileFieldName,
            String filename,
            String mimeType,
            String extraFieldName1,
            String extraFieldValue1,
            String extraFieldName2,
            String extraFieldValue2
    ) {}
}
