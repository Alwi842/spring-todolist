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
        user.setRole(userRequest.getRole());
        User register = userRepository.save(user);
        return convertToResponse(register);
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
