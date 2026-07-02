package com.hungphu.crm.features.maintenance.dto;

import com.hungphu.crm.shared.enums.FileType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {
    private UUID id;
    private UUID parentId;
    private UserInfo user;
    private String content;
    private List<AttachmentInfo> attachments;
    private List<CommentResponse> replies;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String avatar;
    }

    @Getter
    @Builder
    public static class AttachmentInfo {
        private UUID id;
        private String fileUrl;
        private FileType fileType;
        private Long fileSize;
    }
}