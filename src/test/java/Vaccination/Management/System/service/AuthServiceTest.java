package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.auth.CreateLoginRequest;
import Vaccination.Management.System.model.dto.auth.CreateRegisterRequest;
import Vaccination.Management.System.model.dto.auth.LoginResponse;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.repository.UserRepository;
import Vaccination.Management.System.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // register

    @Test
    void register_success_whenPhoneAndEmailAreNew() {
        CreateRegisterRequest request = buildRegisterRequest("0901234567", "password123", "Nguyen Van A", "a@example.com");
        when(userRepository.existsByPhone("0901234567")).thenReturn(false);
        when(userRepository.existsByEmail("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsUserExisted_whenPhoneAlreadyInUse() {
        CreateRegisterRequest request = buildRegisterRequest("0901234567", "password123", "Nguyen Van A", null);
        when(userRepository.existsByPhone("0901234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsEmailExisted_whenEmailAlreadyInUse() {
        CreateRegisterRequest request = buildRegisterRequest("0901234567", "password123", "Nguyen Van A", "dup@example.com");
        when(userRepository.existsByPhone("0901234567")).thenReturn(false);
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.EMAIL_EXISTED));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_nullEmail_skipsEmailCheck_andSavesUser() {
        CreateRegisterRequest request = buildRegisterRequest("0901234567", "password123", "Nguyen Van A", null);
        when(userRepository.existsByPhone("0901234567")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        authService.register(request);

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_savesUserWithCitizenRoleAndEncodedPassword() {
        CreateRegisterRequest request = buildRegisterRequest("0901234567", "password123", "Nguyen Van A", null);
        when(userRepository.existsByPhone("0901234567")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.CITIZEN);
        assertThat(saved.getPassword()).isEqualTo("hashed_password");
        assertThat(saved.getPhone()).isEqualTo("0901234567");
        assertThat(saved.getFullName()).isEqualTo("Nguyen Van A");
    }

    // login

    @Test
    void login_success_returnsTokenAndRole() {
        CreateLoginRequest request = buildLoginRequest("0901234567", "password123");
        User user = buildUser("0901234567", UserRole.CITIZEN);
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("0901234567", "CITIZEN")).thenReturn("jwt.token.here");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getRole()).isEqualTo("CITIZEN");
    }

    @Test
    void login_throwsInvalidCredentials_whenAuthenticationFails() {
        CreateLoginRequest request = buildLoginRequest("0901234567", "wrong_password");
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_throwsInvalidCredentials_whenUserNotFoundAfterAuth() {
        CreateLoginRequest request = buildLoginRequest("0901234567", "password123");
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_success_medicalStaffRoleReflectedInResponse() {
        CreateLoginRequest request = buildLoginRequest("0901234567", "password123");
        User user = buildUser("0901234567", UserRole.MEDICAL_STAFF);
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("0901234567", "MEDICAL_STAFF")).thenReturn("staff.token");

        LoginResponse response = authService.login(request);

        assertThat(response.getRole()).isEqualTo("MEDICAL_STAFF");
        assertThat(response.getToken()).isEqualTo("staff.token");
    }

    // helpers

    private CreateRegisterRequest buildRegisterRequest(String phone, String password,
                                                        String fullName, String email) {
        CreateRegisterRequest req = new CreateRegisterRequest();
        req.setPhone(phone);
        req.setPassword(password);
        req.setFullName(fullName);
        req.setEmail(email);
        return req;
    }

    private CreateLoginRequest buildLoginRequest(String phone, String password) {
        CreateLoginRequest req = new CreateLoginRequest();
        req.setPhone(phone);
        req.setPassword(password);
        return req;
    }

    private User buildUser(String phone, UserRole role) {
        return User.builder()
                .id(1L)
                .phone(phone)
                .password("hashed")
                .fullName("Test User")
                .role(role)
                .active(true)
                .build();
    }
}
