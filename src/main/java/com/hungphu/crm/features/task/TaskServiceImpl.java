package com.hungphu.crm.features.task;

import com.hungphu.crm.features.project.entity.Project;
import com.hungphu.crm.features.project.repository.ProjectRepository;
import com.hungphu.crm.features.task.dto.*;
import com.hungphu.crm.features.task.entity.Task;
import com.hungphu.crm.features.task.entity.TaskEvidence;
import com.hungphu.crm.features.task.entity.TaskMember;
import com.hungphu.crm.features.task.mapper.TaskMapper;
import com.hungphu.crm.features.task.repository.TaskEvidenceRepository;
import com.hungphu.crm.features.task.repository.TaskRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.TaskMemberRole;
import com.hungphu.crm.shared.enums.TaskStatus;
import com.hungphu.crm.shared.enums.UserRole;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.response.PageMeta;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import com.hungphu.crm.shared.storage.FileStorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskEvidenceRepository evidenceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final FileStorageService fileStorageService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
            TaskStatus.CHUA_THUC_HIEN, Set.of(TaskStatus.DANG_THUC_HIEN),
            TaskStatus.DANG_THUC_HIEN, Set.of(TaskStatus.CHO_DANH_GIA),
            TaskStatus.CHO_DANH_GIA,   Set.of(TaskStatus.HOAN_THANH, TaskStatus.TU_CHOI)
    );

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findByProject(UUID projectId) {
        return taskRepository.findByProjectIdWithDetails(projectId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> findByProjectWithFilters(UUID projectId,
                                                                    UUID assigneeId,
                                                                    TaskStatus status,
                                                                    LocalDate deadlineFrom,
                                                                    LocalDate deadlineTo,
                                                                    Pageable pageable) {
        Page<Task> page = taskRepository.findByProjectWithFilters(
                projectId, assigneeId, status, deadlineFrom, deadlineTo, pageable);
        return toPagedResponse(page, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> findMyTasks(TaskStatus status,
                                                       Pageable pageable,
                                                       UserDetailsImpl currentUser) {
        Page<Task> page = taskRepository.findMyTasks(currentUser.getId(), status, pageable);
        return toPagedResponse(page, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> findMyEmployeesTasks(TaskStatus status,
                                                                Pageable pageable,
                                                                UserDetailsImpl currentUser) {
        Page<Task> page = taskRepository.findMyEmployeesTasks(currentUser.getId(), status, pageable);
        return toPagedResponse(page, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> findAll(TaskStatus status,
                                                   UUID projectId,
                                                   UUID assignedTo,
                                                   Pageable pageable) {
        Page<Task> page = taskRepository.findAllByFilters(status, projectId, assignedTo, pageable);
        return toPagedResponse(page, pageable);
    }

    private ApiResponse<List<TaskResponse>> toPagedResponse(Page<Task> page, Pageable pageable) {
        List<TaskResponse> data = page.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .page(pageable.getPageNumber() + 1)
                .limit(pageable.getPageSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ApiResponse.success(data, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id, UserDetailsImpl currentUser) {
        Task task = findOrThrow(id);

        if (currentUser.getRole() == UserRole.EMPLOYEE
                && !isTaskParticipant(task, currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem task này");
        }

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse create(UUID projectId, CreateTaskRequest request, UserDetailsImpl currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", projectId));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setSiteAddress(request.getSiteAddress());
        task.setDeadline(request.getDeadline());
        task.setTaskType(request.getTaskType());
        task.setAssignedBy(userRepository.getReferenceById(currentUser.getId()));

        if (request.getSupervisorId() != null) {
            task.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        }

        replaceTaskMembers(task, request.getAssignedTo(), request.getMemberIds());

        log.info("Creating task for project {} by {}", projectId, currentUser.getId());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request, UserDetailsImpl currentUser) {
        Task task = findOrThrow(id);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getSiteAddress() != null) task.setSiteAddress(request.getSiteAddress());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getTaskType() != null) task.setTaskType(request.getTaskType());

        if (request.isClearSupervisor()) {
            task.setSupervisor(null);
        } else if (request.getSupervisorId() != null) {
            task.setSupervisor(userRepository.getReferenceById(request.getSupervisorId()));
        }

        if (request.getAssignedTo() != null || request.getMemberIds() != null) {
            UUID effectiveLeadId = request.getAssignedTo() != null
                    ? request.getAssignedTo()
                    : task.getAssignedTo().getId();

            List<UUID> effectiveMemberIds = request.getMemberIds() != null
                    ? request.getMemberIds()
                    : task.getMembers().stream()
                            .filter(m -> m.getMemberRole() == TaskMemberRole.MEMBER)
                            .map(m -> m.getUser().getId())
                            .toList();

            replaceTaskMembers(task, effectiveLeadId, effectiveMemberIds);
        }

        log.info("Task {} updated by {}", id, currentUser.getId());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(UUID id,
                                     UpdateTaskStatusRequest request,
                                     UserDetailsImpl currentUser) {
        Task task = findOrThrow(id);

        validatePermissionToUpdateStatus(task, currentUser);
        validateTransition(task.getStatus(), request.getStatus());

        if (request.getStatus() == TaskStatus.CHO_DANH_GIA && task.getEvidences().isEmpty()) {
            throw new BusinessException(
                    "Cần upload ít nhất 1 minh chứng trước khi chờ đánh giá",
                    HttpStatus.BAD_REQUEST,
                    "TASK_004"
            );
        }

        if (request.getStatus() == TaskStatus.TU_CHOI
                && !StringUtils.hasText(request.getRejectionReason())) {
            throw new BusinessException(
                    "Cần ghi rõ lý do từ chối",
                    HttpStatus.BAD_REQUEST,
                    "TASK_005"
            );
        }

        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.TU_CHOI) {
            task.setRejectionReason(request.getRejectionReason());
        } else {
            task.setRejectionReason(null);
        }

        if (request.getStatus() == TaskStatus.HOAN_THANH) {
            task.setCompletedAt(LocalDateTime.now());
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void addEvidence(UUID taskId, MultipartFile file, UserDetailsImpl currentUser) {
        Task task = findOrThrow(taskId);

        if (!isTaskParticipant(task, currentUser.getId())) {
            throw new AccessDeniedException("Bạn không thuộc nhóm thực hiện task này");
        }

        if (task.getEvidences().size() >= 3) {
            throw new BusinessException(
                    "Tối đa 3 minh chứng mỗi task",
                    HttpStatus.BAD_REQUEST,
                    "TASK_003"
            );
        }

        if (task.getStatus() != TaskStatus.DANG_THUC_HIEN) {
            throw new BusinessException(
                    "Chỉ upload minh chứng khi task đang thực hiện",
                    HttpStatus.BAD_REQUEST,
                    "TASK_002"
            );
        }

        String relativePath = fileStorageService.store(file, "tasks/" + taskId);

        TaskEvidence evidence = new TaskEvidence();
        evidence.setTask(task);
        evidence.setFileUrl(relativePath);
        evidence.setFileType(fileStorageService.resolveFileType(file));
        evidence.setUploadedBy(userRepository.getReferenceById(currentUser.getId()));

        evidenceRepository.save(evidence);
    }

    @Override
    @Transactional
    public void deleteEvidence(UUID taskId, UUID evidenceId) {
        TaskEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Minh chứng", evidenceId));

        fileStorageService.delete(evidence.getFileUrl());
        evidenceRepository.delete(evidence);
    }

    private void validatePermissionToUpdateStatus(Task task, UserDetailsImpl currentUser) {
        UserRole role = currentUser.getRole();

        if (role == UserRole.ADMIN || role == UserRole.MANAGER) {
            return;
        }

        boolean isLead = task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isLead) {
            throw new AccessDeniedException("Chỉ trưởng task mới được cập nhật trạng thái");
        }
    }

    private void validateTransition(TaskStatus current, TaskStatus next) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new BusinessException(
                    "Không thể chuyển từ " + current + " sang " + next,
                    HttpStatus.BAD_REQUEST,
                    "TASK_002"
            );
        }
    }

    private void replaceTaskMembers(Task task, UUID leadUserId, List<UUID> memberIds) {
        User leadUser = getActiveEmployeeOrThrow(leadUserId);

        List<UUID> normalizedMemberIds = Optional.ofNullable(memberIds)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !id.equals(leadUserId))
                .toList();

        List<User> memberUsers = normalizedMemberIds.stream()
                .map(this::getActiveEmployeeOrThrow)
                .toList();

        task.setAssignedTo(leadUser);

        // Xóa toàn bộ members cũ và flush ngay để tránh duplicate key
        // khi INSERT members mới trong cùng transaction
        task.getMembers().clear();
        taskRepository.saveAndFlush(task);  // ép Hibernate DELETE trước
        entityManager.flush();              // đảm bảo DB nhận DELETE

        TaskMember leadMember = new TaskMember();
        leadMember.setTask(task);
        leadMember.setUser(leadUser);
        leadMember.setMemberRole(TaskMemberRole.LEAD);
        task.getMembers().add(leadMember);

        for (User memberUser : memberUsers) {
            TaskMember member = new TaskMember();
            member.setTask(task);
            member.setUser(memberUser);
            member.setMemberRole(TaskMemberRole.MEMBER);
            task.getMembers().add(member);
        }
    }

    private User getActiveEmployeeOrThrow(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        if (user.getRole() != UserRole.EMPLOYEE) {
            throw new BusinessException(
                    "Người thực hiện task phải là nhân viên",
                    HttpStatus.BAD_REQUEST,
                    "TASK_006"
            );
        }

        if (!user.isActive()) {
            throw new BusinessException(
                    "Chỉ được giao task cho nhân viên đang hoạt động",
                    HttpStatus.BAD_REQUEST,
                    "TASK_007"
            );
        }

        return user;
    }

    private boolean isTaskParticipant(Task task, UUID userId) {
        return task.getMembers().stream()
                .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(userId));
    }

    private Task findOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}