package com.example.todolist.controller;

import com.example.todolist.dto.request.TodolistRequest;
import com.example.todolist.dto.response.ApiResponse;
import com.example.todolist.dto.response.PaginatedResponse;
import com.example.todolist.dto.response.TodolistResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.service.TodolistService;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todolist")
public class TodolistController {
    @Autowired
    private TodolistService todolistService;
    @GetMapping
    private ResponseEntity<?> getAllTodolist(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size){
        try {
            Page<TodolistResponse> response = todolistService.findAll(page,size);
            return ResponseEntity
                    .ok(new PaginatedResponse<>(200, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    private ResponseEntity<?> getTodolistById(@PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResponse<>(HttpStatus.OK.value(), todolistService.findById(id)));
        } catch (DataNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @PostMapping(consumes = "multipart/form-data")
    private ResponseEntity<?> createTodolist(@Valid @ModelAttribute @RequestBody TodolistRequest todolistRequest){
        try{
            TodolistResponse response = todolistService.create(todolistRequest);
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
        } catch (DuplicateDataException e){
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateTodolist(@PathVariable Long id, @Valid @ModelAttribute @RequestBody TodolistRequest todolistRequest){
        try{
            TodolistResponse response = todolistService.updateTodolist(id,todolistRequest);
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
        } catch (DataNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodolist(@PathVariable Long id){
        try{
            todolistService.deleteTodolist(id);
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResponse<>(HttpStatus.OK.value(),"Todolist deleted succesfully"));
        } catch(DataNotFoundException e){
            return ResponseEntity.status((HttpStatus.NOT_FOUND.value()))
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TodolistResponse>>> searchByTitle(@RequestParam String title) {
        List<TodolistResponse> response = todolistService.searchByTitle(title);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TodolistResponse>>> filterByCategory(@RequestParam Long categoryId) {
        List<TodolistResponse> response = todolistService.filterByCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TodolistResponse>>> getTodolistByUserId(@PathVariable UUID userId) {
        List<TodolistResponse> response = todolistService.findByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }
}
