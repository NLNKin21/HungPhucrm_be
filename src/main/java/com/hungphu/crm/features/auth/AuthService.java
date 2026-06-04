package com.hungphu.crm.features.auth;

import com.hungphu.crm.features.auth.dto.*;
import com.hungphu.crm.features.user.dto.UserResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenResponse refresh(RefreshTokenRequest request);

    UserResponse me(UserDetailsImpl currentUser);

    void changePassword(ChangePasswordRequest request, UserDetailsImpl currentUser);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
