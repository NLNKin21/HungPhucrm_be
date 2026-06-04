package com.hungphu.crm.features.consultation;

import com.hungphu.crm.features.consultation.dto.*;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.PriorityLevel;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ConsultationService {

    ApiResponse<List<ConsultationResponse>> findAll(ConsultationStatus status,
                                                    UUID assignedToId,
                                                    UUID assignedById,
                                                    PriorityLevel priority,
                                                    Pageable pageable,
                                                    UserDetailsImpl currentUser);

    ConsultationResponse findById(UUID id);

    ConsultationResponse create(CreateConsultationRequest request, UserDetailsImpl currentUser);

    ConsultationResponse update(UUID id, UpdateConsultationRequest request, UserDetailsImpl currentUser);

    ConsultationResponse updateStatus(UUID id, UpdateConsultationStatusRequest request, UserDetailsImpl currentUser);

    ConsultationStatsResponse getStats(UserDetailsImpl currentUser);

    void delete(UUID id);
}