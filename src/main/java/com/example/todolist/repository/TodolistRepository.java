package com.example.todolist.repository;

import com.example.todolist.model.Todolist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TodolistRepository extends JpaRepository<Todolist, Long> {
    Optional<Todolist> findById(Long id);
    List<Todolist> findByUserId(UUID userId);
    List<Todolist> findByCategoryId(Long categoryId);
    List<Todolist> findByTitleContainingIgnoreCase(String title);
}
