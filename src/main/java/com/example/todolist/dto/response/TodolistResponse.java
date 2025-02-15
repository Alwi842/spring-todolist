package com.example.todolist.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TodolistResponse {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private UUID userId;
    private boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
