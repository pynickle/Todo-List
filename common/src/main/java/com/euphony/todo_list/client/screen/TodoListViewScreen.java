package com.euphony.todo_list.client.screen;

import com.euphony.todo_list.client.overlay.TodoOverlay;
import com.euphony.todo_list.todo.TodoItem;
import com.euphony.todo_list.todo.TodoListManager;
import com.euphony.todo_list.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class TodoListViewScreen extends Screen {
    public static final WidgetSprites ADD_SPRITES = new WidgetSprites(Utils.prefix("add"), Utils.prefix("add_disabled"), Utils.prefix("add_highlighted"));

    private int scrollOff;
    private final Screen parentScreen;

    public TodoListViewScreen(Screen parentScreen) {
        super(Component.translatable("todo_list.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.height / 2;

        this.addRenderableWidget(new ImageButton(this.width - 30, 8, 18, 18,
                ADD_SPRITES,
                button -> Minecraft.getInstance().setScreen(new TodoAddScreen(null, this)),
                null
        ));

        // Pin 按钮 - 用于切换悬浮窗显示
        Button pinButton = Button.builder(
            TodoOverlay.isVisible() ?
                Component.translatable("todo_list.unpin") :
                Component.translatable("todo_list.pin"),
            button -> {
                TodoOverlay.setVisible(!TodoOverlay.isVisible());
                // 更新按钮文字
                button.setMessage(TodoOverlay.isVisible() ?
                    Component.translatable("todo_list.unpin") :
                    Component.translatable("todo_list.pin"));
            }
        ).pos(10, 10)
        .size(60, 20)
        .build();

        this.addRenderableWidget(pinButton);

        TodoListManager todoListManager = TodoListManager.getInstance();
        List<TodoItem> todoItems = todoListManager.getAllTodoItems();

        int displayIndex = 0;
        for(int itemIndex = 0; itemIndex < todoItems.size(); itemIndex++) {
            TodoItem todoItem = todoItems.get(itemIndex);

            // 只显示在当前滚动范围内的项目
            if (!this.canScroll(todoItems.size()) || (itemIndex >= this.scrollOff && itemIndex < 7 + this.scrollOff)) {
                int yPos = j - 80 + displayIndex * 25;

                // 复选框
                Checkbox checkbox = Checkbox.builder(
                        Component.empty(),
                        this.font
                ).pos(i - 120, yPos)
                .selected(todoItem.isCompleted())
                .onValueChange((cb, selected) -> {
                    todoListManager.toggleCompleted(todoItem.getId());
                    this.rebuildWidgets(); // 重新构建界面以刷新显示
                })
                .build();

                addRenderableWidget(checkbox);

                // 待办事项标题 - 如果已完成则显示删除线效果，并确保文本不会溢出
                String titleText = todoItem.getTitle();

                // 计算可用宽度：从复选框右侧到编辑按钮左侧
                int availableWidth = 150; // 可调整的文本显示区域宽度

                // 截断过长的文本
                if (this.font.width(titleText) > availableWidth) {
                    // 逐字符减少直到文本适合宽度（包括省略号的宽度）
                    String ellipsis = "...";
                    int ellipsisWidth = this.font.width(ellipsis);

                    while (this.font.width(titleText) + ellipsisWidth > availableWidth && titleText.length() > 0) {
                        titleText = titleText.substring(0, titleText.length() - 1);
                    }
                    titleText += ellipsis;
                }

                Component titleComponent = todoItem.isCompleted() ?
                    Component.literal("§m" + titleText) :
                    Component.literal(titleText);

                StringWidget titleWidget = new StringWidget(i - 90, yPos + 5, availableWidth, 12, titleComponent, this.font);
                addRenderableWidget(titleWidget);

                // 编辑按钮
                Button editBtn = Button.builder(
                    Component.translatable("todo_list.edit"),
                    button -> Minecraft.getInstance().setScreen(new TodoAddScreen(todoItem, this))
                ).pos(i + 70, yPos)
                .size(35, 20)
                .build();

                addRenderableWidget(editBtn);

                // 删除按钮
                Button deleteBtn = Button.builder(
                    Component.translatable("todo_list.delete"),
                    button -> {
                        todoListManager.removeTodoItem(todoItem.getId());
                        this.rebuildWidgets(); // 重新构建界面以更新显示
                    }
                ).pos(i + 110, yPos)
                .size(35, 20)
                .build();

                addRenderableWidget(deleteBtn);

                displayIndex++;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // 绘制说明文字
        guiGraphics.drawCenteredString(this.font,
            Component.translatable("todo_list.instructions"),
            this.width / 2, this.height - 30, 0xAAAAAA);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        } else {
            int i = TodoListManager.getInstance().getAllTodoItems().size();
            if (this.canScroll(i)) {
                int j = i - 7;
                this.scrollOff = Mth.clamp((int)(this.scrollOff - scrollY), 0, j);
                this.rebuildWidgets(); // 重新构建界面以更新滚动位置
            }

            return true;
        }
    }

    @Override
    public void onClose() {
        if (parentScreen != null) {
            this.minecraft.setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    private boolean canScroll(int numOffers) {
        return numOffers > 7;
    }
}
