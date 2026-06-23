package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import com.hungphu.crm.features.maintenance.entity.MaintenanceSchedule;
import com.hungphu.crm.features.maintenance.mapper.MaintenanceMapper;
import com.hungphu.crm.features.maintenance.repository.MaintenanceContractRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceEvidenceRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceScheduleRepository;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.MaintenanceStatus;
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
import java.time.temporal.ChronoUnit;
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

        // ── Thêm mới: Check và ghi nhận hoàn thành trễ ──
        LocalDate today = LocalDate.now();
        if (schedule.getScheduledDate().isBefore(today)) {
            schedule.setCompletedLate(true);
            schedule.setDaysLate((int) ChronoUnit.DAYS.between(schedule.getScheduledDate(), today));
            log.warn("Schedule {} completed late ({} days)", scheduleId, schedule.getDaysLate());
        }

        // Lưu notes nếu có
        if (notes != null && !notes.isBlank()) {
            schedule.setNotes(notes.trim());
        }
        // ─────────────────────────────────────────────────

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

    private MaintenanceContract buildContract(CreateContractRequest request, Customer customer) {
        MaintenanceContract contract = new MaintenanceContract();
        contract.setCustomer(customer);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setCycleMonths(request.getCycleMonths() != null 
                ? request.getCycleMonths() : 2);
        return contract;
    }

    private void generateSchedules(MaintenanceContract contract) {
        int cycle = contract.getCycleMonths();
        LocalDate current = contract.getStartDate().plusMonths(cycle); 
        
        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setContract(contract);
            schedule.setScheduledDate(current);
            schedule.setAssignedTo(contract.getAssignedTo());
            contract.getSchedules().add(schedule);
            current = current.plusMonths(cycle);
        }
    }

    private MaintenanceContract findContractOrThrow(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng bảo trì", id));
    }

    @Override
    @Transactional
    public ContractResponse regenerateSchedules(UUID contractId) {
        MaintenanceContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng bảo trì", contractId));
        
        // Xóa tất cả schedules cũ
        contract.getSchedules().clear();
        contractRepository.save(contract); // flush để xóa orphans
        
        // Tạo lại schedules mới với logic đúng
        generateSchedules(contract);
        
        MaintenanceContract saved = contractRepository.save(contract);
        log.info("Regenerated {} schedules for contract {} (cycle: {} months)", 
                saved.getSchedules().size(), contractId, saved.getCycleMonths());
        
        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse renewContract(UUID contractId,
                                        RenewContractRequest request,
                                        UserDetailsImpl currentUser) {
        MaintenanceContract contract = findContractOrThrow(contractId);

        // Validate: newEndDate phải sau endDate hiện tại
        if (!request.getNewEndDate().isAfter(contract.getEndDate())) {
            throw new BusinessException(
                    "Ngày gia hạn phải sau ngày kết thúc hiện tại (" + contract.getEndDate() + ")",
                    HttpStatus.BAD_REQUEST, "MAINT_007");
        }

        LocalDate oldEndDate = contract.getEndDate();

        // Update endDate
        contract.setEndDate(request.getNewEndDate());

        // Update cycleMonths nếu có
        if (request.getCycleMonths() != null) {
            contract.setCycleMonths(request.getCycleMonths());
        }

        // Update assignedTo nếu có
        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        // Reset status về MOI
        contract.setStatus(MaintenanceStatus.MOI);

        // Tạo thêm schedules mới từ oldEndDate đến newEndDate
        generateAdditionalSchedules(contract, oldEndDate);

        MaintenanceContract saved = contractRepository.save(contract);

        log.info("Contract {} renewed by user {}: {} → {}, {} new schedules",
                contractId, currentUser.getId(),
                oldEndDate, request.getNewEndDate(),
                saved.getSchedules().size());

        return maintenanceMapper.toContractResponse(saved);
    }

    // ── Private helper ──

    private void generateAdditionalSchedules(MaintenanceContract contract, LocalDate fromDate) {
        int cycle = contract.getCycleMonths();

        // Tìm ngày của schedule cuối cùng (đã có)
        LocalDate lastScheduleDate = contract.getSchedules().stream()
                .map(MaintenanceSchedule::getScheduledDate)
                .max(LocalDate::compareTo)
                .orElse(fromDate);

        // Bắt đầu tạo từ schedule tiếp theo
        LocalDate current = lastScheduleDate.plusMonths(cycle);

        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setContract(contract);
            schedule.setScheduledDate(current);
            schedule.setAssignedTo(contract.getAssignedTo());
            contract.getSchedules().add(schedule);
            current = current.plusMonths(cycle);
        }
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID contractId, 
                                            UpdateContractRequest request,
                                            UserDetailsImpl currentUser) {
        MaintenanceContract contract = findContractOrThrow(contractId);
        
        // Validate dates
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu",
                    HttpStatus.BAD_REQUEST, "MAINT_003");
        }

        boolean needRegenerateSchedules = false;

        // Check if date or cycle changed
        if (!contract.getStartDate().equals(request.getStartDate()) ||
            !contract.getEndDate().equals(request.getEndDate()) ||
            !contract.getCycleMonths().equals(request.getCycleMonths())) {
            needRegenerateSchedules = true;
        }

        // Update basic fields
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        if (request.getCycleMonths() != null) {
            contract.setCycleMonths(request.getCycleMonths());
        }

        // Update assignedTo
        if (request.getAssignedTo() != null) {
            User assignee = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", request.getAssignedTo()));
            contract.setAssignedTo(assignee);
        } else {
            contract.setAssignedTo(null);
        }

        // Regenerate schedules if needed
        if (needRegenerateSchedules) {
            regenerateContractSchedules(contract);
        } else {
            // Only update assignedTo for pending schedules
            updatePendingSchedulesAssignee(contract);
        }

        MaintenanceContract saved = contractRepository.save(contract);
        log.info("Contract {} updated by user {}", contractId, currentUser.getId());

        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContract(UUID contractId) {
        MaintenanceContract contract = findContractOrThrow(contractId);
        
        // Check if has completed schedules
        long completedCount = contract.getSchedules().stream()
                .filter(s -> s.getStatus() == ScheduleStatus.HOAN_THANH)
                .count();
        
        if (completedCount > 0) {
            throw new BusinessException(
                    "Không thể xóa hợp đồng đã có " + completedCount + " lịch hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_006");
        }

        contractRepository.delete(contract);
        log.info("Contract {} deleted", contractId);
    }

    // ── Private helpers ──

    private void regenerateContractSchedules(MaintenanceContract contract) {
        // Giữ lại schedules đã hoàn thành
        List<MaintenanceSchedule> completedSchedules = contract.getSchedules().stream()
                .filter(s -> s.getStatus() == ScheduleStatus.HOAN_THANH)
                .toList();

        // Xóa tất cả schedules pending/overdue
        contract.getSchedules().removeIf(s -> s.getStatus() != ScheduleStatus.HOAN_THANH);

        // Tìm ngày bắt đầu để tạo schedules mới
        LocalDate lastCompletedDate = completedSchedules.stream()
                .map(MaintenanceSchedule::getScheduledDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Tạo schedules mới
        int cycle = contract.getCycleMonths();
        LocalDate current;
        
        if (lastCompletedDate != null) {
            // Bắt đầu từ schedule tiếp theo sau ngày hoàn thành cuối cùng
            current = lastCompletedDate.plusMonths(cycle);
        } else {
            // Không có schedule hoàn thành → bắt đầu từ startDate + cycle
            current = contract.getStartDate().plusMonths(cycle);
        }

        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setContract(contract);
            schedule.setScheduledDate(current);
            schedule.setAssignedTo(contract.getAssignedTo());
            contract.getSchedules().add(schedule);
            current = current.plusMonths(cycle);
        }

        log.info("Regenerated schedules for contract {}: {} completed kept, {} new created",
                contract.getId(), completedSchedules.size(), 
                contract.getSchedules().size() - completedSchedules.size());
    }

    private void updatePendingSchedulesAssignee(MaintenanceContract contract) {
        contract.getSchedules().stream()
                .filter(s -> s.getStatus() == ScheduleStatus.CHO_THUC_HIEN || 
                            s.getStatus() == ScheduleStatus.QUA_HAN)
                .forEach(s -> s.setAssignedTo(contract.getAssignedTo()));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceStatsResponse getStats() {
        List<MaintenanceContract> contracts = contractRepository.findAll();
        
        long totalContracts = contracts.size();
        long activeContracts = contracts.stream()
                .filter(c -> c.getStatus() == MaintenanceStatus.MOI).count();
        long expiringContracts = contracts.stream()
                .filter(c -> c.getStatus() == MaintenanceStatus.SAP_HET_HAN).count();
        long expiredContracts = contracts.stream()
                .filter(c -> c.getStatus() == MaintenanceStatus.HET_HAN).count();

        // Schedule stats
        List<Object[]> statusCounts = scheduleRepository.countByStatus();
        long pendingSchedules = 0, overdueSchedules = 0, completedSchedules = 0;
        long totalSchedules = 0;
        
        for (Object[] row : statusCounts) {
            ScheduleStatus status = (ScheduleStatus) row[0];
            long count = (Long) row[1];
            totalSchedules += count;
            switch (status) {
                case CHO_THUC_HIEN -> pendingSchedules = count;
                case QUA_HAN -> overdueSchedules = count;
                case HOAN_THANH -> completedSchedules = count;
            }
        }

        long completedLateCount = scheduleRepository.countCompletedLate();

        // Upcoming & overdue lists
        LocalDate today = LocalDate.now();
        List<ScheduleResponse> upcomingSchedules = scheduleRepository
                .findUpcomingSchedules(today, today.plusDays(7))
                .stream()
                .map(maintenanceMapper::toScheduleResponse)
                .toList();

        List<ScheduleResponse> overdueList = scheduleRepository
                .findAllOverdue()
                .stream()
                .map(maintenanceMapper::toScheduleResponse)
                .toList();

        return MaintenanceStatsResponse.builder()
                .totalContracts(totalContracts)
                .activeContracts(activeContracts)
                .expiringContracts(expiringContracts)
                .expiredContracts(expiredContracts)
                .totalSchedules(totalSchedules)
                .pendingSchedules(pendingSchedules)
                .overdueSchedules(overdueSchedules)
                .completedSchedules(completedSchedules)
                .completedLateCount(completedLateCount)
                .upcomingSchedules(upcomingSchedules)
                .overdueList(overdueList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getOverdueSchedules() {
        return scheduleRepository.findAllOverdue()
                .stream()
                .map(maintenanceMapper::toScheduleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getUpcomingSchedules(int days) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findUpcomingSchedules(today, today.plusDays(days))
                .stream()
                .map(maintenanceMapper::toScheduleResponse)
                .toList();
    }
}