package com.example.todolist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data //anotasi lombok buat setter getter otomatis
@AllArgsConstructor // buat constructor yang membutuhkan semua  field(argument)
@NoArgsConstructor // buat constructor tanpa argument
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name="name", nullable =false)
    private String name;

    @Column (name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist //anotasi buat data waktu secara otomatis ketika data pertama kali dibuat
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
        this.updatedAt=LocalDateTime.now();
    }
    @PreUpdate // anotasi buat data waktu secara otomatis ketika data di update.
    public void onUpdate(){
        this.updatedAt=LocalDateTime.now();
    }
}
