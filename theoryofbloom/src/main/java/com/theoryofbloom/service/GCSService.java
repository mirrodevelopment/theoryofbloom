package com.theoryofbloom.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class GCSService {

    private final Storage storage;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    // ── Validation constants ──────────────────────────────────────────────────
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB

    // ── Extension map (derived from MIME type) ────────────────────────────────
    private static final java.util.Map<String, String> MIME_TO_EXT =
            java.util.Map.of(
                    "image/jpeg", ".jpg",
                    "image/png",  ".png",
                    "image/webp", ".webp"
            );

    public GCSService(Storage storage) {
        this.storage = storage;
    }

    /**
     * Upload a file to a specific GCS folder.
     *
     * <p>Validates file type (jpeg/png/webp only) and size (≤ 5 MB).
     * Generates a UUID-based filename to avoid collisions and
     * prevent predictable URLs.
     *
     * @param file   the multipart file to upload
     * @param folder e.g. "products", "books", "banners", "users", "blogs"
     * @return public GCS URL
     * @throws RuntimeException if type or size validation fails
     * @throws IOException      if the GCS write fails
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {

        // 1 — File type validation
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException(
                    "Invalid file type: " + contentType +
                    ". Allowed types: JPEG, PNG, WEBP."
            );
        }

        // 2 — File size validation
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    "File size exceeds 5 MB limit (" +
                    (file.getSize() / (1024 * 1024)) + " MB received)."
            );
        }

        // 3 — UUID-based safe filename
        String ext = MIME_TO_EXT.getOrDefault(contentType, ".jpg");
        String cleanFolder = folder.endsWith("/") ? folder : folder + "/";
        String fileName = cleanFolder + UUID.randomUUID() + ext;

        // 4 — Upload to GCS
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, file.getBytes());

        return "https://storage.googleapis.com/"
                + bucketName
                + "/"
                + fileName;
    }

    /**
     * Convenience overload — defaults to "products/" folder.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "products");
    }
}

