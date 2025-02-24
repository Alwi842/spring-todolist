package com.example.todolist.service;

import com.example.todolist.dto.request.LoginRequest;
import com.example.todolist.dto.request.UserRequest;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Initialize BCrypt
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->new UsernameNotFoundException("User not founc with username " + username));
        return new CustomUserDetails(user);
    }

    @Transactional
    public UserResponse registerUser(UserRequest userRequest){
        if(userRepository.findByUsername(userRequest.getUsername()).isPresent()){
            throw new RuntimeException("Username already eaxist");
        }
        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            throw new RuntimeException("Email already eaxist");
        }
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        if(!passwordEncoder.matches(userRequest.getPassword(), user.getPassword())){
            throw new RuntimeException("Register failed, something went wrong with our system");
        }
        user.setRole(Optional.ofNullable(userRequest.getRole()).orElse("USER"));
        User register = userRepository.save(user);
        return convertToResponse(register);
    }
    @Transactional
    public UserResponse updateUser(UserRequest userRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found with username " + username));

        if (userRequest.getUsername() != null) user.setUsername(userRequest.getUsername());
        if (userRequest.getEmail() != null) user.setEmail(userRequest.getEmail());
        if (userRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        if (userRequest.getRole() != null && user.getRole().equals("ADMIN")) {
            user.setRole(userRequest.getRole());
        }

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }

    public UserResponse loginUser(LoginRequest loginRequest){
        try {
            Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            User user=userOptional.get();
            if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
                throw new RuntimeException("Password not match");
            }

            return convertToResponse(user);
        } catch(RuntimeException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Page<UserResponse> getAllUser(int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userRepository.findAll(pageable);
            return users.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data users",e);
        }
    }
    @Transactional
    public UserResponse deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found with username " + username));
        if(user.getRole().equals("ADMIN")) throw new RuntimeException("You can't delete admin account");
        try {
            userRepository.delete(user);
            return convertToResponse(user);
        } catch(RuntimeException e){
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("Failed to delete user",e);
        }
    }
    public UserResponse getUserByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new DataNotFoundException("User not found with username " + username));
            return convertToResponse(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data user",e);
        }
    }
    @Transactional
    public UserResponse updateUserAdmin(UserRequest userRequest, String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new DataNotFoundException("User not found with username " + username));
            if (userRequest.getUsername() != null) user.setUsername(userRequest.getUsername());
            if (userRequest.getEmail() != null) user.setEmail(userRequest.getEmail());
            if (userRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            if (userRequest.getRole() != null && !user.getRole().equals("ADMIN")) user.setRole(userRequest.getRole());
            User updatedUser = userRepository.save(user);
            return convertToResponse(updatedUser);
        } catch (DataNotFoundException e){
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to update user",e);
        }
    }
    public Page<UserResponse> findByUsernameContainingIgnoreCase(String role, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userRepository.findByUsernameContainingIgnoreCase(role, pageable);
            return users.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data users",e);
        }
    }
    private UserResponse convertToResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
