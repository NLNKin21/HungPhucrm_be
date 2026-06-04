package com.hungphu.crm.features.project.event;

import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.shared.enums.ProjectStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired mỗi khi constructionStatus của Project thay đổi.
 * Dùng ApplicationEvent thay vì gọi trực tiếp MaintenanceService
 * để tránh circular dependency giữa project ↔ maintenance module.
 */
@Getter
public class ProjectStatusChangedEvent extends ApplicationEvent {

    private final Project      project;
    private final ProjectStatus fromStatus;
    private final ProjectStatus toStatus;

    public ProjectStatusChangedEvent(Object source,
                                     Project project,
                                     ProjectStatus fromStatus,
                                     ProjectStatus toStatus) {
        super(source);
        this.project    = project;
        this.fromStatus = fromStatus;
        this.toStatus   = toStatus;
    }
}