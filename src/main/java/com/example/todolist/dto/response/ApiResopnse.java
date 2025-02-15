package com.example.todolist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResopnse<T> {
    private int status;
    private T data; //T adalah generic type

}
