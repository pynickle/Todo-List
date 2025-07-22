package com.euphony.todo_list.data;

import com.euphony.todo_list.todo.Tag;
import com.euphony.todo_list.todo.TagManager;
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
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * 待办清单数据持久化管理器
 * 负责保存和加载待办事项到文件系统
 */
public class TodoDataManager {
    private static final String TODO_FOLDER = "todolist";
    private static final String TODO_FILE = "todos.json";
    private static final String TAGS_FILE = "tags.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * 获取当前世界的待办清单存储路径
     */
    private static Path getTodoDirectory() {
        Minecraft minecraft = Minecraft.getInstance();
        Path gameDir = minecraft.gameDirectory.toPath();

        // 获取当前世界名称
        String worldName = getCurrentWorldName();
        if (worldName == null) {
            worldName = "global"; // 如果不在世界中，使用全局存储
        }

        // 创建路径：gameDir/todolist/worldName/
        Path todoDir = gameDir.resolve(TODO_FOLDER).resolve(worldName);

        // 确保目录存在
        try {
            Files.createDirectories(todoDir);
        } catch (IOException e) {
            System.err.println("Failed to create todo directory: " + e.getMessage());
        }

        return todoDir;
    }

    private static Path getTodoFilePath() {
        return getTodoDirectory().resolve(TODO_FILE);
    }

    private static Path getTagsFilePath() {
        return getTodoDirectory().resolve(TAGS_FILE);
    }

    /**
     * 获取当前世界名称
     */
    public static String getCurrentWorldName() {
        Minecraft minecraft = Minecraft.getInstance();

        // 检查是否在单人世界
        if (minecraft.hasSingleplayerServer()) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            if (server != null) {
                // 使用世界存储路径来获取真正的文件夹名字
                // 备用方案：使用 levelName（这是文件夹名字，不是显示名称）
                return server.getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
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
     * 保存待办清单和标签到文件
     */
    public static boolean saveTodos(List<TodoItem> todos) {
        boolean todosSuccess = saveTodosToFile(todos);
        boolean tagsSuccess = saveTagsToFile();
        return todosSuccess && tagsSuccess;
    }

    private static boolean saveTodosToFile(List<TodoItem> todos) {
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

            LOGGER.info("Successfully saved " + todos.size() + " todos to: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to save todos: " + e.getMessage());
            return false;
        }
    }

    private static boolean saveTagsToFile() {
        Path filePath = getTagsFilePath();

        try {
            Map<UUID, Tag> tagsMap = TagManager.getInstance().getTagsMap();
            List<TagData> tagDataList = new ArrayList<>();

            for (Tag tag : tagsMap.values()) {
                tagDataList.add(new TagData(tag));
            }

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                GSON.toJson(tagDataList, writer);
            }

            LOGGER.info("Successfully saved " + tagDataList.size() + " tags to: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to save tags: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从文件加载待办清单和标签
     */
    public static List<TodoItem> loadTodos() {
        // 首先加载标签
        loadTagsFromFile();

        // 然后加载待办事项
        return loadTodosFromFile();
    }

    private static List<TodoItem> loadTodosFromFile() {
        Path filePath = getTodoFilePath();

        if (!Files.exists(filePath)) {
            LOGGER.info("Todo file not found, starting with empty list: " + filePath);
            return new ArrayList<>();
        }

        try {
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

                LOGGER.info("Successfully loaded " + todos.size() + " todos from: " + filePath);
                return todos;
            }

        } catch (Exception e) {
            System.err.println("Failed to load todos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void loadTagsFromFile() {
        Path filePath = getTagsFilePath();

        if (!Files.exists(filePath)) {
            LOGGER.info("Tags file not found, starting with empty tags: " + filePath);
            return;
        }

        try {
            try (FileReader reader = new FileReader(filePath.toFile())) {
                Type listType = new TypeToken<List<TagData>>(){}.getType();
                List<TagData> tagDataList = GSON.fromJson(reader, listType);

                if (tagDataList == null) {
                    return;
                }

                // 转换为 Tag 对象并添加到 TagManager
                Map<UUID, Tag> tagsMap = new HashMap<>();
                for (TagData data : tagDataList) {
                    Tag tag = data.toTag();
                    tagsMap.put(tag.getId(), tag);
                }

                TagManager.getInstance().setTagsFromMap(tagsMap);
                LOGGER.info("Successfully loaded " + tagDataList.size() + " tags from: " + filePath);
            }

        } catch (Exception e) {
            System.err.println("Failed to load tags: " + e.getMessage());
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
        private List<String> tagIds; // 标签ID列表

        public TodoData(TodoItem item) {
            this.id = item.getId().toString();
            this.title = item.getTitle();
            this.description = item.getDescription();
            this.completed = item.isCompleted();
            this.createdAt = item.getCreatedAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.dueDate = item.getDueDate() != null ?
                item.getDueDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

            // 保存标签ID
            this.tagIds = new ArrayList<>();
            for (Tag tag : item.getTags()) {
                this.tagIds.add(tag.getId().toString());
            }
        }

        public TodoItem toTodoItem() {
            TodoItem item = new TodoItem(title, description);
            // 使用反射设置私有字段
            try {
                var idField = TodoItem.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(item, java.util.UUID.fromString(id));

                var completedField = TodoItem.class.getDeclaredField("completed");
                completedField.setAccessible(true);
                completedField.set(item, completed);

                var createdAtField = TodoItem.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(item, LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                if (dueDate != null) {
                    var dueDateField = TodoItem.class.getDeclaredField("dueDate");
                    dueDateField.setAccessible(true);
                    dueDateField.set(item, LocalDateTime.parse(dueDate, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                // 恢复标签
                if (tagIds != null) {
                    List<Tag> tags = new ArrayList<>();
                    TagManager tagManager = TagManager.getInstance();
                    for (String tagIdStr : tagIds) {
                        try {
                            UUID tagId = UUID.fromString(tagIdStr);
                            Tag tag = tagManager.getTag(tagId);
                            if (tag != null) {
                                tags.add(tag);
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid tag ID: " + tagIdStr);
                        }
                    }
                    item.setTags(tags);
                }

            } catch (Exception e) {
                System.err.println("Failed to restore todo item fields: " + e.getMessage());
            }

            return item;
        }
    }

    /**
     * 用于JSON序列化的标签数据类
     */
    private static class TagData {
        private String id;
        private String name;
        private int color;

        public TagData(Tag tag) {
            this.id = tag.getId().toString();
            this.name = tag.getName();
            this.color = tag.getColor();
        }

        public Tag toTag() {
            return new Tag(UUID.fromString(id), name, color);
        }
    }
}
