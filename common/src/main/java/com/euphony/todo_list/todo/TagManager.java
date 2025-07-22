package com.euphony.todo_list.todo;

import java.util.*;
import java.util.stream.Collectors;

public class TagManager {
    private static TagManager instance;
    private final Map<UUID, Tag> tags = new HashMap<>();

    // 预定义的标签颜色
    private static final int[] PREDEFINED_COLORS = {
        0xFF4CAF50, // 绿色
        0xFF2196F3, // 蓝色
        0xFFFF9800, // 橙色
        0xFF9C27B0, // 紫色
        0xFFF44336, // 红色
        0xFF607D8B, // 蓝灰色
        0xFFFFEB3B, // 黄色
        0xFF795548, // 棕色
        0xFF009688, // 青色
        0xFFE91E63  // 粉色
    };

    private int colorIndex = 0;

    public static TagManager getInstance() {
        if (instance == null) {
            instance = new TagManager();
        }
        return instance;
    }

    public Tag createTag(String name) {
        // 检查是否已存在同名标签
        for (Tag tag : tags.values()) {
            if (tag.getName().equalsIgnoreCase(name)) {
                return tag; // 返回已存在的标签
            }
        }

        // 创建新标签
        int color = PREDEFINED_COLORS[colorIndex % PREDEFINED_COLORS.length];
        colorIndex++;

        Tag tag = new Tag(name, color);
        tags.put(tag.getId(), tag);
        return tag;
    }

    public Tag createTag(String name, int color) {
        // 检查是否已存在同名标签
        for (Tag tag : tags.values()) {
            if (tag.getName().equalsIgnoreCase(name)) {
                tag.setColor(color); // 更新颜色
                return tag;
            }
        }

        Tag tag = new Tag(name, color);
        tags.put(tag.getId(), tag);
        return tag;
    }

    public void addTag(Tag tag) {
        tags.put(tag.getId(), tag);
    }

    public void removeTag(UUID tagId) {
        tags.remove(tagId);
    }

    public Tag getTag(UUID tagId) {
        return tags.get(tagId);
    }

    public List<Tag> getAllTags() {
        return new ArrayList<>(tags.values());
    }

    public List<Tag> searchTags(String query) {
        return tags.values().stream()
                .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void clear() {
        tags.clear();
        colorIndex = 0;
    }

    // 获取所有标签的Map形式，用于序列化
    public Map<UUID, Tag> getTagsMap() {
        return new HashMap<>(tags);
    }

    // 从Map设置标签，用于反序列化
    public void setTagsFromMap(Map<UUID, Tag> tagsMap) {
        this.tags.clear();
        this.tags.putAll(tagsMap);
    }
}
