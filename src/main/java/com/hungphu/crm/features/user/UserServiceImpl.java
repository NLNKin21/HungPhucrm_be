package com.hungphu.crm.features.user;

import com.hungphu.crm.features.user.dto.*;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.mapper.UserMapper;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.enums.UserRole;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.mail.EmailService;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // ✅ thêm method này
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getManagers() {
        return userRepository.findByRoleAndActiveTrue(UserRole.MANAGER).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request, UserDetailsImpl currentUser) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại", HttpStatus.CONFLICT, "USER_002");
        }

        String plainPassword = generateRandomPassword();

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDob(request.getDob());
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(request.getRole());
        user.setCreatedBy(userRepository.getReferenceById(currentUser.getId()));

        User manager = resolveManagerForCreate(request);
        user.setManager(manager);

        UserResponse response = userMapper.toResponse(userRepository.save(user));
        log.info("User {} created by {}", request.getEmail(), currentUser.getId());

        emailService.sendWelcomeEmail(request.getEmail(), request.getFullName(), plainPassword);

        return response;
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findOrThrow(id);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (user.getRole() == UserRole.EMPLOYEE) {
            if (request.isClearManager()) {
                user.setManager(null);
            } else if (request.getManagerId() != null) {
                User manager = userRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", request.getManagerId()));

                if (manager.getRole() != UserRole.MANAGER) {
                    throw new BusinessException(
                            "Người quản lý phải có vai trò MANAGER",
                            HttpStatus.BAD_REQUEST,
                            "USER_005"
                    );
                }

                if (!manager.isActive()) {
                    throw new BusinessException(
                            "Người quản lý phải đang hoạt động",
                            HttpStatus.BAD_REQUEST,
                            "USER_006"
                    );
                }

                user.setManager(manager);
            }
        } else {
            user.setManager(null);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateRole(UUID id, UpdateUserRoleRequest request) {
        User user = findOrThrow(id);
        user.setRole(request.getRole());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateActive(UUID id, boolean active) {
        User user = findOrThrow(id);
        user.setActive(active);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findMyEmployees(UserDetailsImpl currentUser) {
        return userRepository.findByManagerId(currentUser.getId()).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignableEmployees(UserDetailsImpl currentUser) {
        List<User> users = userRepository.findByRoleInAndActiveTrue(
                List.of(UserRole.ADMIN, UserRole.MANAGER, UserRole.EMPLOYEE)
        );

        return users.stream()
                .sorted((a, b) -> {
                    // Ưu tiên sort theo role rồi đến tên
                    int roleOrderA = getRoleOrder(a.getRole());
                    int roleOrderB = getRoleOrder(b.getRole());

                    if (roleOrderA != roleOrderB) {
                        return Integer.compare(roleOrderA, roleOrderB);
                    }

                    String nameA = a.getFullName() != null ? a.getFullName() : "";
                    String nameB = b.getFullName() != null ? b.getFullName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                })
                .map(userMapper::toResponse)
                .toList();
    }

    private int getRoleOrder(UserRole role) {
        return switch (role) {
            case ADMIN -> 1;
            case MANAGER -> 2;
            case EMPLOYEE -> 3;
        };
    }

    private User resolveManagerForCreate(CreateUserRequest request) {
        if (request.getRole() != UserRole.EMPLOYEE) {
            if (request.getManagerId() != null) {
                throw new BusinessException(
                        "Chỉ nhân viên mới cần chọn người quản lý",
                        HttpStatus.BAD_REQUEST,
                        "USER_003"
                );
            }
            return null;
        }

        if (request.getManagerId() == null) {
            throw new BusinessException(
                    "Nhân viên phải có người quản lý",
                    HttpStatus.BAD_REQUEST,
                    "USER_004"
            );
        }

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getManagerId()));

        if (manager.getRole() != UserRole.MANAGER) {
            throw new BusinessException(
                    "Người quản lý phải có vai trò MANAGER",
                    HttpStatus.BAD_REQUEST,
                    "USER_005"
            );
        }

        if (!manager.isActive()) {
            throw new BusinessException(
                    "Người quản lý phải đang hoạt động",
                    HttpStatus.BAD_REQUEST,
                    "USER_006"
            );
        }

        return manager;
    }
}