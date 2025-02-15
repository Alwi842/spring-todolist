package com.example.todolist.controller;

import com.example.todolist.dto.request.UserRequest;
import com.example.todolist.dto.response.ApiResopnse;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    //reads
    @GetMapping
    public ResponseEntity<?> getAllUser() {
        try{
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), userService.findAll()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @GetMapping("/user")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), userService.findByUsername(username)));  // localhost:8080/api/users/username?username={username}")
        } catch(DataNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    //create
    @PostMapping
    public ResponseEntity<?> createUsers(@RequestBody List<UserRequest> userRequests) {
        try {
            List<UserResponse> createdUsers = new ArrayList<>();

            for (UserRequest userRequest : userRequests) {
                createdUsers.add(userService.create(userRequest)); // Call the service for each user
            }

            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResopnse<>(HttpStatus.CREATED.value(), createdUsers));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(new ApiResopnse<>(HttpStatus.CONFLICT.value(), e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserRequest userRequest) {
        try {
            UserResponse updatedUser = userService.update(username, userRequest);
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), updatedUser));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(new ApiResopnse<>(HttpStatus.CONFLICT.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            userService.delete(username);
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), "User deleted successfully"));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}