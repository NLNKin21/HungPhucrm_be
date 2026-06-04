package com.hungphu.crm.features.auth;

import com.hungphu.crm.features.auth.dto.*;
import com.hungphu.crm.features.auth.entity.PasswordResetToken;
import com.hungphu.crm.features.auth.repository.PasswordResetTokenRepository;
import com.hungphu.crm.features.user.dto.UserResponse;
import com.hungphu.crm.features.user.entity.User;
import com.hungphu.crm.features.user.mapper.UserMapper;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.mail.EmailService;
import com.hungphu.crm.shared.security.JwtUtil;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
            return buildLoginResponse(principal);
        } catch (BadCredentialsException e) {
            throw new BusinessException("Sai email hoặc mật khẩu", HttpStatus.UNAUTHORIZED, "AUTH_001");
        }
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new BusinessException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED, "AUTH_003");
        }
        var auth = jwtUtil.getAuthentication(request.getRefreshToken());
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại", HttpStatus.UNAUTHORIZED, "AUTH_003"));

        UserDetailsImpl details = new UserDetailsImpl(
                user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(), user.isActive());

        return TokenResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(details))
                .refreshToken(jwtUtil.generateRefreshToken(details))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST, "AUTH_005");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            resetTokenRepository.deleteUnusedByUserId(user.getId());

            String rawToken = UUID.randomUUID().toString();
            String tokenHash = sha256(rawToken);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(tokenHash);
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            resetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            log.info("Password reset requested for user {}", user.getId());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256(request.getToken());

        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST, "AUTH_006"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST, "AUTH_006");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
        log.info("Password reset completed for user {}", user.getId());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private LoginResponse buildLoginResponse(UserDetailsImpl principal) {
        return LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(principal))
                .refreshToken(jwtUtil.generateRefreshToken(principal))
                .user(LoginResponse.UserInfo.builder()
                        .id(principal.getId())
                        .fullName(principal.getUsername())
                        .email(principal.getEmail())
                        .role(principal.getRole())
                        .build())
                .build();
    }
}
