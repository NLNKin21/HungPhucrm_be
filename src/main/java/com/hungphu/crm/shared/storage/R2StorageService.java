package com.hungphu.crm.shared.storage;

import com.hungphu.crm.shared.config.StorageProperties;
import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
@RequiredArgsConstructor
public class R2StorageService implements FileStorageService {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/webm"
    );
    private static final Set<String> ALLOWED_DOC_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;   // 10MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;   // 50MB
    private static final long MAX_DOC_SIZE   = 10 * 1024 * 1024;   // 10MB

    @Override
    public String store(MultipartFile file, String directory) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String key = String.format("%s/%s.%s",
                directory,
                UUID.randomUUID(),
                extension
        );

        try {
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : "application/octet-stream";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.getR2().getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String publicUrl = storageProperties.getR2().getPublicUrl();
            String fileUrl = publicUrl.endsWith("/")
                    ? publicUrl + key
                    : publicUrl + "/" + key;

            log.info("File uploaded to R2: key={}, size={}, type={}",
                    key, file.getSize(), contentType);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to R2: {}", e.getMessage());
            throw new BusinessException(
                    "Lỗi upload file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_001"
            );
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageProperties.getR2().getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("File deleted from R2: key={}", key);

        } catch (Exception e) {
            log.warn("Failed to delete file from R2: {}", e.getMessage());
            // Không throw — xoá file lỗi không nên chặn business logic
        }
    }

    @Override
    public FileType resolveFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return FileType.IMAGE;

        if (contentType.startsWith("image/")) return FileType.IMAGE;
        if (contentType.startsWith("video/")) return FileType.VIDEO;
        if (contentType.equals("application/pdf")) return FileType.PDF;

        // Fallback: check extension
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".mp4") || lower.endsWith(".mov")
                    || lower.endsWith(".avi") || lower.endsWith(".webm")) {
                return FileType.VIDEO;
            }
            if (lower.endsWith(".pdf")) return FileType.PDF;
        }

        return FileType.IMAGE;
    }

    // ══════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không được để trống",
                    HttpStatus.BAD_REQUEST, "STORAGE_002");
        }

        String contentType = file.getContentType();
        long size = file.getSize();

        if (contentType != null) {
            if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
                if (size > MAX_IMAGE_SIZE) {
                    throw new BusinessException(
                            "Ảnh không được vượt quá 10MB",
                            HttpStatus.BAD_REQUEST, "STORAGE_003");
                }
                return;
            }

            if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
                if (size > MAX_VIDEO_SIZE) {
                    throw new BusinessException(
                            "Video không được vượt quá 50MB",
                            HttpStatus.BAD_REQUEST, "STORAGE_004");
                }
                return;
            }

            if (ALLOWED_DOC_TYPES.contains(contentType)) {
                if (size > MAX_DOC_SIZE) {
                    throw new BusinessException(
                            "Tài liệu không được vượt quá 10MB",
                            HttpStatus.BAD_REQUEST, "STORAGE_005");
                }
                return;
            }
        }

        throw new BusinessException(
                "Loại file không được hỗ trợ. Cho phép: ảnh, video, PDF, Word, Excel",
                HttpStatus.BAD_REQUEST, "STORAGE_006");
    }

    private String getExtension(String filename) {
        if (filename == null) return "bin";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "bin";
    }

    private String extractKeyFromUrl(String fileUrl) {
        String publicUrl = storageProperties.getR2().getPublicUrl();
        if (fileUrl.startsWith(publicUrl)) {
            String key = fileUrl.substring(publicUrl.length());
            return key.startsWith("/") ? key.substring(1) : key;
        }
        // Nếu URL không match publicUrl, dùng toàn bộ làm key
        return fileUrl;
    }
}