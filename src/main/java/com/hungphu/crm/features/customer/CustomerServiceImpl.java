package com.hungphu.crm.features.customer;

import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.consultation.repository.ConsultationRepository;
import com.hungphu.crm.features.customer.dto.*;
import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.customer.mapper.CustomerMapper;
import com.hungphu.crm.features.customer.repository.CustomerRepository;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.ConsultationStatus;
import com.hungphu.crm.shared.enums.PriorityLevel;
import com.hungphu.crm.shared.enums.UserRole;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    // Consultation đã kết thúc — không sync vào
    private static final List<ConsultationStatus> CLOSED_STATUSES = List.of(
            ConsultationStatus.THANH_CONG,
            ConsultationStatus.THAT_BAI
    );

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;

    // ────────────────────────────────────────────────
    // READ
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerMapper.toResponse(findOrThrow(id));
    }

    // ────────────────────────────────────────────────
    // CREATE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request, UserDetailsImpl currentUser) {
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setElevatorType(request.getElevatorType());
        customer.setProjectType(request.getProjectType());
        customer.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        User assignedUser = resolveAssignableEmployee(request.getAssignedUserId(), currentUser);
        customer.setAssignedUser(assignedUser);

        Customer saved = customerRepository.save(customer);

        // Tự động tạo consultation nếu có người được giao
        syncConsultation(saved, assignedUser, currentUser);

        log.info("Customer {} created by {}", request.getFullName(), currentUser.getId());
        return customerMapper.toResponse(saved);
    }

    // ────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerResponse update(UUID id, UpdateCustomerRequest request, UserDetailsImpl currentUser) {
        Customer customer = findOrThrow(id);

        if (StringUtils.hasText(request.getFullName())) {
            customer.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getElevatorType() != null) {
            customer.setElevatorType(request.getElevatorType());
        }
        if (request.getProjectType() != null) {
            customer.setProjectType(request.getProjectType());
        }

        User assignedUser = resolveAssignableEmployee(request.getAssignedUserId(), currentUser);
        customer.setAssignedUser(assignedUser);

        Customer saved = customerRepository.save(customer);

        // Đồng bộ lại consultation
        syncConsultation(saved, assignedUser, currentUser);

        return customerMapper.toResponse(saved);
    }

    // ────────────────────────────────────────────────
    // SYNC CONSULTATION
    // ────────────────────────────────────────────────

    private void syncConsultation(Customer customer, User assignedUser, UserDetailsImpl currentUser) {
        // Tìm consultation active của customer (chưa THANH_CONG / THAT_BAI)
        Consultation consultation = consultationRepository
                .findFirstByCustomer_IdAndStatusNotInOrderByCreatedAtDesc(
                        customer.getId(),
                        CLOSED_STATUSES
                )
                .orElse(null);

        User assigner = userRepository.getReferenceById(currentUser.getId());

        // Nếu bỏ giao (assignedUser = null)
        if (assignedUser == null) {
            if (consultation != null) {
                // Cập nhật thông tin khách nhưng clear người nhận
                consultation.setCustomerName(customer.getFullName());
                consultation.setCustomerPhone(customer.getPhone());
                consultation.setSiteAddress(customer.getAddress());
                consultation.setAssignedBy(assigner);
                consultation.setAssignedTo(null);
                consultationRepository.save(consultation);
                log.info("Cleared assignedTo on consultation {} for customer {}",
                        consultation.getId(), customer.getId());
            }
            return;
        }

        // Chưa có consultation active => tạo mới
        if (consultation == null) {
            consultation = new Consultation();
            consultation.setCustomer(customer);
            consultation.setStatus(ConsultationStatus.DA_TIEP_NHAN);
            consultation.setAcceptedAt(LocalDateTime.now());
            consultation.setPriority(PriorityLevel.TRUNG_BINH);
            log.info("Creating new consultation for customer {}", customer.getId());
        }

        UUID previousAssignedToId = consultation.getAssignedTo() != null
                ? consultation.getAssignedTo().getId()
                : null;

        boolean isReassigned = !assignedUser.getId().equals(previousAssignedToId);

        // Cập nhật thông tin khách hàng
        consultation.setCustomerName(customer.getFullName());
        consultation.setCustomerPhone(customer.getPhone());
        consultation.setSiteAddress(customer.getAddress());
        consultation.setAssignedBy(assigner);
        consultation.setAssignedTo(assignedUser);

        // Nếu đổi người phụ trách => reset về CHO_TIEP_NHAN
        if (isReassigned) {
            consultation.setStatus(ConsultationStatus.DA_TIEP_NHAN);
            consultation.setAcceptedAt(LocalDateTime.now());
            consultation.setAcceptedAt(null);
            consultation.setFailureReason(null);
            log.info("Reassigned consultation for customer {} to employee {}",
                    customer.getId(), assignedUser.getId());
        }

        consultationRepository.save(consultation);
    }

    // ────────────────────────────────────────────────
    // VALIDATE ASSIGNED EMPLOYEE
    // ────────────────────────────────────────────────

    private User resolveAssignableEmployee(UUID assignedUserId, UserDetailsImpl currentUser) {
        if (assignedUserId == null) {
            return null;
        }

        User assignedUser = userRepository.findById(assignedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", assignedUserId));

        if (assignedUser.getRole() != UserRole.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được giao khách hàng cho nhân viên"
            );
        }

        if (!assignedUser.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được giao cho nhân viên đang hoạt động"
            );
        }

        boolean isAdmin = hasRole(currentUser, "ADMIN");

        if (!isAdmin) {
            boolean isSubordinate = assignedUser.getManager() != null
                    && assignedUser.getManager().getId().equals(currentUser.getId());

            if (!isSubordinate) {
                throw new AccessDeniedException(
                        "Bạn chỉ được giao khách hàng cho nhân viên thuộc cấp dưới của mình"
                );
            }
        }

        return assignedUser;
    }

    private boolean hasRole(UserDetailsImpl currentUser, String role) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public Customer findOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", id));
    }
}