package com.example.todolist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TodolistRequest {
    @NotBlank
    @Size(max=255)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private Long categoryId;

    @NotBlank
    private String username;

    @NotBlank
    private Boolean isCompleted;

}
