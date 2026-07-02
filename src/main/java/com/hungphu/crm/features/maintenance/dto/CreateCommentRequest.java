package com.hungphu.crm.features.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung bình luận không được trống")
    private String content;

    private UUID parentId; // NULL = comment gốc, có giá trị = reply
}