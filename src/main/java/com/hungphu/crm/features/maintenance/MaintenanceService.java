package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    List<ContractResponse> findAllContracts();

    ContractResponse findContractById(UUID id);

    ContractResponse createContract(CreateContractRequest request, UserDetailsImpl currentUser);

    ContractResponse createContractInternal(CreateContractRequest request);

    List<ScheduleResponse> findSchedulesByContract(UUID contractId);

    // files là optional — có thể null hoặc empty list
    ScheduleResponse completeSchedule(UUID scheduleId, List<MultipartFile> files,
                                      String notes, UserDetailsImpl currentUser);
}