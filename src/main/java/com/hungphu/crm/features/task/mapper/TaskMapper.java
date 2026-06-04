package com.hungphu.crm.features.task.mapper;

import com.hungphu.crm.features.task.dto.TaskResponse;
import com.hungphu.crm.features.task.entity.Task;
import com.hungphu.crm.features.task.entity.TaskEvidence;
import com.hungphu.crm.features.task.entity.TaskMember;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.TaskMemberRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "assignedBy", source = "assignedBy")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "supervisor", source = "supervisor")
    @Mapping(target = "members", expression = "java(toMemberInfos(task.getMembers()))")
    TaskResponse toResponse(Task task);

    default TaskResponse.UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return TaskResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    default List<TaskResponse.UserInfo> toMemberInfos(List<TaskMember> members) {
        if (members == null) return List.of();
        return members.stream()
                .filter(m -> m.getMemberRole() == TaskMemberRole.MEMBER)
                .map(TaskMember::getUser)
                .map(this::toUserInfo)
                .toList();
    }

    default TaskResponse.EvidenceInfo toEvidenceInfo(TaskEvidence evidence) {
        return TaskResponse.EvidenceInfo.builder()
                .id(evidence.getId())
                .fileUrl(evidence.getFileUrl())
                .fileType(evidence.getFileType())
                .uploadedAt(evidence.getUploadedAt())
                .build();
    }
}