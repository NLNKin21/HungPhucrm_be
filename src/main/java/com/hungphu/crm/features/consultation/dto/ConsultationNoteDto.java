package com.hungphu.crm.features.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

// ── Request ───────────────────────────────────────────────────────────────────

// Dùng inner-class để gom gọn trong 1 file; tách ra nếu codebase lớn hơn.

public class ConsultationNoteDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Nội dung ghi chú không được trống")
        @Size(max = 2000, message = "Ghi chú tối đa 2000 ký tự")
        private String content;
    }

    @Getter @Builder
    public static class Response {
        private UUID   id;
        private String content;
        private AuthorInfo author;
        private LocalDateTime createdAt;

        @Getter @Builder
        public static class AuthorInfo {
            private UUID   id;
            private String fullName;
        }
    }
}