package com.euphony.todo_list.data;

import com.euphony.todo_list.todo.TodoItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 待办清单数据持久化管理器
 * 负责保存和加载待办事项到文件系统
 */
public class TodoDataManager {
    private static final String TODO_FOLDER = "todolist";
    private static final String TODO_FILE = "todos.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * 获取当前世界的待办清单存储路径
     */
    private static Path getTodoFilePath() {
        Minecraft minecraft = Minecraft.getInstance();
        Path gameDir = minecraft.gameDirectory.toPath();

        // 获取当前世界名称
        String worldName = getCurrentWorldName();
        if (worldName == null) {
            worldName = "global"; // 如果不在世界中，使用全局存储
        }

        // 创建路径：gameDir/todolist/worldName/todos.json
        Path todoDir = gameDir.resolve(TODO_FOLDER).resolve(worldName);

        // 确保目录存在
        try {
            Files.createDirectories(todoDir);
        } catch (IOException e) {
            System.err.println("Failed to create todo directory: " + e.getMessage());
        }

        return todoDir.resolve(TODO_FILE);
    }

    /**
     * 获取当前世界名称
     */
    public static String getCurrentWorldName() {
        Minecraft minecraft = Minecraft.getInstance();

        // 检查是否在单人世界
        if (minecraft.level != null && minecraft.hasSingleplayerServer()) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            if (server != null) {
                // 使用世界存储路径来获取真正的文件夹名字
                String name = server.getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                // 备用方案：使用 levelName（这是文件夹名字，不是显示名称）
                if(name != null) {
                    return name;
                }
                return server.getWorldData().getLevelSettings().levelName();
            }
        }

        // 检查是否在多人服务器
        if (minecraft.getCurrentServer() != null) {
            // 服务器IP地址，替换特殊字符以确保文件名安全
            return minecraft.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9._-]", "_");
        }

        return null;
    }

    /**
     * 保存待办清单到文件
     */
    public static boolean saveTodos(List<TodoItem> todos) {
        Path filePath = getTodoFilePath();

        try {
            // 创建待办事项的序列化数据
            List<TodoData> todoDataList = new ArrayList<>();
            for (TodoItem item : todos) {
                todoDataList.add(new TodoData(item));
            }

            // 写入文件
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                GSON.toJson(todoDataList, writer);
            }

            System.out.println("Successfully saved " + todos.size() + " todos to: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to save todos: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从文件加载待办清单
     */
    public static List<TodoItem> loadTodos() {
        Path filePath = getTodoFilePath();

        if (!Files.exists(filePath)) {
            System.out.println("Todo file not found, starting with empty list: " + filePath);
            return new ArrayList<>();
        }

        try {
            // 读取文件
            try (FileReader reader = new FileReader(filePath.toFile())) {
                Type listType = new TypeToken<List<TodoData>>(){}.getType();
                List<TodoData> todoDataList = GSON.fromJson(reader, listType);

                if (todoDataList == null) {
                    return new ArrayList<>();
                }

                // 转换为 TodoItem 对象
                List<TodoItem> todos = new ArrayList<>();
                for (TodoData data : todoDataList) {
                    todos.add(data.toTodoItem());
                }

                System.out.println("Successfully loaded " + todos.size() + " todos from: " + filePath);
                return todos;
            }

        } catch (Exception e) {
            System.err.println("Failed to load todos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 用于JSON序列化的待办事项数据类
     */
    private static class TodoData {
        private String id;
        private String title;
        private String description;
        private boolean completed;
        private String createdAt;
        private String dueDate;

        public TodoData(TodoItem item) {
            this.id = item.getId().toString();
            this.title = item.getTitle();
            this.description = item.getDescription();
            this.completed = item.isCompleted();
            this.createdAt = item.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.dueDate = item.getDueDate() != null ?
                item.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        }

        public TodoItem toTodoItem() {
            TodoItem item = new TodoItem(title, description);
            // 使用反射或其他方式设置私有字段
            try {
                var idField = TodoItem.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(item, java.util.UUID.fromString(id));

                var completedField = TodoItem.class.getDeclaredField("completed");
                completedField.setAccessible(true);
                completedField.set(item, completed);

                var createdAtField = TodoItem.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(item, LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                if (dueDate != null) {
                    var dueDateField = TodoItem.class.getDeclaredField("dueDate");
                    dueDateField.setAccessible(true);
                    dueDateField.set(item, LocalDateTime.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

            } catch (Exception e) {
                System.err.println("Failed to restore todo item fields: " + e.getMessage());
            }

            return item;
        }
    }
}
