package com.example.todolist.repository;

import com.example.todolist.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UserRepositoryTest {
    @Mock
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("alwi");
        user.setPassword("123");
        user.setEmail("alwi@gmail.com");
        user.setRole("Admin");
    }

    //nama method pake _ di depan untuk membedakan method berdasarkan ekspektasi
    @Test
    public void testFindByUsername_success(){
        //when : buat memberitahu mockito kalau kita ingin mencari data dan memberikan hasilnya
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        //menampung objek yang ditemukan pada saat when yang diambil dari findByUsername
        Optional<User> user = this.userRepository.findByUsername("alwi");
        assertTrue(user.isPresent());
        assertEquals("Admin", user.get().getRole());
        assertThat(user.get().getUsername()).isEqualTo("alwi");
    }

    @Test
    public void testFindByUsername_notFound(){
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        Optional<User> user = this.userRepository.findByUsername("alwi");
        assertTrue(user.isEmpty());
    }
    //email
    @Test
    public void testFindByEmail_success(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Optional<User> user = this.userRepository.findByEmail("alwi@gmail.com");
        assertTrue(user.isPresent());
        assertEquals("Admin", user.get().getRole());
        assertThat(user.get().getEmail()).isEqualTo("alwi@gmail.com");
    }
    @Test
    public void testFindByEmail_notFound(){
        when(userRepository.findByEmail("empty")).thenReturn(Optional.empty());
        Optional<User> user = this.userRepository.findByEmail("alwi@gmail.com");
        assertTrue(user.isEmpty());
    }
    //findbyid
    @Test
    public void testFindById_success() {
        // Mock repository to return a list instead of Optional<User>
        when(userRepository.findById(user.getId())).thenReturn(List.of(user));

        // Call the method under test
        List<User> users = this.userRepository.findById(user.getId());

        // Assertions
        assertFalse(users.isEmpty()); // Check that the list is not empty
        assertEquals(1, users.size()); // Ensure only one user is returned
        assertEquals("Admin", users.get(0).getRole());
        assertThat(users.get(0).getEmail()).isEqualTo("alwi@gmail.com");
    }
    @Test
    public void testFindById_notFound() {
        when(userRepository.findById(user.getId())).thenReturn(List.of());
        List<User> users = this.userRepository.findById(user.getId());
        assertTrue(users.isEmpty());
    }

}
