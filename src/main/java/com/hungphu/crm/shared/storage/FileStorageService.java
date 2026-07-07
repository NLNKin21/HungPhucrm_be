package com.hungphu.crm.shared.storage;

import com.hungphu.crm.shared.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Upload file và trả về public URL
     *
     * @param file      file upload
     * @param directory thư mục con (ví dụ: "maintenance/evidence/taskId")
     * @return public URL của file
     */
    String store(MultipartFile file, String directory);

    /**
     * Xoá file theo URL hoặc key
     *
     * @param fileUrl URL hoặc key của file
     */
    void delete(String fileUrl);

    /**
     * Xác định loại file
     */
    FileType resolveFileType(MultipartFile file);
}