package com.euphony.todo_list;

import com.euphony.todo_list.config.TodoConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class TodoList {
    public static final String MOD_ID = "todo_list";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        TodoConfig.getInstance().load();
    }
}
