package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.CreateContractRequest;
import com.hungphu.crm.features.maintenance.repository.MaintenanceContractRepository;
import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.project.event.ProjectStatusChangedEvent;
import com.hungphu.crm.shared.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceContractListener {

    private final MaintenanceService            maintenanceService;
    private final MaintenanceContractRepository contractRepository;

    private static final int DEFAULT_CONTRACT_MONTHS = 12;

    @EventListener
    @Transactional
    public void onProjectStatusChanged(ProjectStatusChangedEvent event) {
        if (event.getToStatus() != ProjectStatus.BAO_TRI) return;

        Project project = event.getProject();

        boolean alreadyExists = contractRepository.existsByProjectId(project.getId());
        if (alreadyExists) {
            log.info("Maintenance contract already exists for project {}, skipping", project.getId());
            return;
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = startDate.plusMonths(DEFAULT_CONTRACT_MONTHS);

        CreateContractRequest request = new CreateContractRequest();
        request.setCustomerId(project.getCustomer().getId());  // ← Thêm customerId
        request.setProjectId(project.getId());
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setCycleMonths(2);

        if (project.getSupervisor() != null) {
            request.setAssignedTo(project.getSupervisor().getId());
        }

        maintenanceService.createContractInternal(request);

        log.info("Auto-created maintenance contract for project {} ({} → {})",
                project.getId(), startDate, endDate);
    }
}