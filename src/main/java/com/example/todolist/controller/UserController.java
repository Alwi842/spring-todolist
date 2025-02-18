package com.example.todolist.controller;

import com.example.todolist.config.SecurityConfig;
import com.example.todolist.dto.request.LoginRequest;
import com.example.todolist.dto.request.UserRequest;
import com.example.todolist.dto.response.ApiResponse;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.service.UserService;
import com.example.todolist.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register") //localhost:8080/api/user/register
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest){
        try {
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), userService.registerUser(userRequest)));
        } catch(DuplicateDataException e){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT.value())
                    .body(new ApiResponse<>(HttpStatus.CONFLICT.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PostMapping("/login") //localhost:8080/api/user/login
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
//            );
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            //cara ke 2 menggunakan service
            UserResponse userDetails = userService.loginUser(loginRequest);


            String token = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), token));
        } catch(RuntimeException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}