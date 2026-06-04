package com.hungphu.crm.shared.storage;

import com.hungphu.crm.shared.enums.FileType;
import com.hungphu.crm.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "application/pdf");

    @Value("${app.storage.path:uploads}")
    private String storagePath;

    public String store(MultipartFile file, String folder) {
        validateFileType(file);
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        String relativePath = folder + "/" + filename;

        try {
            Path targetDir = Paths.get(storagePath).resolve(folder);
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new BusinessException("Không thể lưu file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return relativePath;
    }

    public FileType resolveFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            return FileType.IMAGE;
        }
        return FileType.PDF;
    }

    public void delete(String relativePath) {
        try {
            Path path = Paths.get(storagePath).resolve(relativePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", relativePath);
        }
    }

    private void validateFileType(MultipartFile file) {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Chỉ chấp nhận JPG, PNG, PDF", HttpStatus.BAD_REQUEST, "SYS_003");
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
