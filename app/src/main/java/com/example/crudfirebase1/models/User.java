package com.example.crudfirebase1.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username, email, id, avatar;
    private List <Todo> todoList = new ArrayList<>();

    public User() {
    }

    public User(String username, String email, String id, String avatar, List<Todo> todoList) {
        this.username = username;
        this.email = email;
        this.id = id;
        this.avatar = avatar;
        this.todoList = todoList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<Todo> getTodoList() {
        return todoList;
    }

    public void setTodoList(List<Todo> todoList) {
        this.todoList = todoList;
    }
}
