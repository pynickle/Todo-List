package com.euphony.todo_list.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TodoList模组配置管理器
 * 使用Minecraft自带的night-config库处理TOML格式配置文件
 */
public class TodoConfig {
    private static TodoConfig instance;
    private static final String CONFIG_FILE_NAME = "todolist-config.toml";

    private FileConfig config;

    // 配置选项枚举
    public enum TextAlignment {
        LEFT("left"),
        CENTER("center");

        private final String value;

        TextAlignment(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TextAlignment fromString(String value) {
            for (TextAlignment alignment : values()) {
                if (alignment.getValue().equals(value)) {
                    return alignment;
                }
            }
            return CENTER; // 默认值
        }
    }

    private TodoConfig() {
        load();
    }

    public static TodoConfig getInstance() {
        if (instance == null) {
            instance = new TodoConfig();
        }
        return instance;
    }

    /**
     * 获取配置文件路径
     */
    private Path getConfigFile() {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        Path configDir = gameDir.resolve("config");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }
        return configDir.resolve(CONFIG_FILE_NAME);
    }

    /**
     * 加载配置文件
     */
    public void load() {
        Path configPath = getConfigFile();

        // 创建默认配置文件（如果不存在）
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        // 使用night-config加载配置文件
        config = FileConfig.builder(configPath).build();
        config.load();

        // 确保所有必需的配置项都存在
        ensureDefaults();
        config.save();
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig(Path configPath) {
        try {
            String defaultConfig = """
                # TodoList Mod Configuration File
                # This file uses TOML format and supports comments
                
                [ui]
                # Text alignment configuration
                # Controls the alignment of todo item titles and tags in TodoListViewScreen
                # Available options:
                #   "left"   - Left align: Both titles and tags are aligned to the left
                #   "center" - Center align: Both titles and tags are centered
                # Default value: "center"
                text_alignment = "center"
                """;

            Files.writeString(configPath, defaultConfig);

        } catch (IOException e) {
            System.err.println("Failed to create default TodoList config: " + e.getMessage());
        }
    }

    /**
     * 确保默认配置值存在
     */
    private void ensureDefaults() {
        // 检查并设置默认的文本对齐方式
        if (!config.contains("ui.text_alignment")) {
            config.set("ui.text_alignment", TextAlignment.CENTER.getValue());
        }
    }

    /**
     * 获取文本对齐方式
     */
    public TextAlignment getTextAlignment() {
        String value = config.get("ui.text_alignment");
        return TextAlignment.fromString(value != null ? value : TextAlignment.CENTER.getValue());
    }

    /**
     * 设置文本对齐方式
     */
    public void setTextAlignment(TextAlignment alignment) {
        config.set("ui.text_alignment", alignment.getValue());
        config.save();
    }

    /**
     * 重新加载配置文件
     */
    public void reload() {
        if (config != null) {
            config.load();
        }
    }

    /**
     * 手动保存配置文件
     */
    public void save() {
        if (config != null) {
            config.save();
        }
    }

    /**
     * 关闭配置文件
     */
    public void close() {
        if (config != null) {
            config.close();
        }
    }
}
