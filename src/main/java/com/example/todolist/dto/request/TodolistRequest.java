package com.example.todolist.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@Data
public class TodolistRequest {
    private String title;
    private String description;
    private String username;
    private Long categoryId;
    private Boolean isCompleted;
    private LocalDateTime deletedAt;
    private MultipartFile imagePath;
}
