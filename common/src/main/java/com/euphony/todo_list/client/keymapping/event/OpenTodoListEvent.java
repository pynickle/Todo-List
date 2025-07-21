package com.euphony.todo_list.client.keymapping.event;

import com.euphony.todo_list.client.keymapping.TodoKeyMappings;
import com.euphony.todo_list.client.screen.TodoAddScreen;
import com.euphony.todo_list.client.screen.TodoListViewScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;

public class OpenTodoListEvent {
    private static boolean wasKeyPressed = false;

    public static void handleKeyInput(Minecraft minecraft) {
        boolean isKeyPressed = TodoKeyMappings.OPEN_TODO_LIST.isDown();

        // 如果当前已经在待办清单相关界面，不处理按键事件
        if (minecraft.screen instanceof TodoListViewScreen || minecraft.screen instanceof TodoAddScreen) {
            wasKeyPressed = isKeyPressed;
            return;
        }

        // 检查是否有输入框正在被使用（额外的安全检查）
        if (minecraft.screen != null && isInputFieldFocused(minecraft)) {
            wasKeyPressed = isKeyPressed;
            return;
        }

        if (isKeyPressed && !wasKeyPressed) {
            openTodoList(minecraft);
        }

        wasKeyPressed = isKeyPressed;
    }

    /**
     * 检查当前是否有输入框获得焦点
     */
    private static boolean isInputFieldFocused(Minecraft minecraft) {
        if (minecraft.screen == null) return false;

        // 检查当前焦点组件是否为输入框
        var focusedWidget = minecraft.screen.getFocused();
        return focusedWidget instanceof EditBox || focusedWidget instanceof MultiLineEditBox;
    }

    public static void openTodoList(Minecraft minecraft) {
        // 只在不是待办清单相关界面时才打开
        if (!(minecraft.screen instanceof TodoListViewScreen) && !(minecraft.screen instanceof TodoAddScreen)) {
            minecraft.setScreen(new TodoListViewScreen(minecraft.screen));
        }
    }
}
