package com.euphony.todo_list.todo;

import com.euphony.todo_list.data.TodoDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void toggleCompleted(UUID id) {
        TodoItem item = getTodoItem(id);
        if (item != null) {
            item.setCompleted(!item.isCompleted());
            saveToFile(); // 自动保存
        }
    }
}