package com.euphony.todo_list.client.screen;

import com.euphony.todo_list.todo.TodoItem;
import com.euphony.todo_list.todo.TodoListManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class TodoAddScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    EditBox titleEditBox;
    MultiLineEditBox descriptionEditBox;

    // 编辑模式相关字段
    private final boolean isEditMode;
    private final UUID editingItemId;
    private final Screen parentScreen;

    // 通用构造函数（支持添加和编辑）
    public TodoAddScreen(TodoItem todoItem, Screen parentScreen) {
        super(todoItem != null ? Component.translatable("todo_list.edit_title") : Component.translatable("todo_list.add_title"));
        this.isEditMode = todoItem != null;
        this.editingItemId = todoItem != null ? todoItem.getId() : null;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        // 界面布局参数
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int formWidth = 280;
        int leftMargin = centerX - formWidth / 2;

        // 标题区域 - 向上移动，给标题留出更多空间
        int titleAreaY = centerY - 110;  // 比原来更高

        // 主标题 - 作为 StringWidget 添加到界面中
        Component screenTitle = isEditMode ?
            Component.translatable("todo_list.edit_title") :
            Component.translatable("todo_list.add_title");
        StringWidget mainTitle = new StringWidget(
            leftMargin, titleAreaY, formWidth, 16,
            screenTitle, this.font
        );
        mainTitle.setColor(0xFFFFFF);
        mainTitle.alignCenter();  // 居中对齐
        addRenderableWidget(mainTitle);

        // 输入框区域 - 稍微向下调整
        int titleFieldY = centerY - 70;

        // 标题输入框标签
        StringWidget titleLabel = new StringWidget(
            leftMargin, titleFieldY - 15, formWidth, 12,
            Component.translatable("todo_list.title_label"),
            this.font
        );
        titleLabel.setColor(0xFFFFFF);
        addRenderableWidget(titleLabel);

        // 标题输入框
        titleEditBox = addRenderableWidget(new EditBox(
                this.font,
                leftMargin,
                titleFieldY,
                formWidth,
                22,
                Component.translatable("todo_list.title_hint")
        ));
        titleEditBox.setMaxLength(100);

        // 描述区域
        int descY = titleFieldY + 40;

        // 描述输入框标签
        StringWidget descLabel = new StringWidget(
            leftMargin, descY - 15, formWidth, 12,
            Component.translatable("todo_list.description_label"),
            this.font
        );
        descLabel.setColor(0xFFFFFF);
        addRenderableWidget(descLabel);

        // 描述输入框
        descriptionEditBox = addRenderableWidget(new MultiLineEditBox(
                this.font,
                leftMargin,
                descY,
                formWidth,
                80,
                Component.translatable("todo_list.add_item"),
                Component.translatable("todo_list.description_hint")
        ));

        // 如果是编辑模式，预填充现有数据
        if (isEditMode && editingItemId != null) {
            TodoItem item = TodoListManager.getInstance().getTodoItem(editingItemId);
            if (item != null) {
                titleEditBox.setValue(item.getTitle());
                descriptionEditBox.setValue(item.getDescription());

                // 添加创建时间显示（仅在编辑模式下）
                String createdTimeText = item.getCreatedAt().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
                Component createdTimeComponent = Component.translatable("todo_list.created_time", createdTimeText);

                // 创建时间标签
                StringWidget createdTimeLabel = new StringWidget(
                    leftMargin, descY + 90, formWidth, 12,
                    createdTimeComponent,
                    this.font
                );
                createdTimeLabel.setColor(0xAAAAAA); // 使用较淡的颜色
                createdTimeLabel.alignCenter(); // 居中显示
                addRenderableWidget(createdTimeLabel);
            }
        }

        // 按钮区域 - 如果是编辑模式需要为创建时间留出空间
        int buttonY = isEditMode ? descY + 110 : descY + 100;
        int buttonWidth = 80;
        int buttonSpacing = 20;

        // 主要操作按钮（保存/添加）
        Component buttonText = isEditMode ?
            Component.translatable("todo_list.save_button") :
            Component.translatable("todo_list.add_button");

        Button primaryButton = Button.builder(
                buttonText,
                button -> {
                    String title = titleEditBox.getValue();
                    String description = descriptionEditBox.getValue();
                    if (!title.isEmpty()) {
                        if (isEditMode && editingItemId != null) {
                            // 更新现有项目
                            TodoListManager.getInstance().updateTodoItem(editingItemId, title, description);
                        } else {
                            // 添加新项目
                            TodoItem todoItem = new TodoItem(title, description);
                            TodoListManager.getInstance().addTodoItem(todoItem);
                        }
                        this.onClose();
                    }
                }
        ).pos(centerX - buttonWidth - buttonSpacing / 2, buttonY)
                .size(buttonWidth, 24)  // 稍微高一点的按钮
                .build();
        addRenderableWidget(primaryButton);

        // 取消按钮
        Button cancelButton = Button.builder(
                CommonComponents.GUI_CANCEL,
                button -> this.onClose()
        ).pos(centerX + buttonSpacing / 2, buttonY)
                .size(buttonWidth, 24)
                .build();
        addRenderableWidget(cancelButton);

        // 设置默认焦点到标题输入框
        this.setInitialFocus(titleEditBox);
    }

    @Override
    public void onClose() {
        if (parentScreen != null) {
            this.minecraft.setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制界面背景装饰
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int formWidth = 280;
        int formHeight = 220;  // 稍微增加高度以容纳标题
        int leftMargin = centerX - formWidth / 2;
        int topMargin = centerY - formHeight / 2;

        // 绘制表单背景框 - 扩展到包含标题区域
        guiGraphics.fill(
            leftMargin - 20, topMargin - 30,  // 向上扩展
            leftMargin + formWidth + 20, topMargin + formHeight + 20,
            0x40000000
        );

        // 绘制表单边框
        guiGraphics.fill(
            leftMargin - 21, topMargin - 31,
            leftMargin + formWidth + 21, topMargin - 30,
            0x80FFFFFF  // 上边框
        );
        guiGraphics.fill(
            leftMargin - 21, topMargin + formHeight + 20,
            leftMargin + formWidth + 21, topMargin + formHeight + 21,
            0x80FFFFFF  // 下边框
        );
        guiGraphics.fill(
            leftMargin - 21, topMargin - 30,
            leftMargin - 20, topMargin + formHeight + 20,
            0x80FFFFFF  // 左边框
        );
        guiGraphics.fill(
            leftMargin + formWidth + 20, topMargin - 30,
            leftMargin + formWidth + 21, topMargin + formHeight + 20,
            0x80FFFFFF  // 右边框
        );

        // 绘制标题下方的分隔线
        int separatorY = centerY - 95;
        guiGraphics.fill(
            leftMargin, separatorY,
            leftMargin + formWidth, separatorY + 1,
            0x80FFFFFF
        );
    }
}
