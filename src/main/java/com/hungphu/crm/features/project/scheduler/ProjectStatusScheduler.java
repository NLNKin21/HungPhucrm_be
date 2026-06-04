package com.hungphu.crm.features.project.scheduler;

import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.shared.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Tự động chuyển project sang HET_HAN khi:
 * - Project đang ở trạng thái BAO_TRI
 * - Tất cả hợp đồng bảo trì liên quan đã hết hạn (end_date < today)
 * - Không có hợp đồng nào đang MOI hoặc SAP_HET_HAN (tức không tái ký)
 *
 * Chạy mỗi ngày lúc 1:00 sáng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectStatusScheduler {

    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 1 * * *") // 01:00 mỗi ngày
    @Transactional
    public void autoExpireProjects() {
        LocalDate today = LocalDate.now();

        List<Project> candidates = projectRepository
                .findProjectsWithExpiredMaintenance(ProjectStatus.BAO_TRI, today);

        for (Project project : candidates) {
            project.setProjectStatus(ProjectStatus.HET_HAN);
            projectRepository.save(project);

            log.info("Project {} auto-expired: all maintenance contracts ended, no renewal",
                    project.getId());
        }

        if (!candidates.isEmpty()) {
            log.info("Auto-expired {} projects", candidates.size());
        }
    }
}
