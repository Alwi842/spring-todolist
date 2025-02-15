package com.example.todolist.service;

import com.example.todolist.dto.request.UserRequest;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Initialize BCrypt
    }
    public List<UserResponse> findAll() {
        try{
            return userRepository.findAll()
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data users",e);
        }
    }
    public UserResponse findByUsername(String username) {
        try {
            return userRepository.findByUsername(username)
                    .map(this::convertToResponse)
                    .orElseThrow(() -> new DataNotFoundException("User not found with username " + username));
        } catch (DataNotFoundException e) {
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("Failed to find user by username "+e.getMessage());
        }
    }
    public UserResponse create(UserRequest userRequest) {
        try {
            User user = new User();
            user.setUsername(userRequest.getUsername());
            user.setEmail(userRequest.getEmail());
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            user.setRole(userRequest.getRole());

            user = userRepository.save(user);
            return convertToResponse(user);

        } catch (DataIntegrityViolationException e) { // Catches duplicate entry errors
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }
    public UserResponse update(String username, UserRequest userRequest) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new DataNotFoundException("User not found with username: " + username));

            if (userRequest.getUsername() != null) user.setUsername(userRequest.getUsername());
            if (userRequest.getEmail() != null) user.setEmail(userRequest.getEmail());
            if (userRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            if (userRequest.getRole() != null) user.setRole(userRequest.getRole());

            user = userRepository.save(user);

            return convertToResponse(user);

        } catch (DataNotFoundException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDataException("Email or username already exists."); // 409 Conflict
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage()); // 500 Internal Server Error
        }
    }

    public void delete(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new DataNotFoundException("User not found with username: " + username));
            userRepository.delete(user);
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    private UserResponse convertToResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
