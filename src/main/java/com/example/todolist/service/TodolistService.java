package com.example.todolist.service;

import com.example.todolist.dto.request.TodolistRequest;
import com.example.todolist.dto.response.CategoryResponse;
import com.example.todolist.dto.response.TodolistResponse;
import com.example.todolist.dto.response.UserResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.model.Todolist;
import com.example.todolist.model.User;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TodolistRepository;
import com.example.todolist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodolistService {
    @Autowired
    private TodolistRepository todolistRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    public List<TodolistResponse> findAll() {
        try{
            return todolistRepository.findAll().stream().map(this::convertToResponse).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public TodolistResponse findById(Long id) {
        try{
            return todolistRepository.findById(id).map(this::convertToResponse)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found with id "+id));
        } catch(DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data todolists",e);
        }
    }
    public TodolistResponse create(TodolistRequest todolistRequest) {
        try{
            User user = userRepository.findByUsername(todolistRequest.getUsername())
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
            if (!categoryRepository.existsById(todolistRequest.getCategoryId())){
                throw new DataNotFoundException("Category not found");
            }
            Todolist todolist =new Todolist();
            todolist.setTitle(todolistRequest.getTitle());
            todolist.setDescription(todolistRequest.getDescription());
            todolist.setCategoryId(todolistRequest.getCategoryId());
            todolist.setUserId(user.getId());
            todolist.setIsCompleted(todolistRequest.getIsCompleted());

            todolist=todolistRepository.save(todolist);
            return convertToResponse(todolist);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TodolistResponse updateTodolist (Long id, TodolistRequest todolistRequest){
        try {
            Todolist todolist = todolistRepository.findById(id)
                    .orElseThrow(()-> new DataNotFoundException("Todolist not found : "+id));
            if (!categoryRepository.existsById(todolistRequest.getCategoryId())){
                throw new DataNotFoundException("Category not found");
            }
            if (todolistRequest.getTitle()!=null) todolist.setTitle(todolistRequest.getTitle());
            if (todolistRequest.getDescription()!=null) todolist.setDescription(todolistRequest.getDescription());
            if (todolistRequest.getIsCompleted()!=null) todolist.setIsCompleted(todolistRequest.getIsCompleted());
            if (todolistRequest.getCategoryId()!=null) todolist.setCategoryId(todolistRequest.getCategoryId());
            todolist = todolistRepository.save(todolist);
            return convertToResponse(todolist);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteTodolist(Long id){
        try{
            if(!todolistRepository.existsById(id)){
                throw new DataNotFoundException("No todo list with id : "+id);
            }
            todolistRepository.deleteById(id);
        } catch (DataNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public TodolistResponse convertToResponse(Todolist todolist) {
        TodolistResponse response = new TodolistResponse();
        CategoryResponse categoryResponse = new CategoryResponse();
        UserResponse userResponse = new UserResponse();
        response.setId(todolist.getId());
        response.setTitle(todolist.getTitle());
        response.setDescription(todolist.getDescription());
        response.setUserId(todolist.getUserId());
        response.setCategoryId(todolist.getCategoryId());

        response.setCompleted(todolist.getIsCompleted());
        response.setCreatedAt(todolist.getCreatedAt());
        response.setUpdatedAt(todolist.getUpdatedAt());
        return response;
    }
}
