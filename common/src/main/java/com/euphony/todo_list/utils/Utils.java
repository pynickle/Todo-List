package com.euphony.todo_list.utils;

import com.euphony.todo_list.TodoList;
import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(TodoList.MOD_ID, path);
    }
}
