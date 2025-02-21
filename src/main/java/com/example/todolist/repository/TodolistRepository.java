package com.example.todolist.repository;

import com.example.todolist.model.Todolist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TodolistRepository extends JpaRepository<Todolist, Long> {
    Optional<Todolist> findById(Long id);
    List<Todolist> findByUserId(UUID userId);
    List<Todolist> findByCategoryId(Long categoryId);
    List<Todolist> findByTitleContainingIgnoreCase(String title);
    Page<Todolist> findAllByDeletedAtIsNull(Pageable pageable);
    @Query("SELECT t FROM Todolist t WHERE t.deletedAt IS NULL AND t.user.username = :username")
    Page<Todolist> findAllByDeletedAtIsNullAndUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT t FROM Todolist t WHERE t.deletedAt IS NOT NULL")
    Page<Todolist> findAllTrashed(Pageable pageable);

    @Query("SELECT t FROM Todolist t WHERE t.deletedAt IS NOT NULL AND t.user.username = :username")
    Page<Todolist> findAllTrashedByUsername(@Param("username") String username, Pageable pageable);
    @Query("SELECT t FROM Todolist t WHERE t.deletedAt IS NULL " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:username IS NULL OR t.user.username = :username)")
    Page<Todolist> findAllByCategoryAndUsername(
            @Param("categoryId") Long categoryId,
            @Param("username") String username,
            Pageable pageable
    );

}
