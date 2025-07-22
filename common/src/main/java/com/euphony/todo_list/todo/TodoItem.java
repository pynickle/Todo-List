package com.euphony.todo_list.todo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoItem {
    private final UUID id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private List<Tag> tags;

    public TodoItem(String title, String description) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.tags = new ArrayList<>();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    // 新增标签相关方法
    public List<Tag> getTags() { return new ArrayList<>(tags); }
    public void setTags(List<Tag> tags) { this.tags = new ArrayList<>(tags); }
    public void addTag(Tag tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }
    public void removeTag(Tag tag) { this.tags.remove(tag); }
    public boolean hasTag(Tag tag) { return this.tags.contains(tag); }
}
