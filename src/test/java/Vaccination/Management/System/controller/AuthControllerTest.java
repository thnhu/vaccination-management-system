package Vaccination.Management.System.controller;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.auth.LoginResponse;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import Vaccination.Management.System.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // POST /auth/register

    @Test
    void register_returns201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "password123",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void register_returns409_whenPhoneAlreadyInUse() throws Exception {
        doThrow(new AppException(ErrorCode.USER_EXISTED))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "password123",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("Phone number already in use"));
    }

    @Test
    void register_returns409_whenEmailAlreadyInUse() throws Exception {
        doThrow(new AppException(ErrorCode.EMAIL_EXISTED))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "password123",
                                  "fullName": "Nguyen Van A",
                                  "email": "dup@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1007))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void register_returns400_whenPhoneIsBlank() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "",
                                  "password": "password123",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1005));
    }

    @Test
    void register_returns400_whenPhoneHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "abc123",
                                  "password": "password123",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1005));
    }

    @Test
    void register_returns400_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "short",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1004));
    }

    @Test
    void register_returns400_whenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "",
                                  "fullName": "Nguyen Van A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1004));
    }

    // POST /auth/login

    @Test
    void login_returns200_withTokenAndRole() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("jwt.token.here", "CITIZEN"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.result.role").value("CITIZEN"));
    }

    @Test
    void login_returns401_whenInvalidCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new AppException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": "wrong_password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.message").value("Invalid phone or password"));
    }

    @Test
    void login_returns400_whenPhoneIsBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1005));
    }

    @Test
    void login_returns400_whenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "0901234567",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1004));
    }

    // POST /auth/logout

    @Test
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Success"));
    }
}
