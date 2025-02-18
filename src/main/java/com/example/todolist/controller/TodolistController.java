package com.example.todolist.controller;

import com.example.todolist.dto.request.TodolistRequest;
import com.example.todolist.dto.response.ApiResponse;
import com.example.todolist.dto.response.PaginatedResponse;
import com.example.todolist.dto.response.TodolistResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.service.TodolistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todolist")
public class TodolistController {
    @Autowired
    private TodolistService todolistService;
    @Value("${file.IMAGE_DIR}")
    private String imageDirectory;
    @Operation(summary = "Get all todolists")
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
    @Operation(summary = "Get todolist by id")
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
    @Operation(summary = "Create todolist")
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
    //hard delete
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
    //soft delete
    @DeleteMapping("/soft/{id}") //localhost:8080/api/todolist/soft/{id}
    public ResponseEntity<?> softDeleteTodolist(@PathVariable Long id){
        try{
            todolistService.softDeleteTodolist(id);
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
    @GetMapping("/image/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        try {
            byte[] image = todolistService.getImageById(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @GetMapping("/file/{name}")
    public ResponseEntity<?> getImageByName(@PathVariable String name) {
        try {
            Path path = Paths.get(imageDirectory + name);
            if(!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "File not found"));
            }
            byte[] image = todolistService.getImageByName(name);
            String fileExtension = name.substring(name.lastIndexOf("."));
            MediaType mediaType = switch (fileExtension) {
                case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
                case ".png" ->  MediaType.IMAGE_PNG;
                default -> MediaType.APPLICATION_OCTET_STREAM;
            };
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}
