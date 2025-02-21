package com.example.todolist.dto.response;

import com.example.todolist.model.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodolistResponse {
    private Long id;
    private String title;
    private String description;
    private String username;
    private CategoryData category;
    private boolean isCompleted;
    private String imagePath;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
    public TodolistResponse(){
        this.category = new CategoryData();
    }
    public void setCategoryId(Long id){
        this.category.setId(id);
    }
    public void setCategoryName(String name){
        this.category.setName(name);
    }
}

@Data
class CategoryData{
    private Long id;
    private String name;
}
