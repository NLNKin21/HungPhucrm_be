package com.hungphu.crm.features.project.dto;

import com.hungphu.crm.shared.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ProjectStatsResponse {

    private long totalTasks;
    private int progressPercent;
    private Map<TaskStatus, Long> tasksByStatus;
    private List<MemberInfo> teamMembers;

    @Getter
    @Builder
    public static class MemberInfo {
        private UUID id;
        private String fullName;
        private String email;
        private String role;
    }
}
