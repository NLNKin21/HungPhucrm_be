package com.hungphu.crm.features.notification.mapper;

import com.hungphu.crm.features.notification.dto.NotificationResponse;
import com.hungphu.crm.features.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "read", source = "read")
    NotificationResponse toResponse(Notification notification);
}
