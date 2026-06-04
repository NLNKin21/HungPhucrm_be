package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.maintenance.mapper.MaintenanceMapper;
import com.hungphu.crm.features.maintenance.repository.MaintenanceContractRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceEvidenceRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceScheduleRepository;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import com.hungphu.crm.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceContractRepository contractRepository;
    private final MaintenanceScheduleRepository scheduleRepository;
    private final MaintenanceEvidenceRepository evidenceRepository;
    private final ProjectRepository             projectRepository;
    private final UserRepository                userRepository;
    private final MaintenanceMapper             maintenanceMapper;
    private final FileStorageService            fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> findAllContracts() {
        return contractRepository.findAllWithDetails().stream()
                .map(maintenanceMapper::toContractResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse findContractById(UUID id) {
        return maintenanceMapper.toContractResponse(findContractOrThrow(id));
    }

    @Override
    @Transactional
    public ContractResponse createContract(CreateContractRequest request,
                                           UserDetailsImpl currentUser) {
        validateDates(request);
        var project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", request.getProjectId()));

        MaintenanceContract contract = buildContract(request, project.getCustomer());
        contract.setProject(project);
        contract.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));
        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        MaintenanceContract saved = contractRepository.save(contract);
        generateSchedules(saved);
        log.info("Maintenance contract created manually for project {} by {}",
                request.getProjectId(), currentUser.getId());
        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse createContractInternal(CreateContractRequest request) {
        validateDates(request);
        var project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", request.getProjectId()));

        MaintenanceContract contract = buildContract(request, project.getCustomer());
        contract.setProject(project);
        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        MaintenanceContract saved = contractRepository.save(contract);
        generateSchedules(saved);
        log.info("[AUTO] Maintenance contract created for project {} ({} → {}), {} schedules",
                request.getProjectId(), request.getStartDate(), request.getEndDate(),
                saved.getSchedules().size());
        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> findSchedulesByContract(UUID contractId) {
        findContractOrThrow(contractId);
        return scheduleRepository.findByContractIdOrderByScheduledDateAsc(contractId)
                .stream().map(maintenanceMapper::toScheduleResponse).toList();
    }

    @Override
    @Transactional
    public ScheduleResponse completeSchedule(UUID scheduleId,
                                             List<MultipartFile> files,
                                             String notes,
                                             UserDetailsImpl currentUser) {
        MaintenanceSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch bảo trì", scheduleId));

        if (schedule.getStatus() == ScheduleStatus.HOAN_THANH) {
            throw new BusinessException("Lịch bảo trì này đã được hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_005");
        }

        // Lưu từng file minh chứng (nếu có)
        if (!CollectionUtils.isEmpty(files)) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String relativePath = fileStorageService.store(
                        file, "maintenance/" + scheduleId);
                MaintenanceEvidence evidence = new MaintenanceEvidence();
                evidence.setSchedule(schedule);
                evidence.setFileUrl(relativePath);
                evidence.setFileType(fileStorageService.resolveFileType(file));
                evidence.setUploadedBy(userRepository.getReferenceById(currentUser.getId()));
                evidenceRepository.save(evidence);
            }
        }

        schedule.setStatus(ScheduleStatus.HOAN_THANH);
        schedule.setCompletedAt(LocalDateTime.now());
        return maintenanceMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateDates(CreateContractRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu",
                    HttpStatus.BAD_REQUEST, "MAINT_003");
        }
    }

    private MaintenanceContract buildContract(CreateContractRequest request,
                                              com.hungphu.crm.features.customer.entity.Customer customer) {
        MaintenanceContract contract = new MaintenanceContract();
        contract.setCustomer(customer);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        return contract;
    }

    private void generateSchedules(MaintenanceContract contract) {
        LocalDate current = contract.getStartDate();
        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setContract(contract);
            schedule.setScheduledDate(current);
            schedule.setAssignedTo(contract.getAssignedTo());
            contract.getSchedules().add(schedule);
            current = current.plusMonths(2);
        }
    }

    private MaintenanceContract findContractOrThrow(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng bảo trì", id));
    }
}