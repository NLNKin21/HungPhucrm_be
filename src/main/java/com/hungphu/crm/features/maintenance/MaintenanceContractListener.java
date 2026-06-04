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

/**
 * Lắng nghe ProjectStatusChangedEvent.
 * Khi project chuyển sang BAO_TRI → tự động tạo hợp đồng bảo trì
 * với thời hạn mặc định 1 năm kể từ ngày bàn giao.
 *
 * Nếu project đó đã có hợp đồng bảo trì rồi thì bỏ qua (idempotent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceContractListener {

    private final MaintenanceService            maintenanceService;
    private final MaintenanceContractRepository contractRepository;

    // Thời hạn hợp đồng bảo trì mặc định (tháng)
    private static final int DEFAULT_CONTRACT_MONTHS = 12;

    @EventListener
    @Transactional
    public void onProjectStatusChanged(ProjectStatusChangedEvent event) {
        // Chỉ xử lý khi chuyển sang BAO_TRI
        if (event.getToStatus() != ProjectStatus.BAO_TRI) return;

        Project project = event.getProject();

        // Idempotent: nếu đã có hợp đồng cho project này thì bỏ qua
        boolean alreadyExists = contractRepository.existsByProjectId(project.getId());
        if (alreadyExists) {
            log.info("Maintenance contract already exists for project {}, skipping auto-create", project.getId());
            return;
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = startDate.plusMonths(DEFAULT_CONTRACT_MONTHS);

        CreateContractRequest request = new CreateContractRequest();
        request.setProjectId(project.getId());
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        // Giám sát của dự án trở thành người phụ trách bảo trì (nếu có)
        if (project.getSupervisor() != null) {
            request.setAssignedTo(project.getSupervisor().getId());
        }

        // Dùng system user — truyền null, service sẽ handle
        maintenanceService.createContractInternal(request);

        log.info("Auto-created maintenance contract for project {} ({} → {})",
                project.getId(), startDate, endDate);
    }
}