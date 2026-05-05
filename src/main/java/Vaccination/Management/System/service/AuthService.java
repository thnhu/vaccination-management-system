package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.DuplicateResourceException;
import Vaccination.Management.System.model.dto.auth.CreateLoginRequest;
import Vaccination.Management.System.model.dto.auth.LoginResponse;
import Vaccination.Management.System.model.dto.auth.CreateRegisterRequest;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.repository.UserRepository;
import Vaccination.Management.System.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(CreateRegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already in use");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(UserRole.CITIZEN)
                .build();
        userRepository.save(user);
    }

    public LoginResponse login(CreateLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(), request.getPassword()
                )
        );
        User user = userRepository.findByPhone(request.getPhone()).orElseThrow();
        String token = jwtUtil.generateToken(user.getPhone(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name());
    }
}
