package com.example.todolist.repository;

import com.example.todolist.model.Todolist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TodolistRepository extends JpaRepository<Todolist, Long> {
    Optional<Todolist> findById(Long id);
}
