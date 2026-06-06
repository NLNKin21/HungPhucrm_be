package com.hungphu.crm.features.consultation;

import com.hungphu.crm.features.consultation.dto.*;
import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.consultation.event.ConsultationSuccessEvent;
import com.hungphu.crm.features.consultation.mapper.ConsultationMapper;
import com.hungphu.crm.features.consultation.repository.ConsultationRepository;
import com.hungphu.crm.features.customer.repository.CustomerRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.PriorityLevel;
import com.hungphu.crm.shared.enums.UserRole;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.response.PageMeta;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository   consultationRepository;
    private final ConsultationMapper       consultationMapper;
    private final UserRepository           userRepository;
    private final CustomerRepository       customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ────────────────────────────────────────────────
    // FIND ALL
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ConsultationResponse>> findAll(ConsultationStatus status,
                                                           UUID assignedToId,
                                                           UUID assignedById,
                                                           PriorityLevel priority,
                                                           Pageable pageable,
                                                           UserDetailsImpl currentUser) {
        UserRole role = currentUser.getRole();
        UUID effectiveAssignedToId = assignedToId;
        UUID effectiveAssignedById = assignedById;

        if (role == UserRole.EMPLOYEE) {
            effectiveAssignedToId = currentUser.getId();
            effectiveAssignedById = null;
        } else if (role == UserRole.MANAGER) {
            effectiveAssignedById = currentUser.getId();
        }

        Page<Consultation> page = consultationRepository.findByFilters(
                status, effectiveAssignedToId, effectiveAssignedById, priority, pageable);

        List<ConsultationResponse> data = page.getContent().stream()
                .map(consultationMapper::toResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .page(pageable.getPageNumber() + 1)
                .limit(pageable.getPageSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ApiResponse.success(data, meta);
    }

    // ────────────────────────────────────────────────
    // FIND BY ID
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse findById(UUID id) {
        return consultationMapper.toResponse(findOrThrow(id));
    }

    // ────────────────────────────────────────────────
    // CREATE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public ConsultationResponse create(CreateConsultationRequest request, UserDetailsImpl currentUser) {
        UUID assignedByUUID = (currentUser.getRole() == UserRole.ADMIN && request.getAssignedById() != null)
                ? request.getAssignedById()
                : currentUser.getId();

        Consultation consultation = new Consultation();
        consultation.setCustomerName(request.getCustomerName());
        consultation.setCustomerPhone(request.getCustomerPhone());
        consultation.setSiteAddress(request.getSiteAddress());
        consultation.setPriority(request.getPriority());
        consultation.setPrice(request.getPrice());
        consultation.setNotes(request.getNotes());
        consultation.setAssignedBy(userRepository.getReferenceById(assignedByUUID));
        consultation.setStatus(ConsultationStatus.DA_TIEP_NHAN);
        consultation.setAcceptedAt(LocalDateTime.now());

        if (request.getCustomerId() != null) {
            consultation.setCustomer(customerRepository.getReferenceById(request.getCustomerId()));
        }

        log.info("Consultation created by {}", currentUser.getId());
        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    // ────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public ConsultationResponse update(UUID id, UpdateConsultationRequest request, UserDetailsImpl currentUser) {
        Consultation consultation = findOrThrow(id);

        if (currentUser.getRole() == UserRole.MANAGER) {
            boolean isOwner = consultation.getAssignedBy() != null
                    && consultation.getAssignedBy().getId().equals(currentUser.getId());
            if (!isOwner) {
                throw new AccessDeniedException("Bạn không có quyền chỉnh sửa số tư vấn này");
            }
        }

        if (StringUtils.hasText(request.getCustomerName()))  consultation.setCustomerName(request.getCustomerName());
        if (StringUtils.hasText(request.getCustomerPhone())) consultation.setCustomerPhone(request.getCustomerPhone());
        if (request.getSiteAddress() != null) consultation.setSiteAddress(request.getSiteAddress());
        if (request.getPriority()    != null) consultation.setPriority(request.getPriority());
        if (request.getPrice()       != null) consultation.setPrice(request.getPrice());
        if (request.getNotes()       != null) consultation.setNotes(request.getNotes());
        if (request.getAssignedToId() != null) {
            consultation.setAssignedTo(userRepository.getReferenceById(request.getAssignedToId()));
        }
        if (request.getAssignedById() != null) {
            consultation.setAssignedBy(userRepository.getReferenceById(request.getAssignedById()));
        }

        log.info("Consultation {} updated by {}", id, currentUser.getId());
        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    // ────────────────────────────────────────────────
    // UPDATE STATUS
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public ConsultationResponse updateStatus(UUID id,
                                            UpdateConsultationStatusRequest request,
                                            UserDetailsImpl currentUser) {
        Consultation consultation = findOrThrow(id);

        UserRole role = currentUser.getRole();
        ConsultationStatus fromStatus = consultation.getStatus();
        ConsultationStatus toStatus = request.getStatus();

        validatePermissionToUpdateStatus(consultation, currentUser);
        validateStatusTransition(fromStatus, toStatus, role);
        validateStatusPayload(request, toStatus);

        // set status
        consultation.setStatus(toStatus);

        // price: chỉ bắt buộc khi sang DANG_BAO_GIA
        if (toStatus == ConsultationStatus.DANG_BAO_GIA) {
            consultation.setPrice(request.getPrice());
            consultation.setElevatorType(request.getElevatorType());
            consultation.setProjectType(request.getProjectType());
        }

        // failureReason
        if (toStatus == ConsultationStatus.THAT_BAI) {
            consultation.setFailureReason(request.getFailureReason().trim());
        } else {
            // rời trạng thái thất bại thì clear lý do
            consultation.setFailureReason(null);
        }

        if (toStatus == ConsultationStatus.THANH_CONG) {
            eventPublisher.publishEvent(new ConsultationSuccessEvent(consultation));
        }

        log.info("Consultation {} status changed from {} to {} by {}",
                id, fromStatus, toStatus, currentUser.getId());

        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    /**
     * Validate luồng chuyển trạng thái hợp lệ.
     */

    // ────────────────────────────────────────────────
    // STATS
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ConsultationStatsResponse getStats(UserDetailsImpl currentUser) {
        UUID scopeManagerId = currentUser.getRole() == UserRole.MANAGER ? currentUser.getId() : null;

        List<Object[]> statusRows = consultationRepository.countByStatus(scopeManagerId);
        Map<ConsultationStatus, Long> byStatus = new EnumMap<>(ConsultationStatus.class);
        long total = 0, successCount = 0, totalActive = 0;

        for (Object[] row : statusRows) {
            ConsultationStatus s = (ConsultationStatus) row[0];
            long count = (Long) row[1];
            byStatus.put(s, count);
            total += count;
            if (s == ConsultationStatus.THANH_CONG) successCount = count;
        }

        int successRate = total == 0 ? 0 : (int) (successCount * 100 / total);

        List<Object[]> empRows = consultationRepository.findEmployeeStats(scopeManagerId);
        List<ConsultationStatsResponse.EmployeeStats> byEmployee = empRows.stream()
                .map(row -> {
                    User u = (User) row[0];
                    return ConsultationStatsResponse.EmployeeStats.builder()
                            .userId(u.getId())
                            .fullName(u.getFullName())
                            .total((Long) row[1])
                            .success((Long) row[2])
                            .inProgress((Long) row[3])
                            .build();
                })
                .toList();

        return ConsultationStatsResponse.builder()
                .byStatus(byStatus)
                .totalActive(totalActive)
                .successRate(successRate)
                .byEmployee(byEmployee)
                .build();
    }

    // ────────────────────────────────────────────────
    // DELETE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID id) {
        Consultation consultation = findOrThrow(id);
            if (consultation.getStatus() != ConsultationStatus.DA_TIEP_NHAN) {
        throw new BusinessException(
                "Không thể xóa số tư vấn đang được xử lý.",
                HttpStatus.BAD_REQUEST,
                "CONS_002"
        );
    }
        consultationRepository.delete(consultation);
    }

    // ────────────────────────────────────────────────
    // HELPER
    // ────────────────────────────────────────────────

    private Consultation findOrThrow(UUID id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tư vấn", id));
    }

    private void validatePermissionToUpdateStatus(Consultation consultation, UserDetailsImpl currentUser) {
        UserRole role = currentUser.getRole();

        if (role == UserRole.ADMIN) {
            return;
        }

        if (role == UserRole.EMPLOYEE) {
            boolean isAssignee = consultation.getAssignedTo() != null
                    && consultation.getAssignedTo().getId().equals(currentUser.getId());

            if (!isAssignee) {
                throw new AccessDeniedException("Bạn không có quyền cập nhật số tư vấn này");
            }
            return;
        }

        if (role == UserRole.MANAGER) {
            boolean isOwner = consultation.getAssignedBy() != null
                    && consultation.getAssignedBy().getId().equals(currentUser.getId());

            if (!isOwner) {
                throw new AccessDeniedException("Bạn không có quyền cập nhật số tư vấn này");
            }
        }
    }

    private void validateStatusTransition(ConsultationStatus from,
                                        ConsultationStatus to,
                                        UserRole role) {
        boolean valid = switch (from) {
            case DA_TIEP_NHAN ->
                    to == ConsultationStatus.DA_LIEN_LAC
                        || to == ConsultationStatus.CHUA_LIEN_LAC_DUOC
                        || to == ConsultationStatus.KHONG_CO_NHU_CAU;

                case DA_LIEN_LAC ->
                    to == ConsultationStatus.CHUA_LIEN_LAC_DUOC
                        || to == ConsultationStatus.KHONG_CO_NHU_CAU
                        || to == ConsultationStatus.DANG_BAO_GIA;

                case CHUA_LIEN_LAC_DUOC ->
                    to == ConsultationStatus.DA_LIEN_LAC
                        || to == ConsultationStatus.KHONG_CO_NHU_CAU;

                case KHONG_CO_NHU_CAU ->
                    to == ConsultationStatus.DA_LIEN_LAC
                        && (role == UserRole.MANAGER || role == UserRole.ADMIN);

            case DANG_BAO_GIA ->
                    to == ConsultationStatus.THANH_CONG
                            || to == ConsultationStatus.THAT_BAI;

            case THAT_BAI ->
                    to == ConsultationStatus.DANG_BAO_GIA
                            && (role == UserRole.MANAGER || role == UserRole.ADMIN);

            case THANH_CONG ->
                    false;

            case DA_CHUYEN_DU_AN ->
                    false;
        };

        if (!valid) {
            throw new BusinessException(
                    String.format("Không thể chuyển từ '%s' sang '%s'", from, to),
                    HttpStatus.BAD_REQUEST,
                    "CONS_005"
            );
        }
    }

    private void validateStatusPayload(UpdateConsultationStatusRequest request,
                                    ConsultationStatus toStatus) {
        if (toStatus == ConsultationStatus.DANG_BAO_GIA) {
            if (request.getPrice() == null || request.getPrice().signum() <= 0) {
                throw new BusinessException(
                        "Cần nhập báo giá hợp lệ khi chuyển sang trạng thái Đang báo giá",
                        HttpStatus.BAD_REQUEST,
                        "CONS_006"
                );
            }
            // ── Validate thêm 2 field mới ──────────────────────────────────
            if (request.getElevatorType() == null) {
                throw new BusinessException(
                        "Cần chọn loại thang máy khi chuyển sang trạng thái Đang báo giá",
                        HttpStatus.BAD_REQUEST,
                        "CONS_007"
                );
            }
            if (request.getProjectType() == null) {
                throw new BusinessException(
                        "Cần chọn loại dự án khi chuyển sang trạng thái Đang báo giá",
                        HttpStatus.BAD_REQUEST,
                        "CONS_008"
                );
            }
            // ────────────────────────────────────────────────────────────────
        }
    
        if (toStatus == ConsultationStatus.THAT_BAI) {
            if (!StringUtils.hasText(request.getFailureReason())) {
                throw new BusinessException(
                        "Cần nhập lý do thất bại",
                        HttpStatus.BAD_REQUEST,
                        "CONS_004"
                );
            }
        }
    }
}