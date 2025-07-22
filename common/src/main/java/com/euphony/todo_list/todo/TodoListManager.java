package com.euphony.todo_list.todo;

import com.euphony.todo_list.data.TodoDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TodoListManager {
    private static TodoListManager instance;
    private final List<TodoItem> todoItems;
    private boolean isLoaded = false;

    private TodoListManager() {
        this.todoItems = new ArrayList<>();
    }

    public static TodoListManager getInstance() {
        if (instance == null) {
            instance = new TodoListManager();
        }
        return instance;
    }

    /**
     * 从文件加载待办事项
     */
    public void loadFromFile() {
        List<TodoItem> loadedItems = TodoDataManager.loadTodos();
        todoItems.clear();
        todoItems.addAll(loadedItems);
        System.out.println("Loaded " + loadedItems.size() + " todo items from file");
    }

    /**
     * 保存待办事项到文件
     */
    public void saveToFile() {
        boolean success = TodoDataManager.saveTodos(new ArrayList<>(todoItems));
        if (success) {
            System.out.println("Successfully saved " + todoItems.size() + " todo items to file");
        } else {
            System.err.println("Failed to save todo items to file");
        }
    }

    public void addTodoItem(TodoItem item) {
        todoItems.add(item);
        saveToFile(); // 自动保存
    }

    public void removeTodoItem(UUID id) {
        todoItems.removeIf(item -> item.getId().equals(id));
        saveToFile(); // 自动保存
    }

    public List<TodoItem> getAllTodoItems() {
        return new ArrayList<>(todoItems);
    }

    public TodoItem getTodoItem(UUID id) {
        return todoItems.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void updateTodoItem(UUID id, String title, String description) {
        TodoItem item = getTodoItem(id);
        if (item != null) {
            item.setTitle(title);
            item.setDescription(description);
            saveToFile(); // 自动保存
        }
    }

    public void updateTodoItem(UUID id, String title, String description, List<Tag> tags) {
        TodoItem item = getTodoItem(id);
        if (item != null) {
            item.setTitle(title);
            item.setDescription(description);
            item.setTags(tags);
            saveToFile(); // 自动保存
        }
    }

    public void toggleCompleted(UUID id) {
        TodoItem item = getTodoItem(id);
        if (item != null) {
            item.setCompleted(!item.isCompleted());
            saveToFile(); // 自动保存
        }
    }

    // 新增：根据标签筛选待办事项
    public List<TodoItem> getFilteredTodoItems(List<Tag> filterTags, String searchText) {
        return todoItems.stream()
                .filter(item -> {
                    // 如果有标签筛选，检查是否包含任一筛选标签
                    if (filterTags != null && !filterTags.isEmpty()) {
                        boolean hasAnyTag = false;
                        for (Tag filterTag : filterTags) {
                            if (item.hasTag(filterTag)) {
                                hasAnyTag = true;
                                break;
                            }
                        }
                        if (!hasAnyTag) return false;
                    }

                    // 如果有搜索文本，检查标题或描述是否包含搜索文本
                    if (searchText != null && !searchText.trim().isEmpty()) {
                        String search = searchText.toLowerCase().trim();
                        return item.getTitle().toLowerCase().contains(search) ||
                               item.getDescription().toLowerCase().contains(search);
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // 新增：根据单个标签筛选待办事项
    public List<TodoItem> getTodoItemsByTag(Tag tag) {
        return todoItems.stream()
                .filter(item -> item.hasTag(tag))
                .collect(Collectors.toList());
    }

    // 新增：搜索待办事项
    public List<TodoItem> searchTodoItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllTodoItems();
        }

        String search = searchText.toLowerCase().trim();
        return todoItems.stream()
                .filter(item ->
                    item.getTitle().toLowerCase().contains(search) ||
                    item.getDescription().toLowerCase().contains(search) ||
                    item.getTags().stream().anyMatch(tag -> tag.getName().toLowerCase().contains(search))
                )
                .collect(Collectors.toList());
    }
}