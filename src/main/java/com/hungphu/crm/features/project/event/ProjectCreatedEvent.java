package com.hungphu.crm.features.project.event;

import com.hungphu.crm.features.project.entity.Project;
import lombok.Getter;

@Getter
public class ProjectCreatedEvent {
    private final Project project;

    public ProjectCreatedEvent(Project project) {
        this.project = project;
    }
}
