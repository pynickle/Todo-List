package com.euphony.todo_list.neoforge;

import com.euphony.todo_list.TodoList;
import net.neoforged.fml.common.Mod;

@Mod(TodoList.MOD_ID)
public final class TodoListNeoForge {
    public TodoListNeoForge() {
        // Run our common setup.
        TodoList.init();
    }
}
