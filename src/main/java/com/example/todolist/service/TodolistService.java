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
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
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
    public Page<TodolistResponse> findByUsername(String username, int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            Page<Todolist> todolists = todolistRepository.findAllByDeletedAtIsNullAndUsername(username,pageable);
            return todolists.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public TodolistResponse findById(Long id) {
        try {
            return todolistRepository.findById(id)
                    .map(this::convertToResponse)
                    .orElseThrow(() -> new DataNotFoundException("Todolist not found with id " + id));
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // Log unexpected exceptions
            System.err.println("Unexpected error occurred while fetching Todolist: " + e.getMessage());
            throw new RuntimeException("Failed to get data todolists", e);
        }
    }


    public Page<TodolistResponse> findAllDeleted(int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            Page<Todolist> todolists = todolistRepository.findAllTrashed(pageable);
            return todolists.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    @Transactional

    public TodolistResponse create(TodolistRequest todolistRequest) {
        try {
            User user = userRepository.findByUsername(todolistRequest.getUsername())
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
            Category category = categoryRepository.findById(todolistRequest.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found"));

            Todolist todolist = new Todolist();
            todolist.setTitle(todolistRequest.getTitle());
            todolist.setDescription(todolistRequest.getDescription());
            todolist.setCategory(category);
            todolist.setUser(user);
            todolist.setIsCompleted(todolistRequest.getIsCompleted());

            if (todolistRequest.getImagePath() != null && !todolistRequest.getImagePath().isEmpty()) {
                MultipartFile file = todolistRequest.getImagePath();
                validateFile(file);

                String uniqueFileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
                String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                Path fullPath = Path.of(imageDirectory, datePath, uniqueFileName);
                Path imagePath = Path.of(datePath, uniqueFileName);

                Files.createDirectories(fullPath.getParent());

                Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

                todolist.setImagePath(imagePath.toString().replace("\\", "/"));
            }

            todolist = todolistRepository.save(todolist);
            return convertToResponse(todolist);

        } catch (IllegalArgumentException | DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Todolist", e);
        }
    }

    // 🆔 Generate Unique Filename
    private String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }


    private static void validateFile(MultipartFile file) {
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

    }


    @Transactional
    public TodolistResponse updateTodolist(Long id, TodolistRequest todolistRequest) {
        try {
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Todolist not found : " + id));

            if (todolistRequest.getTitle() != null) todolist.setTitle(todolistRequest.getTitle());
            if (todolistRequest.getDescription() != null) todolist.setDescription(todolistRequest.getDescription());
            if (todolistRequest.getIsCompleted() != null) todolist.setIsCompleted(todolistRequest.getIsCompleted());

            if (todolistRequest.getCategoryId() != null) {
                Category category = categoryRepository.findById(todolistRequest.getCategoryId())
                        .orElseThrow(() -> new DataNotFoundException("Category not found"));
                todolist.setCategory(category);
            }

            if (todolistRequest.getUsername() != null) {
                User user = userRepository.findByUsername(todolistRequest.getUsername())
                        .orElseThrow(() -> new DataNotFoundException("User not found"));
                todolist.setUser(user);
            }

            if (todolistRequest.getImagePath() != null && !todolistRequest.getImagePath().isEmpty()) {
                MultipartFile file = todolistRequest.getImagePath();
                validateFile(file);

                String uniqueFileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
                String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                Path fullPath = Path.of(imageDirectory, datePath, uniqueFileName);
                Path imagePath = Path.of(datePath, uniqueFileName);

                Files.createDirectories(fullPath.getParent());

                Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

                if (todolist.getImagePath() != null) {
                    Path oldImagePath = Path.of(imageDirectory, todolist.getImagePath());
                    Files.deleteIfExists(oldImagePath);
                }
                todolist.setImagePath(imagePath.toString().replace("\\", "/"));

            }

            todolist = todolistRepository.save(todolist);
            return convertToResponse(todolist);

        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Todolist", e);
        }
    }


    @Transactional
    public void deleteTodolist(Long id) {
        try {
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("No todo list with id : " + id));

            // Check and delete the associated image if exists
            if (todolist.getImagePath() != null && !todolist.getImagePath().isEmpty()) {
                Path imagePath = Paths.get(imageDirectory, todolist.getImagePath());
                try {
                    if (Files.exists(imagePath)) {
                        Files.delete(imagePath);
                    }
                } catch (Exception e) {
                    throw e;
                }
            }

            todolistRepository.deleteById(id);
        } catch (DataNotFoundException e) {
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
        } catch(DataNotFoundException e){
            throw e;
        }catch (IOException e){
            throw new RuntimeException("Failed to read image file",e);
        } catch(RuntimeException e){
            throw e;
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
    public List<TodolistResponse> findByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(()-> new DataNotFoundException("User not found with username : "+username));
            return todolistRepository.findByUserId(user.getId())
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public Page<TodolistResponse> findAllByCategoryAndUsername(int page, int size, Long categoryId, String username) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            return todolistRepository.findAllByCategoryAndUsername(categoryId, username, pageable)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public Page<TodolistResponse> findAllTrashedByUsername(int page, int size, String username) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            return todolistRepository.findAllTrashedByUsername(username, pageable)
                    .map(this::convertToResponse);
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
