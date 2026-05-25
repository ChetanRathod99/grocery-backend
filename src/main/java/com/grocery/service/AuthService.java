package com.grocery.service;

import com.grocery.dto.*;
import com.grocery.entity.*;
import com.grocery.exception.BadRequestException;
import com.grocery.repository.*;
import com.grocery.security.JwtService;
import com.grocery.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Transactional
    public ApiMessage register(RegisterRequest request) {
        if (users.existsByEmail(request.getEmail())) throw new BadRequestException("Email is already registered");
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(encoder.encode(request.getPassword()));
        user.getRoles().add(roles.findByName(RoleName.USER).orElseThrow());
        issueOtp(user);
        users.save(user);
        emailService.sendOtp(user.getEmail(), "Verify your Grocery Fresh account", user.getOtp());
        return new ApiMessage("Registration successful. Check email for verification OTP.");
    }

    public AuthResponse login(AuthRequest request) {
        User user = users.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email address"));
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Incorrect password. Please try again.");
        }
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        var roleNames = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), roleNames);
    }

    @Transactional
    public ApiMessage verifyEmail(OtpRequest request) {
        User user = users.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Invalid email"));
        validateOtp(user, request.getOtp());
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        return new ApiMessage("Email verified successfully");
    }

    @Transactional
    public ApiMessage forgotPassword(ForgotPasswordRequest request) {
        User user = users.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Email not registered"));
        issueOtp(user);
        emailService.sendOtp(user.getEmail(), "Reset your Grocery Fresh password", user.getOtp());
        return new ApiMessage("Password reset OTP sent");
    }

    @Transactional
    public ApiMessage resetPassword(ResetPasswordRequest request) {
        User user = users.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Invalid email"));
        validateOtp(user, request.getOtp());
        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        return new ApiMessage("Password reset successfully");
    }

    private void issueOtp(User user) {
        user.setOtp(OtpUtil.generate());
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
    }

    private void validateOtp(User user, String otp) {
        if (user.getOtp() == null || user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now()) || !user.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid or expired OTP");
        }
    }
}
