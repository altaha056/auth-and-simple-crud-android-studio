package com.example.crudfirebase1.models;

public class Todo {
    private String description;
    private boolean finished = false;

    public Todo() {
    }

    public Todo(String description, boolean finished) {
        this.description = description;
        this.finished = finished;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
