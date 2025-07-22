package com.euphony.todo_list.neoforge.client;

import com.euphony.todo_list.TodoList;
import com.euphony.todo_list.client.keymapping.TodoKeyMappings;
import com.euphony.todo_list.client.keymapping.event.OpenTodoListEvent;
import com.euphony.todo_list.client.overlay.TodoOverlay;
import com.euphony.todo_list.todo.TodoListManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TodoList.MOD_ID, value = Dist.CLIENT)
public class TodoListNeoForgeClient {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TodoKeyMappings.OPEN_TODO_LIST);
    }

    @SubscribeEvent
    public static void onKeyPressed(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        OpenTodoListEvent.handleKeyInput(minecraft);
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyReleased.Post event) {
        if(!TodoKeyMappings.OPEN_TODO_LIST.matches(event.getKeyCode(), event.getScanCode())) return;

        OpenTodoListEvent.handleKeyRelease(Minecraft.getInstance());
    }

    // 渲染悬浮窗
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        TodoOverlay.render(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onWorldLoad(PlayerEvent.PlayerLoggedInEvent event) {
        TodoListManager.getInstance().loadFromFile();
    }
}
