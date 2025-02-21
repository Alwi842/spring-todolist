package com.example.todolist.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserTest {
    private User user;

    @BeforeEach //annotasi untuk menjalankan method sebelum test
    public void setUp(){
        //karena test tidak menggunakan database maka perlu mock/data tiruan
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("alwi");
        user.setPassword("123");
        user.setEmail("alwi@gmail.com");
        user.setRole("Admin");
    }

    //unit test untuk membuat user
    @Test //annotasi untuk membuat unit test
    public void testCreateUser(){
        user.onCreate(); //untuk bikin data tanggal pada model user
        user.setId(UUID.randomUUID()); // untuk generate random UUID
        //assert untuk mmembuat pernyataan
        assertNotNull(user.getId()); //untuk memastikan id tidak null
        assertEquals("alwi", user.getUsername());// untuk memastikan data sesuai
        assertEquals("alwi@gmail.com", user.getEmail());
        assertEquals("123", user.getPassword());
        assertEquals("Admin", user.getRole());
        assertThat(user.getCreatedAt()).isNotNull(); //untuk memastikan data tidak null
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }
}
