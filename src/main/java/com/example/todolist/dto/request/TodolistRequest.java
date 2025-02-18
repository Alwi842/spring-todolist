package com.example.todolist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@Data
public class TodolistRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String username;
    @NotBlank
    private Long categoryId;
    @NotNull
    private Boolean isCompleted;
    private MultipartFile imagePath;
}
