package com.euphony.todo_list.fabric;

import com.euphony.todo_list.TodoList;
import net.fabricmc.api.ModInitializer;

public final class TodoListFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TodoList.init();
    }
}
