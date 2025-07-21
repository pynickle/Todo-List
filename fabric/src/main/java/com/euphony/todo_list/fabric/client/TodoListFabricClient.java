package com.euphony.todo_list.fabric.client;

import com.euphony.todo_list.client.keymapping.TodoKeyMappings;
import com.euphony.todo_list.client.keymapping.event.OpenTodoListEvent;
import com.euphony.todo_list.client.overlay.TodoOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;

public final class TodoListFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TodoKeyMappings.OPEN_TODO_LIST);

        ClientTickEvents.END_CLIENT_TICK.register(OpenTodoListEvent::handleKeyInput);

        // 注册悬浮窗渲染事件
        HudRenderCallback.EVENT.register(TodoOverlay::render);

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, key, scancode, modifiers) -> {
                if(!TodoKeyMappings.OPEN_TODO_LIST.matches(key, scancode)) return;

                OpenTodoListEvent.openTodoList(Minecraft.getInstance());
            });
        });
    }
}
