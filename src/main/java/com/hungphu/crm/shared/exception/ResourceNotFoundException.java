package com.hungphu.crm.shared.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " không tồn tại: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
