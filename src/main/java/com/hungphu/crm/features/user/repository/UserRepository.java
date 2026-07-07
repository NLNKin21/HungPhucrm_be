package com.hungphu.crm.features.user.repository;

import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.shared.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    
    List<User> findByRoleIn(List<UserRole> roles);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    List<User> findByRole(UserRole role);

    List<User> findByManagerId(UUID managerId);

    // Dùng cho ADMIN: lấy tất cả EMPLOYEE active
    List<User> findByRoleAndActiveTrue(UserRole role);
    List<User> findByRoleInAndActiveTrue(List<UserRole> roles);

}
