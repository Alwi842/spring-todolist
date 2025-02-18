package com.example.todolist.service;

import com.example.todolist.dto.request.TodolistRequest;
import com.example.todolist.dto.response.TodolistResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.model.Category;
import com.example.todolist.model.Todolist;
import com.example.todolist.model.User;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TodolistRepository;
import com.example.todolist.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TodolistService {
    @Autowired
    private TodolistRepository todolistRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private static final String imageDirectory = "src/main/resources/static/images/";

    private static final String[] allowedFileTypes = { "image/jpeg", "image/png", "image/jpg" }; //allowed file types
    public Page<TodolistResponse> findAll(int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            Page<Todolist> todolists = todolistRepository.findAllByDeletedAtIsNull(pageable);
            return todolists.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public TodolistResponse findById(Long id) {
        try{
            return todolistRepository.findById(id).map(this::convertToResponse)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found with id "+id));
        } catch(DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }

    @Transactional
    public TodolistResponse create(TodolistRequest todolistRequest) {
        try{
            User user = userRepository.findByUsername(todolistRequest.getUsername())
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
            Category category=categoryRepository.findById(todolistRequest.getCategoryId())
                    .orElseThrow(()-> new DataNotFoundException("Category not found"));
            if (!categoryRepository.existsById(todolistRequest.getCategoryId())){
                throw new DataNotFoundException("Category not found");
            }
            Todolist todolist =new Todolist();
            todolist.setTitle(todolistRequest.getTitle());
            todolist.setDescription(todolistRequest.getDescription());
            todolist.setCategory(category);
            todolist.setUser(user);
            todolist.setIsCompleted(todolistRequest.getIsCompleted());
            if(todolistRequest.getImagePath() != null && !todolistRequest.getImagePath().isEmpty()) {
                MultipartFile file = todolistRequest.getImagePath();
                String customFileName = validateFile(file);

                Path path = Path.of(imageDirectory + customFileName);
                Files.copy(file.getInputStream(), path);
                todolist.setImagePath(customFileName);
            }
            todolist=todolistRepository.save(todolist);
            return convertToResponse(todolist);
        } catch(IllegalArgumentException | DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String validateFile(MultipartFile file) {
        long maxFileSize = 5 * 1024 * 1024;
        if(file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds the maximum limit of " + maxFileSize / (1024 * 1024) + "MB");
        }

        String fileType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : allowedFileTypes) {
            if (allowedType.equals(fileType)) {
                isValidType = true;
                break;
            }
        }

        if(!isValidType) {
            throw new RuntimeException("Invalid file type. Only JPEG, PNG, and JPG files are allowed.");
        }

        String originalFilename = file.getOriginalFilename();
        return "todolist_image" + "_" + originalFilename;
    }


    @Transactional
    public TodolistResponse updateTodolist (Long id, TodolistRequest todolistRequest){
        try {
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found : "+id));

            if (todolistRequest.getTitle()!=null) todolist.setTitle(todolistRequest.getTitle());
            if (todolistRequest.getDescription()!=null) todolist.setDescription(todolistRequest.getDescription());
            if (todolistRequest.getIsCompleted()!=null) todolist.setIsCompleted(todolistRequest.getIsCompleted());
            if (todolistRequest.getCategoryId()!=null) {
                Category category=categoryRepository.findById(todolistRequest.getCategoryId())
                        .orElseThrow(()-> new DataNotFoundException("Category not found"));
                todolist.setCategory(category);
            }
            if (todolistRequest.getUsername()!=null) {
                User user = userRepository.findByUsername(todolistRequest.getUsername())
                        .orElseThrow(() -> new DataNotFoundException("User not found"));
                todolist.setUser(user);
            }
            if(todolistRequest.getImagePath() != null && !todolistRequest.getImagePath().isEmpty()) {
                MultipartFile file = todolistRequest.getImagePath();
                String customFileName = validateFile(file);

                Path path = Path.of(imageDirectory + customFileName);
                Files.copy(file.getInputStream(), path);
                todolist.setImagePath(customFileName);
            }

            todolist = todolistRepository.save(todolist);
            return convertToResponse(todolist);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public void deleteTodolist(Long id){
        try{
            if(!todolistRepository.existsById(id)){
                throw new DataNotFoundException("No todo list with id : "+id);
            }
            todolistRepository.deleteById(id);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //soft delete
    @Transactional
    public void softDeleteTodolist(Long id){
        try{
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found : "+id));
            todolist.setDeletedAt(LocalDateTime.now());
            todolist = todolistRepository.save(todolist);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<TodolistResponse> searchByTitle(String title) {
        try {
            return todolistRepository.findByTitleContainingIgnoreCase(title)
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public List<TodolistResponse> filterByCategory(Long categoryId) {
        try {
            return todolistRepository.findByCategoryId(categoryId)
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public byte[] getImageById(Long id) {
        try {
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found with id : "+id));
            if (todolist.getImagePath() != null && !todolist.getImagePath().isEmpty()) {
                Path filePath = Paths.get(imageDirectory, todolist.getImagePath());
                return Files.readAllBytes(filePath);
            }
            throw new RuntimeException("Image path doesent exist with id : "+id);
        } catch (IOException e){
            throw new RuntimeException("Failed to read image file",e);
        }catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public byte[] getImageByName(String imageName) {
        try {
            Path filePath = Paths.get(imageDirectory, imageName);
            return Files.readAllBytes(filePath);
        } catch (IOException e){
            throw new RuntimeException("Failed to read image file",e);
        }catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public List<TodolistResponse> findByUserId(UUID userId) {
        try {
            return todolistRepository.findByUserId(userId)
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public TodolistResponse convertToResponse(Todolist todolist) {
        TodolistResponse response = new TodolistResponse();
        response.setId(todolist.getId());
        response.setTitle(todolist.getTitle());
        response.setDescription(todolist.getDescription());
        response.setUsername(todolist.getUser().getUsername());
        response.setCategoryId(todolist.getCategory().getId());
        response.setCategoryName(todolist.getCategory().getName());
        response.setImagePath(todolist.getImagePath());
        response.setCompleted(todolist.getIsCompleted());
        response.setCreatedAt(todolist.getCreatedAt());
        response.setUpdatedAt(todolist.getUpdatedAt());
        return response;
    }
}
