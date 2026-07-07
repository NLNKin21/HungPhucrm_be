package com.hungphu.crm.shared.storage;

import com.hungphu.crm.shared.config.StorageProperties;
import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements FileStorageService {

    private final StorageProperties storageProperties;
    private final Path rootLocation;

    public LocalStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.rootLocation = Paths.get(storageProperties.getLocal().getUploadDir());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không được để trống",
                    HttpStatus.BAD_REQUEST, "STORAGE_002");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String filename = UUID.randomUUID() + "." + extension;

            Path dirPath = rootLocation.resolve(directory);
            Files.createDirectories(dirPath);

            Path targetPath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = directory + "/" + filename;
            String publicUrl = storageProperties.getLocal().getPublicUrl();
            String fileUrl = publicUrl.endsWith("/")
                    ? publicUrl + relativePath
                    : publicUrl + "/" + relativePath;

            log.info("File stored locally: path={}, size={}", targetPath, file.getSize());
            return fileUrl;

        } catch (IOException e) {
            throw new BusinessException(
                    "Lỗi lưu file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_001");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String publicUrl = storageProperties.getLocal().getPublicUrl();
            String relativePath = fileUrl.startsWith(publicUrl)
                    ? fileUrl.substring(publicUrl.length())
                    : fileUrl;
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);

            Path filePath = rootLocation.resolve(relativePath);
            Files.deleteIfExists(filePath);
            log.info("File deleted locally: {}", filePath);

        } catch (IOException e) {
            log.warn("Failed to delete local file: {}", e.getMessage());
        }
    }

    @Override
    public FileType resolveFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return FileType.IMAGE;

        if (contentType.startsWith("image/")) return FileType.IMAGE;
        if (contentType.startsWith("video/")) return FileType.VIDEO;
        if (contentType.equals("application/pdf")) return FileType.PDF;

        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".mp4") || lower.endsWith(".mov")
                    || lower.endsWith(".avi") || lower.endsWith(".webm"))
                return FileType.VIDEO;
            if (lower.endsWith(".pdf")) return FileType.PDF;
        }

        return FileType.IMAGE;
    }

    private String getExtension(String filename) {
        if (filename == null) return "bin";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "bin";
    }
}