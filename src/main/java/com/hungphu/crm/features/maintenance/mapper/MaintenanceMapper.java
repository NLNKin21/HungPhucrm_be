package com.hungphu.crm.features.maintenance.mapper;

import com.hungphu.crm.features.maintenance.dto.ContractResponse;
import com.hungphu.crm.features.maintenance.dto.ScheduleResponse;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {

    @Mapping(target = "project.id", source = "project.id")
    @Mapping(target = "project.name", source = "project.name")
    @Mapping(target = "customer.id", source = "customer.id")
    @Mapping(target = "customer.fullName", source = "customer.fullName")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "schedulesGenerated", expression = "java(contract.getSchedules().size())")
    ContractResponse toContractResponse(MaintenanceContract contract);

    @Mapping(target = "assignedTo", source = "assignedTo")
    ScheduleResponse toScheduleResponse(MaintenanceSchedule schedule);

    default ScheduleResponse.UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return ScheduleResponse.UserInfo.builder().id(user.getId()).fullName(user.getFullName()).build();
    }

    default ContractResponse.UserInfo toContractUserInfo(User user) {
        if (user == null) return null;
        return ContractResponse.UserInfo.builder().id(user.getId()).fullName(user.getFullName()).build();
    }
}
