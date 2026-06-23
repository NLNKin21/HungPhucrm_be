package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.maintenance.dto.ContractResponse;
import com.hungphu.crm.features.maintenance.dto.ScheduleResponse;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {

    @Mapping(target = "project.id", source = "project.id")
    @Mapping(target = "project.name", source = "project.name")
    @Mapping(target = "customer.id", source = "customer.id")
    @Mapping(target = "customer.fullName", source = "customer.fullName")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "cycleMonths", source = "cycleMonths")
    @Mapping(target = "schedulesGenerated", expression = "java(contract.getSchedules().size())")
    ContractResponse toContractResponse(MaintenanceContract contract);

    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "evidences", source = "evidences")
    @Mapping(target = "overdue", expression = "java(isOverdue(schedule))")
    ScheduleResponse toScheduleResponse(MaintenanceSchedule schedule);

    // ── Helper methods ──

    default boolean isOverdue(MaintenanceSchedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.QUA_HAN) {
            return true;
        }
        if (schedule.getStatus() == ScheduleStatus.CHO_THUC_HIEN) {
            return schedule.getScheduledDate().isBefore(LocalDate.now());
        }
        return false;
    }

    default ScheduleResponse.UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return ScheduleResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    default ContractResponse.UserInfo toContractUserInfo(User user) {
        if (user == null) return null;
        return ContractResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    default ScheduleResponse.EvidenceInfo toEvidenceInfo(MaintenanceEvidence evidence) {
        if (evidence == null) return null;
        return ScheduleResponse.EvidenceInfo.builder()
                .id(evidence.getId())
                .fileUrl(evidence.getFileUrl())
                .fileType(evidence.getFileType())
                .uploadedBy(toUserInfo(evidence.getUploadedBy()))
                .uploadedAt(evidence.getUploadedAt())
                .build();
    }

    default List<ScheduleResponse.EvidenceInfo> toEvidenceInfoList(List<MaintenanceEvidence> evidences) {
        if (evidences == null) return null;
        return evidences.stream().map(this::toEvidenceInfo).toList();
    }
}