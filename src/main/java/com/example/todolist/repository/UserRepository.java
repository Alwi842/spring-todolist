package com.example.todolist.repository;

import com.example.todolist.model.Todolist;
import com.example.todolist.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findById(UUID userId);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

}
