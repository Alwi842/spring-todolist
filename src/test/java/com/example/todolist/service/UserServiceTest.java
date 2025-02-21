package com.example.todolist.service;

import com.example.todolist.dto.request.LoginRequest;
import com.example.todolist.dto.request.UserRequest;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private LoginRequest loginRequest;
    private UserRequest registerRequest;
    private User user;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        registerRequest = new UserRequest();
        registerRequest.setUsername("alwi");
        registerRequest.setPassword("123");
        registerRequest.setEmail("alwi@gmail.com");
        registerRequest.setRole("Admin");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("alwi");
        loginRequest.setPassword("123");
        
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("alwi");
        user.setPassword(passwordEncoder.encode("123"));
        user.setEmail("alwi@gmail.com");
        user.setRole("Admin");

    }
    @Test
    public void testRegisterUser_success() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse userResponse = userService.registerUser(registerRequest);

        assertThat(userResponse).isNotNull();
        assertThat(user.getUsername()).isEqualTo(userResponse.getUsername());
        assertThat(user.getEmail()).isEqualTo(userResponse.getEmail());
        verify(userRepository,times(1)).save(any(User.class));
    }
    //loginuser
    @Test
    public void testLoginUser_success() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        System.out.println(loginRequest.getPassword());
        System.out.println(user.getPassword());
        UserResponse userResponse = userService.loginUser(loginRequest);
        assertThat(userResponse).isNotNull(); //memastikan response tidak null
        assertThat(user.getUsername()).isEqualTo(userResponse.getUsername());
        assertThat(user.getEmail()).isEqualTo(userResponse.getEmail());
        verify(userRepository,times(1)).findByUsername(loginRequest.getUsername());
    }
}
