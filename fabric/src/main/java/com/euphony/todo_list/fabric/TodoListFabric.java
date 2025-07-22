package com.euphony.todo_list.fabric;

import com.euphony.todo_list.TodoList;
import com.euphony.todo_list.todo.TodoListManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class TodoListFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TodoList.init();
    }
}
