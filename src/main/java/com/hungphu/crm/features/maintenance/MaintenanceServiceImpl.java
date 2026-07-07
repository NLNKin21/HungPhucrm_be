package com.hungphu.crm.features.maintenance;

import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.customer.repository.CustomerRepository;
import com.hungphu.crm.features.maintenance.dto.*;
import com.hungphu.crm.features.maintenance.entity.MaintenanceApproval;
import com.hungphu.crm.features.maintenance.entity.MaintenanceAttachment;
import com.hungphu.crm.features.maintenance.entity.MaintenanceComment;
import com.hungphu.crm.features.maintenance.entity.MaintenanceContract;
import com.hungphu.crm.features.maintenance.entity.MaintenanceEvidence;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTask;
import com.hungphu.crm.features.maintenance.entity.MaintenanceTemplate;
import com.hungphu.crm.features.maintenance.mapper.MaintenanceMapper;
import com.hungphu.crm.features.maintenance.repository.MaintenanceApprovalRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceCommentRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceContractRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceEvidenceRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTaskRepository;
import com.hungphu.crm.features.maintenance.repository.MaintenanceTemplateRepository;
import com.hungphu.crm.features.notification.NotificationService;
import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.ApprovalAction;
import com.hungphu.crm.shared.enums.MaintenanceStatus;
import com.hungphu.crm.shared.enums.NotificationType;
import com.hungphu.crm.shared.enums.ScheduleStatus;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.mail.EmailService;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import com.hungphu.crm.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final MaintenanceTaskRepository     taskRepository;
    private final MaintenanceCommentRepository  commentRepository;
    private final ProjectRepository             projectRepository;
    private final UserRepository                userRepository;
    private final CustomerRepository            customerRepository;
    private final MaintenanceMapper             maintenanceMapper;
    private final FileStorageService            fileStorageService;
    private final MaintenanceTemplateRepository templateRepository;
    private final MaintenanceEvidenceRepository evidenceRepository;
    private final MaintenanceApprovalRepository approvalRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // ══════════════════════════════════════════════════════════════════════════
    // Contracts
    // ══════════════════════════════════════════════════════════════════════════

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

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", request.getCustomerId()));

        MaintenanceContract contract = new MaintenanceContract();
        contract.setCustomer(customer);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setCycleMonths(request.getCycleMonths() != null ? request.getCycleMonths() : 2);
        contract.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        // ★ MỚI: firstMaintenanceImmediate
        contract.setFirstMaintenanceImmediate(
                request.getFirstMaintenanceImmediate() != null
                        ? request.getFirstMaintenanceImmediate()
                        : true  // mặc định: ngay khi ký
        );

        // Dự án (tuỳ chọn)
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dự án", request.getProjectId()));

            if (!project.getCustomer().getId().equals(request.getCustomerId())) {
                throw new BusinessException(
                        "Dự án không thuộc về khách hàng đã chọn",
                        HttpStatus.BAD_REQUEST, "MAINT_008");
            }
            contract.setProject(project);
        }

        // Phân công (tuỳ chọn)
        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        // ★ MỚI: Supervisor
        if (request.getSupervisorId() != null) {
            contract.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        }

        MaintenanceContract saved = contractRepository.save(contract);
        generateTasks(saved);

        log.info("Maintenance contract created for customer {} by {}",
                request.getCustomerId(), currentUser.getId());

        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse createContractInternal(CreateContractRequest request) {
        validateDates(request);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", request.getProjectId()));

        MaintenanceContract contract = new MaintenanceContract();
        contract.setCustomer(project.getCustomer());
        contract.setProject(project);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setCycleMonths(request.getCycleMonths() != null ? request.getCycleMonths() : 2);

        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        MaintenanceContract saved = contractRepository.save(contract);
        generateTasks(saved);

        log.info("[AUTO] Maintenance contract created for project {} ({} → {}), {} tasks",
                request.getProjectId(), request.getStartDate(), request.getEndDate(),
                saved.getTasks().size());

        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID contractId,
                                        UpdateContractRequest request,
                                        UserDetailsImpl currentUser) {
        MaintenanceContract contract = findContractOrThrow(contractId);

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu",
                    HttpStatus.BAD_REQUEST, "MAINT_003");
        }

        boolean needRegenerate = !contract.getStartDate().equals(request.getStartDate()) ||
                !contract.getEndDate().equals(request.getEndDate()) ||
                !contract.getCycleMonths().equals(request.getCycleMonths());

        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        if (request.getCycleMonths() != null) {
            contract.setCycleMonths(request.getCycleMonths());
        }

        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        } else {
            contract.setAssignedTo(null);
        }

        // ★ MỚI: Supervisor
        if (request.getSupervisorId() != null) {
            contract.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        } else {
            contract.setSupervisor(null);
        }

        // ★ MỚI: firstMaintenanceImmediate
        if (request.getFirstMaintenanceImmediate() != null) {
            boolean oldImmediate = contract.isFirstMaintenanceImmediate();
            contract.setFirstMaintenanceImmediate(request.getFirstMaintenanceImmediate());
            if (oldImmediate != request.getFirstMaintenanceImmediate()) {
                needRegenerate = true;
            }
        }

        if (needRegenerate) {
            regenerateContractTasks(contract);
        } else {
            updatePendingTasksAssignee(contract);
        }

        MaintenanceContract saved = contractRepository.save(contract);
        log.info("Contract {} updated by user {}", contractId, currentUser.getId());
        return maintenanceMapper.toContractResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContract(UUID contractId) {
        MaintenanceContract contract = findContractOrThrow(contractId);

        long completedCount = contract.getTasks().stream()
                .filter(t -> t.getStatus() == ScheduleStatus.HOAN_THANH)
                .count();

        if (completedCount > 0) {
            throw new BusinessException(
                    "Không thể xóa hợp đồng đã có " + completedCount + " tác vụ hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_006");
        }

        contractRepository.delete(contract);
        log.info("Contract {} deleted", contractId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Customer Lookup (public — không cần auth)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public CustomerLookupResponse lookupByPhone(String phone) {
        // Chuẩn hóa phone
        String normalizedPhone = normalizePhone(phone);

        List<MaintenanceContract> contracts =
                contractRepository.findByCustomerPhone(normalizedPhone);

        if (contracts.isEmpty()) {
            // Thử tìm không normalize
            contracts = contractRepository.findByCustomerPhone(phone.trim());
        }

        if (contracts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy thông tin bảo trì cho số điện thoại: " + phone);
        }

        // Lấy thông tin customer từ contract đầu tiên
        var customer = contracts.get(0).getCustomer();

        List<CustomerLookupResponse.ContractSummary> contractSummaries = contracts.stream()
                .map(this::toContractSummary)
                .toList();

        return CustomerLookupResponse.builder()
                .customerName(customer.getFullName())
                .phone(customer.getPhone())
                .contracts(contractSummaries)
                .build();
    }

    private CustomerLookupResponse.ContractSummary toContractSummary(
            MaintenanceContract contract) {

        List<MaintenanceTask> tasks =
                taskRepository.findByContractIdWithEvidences(contract.getId());

        long completedCount = tasks.stream()
                .filter(t -> t.getStatus() == ScheduleStatus.HOAN_THANH)
                .count();

        List<CustomerLookupResponse.TaskSummary> taskSummaries = tasks.stream()
                .map(this::toTaskSummary)
                .toList();

        return CustomerLookupResponse.ContractSummary.builder()
                .id(contract.getId())
                .projectName(contract.getProject() != null
                        ? contract.getProject().getName() : null)
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .status(contract.getStatus())
                .totalTasks(tasks.size())
                .completedTasks((int) completedCount)
                .tasks(taskSummaries)
                .build();
    }

    private CustomerLookupResponse.TaskSummary toTaskSummary(MaintenanceTask task) {
        // Chỉ show evidences của task đã hoàn thành
        List<CustomerLookupResponse.EvidenceSummary> evidences = List.of();

        if (task.getStatus() == ScheduleStatus.HOAN_THANH
                && task.getEvidences() != null) {
            evidences = task.getEvidences().stream()
                    .map(ev -> CustomerLookupResponse.EvidenceSummary.builder()
                            .id(ev.getId())
                            .fileUrl(ev.getFileUrl())
                            .fileType(ev.getFileType())
                            .description(ev.getDescription())
                            .uploadedAt(ev.getUploadedAt())
                            .build())
                    .toList();
        }

        return CustomerLookupResponse.TaskSummary.builder()
                .id(task.getId())
                .title(task.getTitle())
                .scheduledDate(task.getScheduledDate())
                .status(task.getStatus())
                .assigneeName(task.getAssignedTo() != null
                        ? task.getAssignedTo().getFullName() : null)
                .techNote(task.getStatus() == ScheduleStatus.HOAN_THANH
                        ? task.getDescription() : null) // Chỉ show ghi chú khi đã xong
                .completedAt(task.getCompletedAt())
                .evidences(evidences)
                .build();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        // Bỏ khoảng trắng, dấu gạch
        String cleaned = phone.trim().replaceAll("[\\s\\-()]", "");
        // Chuyển 0xxx → +84xxx
        if (cleaned.startsWith("0")) {
            return "+84" + cleaned.substring(1);
        }
        return cleaned;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> findActiveContractsByCustomer(UUID customerId, UUID projectId) {
        List<MaintenanceStatus> activeStatuses = List.of(
                MaintenanceStatus.MOI,
                MaintenanceStatus.SAP_HET_HAN
        );

        List<MaintenanceContract> contracts;
        if (projectId != null) {
            contracts = contractRepository.findByCustomerIdAndProjectIdAndStatusIn(
                    customerId, projectId, activeStatuses);
        } else {
            contracts = contractRepository.findByCustomerIdAndStatusIn(
                    customerId, activeStatuses);
        }

        return contracts.stream()
                .map(maintenanceMapper::toContractResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContractResponse renewContract(UUID contractId,
                                        RenewContractRequest request,
                                        UserDetailsImpl currentUser) {
        MaintenanceContract contract = findContractOrThrow(contractId);

        if (!request.getNewEndDate().isAfter(contract.getEndDate())) {
            throw new BusinessException(
                    "Ngày gia hạn phải sau ngày kết thúc hiện tại (" + contract.getEndDate() + ")",
                    HttpStatus.BAD_REQUEST, "MAINT_007");
        }

        LocalDate oldEndDate = contract.getEndDate();
        contract.setEndDate(request.getNewEndDate());

        if (request.getCycleMonths() != null) {
            contract.setCycleMonths(request.getCycleMonths());
        }
        if (request.getAssignedTo() != null) {
            contract.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        // ★ MỚI: Supervisor
        if (request.getSupervisorId() != null) {
            contract.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        }

        contract.setStatus(MaintenanceStatus.MOI);
        generateAdditionalTasks(contract, oldEndDate);

        MaintenanceContract saved = contractRepository.save(contract);
        log.info("Contract {} renewed by user {}: {} → {}",
                contractId, currentUser.getId(), oldEndDate, request.getNewEndDate());

        return maintenanceMapper.toContractResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tasks
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> findTasksByContract(UUID contractId) {
        findContractOrThrow(contractId);
        return taskRepository.findByContractIdWithDetailsOrderByScheduledDateAsc(contractId)
                .stream()
                .map(maintenanceMapper::toTaskResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> findAllTasks(
            ScheduleStatus status,
            UUID assignedTo,
            UUID contractId,
            UUID customerId,
            String from,
            String to,
            UserDetailsImpl currentUser) {

        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null ? LocalDate.parse(to) : null;

        UUID visibleToUserId = null;
        if (currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            visibleToUserId = currentUser.getId();
        }

        return taskRepository.findAllWithFilters(
                        status,
                        assignedTo,
                        contractId,
                        customerId,
                        fromDate,
                        toDate,
                        visibleToUserId
                ).stream()
                .map(maintenanceMapper::toTaskResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceTaskResponse findTaskById(UUID taskId) {
        MaintenanceTask task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));
        return maintenanceMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    public MaintenanceTaskResponse updateTask(UUID taskId,
                                            UpdateTaskRequest request,
                                            UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() == ScheduleStatus.HOAN_THANH) {
            throw new BusinessException(
                    "Không thể cập nhật tác vụ đã hoàn thành",
                    HttpStatus.BAD_REQUEST,
                    "MAINT_009"
            );
        }

        if (request.getAssignedTo() != null) {
            task.setAssignedTo(userRepository.getReferenceById(request.getAssignedTo()));
        }

        if (request.getWatcherId() != null) {
            task.setWatcher(userRepository.getReferenceById(request.getWatcherId()));
        }

        if (request.getScheduledDate() != null) {
            task.setScheduledDate(request.getScheduledDate());
        }

        if (request.getContactPhone() != null) {
            task.setContactPhone(request.getContactPhone());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        log.info("Task {} updated by user {}", taskId, currentUser.getId());
        return maintenanceMapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public MaintenanceTaskResponse completeTask(UUID taskId, UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() == ScheduleStatus.HOAN_THANH) {
            throw new BusinessException("Tác vụ đã được hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_005");
        }

        // ★ MỚI: Nếu task có supervisor → phải dùng workflow duyệt
        if (task.getSupervisor() != null) {
            throw new BusinessException(
                    "Tác vụ có người giám sát. Vui lòng upload minh chứng và gửi duyệt",
                    HttpStatus.BAD_REQUEST, "MAINT_010");
        }

        // Nếu không có supervisor → cho phép complete trực tiếp (giữ tương thích cũ)
        LocalDate today = LocalDate.now();
        if (task.getScheduledDate().isBefore(today)) {
            task.setCompletedLate(true);
            task.setDaysLate((int) ChronoUnit.DAYS.between(task.getScheduledDate(), today));
        }

        task.setStatus(ScheduleStatus.HOAN_THANH);
        task.setCompletedAt(LocalDateTime.now());

        return maintenanceMapper.toTaskResponse(taskRepository.save(task));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Comments
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByTask(UUID taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        return commentRepository.findRootCommentsByTaskId(taskId)
                .stream()
                .map(maintenanceMapper::toCommentResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse addComment(UUID taskId, String content, UUID parentId,
                                      List<MultipartFile> files, UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        MaintenanceComment comment = new MaintenanceComment();
        comment.setTask(task);
        comment.setUser(userRepository.getReferenceById(currentUser.getId()));
        comment.setContent(content.trim());

        // Reply
        if (parentId != null) {
            MaintenanceComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bình luận", parentId));
            comment.setParent(parent);
        }

        MaintenanceComment saved = commentRepository.save(comment);

        // Upload attachments
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String relativePath = fileStorageService.store(
                        file, "maintenance/comments/" + saved.getId());

                MaintenanceAttachment attachment = new MaintenanceAttachment();
                attachment.setComment(saved);
                attachment.setFileUrl(relativePath);
                attachment.setFileType(fileStorageService.resolveFileType(file));
                attachment.setFileSize(file.getSize());
                saved.getAttachments().add(attachment);
            }
            saved = commentRepository.save(saved);
        }

        log.info("Comment added to task {} by user {}", taskId, currentUser.getId());
        return maintenanceMapper.toCommentResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Stats
    // ══════════════════════════════════════════════════════════════════════════

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

        // Task stats
        List<Object[]> statusCounts = taskRepository.countByStatus();
        long pendingTasks = 0, overdueTasks = 0, completedTasks = 0;
        long totalTasks = 0;

        for (Object[] row : statusCounts) {
            ScheduleStatus status = (ScheduleStatus) row[0];
            long count = (Long) row[1];
            totalTasks += count;
            switch (status) {
                case CHO_THUC_HIEN -> pendingTasks = count;
                case QUA_HAN -> overdueTasks = count;
                case HOAN_THANH -> completedTasks = count;
            }
        }

        long completedLateCount = taskRepository.countCompletedLate();

        LocalDate today = LocalDate.now();
        List<MaintenanceTaskResponse> upcomingTasks = taskRepository
                .findUpcomingTasks(today, today.plusDays(7))
                .stream()
                .map(maintenanceMapper::toTaskResponse)
                .toList();

        List<MaintenanceTaskResponse> overdueList = taskRepository
                .findAllOverdue()
                .stream()
                .map(maintenanceMapper::toTaskResponse)
                .toList();

        return MaintenanceStatsResponse.builder()
                .totalContracts(totalContracts)
                .activeContracts(activeContracts)
                .expiringContracts(expiringContracts)
                .expiredContracts(expiredContracts)
                .totalSchedules(totalTasks)
                .pendingSchedules(pendingTasks)
                .overdueSchedules(overdueTasks)
                .completedSchedules(completedTasks)
                .completedLateCount(completedLateCount)
                .upcomingSchedules(upcomingTasks)
                .overdueList(overdueList)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void validateDates(CreateContractRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu",
                    HttpStatus.BAD_REQUEST, "MAINT_003");
        }
    }

    private MaintenanceContract findContractOrThrow(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng bảo trì", id));
    }

    /**
     * Tính ngày kết thúc hợp đồng: startDate + months - 1 ngày
     * Ví dụ: 05/07/2026 + 24 tháng = 04/07/2028
     */
    private LocalDate calculateEndDate(LocalDate startDate, int durationMonths) {
        return startDate.plusMonths(durationMonths).minusDays(1);
    }

    // Templates

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> findAllTemplates() {
        return templateRepository.findAllWithDetails().stream()
                .map(maintenanceMapper::toTemplateResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse findTemplateById(UUID id) {
        MaintenanceTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu", id));
        return maintenanceMapper.toTemplateResponse(template);
    }

    @Override
    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request, UserDetailsImpl currentUser) {
        MaintenanceTemplate template = new MaintenanceTemplate();
        template.setTitle(request.getTitle().trim());
        template.setDescription(request.getDescription());
        template.setCycleMonths(request.getCycleMonths() != null ? request.getCycleMonths() : 2);
        template.setDurationMonths(request.getDurationMonths() != null ? request.getDurationMonths() : 12);
        template.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        if (request.getDefaultAssignedTo() != null) {
            template.setDefaultAssignedTo(userRepository.getReferenceById(request.getDefaultAssignedTo()));
        }
        if (request.getDefaultWatcherId() != null) {
            template.setDefaultWatcher(userRepository.getReferenceById(request.getDefaultWatcherId()));
        }

        MaintenanceTemplate saved = templateRepository.save(template);
        log.info("Template {} created by user {}", saved.getId(), currentUser.getId());
        return maintenanceMapper.toTemplateResponse(saved);
    }

    @Override
    @Transactional
    public TemplateResponse updateTemplate(UUID id, UpdateTemplateRequest request, UserDetailsImpl currentUser) {
        MaintenanceTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu", id));

        template.setTitle(request.getTitle().trim());
        template.setDescription(request.getDescription());
        if (request.getCycleMonths() != null) template.setCycleMonths(request.getCycleMonths());
        if (request.getDurationMonths() != null) template.setDurationMonths(request.getDurationMonths());

        if (request.getDefaultAssignedTo() != null) {
            template.setDefaultAssignedTo(userRepository.getReferenceById(request.getDefaultAssignedTo()));
        } else {
            template.setDefaultAssignedTo(null);
        }
        if (request.getDefaultWatcherId() != null) {
            template.setDefaultWatcher(userRepository.getReferenceById(request.getDefaultWatcherId()));
        } else {
            template.setDefaultWatcher(null);
        }

        MaintenanceTemplate saved = templateRepository.save(template);
        log.info("Template {} updated by user {}", id, currentUser.getId());
        return maintenanceMapper.toTemplateResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID id) {
        MaintenanceTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu", id));
        
        // Soft delete - đánh dấu inactive
        template.setActive(false);
        templateRepository.save(template);
        log.info("Template {} deactivated", id);
    }

    @Override
    @Transactional
    public ContractResponse createContractFromTemplate(CreateContractFromTemplateRequest request,
                                                        UserDetailsImpl currentUser) {
        MaintenanceTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu", request.getTemplateId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", request.getCustomerId()));

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate endDate = calculateEndDate(startDate, template.getDurationMonths());

        // Tạo contract
        MaintenanceContract contract = new MaintenanceContract();
        contract.setCustomer(customer);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setCycleMonths(template.getCycleMonths());
        contract.setTemplate(template);
        contract.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        // Project (tuỳ chọn)
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dự án", request.getProjectId()));
            if (!project.getCustomer().getId().equals(request.getCustomerId())) {
                throw new BusinessException("Dự án không thuộc về khách hàng đã chọn",
                        HttpStatus.BAD_REQUEST, "MAINT_008");
            }
            contract.setProject(project);
        }

        // Assignee: request override > template default
        UUID assigneeId = request.getAssignedTo() != null
                ? request.getAssignedTo()
                : (template.getDefaultAssignedTo() != null ? template.getDefaultAssignedTo().getId() : null);
        if (assigneeId != null) {
            contract.setAssignedTo(userRepository.getReferenceById(assigneeId));
        }

        MaintenanceContract saved = contractRepository.save(contract);

        // Generate tasks với description từ template
        generateTasksFromTemplate(saved, template, request.getWatcherId());

        log.info("Contract created from template {} for customer {} by {}",
                template.getId(), request.getCustomerId(), currentUser.getId());

        return maintenanceMapper.toContractResponse(saved);
    }

    // ── Private helper ──

    private void generateTasksFromTemplate(MaintenanceContract contract,
                                        MaintenanceTemplate template,
                                        UUID watcherIdOverride) {
        int cycle = contract.getCycleMonths();
        boolean immediate = contract.isFirstMaintenanceImmediate();

        // ★ THAY ĐỔI: Lần 1
        LocalDate current = immediate
                ? contract.getStartDate()
                : contract.getStartDate().plusMonths(cycle);

        int index = 1;

        String customerName = contract.getCustomer().getFullName();
        String projectName = contract.getProject() != null
                ? contract.getProject().getName()
                : null;
        String contactPhone = contract.getCustomer().getPhone();

        UUID watcherId = watcherIdOverride != null
                ? watcherIdOverride
                : (template.getDefaultWatcher() != null ? template.getDefaultWatcher().getId() : null);

        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceTask task = new MaintenanceTask();
            task.setContract(contract);
            task.setTitle(buildTaskTitle(customerName, projectName, index));
            task.setDescription(template.getDescription());
            task.setContactPhone(contactPhone);
            task.setScheduledDate(current);
            task.setCreatedBy(contract.getCreatedBy());
            task.setAssignedTo(contract.getAssignedTo());
            task.setSupervisor(contract.getSupervisor());  // ★ MỚI

            if (watcherId != null) {
                task.setWatcher(userRepository.getReferenceById(watcherId));
            }

            contract.getTasks().add(task);
            current = current.plusMonths(cycle);
            index++;
        }
    }

    // ── Task generation ──

    private void generateTasks(MaintenanceContract contract) {
        int cycle = contract.getCycleMonths();
        boolean immediate = contract.isFirstMaintenanceImmediate();

        // ★ THAY ĐỔI: Lần 1 bắt đầu từ đâu
        LocalDate current = immediate
                ? contract.getStartDate()                      // Lần 1 = ngày ký
                : contract.getStartDate().plusMonths(cycle);    // Lần 1 = sau chu kỳ

        int index = 1;

        String customerName = contract.getCustomer().getFullName();
        String projectName = contract.getProject() != null
                ? contract.getProject().getName()
                : null;
        String contactPhone = contract.getCustomer().getPhone();

        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceTask task = new MaintenanceTask();
            task.setContract(contract);
            task.setTitle(buildTaskTitle(customerName, projectName, index));
            task.setContactPhone(contactPhone);
            task.setScheduledDate(current);
            task.setCreatedBy(contract.getCreatedBy());
            task.setAssignedTo(contract.getAssignedTo());
            task.setSupervisor(contract.getSupervisor());  // ★ MỚI: copy supervisor

            contract.getTasks().add(task);
            current = current.plusMonths(cycle);
            index++;
        }
    }


    private String buildTaskTitle(String customerName, String projectName, int index) {
        if (projectName != null) {
            return String.format("Bảo trì định kỳ - %s - Lần %d", projectName, index);
        }
        return String.format("Bảo trì định kỳ - %s - Lần %d", customerName, index);
    }

    private void generateAdditionalTasks(MaintenanceContract contract, LocalDate fromDate) {
        int cycle = contract.getCycleMonths();

        LocalDate lastTaskDate = contract.getTasks().stream()
                .map(MaintenanceTask::getScheduledDate)
                .max(LocalDate::compareTo)
                .orElse(fromDate);

        int lastIndex = contract.getTasks().size();

        String customerName = contract.getCustomer().getFullName();
        String projectName = contract.getProject() != null
                ? contract.getProject().getName()
                : null;
        String contactPhone = contract.getCustomer().getPhone();

        LocalDate current = lastTaskDate.plusMonths(cycle);
        int index = lastIndex + 1;

        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceTask task = new MaintenanceTask();
            task.setContract(contract);
            task.setTitle(buildTaskTitle(customerName, projectName, index));
            task.setContactPhone(contactPhone);
            task.setScheduledDate(current);
            task.setCreatedBy(contract.getCreatedBy());
            task.setAssignedTo(contract.getAssignedTo());
            task.setSupervisor(contract.getSupervisor());  // ★ MỚI

            contract.getTasks().add(task);
            current = current.plusMonths(cycle);
            index++;
        }
    }

    private void regenerateContractTasks(MaintenanceContract contract) {
        List<MaintenanceTask> completedTasks = contract.getTasks().stream()
                .filter(t -> t.getStatus() == ScheduleStatus.HOAN_THANH)
                .toList();

        contract.getTasks().removeIf(t -> t.getStatus() != ScheduleStatus.HOAN_THANH);

        LocalDate lastCompletedDate = completedTasks.stream()
                .map(MaintenanceTask::getScheduledDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        int cycle = contract.getCycleMonths();
        int startIndex = completedTasks.size() + 1;

        LocalDate current = lastCompletedDate != null
                ? lastCompletedDate.plusMonths(cycle)
                : (contract.isFirstMaintenanceImmediate()       // ★ THAY ĐỔI
                        ? contract.getStartDate()
                        : contract.getStartDate().plusMonths(cycle));

        String customerName = contract.getCustomer().getFullName();
        String projectName = contract.getProject() != null
                ? contract.getProject().getName()
                : null;
        String contactPhone = contract.getCustomer().getPhone();

        int index = startIndex;
        while (!current.isAfter(contract.getEndDate())) {
            MaintenanceTask task = new MaintenanceTask();
            task.setContract(contract);
            task.setTitle(buildTaskTitle(customerName, projectName, index));
            task.setContactPhone(contactPhone);
            task.setScheduledDate(current);
            task.setCreatedBy(contract.getCreatedBy());
            task.setAssignedTo(contract.getAssignedTo());
            task.setSupervisor(contract.getSupervisor());  // ★ MỚI

            contract.getTasks().add(task);
            current = current.plusMonths(cycle);
            index++;
        }

        log.info("Regenerated tasks for contract {}: {} completed kept, {} new created",
                contract.getId(), completedTasks.size(),
                contract.getTasks().size() - completedTasks.size());
    }

    private void updatePendingTasksAssignee(MaintenanceContract contract) {
        contract.getTasks().stream()
                .filter(t -> t.getStatus() == ScheduleStatus.CHO_THUC_HIEN ||
                            t.getStatus() == ScheduleStatus.QUA_HAN ||
                            t.getStatus() == ScheduleStatus.CAN_BO_SUNG)  // ★ MỚI: thêm CAN_BO_SUNG
                .forEach(t -> {
                    t.setAssignedTo(contract.getAssignedTo());
                    t.setSupervisor(contract.getSupervisor());  // ★ MỚI
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceResponse> findEvidencesByTask(UUID taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));
        return evidenceRepository.findByTaskIdOrderByUploadedAtDesc(taskId)
                .stream()
                .map(maintenanceMapper::toEvidenceResponse)
                .toList();
    }

    @Override
    @Transactional
    public EvidenceResponse addEvidence(UUID taskId, String description,
                                        MultipartFile file, UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() == ScheduleStatus.HOAN_THANH) {
            throw new BusinessException("Không thể thêm minh chứng cho tác vụ đã hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_011");
        }
        if (task.getStatus() == ScheduleStatus.CHO_DUYET) {
            throw new BusinessException("Tác vụ đang chờ duyệt. Vui lòng đợi supervisor phản hồi",
                    HttpStatus.BAD_REQUEST, "MAINT_012");
        }

        String relativePath = fileStorageService.store(file, "maintenance/evidence/" + taskId);

        MaintenanceEvidence evidence = new MaintenanceEvidence();
        evidence.setTask(task);
        evidence.setUploadedBy(userRepository.getReferenceById(currentUser.getId()));
        evidence.setFileUrl(relativePath);
        evidence.setFileType(fileStorageService.resolveFileType(file));
        evidence.setFileSize(file.getSize());
        evidence.setDescription(description);

        MaintenanceEvidence saved = evidenceRepository.save(evidence);
        log.info("Evidence uploaded for task {} by user {}", taskId, currentUser.getId());

        return maintenanceMapper.toEvidenceResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEvidence(UUID taskId, UUID evidenceId, UserDetailsImpl currentUser) {
        MaintenanceEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Minh chứng", evidenceId));

        if (!evidence.getTask().getId().equals(taskId)) {
            throw new BusinessException("Minh chứng không thuộc tác vụ này",
                    HttpStatus.BAD_REQUEST, "MAINT_013");
        }

        if (evidence.getTask().getStatus() == ScheduleStatus.HOAN_THANH) {
            throw new BusinessException("Không thể xoá minh chứng của tác vụ đã hoàn thành",
                    HttpStatus.BAD_REQUEST, "MAINT_014");
        }

        // ★ MỚI: Xoá file từ storage
        fileStorageService.delete(evidence.getFileUrl());

        evidenceRepository.delete(evidence);
        log.info("Evidence {} deleted from task {} by user {}", evidenceId, taskId, currentUser.getId());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Workflow: Submit → Approve / Reject
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public MaintenanceTaskResponse submitTask(UUID taskId, UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() != ScheduleStatus.CHO_THUC_HIEN
                && task.getStatus() != ScheduleStatus.QUA_HAN
                && task.getStatus() != ScheduleStatus.CAN_BO_SUNG) {
            throw new BusinessException(
                    "Chỉ có thể gửi duyệt tác vụ ở trạng thái Chờ thực hiện, Quá hạn, hoặc Cần bổ sung",
                    HttpStatus.BAD_REQUEST, "MAINT_015");
        }

        long evidenceCount = evidenceRepository.countByTaskId(taskId);
        if (evidenceCount == 0) {
            throw new BusinessException("Vui lòng upload ít nhất 1 minh chứng trước khi gửi duyệt",
                    HttpStatus.BAD_REQUEST, "MAINT_016");
        }

        if (task.getSupervisor() == null) {
            throw new BusinessException("Tác vụ chưa có người giám sát. Vui lòng liên hệ admin",
                    HttpStatus.BAD_REQUEST, "MAINT_017");
        }

        task.setStatus(ScheduleStatus.CHO_DUYET);
        task.setSubmittedAt(LocalDateTime.now());

        MaintenanceTask saved = taskRepository.save(task);

        // ── In-app notification cho supervisor ──
        notificationService.createNotification(
                task.getSupervisor(),
                "Tác vụ chờ duyệt",
                String.format("KTV %s đã gửi minh chứng cho tác vụ \"%s\". Vui lòng duyệt.",
                        currentUser.getUsername(), task.getTitle()),
                NotificationType.MAINTENANCE_SUBMITTED,
                "maintenance_task",
                task.getId()
        );

        // ★ THÊM: Email cho supervisor
        if (task.getSupervisor().getEmail() != null) {
            emailService.sendMaintenanceSubmitted(
                    task.getSupervisor().getEmail(),
                    task.getTitle(),
                    currentUser.getUsername()
            );
        }

        log.info("Task {} submitted for review by user {}", taskId, currentUser.getId());
        return maintenanceMapper.toTaskResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceTaskResponse approveTask(UUID taskId, UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() != ScheduleStatus.CHO_DUYET) {
            throw new BusinessException("Chỉ có thể duyệt tác vụ đang ở trạng thái Chờ duyệt",
                    HttpStatus.BAD_REQUEST, "MAINT_018");
        }

        validateSupervisorPermission(task, currentUser);

        LocalDate today = LocalDate.now();
        if (task.getScheduledDate().isBefore(today)) {
            task.setCompletedLate(true);
            task.setDaysLate((int) ChronoUnit.DAYS.between(task.getScheduledDate(), today));
        }

        task.setStatus(ScheduleStatus.HOAN_THANH);
        task.setCompletedAt(LocalDateTime.now());

        MaintenanceApproval approval = new MaintenanceApproval();
        approval.setTask(task);
        approval.setApprovedBy(userRepository.getReferenceById(currentUser.getId()));
        approval.setAction(ApprovalAction.APPROVED);
        approvalRepository.save(approval);

        MaintenanceTask saved = taskRepository.save(task);

        // ── In-app notification + Email cho KTV ──
        if (task.getAssignedTo() != null) {
            notificationService.createNotification(
                    task.getAssignedTo(),
                    "Tác vụ đã được duyệt",
                    String.format("Tác vụ \"%s\" đã được %s duyệt hoàn thành.",
                            task.getTitle(), currentUser.getUsername()),
                    NotificationType.MAINTENANCE_APPROVED,
                    "maintenance_task",
                    task.getId()
            );

            // ★ THÊM: Email cho KTV
            if (task.getAssignedTo().getEmail() != null) {
                emailService.sendMaintenanceApproved(
                        task.getAssignedTo().getEmail(),
                        task.getTitle(),
                        currentUser.getUsername()
                );
            }
        }

        log.info("Task {} approved by supervisor {}", taskId, currentUser.getId());
        return maintenanceMapper.toTaskResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceTaskResponse rejectTask(UUID taskId, String reason,
                                            UserDetailsImpl currentUser) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));

        if (task.getStatus() != ScheduleStatus.CHO_DUYET) {
            throw new BusinessException("Chỉ có thể từ chối tác vụ đang ở trạng thái Chờ duyệt",
                    HttpStatus.BAD_REQUEST, "MAINT_019");
        }

        validateSupervisorPermission(task, currentUser);

        task.setStatus(ScheduleStatus.CAN_BO_SUNG);
        task.setSubmittedAt(null);

        MaintenanceApproval approval = new MaintenanceApproval();
        approval.setTask(task);
        approval.setApprovedBy(userRepository.getReferenceById(currentUser.getId()));
        approval.setAction(ApprovalAction.REJECTED);
        approval.setReason(reason);
        approvalRepository.save(approval);

        MaintenanceTask saved = taskRepository.save(task);

        // ── In-app notification + Email cho KTV ──
        if (task.getAssignedTo() != null) {
            notificationService.createNotification(
                    task.getAssignedTo(),
                    "Tác vụ bị từ chối",
                    String.format("Tác vụ \"%s\" bị từ chối bởi %s. Lý do: %s",
                            task.getTitle(), currentUser.getUsername(), reason),
                    NotificationType.MAINTENANCE_REJECTED,
                    "maintenance_task",
                    task.getId()
            );

            // ★ THÊM: Email cho KTV
            if (task.getAssignedTo().getEmail() != null) {
                emailService.sendMaintenanceRejected(
                        task.getAssignedTo().getEmail(),
                        task.getTitle(),
                        currentUser.getUsername(),
                        reason
                );
            }
        }

        log.info("Task {} rejected by supervisor {}: {}", taskId, currentUser.getId(), reason);
        return maintenanceMapper.toTaskResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> findApprovalsByTask(UUID taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tác vụ bảo trì", taskId));
        return approvalRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(maintenanceMapper::toApprovalResponse)
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helper — validate supervisor permission
    // ══════════════════════════════════════════════════════════════════════════

    private void validateSupervisorPermission(MaintenanceTask task, UserDetailsImpl currentUser) {
        boolean isSupervisor = task.getSupervisor() != null
                && task.getSupervisor().getId().equals(currentUser.getId());

        boolean isAdminOrManager = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER"));

        if (!isSupervisor && !isAdminOrManager) {
            throw new BusinessException(
                    "Chỉ người giám sát hoặc admin/manager mới có quyền duyệt/từ chối",
                    HttpStatus.FORBIDDEN, "MAINT_020");
        }
    }
}