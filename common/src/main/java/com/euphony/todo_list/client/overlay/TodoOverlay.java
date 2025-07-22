package com.euphony.todo_list.client.overlay;

import com.euphony.todo_list.todo.TodoItem;
import com.euphony.todo_list.todo.TodoListManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 简洁的待办清单悬浮窗口 - 仅用于显示
 */
public class TodoOverlay {
    private static boolean isVisible = false;
    private static final int OVERLAY_WIDTH = 180;  // 稍微缩小宽度
    private static final int MIN_HEIGHT = 40;      // 最小高度（只有标题）
    private static final int MAX_HEIGHT = 120;     // 最大高度上限
    private static final int ITEM_HEIGHT = 12;
    private static final int PADDING = 4;          // 减少内边距
    private static final int HEADER_HEIGHT = 16;   // 标题区域高度

    public static void setVisible(boolean visible) {
        isVisible = visible;
    }

    public static boolean isVisible() {
        return isVisible;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) return; // 只在游戏界面显示，不在菜单界面显示

        // 检查是否在F1模式（隐藏GUI）或F3模式（调试信息显示）
        if (minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) {
            return; // F1或F3模式时不显示
        }

        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        // 获取待办事项并计算动态高度
        TodoListManager manager = TodoListManager.getInstance();
        List<TodoItem> todoItems = manager.getAllTodoItems();

        // 动态计算悬浮窗高度
        int dynamicHeight = calculateOptimalHeight(todoItems.size());
        int maxDisplayItems = Math.min(todoItems.size(), (dynamicHeight - HEADER_HEIGHT - PADDING * 2) / ITEM_HEIGHT);

        // 计算悬浮窗位置（屏幕右侧）
        int overlayX = screenWidth - OVERLAY_WIDTH - 10;
        int overlayY = 10;

        // 绘制背景 - 使用更透明的背景
        guiGraphics.fill(overlayX - PADDING, overlayY - PADDING,
                        overlayX + OVERLAY_WIDTH, overlayY + dynamicHeight + PADDING,
                        0x60000000); // 更透明的背景 (60% -> 40% 透明度)

        // 绘制边框 - 使用更淡的边框
        int borderColor = 0x80FFFFFF; // 50% 透明的白色
        guiGraphics.fill(overlayX - PADDING - 1, overlayY - PADDING - 1,
                        overlayX + OVERLAY_WIDTH + 1, overlayY - PADDING,
                        borderColor); // 上边框
        guiGraphics.fill(overlayX - PADDING - 1, overlayY + dynamicHeight + PADDING,
                        overlayX + OVERLAY_WIDTH + 1, overlayY + dynamicHeight + PADDING + 1,
                        borderColor); // 下边框
        guiGraphics.fill(overlayX - PADDING - 1, overlayY - PADDING,
                        overlayX - PADDING, overlayY + dynamicHeight + PADDING,
                        borderColor); // 左边框
        guiGraphics.fill(overlayX + OVERLAY_WIDTH, overlayY - PADDING,
                        overlayX + OVERLAY_WIDTH + 1, overlayY + dynamicHeight + PADDING,
                        borderColor); // 右边框

        // 绘制标题 - 使用稍小的字体效果
        Component title = Component.translatable("todo_list.overlay_title");
        guiGraphics.drawString(font, title, overlayX + 2, overlayY + 2, 0xFFFFFF);

        int currentY = overlayY + HEADER_HEIGHT;

        if (todoItems.isEmpty()) {
            // 没有待办事项时显示提示
            Component emptyText = Component.translatable("todo_list.overlay_empty");
            guiGraphics.drawString(font, emptyText, overlayX + 2, currentY, 0xAAAAAA);
        } else {
            // 显示待办事项
            for (int i = 0; i < maxDisplayItems; i++) {
                TodoItem item = todoItems.get(i);

                // 绘制复选框符号
                String checkbox = item.isCompleted() ? "☑" : "☐";
                guiGraphics.drawString(font, checkbox, overlayX + 2, currentY, 0xFFFFFF);

                // 绘制标题 - 根据实际像素宽度动态计算最大字符数
                String title_text = item.getTitle();

                // 计算可用宽度：总宽度 - 复选框宽度 - 右边距
                int availableWidth = OVERLAY_WIDTH - 16 - 8; // 16是复选框+间距，8是右边距

                // 动态计算最大字符数
                int maxLength = calculateMaxTextLength(font, title_text, availableWidth);
                if (title_text.length() > maxLength && maxLength > 3) {
                    title_text = title_text.substring(0, maxLength - 3) + "...";
                }

                Component titleComponent = item.isCompleted() ?
                    Component.literal("§m" + title_text) :
                    Component.literal(title_text);

                guiGraphics.drawString(font, titleComponent, overlayX + 14, currentY,
                    item.isCompleted() ? 0xAAAAAA : 0xFFFFFF);

                currentY += ITEM_HEIGHT;
            }

            // 如果有更多项目，显示省略号和数量
            if (todoItems.size() > maxDisplayItems) {
                String moreText = "+" + (todoItems.size() - maxDisplayItems) + " more...";
                guiGraphics.drawString(font, moreText, overlayX + 2, currentY, 0xAAAAAA);
            }
        }
    }

    /**
     * 根据待办事项数量计算最佳高度
     */
    private static int calculateOptimalHeight(int itemCount) {
        if (itemCount == 0) {
            // 没有项目时的最小高度
            return MIN_HEIGHT;
        }

        // 计算所需高度：标题 + 项目 + 内边距
        int requiredHeight = HEADER_HEIGHT + (itemCount * ITEM_HEIGHT) + (PADDING * 2);

        // 限制在最大高度内
        return Math.min(requiredHeight, MAX_HEIGHT);
    }

    /**
     * 计算给定文本在指定宽度内的最大字符数
     */
    private static int calculateMaxTextLength(Font font, String text, int maxWidth) {
        int ellipsisWidth = font.width("..."); // 省略号的宽度
        int textWidth = 0;
        int charCount = 0;

        // 遍历文本中的每个字符，计算宽度
        for (char c : text.toCharArray()) {
            textWidth += font.width(Character.toString(c));
            charCount++;

            // 如果当前字符数的宽度加上省略号的宽度超过最大宽度，返回当前字符数
            if (textWidth + ellipsisWidth > maxWidth) {
                return charCount;
            }
        }

        // 如果没有超过最大宽度，返回文本的总字符数
        return charCount;
    }
}
