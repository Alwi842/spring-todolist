package com.example.todolist.repository;

import com.example.todolist.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    //jparepository : dependensi yang nyediain query otomatis
    Optional<Category> findByName(String nama);
    Optional<Category> findById(Long id);
//    @Query(value = "Select * from users where name = ?1", nativeQuery = true)
//    Optional<Category> findByNameCustomQuery(String name);
}
