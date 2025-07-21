package com.euphony.todo_list.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class TodoKeyMappings {
    public static final KeyMapping OPEN_TODO_LIST = new KeyMapping(
            "key.todo_list.open_todo_list",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.todo_list.keymapping"
    );
}
