package com.example.todolist.controller;

import com.example.todolist.dto.request.TodolistRequest;
import com.example.todolist.dto.response.ApiResopnse;
import com.example.todolist.dto.response.TodolistResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.service.TodolistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todolist")
public class TodolistController {
    @Autowired
    private TodolistService todolistService;
    @GetMapping
    private ResponseEntity<?> getAllTodolist(){
        try {
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), todolistService.findAll()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    private ResponseEntity<?> getTodolistById(@PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(), todolistService.findById(id)));
        } catch (DataNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new ApiResopnse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @PostMapping
    private ResponseEntity<?> createTodolist(@RequestBody TodolistRequest todolistRequest){
        try{
            TodolistResponse response = todolistService.create(todolistRequest);
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResopnse<>(HttpStatus.CREATED.value(), response));
        } catch (DuplicateDataException e){
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(new ApiResopnse<>(HttpStatus.CREATED.value(), e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodolist(@PathVariable Long id, @RequestBody TodolistRequest todolistRequest){
        try{
            TodolistResponse response = todolistService.updateTodolist(id,todolistRequest);
            return ResponseEntity.status(HttpStatus.CREATED.value())
                    .body(new ApiResopnse<>(HttpStatus.CREATED.value(), response));
        } catch (DataNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodolist(@PathVariable Long id){
        try{
            todolistService.deleteTodolist(id);
            return ResponseEntity.status(HttpStatus.OK.value())
                    .body(new ApiResopnse<>(HttpStatus.OK.value(),"Todolist deleted succesfully"));
        } catch(DataNotFoundException e){
            return ResponseEntity.status((HttpStatus.NOT_FOUND.value()))
                    .body(new ApiResopnse<>(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
