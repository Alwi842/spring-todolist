package com.example.todolist.service;

import com.example.todolist.dto.request.CategoryRequest;
import com.example.todolist.dto.response.CategoryResponse;
import com.example.todolist.exception.DataNotFoundException;
import com.example.todolist.exception.DuplicateDataException;
import com.example.todolist.model.Category;
import com.example.todolist.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll(){
        try {
            return categoryRepository.findAll()
                    .stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data categories",e);
        }
    }
    public CategoryResponse create(CategoryRequest categoryRequest){
        try {
            if(categoryRepository.findByName(categoryRequest.getName()).isPresent()){
                throw new DuplicateDataException("Category already exists");
            }
            Category category = new Category();
            category.setName(categoryRequest.getName());
            category = categoryRepository.save(category);
            return convertToResponse(category);
        } catch (DuplicateDataException e) {
            throw e; //error yang di lempakkan ke controller
        }  catch (Exception e){
            throw new RuntimeException("Failed to create category",e);
        }
    }
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest){
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Category not found with id "+id));
            if(categoryRepository.findByName(categoryRequest.getName()).isPresent()){
                throw new DuplicateDataException("Category already exists");
            }
            category.setName(categoryRequest.getName());
            category = categoryRepository.save(category);
            return convertToResponse(category);
        } catch(DataNotFoundException | DuplicateDataException e){
            throw e;
        } catch (Exception e){
            throw new RuntimeException("Failed to update category"+e.getMessage(),e);
        }
    }
    public void deleteCategory(Long id){
        try {
            if(!categoryRepository.existsById(id)){
                throw new DataNotFoundException("Category not found with id "+id);
            }
            categoryRepository.deleteById(id);
        } catch (DataNotFoundException e) {
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("Failed to delete category",e);
        }
    }
    public Optional<CategoryResponse> findById(Long id){
        try {
            if(categoryRepository.existsById(id)){
                throw new DataNotFoundException("Category not found with id "+id);
            }
            return categoryRepository.findById(id).map(this::convertToResponse);
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find category by id "+e.getMessage());
        }
    }
    public Optional<CategoryResponse>findByName(String name){
       try {
           return categoryRepository.findByName(name).map(this::convertToResponse);
       } catch (Exception e) {
           throw new RuntimeException("Failed to find category by name "+e.getMessage());
       }
    }
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
