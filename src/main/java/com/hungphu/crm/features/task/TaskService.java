package com.hungphu.crm.features.task;

import com.hungphu.crm.features.task.dto.*;
import com.hungphu.crm.shared.enums.TaskStatus;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    List<TaskResponse> findByProject(UUID projectId);

    ApiResponse<List<TaskResponse>> findByProjectWithFilters(UUID projectId, UUID assigneeId, TaskStatus status,
                                                             LocalDate deadlineFrom,
                                                             LocalDate deadlineTo,
                                                             Pageable pageable);

    ApiResponse<List<TaskResponse>> findMyTasks(TaskStatus status, Pageable pageable, UserDetailsImpl currentUser);

    ApiResponse<List<TaskResponse>> findMyEmployeesTasks(TaskStatus status, Pageable pageable, UserDetailsImpl currentUser);

    ApiResponse<List<TaskResponse>> findAll(TaskStatus status, UUID projectId, UUID assignedTo, Pageable pageable);

    TaskResponse findById(UUID id, UserDetailsImpl currentUser);

    TaskResponse create(UUID projectId, CreateTaskRequest request, UserDetailsImpl currentUser);

    TaskResponse updateTask(UUID id, UpdateTaskRequest request, UserDetailsImpl currentUser);

    TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest request, UserDetailsImpl currentUser);

    void addEvidence(UUID taskId, MultipartFile file, UserDetailsImpl currentUser);

    void deleteEvidence(UUID taskId, UUID evidenceId);
}   