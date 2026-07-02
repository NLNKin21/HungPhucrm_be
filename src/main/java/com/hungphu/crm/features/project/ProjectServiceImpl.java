package com.hungphu.crm.features.project;

import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.consultation.repository.ConsultationRepository;
import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.customer.repository.CustomerRepository;
import com.hungphu.crm.features.project.dto.*;
import com.hungphu.crm.features.project.entity.PaymentInstallment;
import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.project.entity.ProjectDocument;
import com.hungphu.crm.features.project.event.ProjectCreatedEvent;
import com.hungphu.crm.features.project.event.ProjectStatusChangedEvent;
import com.hungphu.crm.features.project.mapper.ProjectMapper;
import com.hungphu.crm.features.project.repository.PaymentInstallmentRepository;
import com.hungphu.crm.features.project.repository.ProjectDocumentRepository;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.features.task.repository.TaskRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.ProjectStatus;
import com.hungphu.crm.shared.enums.TaskStatus;
import com.hungphu.crm.shared.enums.UserRole;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.response.PageMeta;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import com.hungphu.crm.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository             projectRepository;
    private final ConsultationRepository        consultationRepository;
    private final CustomerRepository            customerRepository;
    private final UserRepository                userRepository;
    private final PaymentInstallmentRepository  paymentRepository;
    private final ProjectDocumentRepository     documentRepository;
    private final ProjectMapper                 projectMapper;
    private final FileStorageService            fileStorageService;
    private final ApplicationEventPublisher     eventPublisher;
    private final TaskRepository                taskRepository;


    // ✅ Bỏ TaskRepository vì không còn tự tạo task nữa

    private static final Map<ProjectStatus, ProjectStatus> ALLOWED_STATUS_TRANSITIONS = Map.of(
            ProjectStatus.GIAM_SAT_XAY_DUNG, ProjectStatus.THI_CONG,
            ProjectStatus.THI_CONG,          ProjectStatus.BAN_GIAO,
            ProjectStatus.BAN_GIAO,          ProjectStatus.BAO_TRI
    );

    // ── findAll ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectResponse>> findAll(UUID customerId,
                                                      boolean myProjectsOnly,
                                                      UserDetailsImpl currentUser,
                                                      Pageable pageable) {
        Page<Project> page;

        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            page = projectRepository.findMyProjects(customerId, currentUser.getId(), pageable);
        } else if (currentUser.getRole() == UserRole.MANAGER) {
            page = projectRepository.findManagedProjects(customerId, currentUser.getId(), pageable);
        } else {
            page = projectRepository.findByFilters(customerId, pageable);
        }

        List<ProjectResponse> data = page.getContent().stream()
                .map(projectMapper::toResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .page(pageable.getPageNumber() + 1)
                .limit(pageable.getPageSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ApiResponse.success(data, meta);
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        return projectMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UserDetailsImpl currentUser) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", request.getCustomerId()));

        // Create project
        Project project = new Project();
        project.setName(request.getName().trim());
        project.setCustomer(customer);
        project.setElevatorType(request.getElevatorType());
        project.setProjectType(request.getProjectType());
        project.setProjectStatus(ProjectStatus.GIAM_SAT_XAY_DUNG); // Default status
        project.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        // Set supervisor if provided
        if (request.getSupervisorId() != null) {
            User supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người giám sát", request.getSupervisorId()));
            project.setSupervisor(supervisor);
        }

        Project saved = projectRepository.save(project);

        // Publish event (nếu cần)
        eventPublisher.publishEvent(new ProjectCreatedEvent(saved));

        log.info("Project {} created directly by admin {}", saved.getId(), currentUser.getId());

        return projectMapper.toResponse(saved);
    }

    // ── getStats ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProjectStatsResponse getStats(UUID projectId) {
        findOrThrow(projectId);

        List<Object[]> rows = taskRepository.countByStatusForProject(projectId);
        Map<TaskStatus, Long> tasksByStatus = new java.util.EnumMap<>(TaskStatus.class);
        long totalTasks = 0;
        long completed  = 0;

        for (Object[] row : rows) {
            TaskStatus status = (TaskStatus) row[0];
            long count        = (Long) row[1];
            tasksByStatus.put(status, count);
            totalTasks += count;
            if (status == TaskStatus.HOAN_THANH) completed = count;
        }

        int progressPercent = totalTasks == 0 ? 0 : (int) (completed * 100 / totalTasks);

        List<User> members = taskRepository.findTeamMembersByProject(projectId);
        List<ProjectStatsResponse.MemberInfo> teamMembers = members.stream()
                .map(u -> ProjectStatsResponse.MemberInfo.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .build())
                .toList();

        return ProjectStatsResponse.builder()
                .totalTasks(totalTasks)
                .progressPercent(progressPercent)
                .tasksByStatus(tasksByStatus)
                .teamMembers(teamMembers)
                .build();
    }

    // ── convertFromConsultation ───────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse convertFromConsultation(UUID consultationId,
                                                   ConvertToProjectRequest request,
                                                   UserDetailsImpl currentUser) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tư vấn", consultationId));

        if (consultation.getStatus() != ConsultationStatus.THANH_CONG) {
            throw new BusinessException(
                    "Tư vấn chưa thành công, không thể chuyển dự án",
                    HttpStatus.BAD_REQUEST, "CONS_003");
        }

        if (projectRepository.existsByConsultationId(consultationId)) {
            throw new BusinessException(
                    "Tư vấn này đã được chuyển thành dự án",
                    HttpStatus.BAD_REQUEST, "CONS_004");
        }

        // ✅ Phân quyền: MANAGER chỉ convert tư vấn do mình phụ trách
        //               EMPLOYEE chỉ convert tư vấn được assign cho mình
        if (currentUser.getRole() == UserRole.MANAGER) {
            boolean isOwner = consultation.getAssignedBy() != null
                    && consultation.getAssignedBy().getId().equals(currentUser.getId());
            if (!isOwner) {
                throw new AccessDeniedException("Bạn không có quyền chuyển tư vấn này thành dự án");
            }
        }

        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            boolean isAssignee = consultation.getAssignedTo() != null
                    && consultation.getAssignedTo().getId().equals(currentUser.getId());
            if (!isAssignee) {
                throw new AccessDeniedException("Bạn không có quyền chuyển tư vấn này thành dự án");
            }
        }

        Customer customer = resolveCustomerFromConsultationOrRequest(
                consultation, request, currentUser);

        Project project = new Project();
        project.setName(request.getProjectName());
        project.setCustomer(customer);
        project.setConsultation(consultation);
        project.setElevatorType(request.getElevatorType());
        project.setProjectType(request.getProjectType());
        project.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        if (request.getSupervisorId() != null) {
            project.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        }

        Project saved = projectRepository.save(project);

        // ✅ Không còn tự động tạo task nữa
        // Giám sát / Admin sẽ tự tạo task trong trang chi tiết dự án

        consultation.setStatus(ConsultationStatus.DA_CHUYEN_DU_AN);
        consultationRepository.save(consultation);

        eventPublisher.publishEvent(new ProjectCreatedEvent(saved));

        log.info("Project {} created from consultation {} by {}",
                saved.getId(), consultationId, currentUser.getId());

        return projectMapper.toResponse(saved);
    }

    // ── updateProjectStatus ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(UUID id,
                                               ProjectStatus newStatus,
                                               UserDetailsImpl currentUser) {
        Project project       = findOrThrow(id);
        ProjectStatus fromStatus = project.getProjectStatus();

        if (newStatus == ProjectStatus.HET_HAN) {
            throw new BusinessException(
                    "Trạng thái 'Hết hạn' chỉ được hệ thống tự động cập nhật",
                    HttpStatus.BAD_REQUEST, "PROJ_004");
        }

        ProjectStatus expectedNext = ALLOWED_STATUS_TRANSITIONS.get(fromStatus);
        if (expectedNext == null || expectedNext != newStatus) {
            throw new BusinessException(
                    String.format("Không thể chuyển từ '%s' sang '%s'", fromStatus, newStatus),
                    HttpStatus.BAD_REQUEST, "PROJ_003");
        }

        project.setProjectStatus(newStatus);
        Project saved = projectRepository.save(project);

        log.info("Project {} status: {} → {} by {}",
                id, fromStatus, newStatus, currentUser.getId());

        eventPublisher.publishEvent(
                new ProjectStatusChangedEvent(this, saved, fromStatus, newStatus));

        return projectMapper.toResponse(saved);
    }

    // ── addPayment ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentInstallmentResponse addPayment(UUID projectId,
                                                 AddPaymentRequest request,
                                                 UserDetailsImpl currentUser) {
        Project project = findOrThrow(projectId);

        if (paymentRepository.existsByProjectIdAndInstallmentNo(
                projectId, request.getInstallmentNo())) {
            throw new BusinessException(
                    "Số đợt thanh toán đã tồn tại",
                    HttpStatus.BAD_REQUEST, "PROJ_002");
        }

        PaymentInstallment payment = new PaymentInstallment();
        payment.setProject(project);
        payment.setInstallmentNo(request.getInstallmentNo());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        return projectMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallmentResponse> getPayments(UUID projectId) {
        findOrThrow(projectId);
        return paymentRepository.findByProjectIdOrderByInstallmentNoAsc(projectId)
                .stream()
                .map(projectMapper::toPaymentResponse)
                .toList();
    }

    // ── uploadDocument / deleteDocument ──────────────────────────────────────

    @Override
    @Transactional
    public void uploadDocument(UUID projectId, MultipartFile file,
                               String label, UserDetailsImpl currentUser) {
        Project project = findOrThrow(projectId);
        String relativePath = fileStorageService.store(
                file, "projects/" + projectId + "/docs");

        ProjectDocument doc = new ProjectDocument();
        doc.setProject(project);
        doc.setLabel(label);
        doc.setFileUrl(relativePath);
        doc.setFileType(fileStorageService.resolveFileType(file));
        doc.setUploadedBy(userRepository.getReferenceById(currentUser.getId()));
        documentRepository.save(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID projectId, UUID docId) {
        ProjectDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu", docId));
        fileStorageService.delete(doc.getFileUrl());
        documentRepository.delete(doc);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Project findOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", id));
    }

    private Customer resolveCustomerFromConsultationOrRequest(
            Consultation consultation,
            ConvertToProjectRequest request,
            UserDetailsImpl currentUser) {

        if (request.getCustomerId() != null) {
            return customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Khách hàng", request.getCustomerId()));
        }

        if (request.getNewCustomer() != null) {
            ConvertToProjectRequest.NewCustomerInfo info = request.getNewCustomer();
            Customer customer = new Customer();
            customer.setFullName(info.getFullName());
            customer.setPhone(info.getPhone());
            customer.setAddress(info.getAddress());
            customer.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));
            return customerRepository.save(customer);
        }

        if (consultation.getCustomer() != null) {
            return consultation.getCustomer();
        }

        Customer customer = new Customer();
        customer.setFullName(consultation.getCustomerName());
        customer.setPhone(consultation.getCustomerPhone());
        customer.setAddress(consultation.getSiteAddress());
        customer.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));
        return customerRepository.save(customer);
    }

  
}