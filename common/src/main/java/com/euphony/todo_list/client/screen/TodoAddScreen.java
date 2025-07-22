package com.euphony.todo_list.client.screen;

import com.euphony.todo_list.client.components.TagSelectionPanel;
import com.euphony.todo_list.todo.Tag;
import com.euphony.todo_list.todo.TodoItem;
import com.euphony.todo_list.todo.TodoListManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoAddScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    EditBox titleEditBox;
    MultiLineEditBox descriptionEditBox;
    TagSelectionPanel tagSelectionPanel;
    private List<Tag> selectedTags = new ArrayList<>();

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

        // 如果是编辑模式，预设标签
        if (isEditMode) {
            this.selectedTags = new ArrayList<>(todoItem.getTags());
        }
    }

    @Override
    protected void init() {
        super.init();

        // 界面布局参数 - 调整为左侧标签面板和右侧主表单
        int panelWidth = 120; // 标签面板宽度
        int formWidth = 240;
        int spacing = 20;
        int leftMargin = 10;
        int centerX = leftMargin + panelWidth + spacing + formWidth / 2;
        int centerY = this.height / 2;

        // 创建标签选择面板
        tagSelectionPanel = new TagSelectionPanel(
            leftMargin, centerY - 100, panelWidth, 200,
            this, selectedTags,
            (tags) -> this.selectedTags = new ArrayList<>(tags)
        );
        addRenderableWidget(tagSelectionPanel);


        // 标题区域 - 向上移动，给标题留出更多空间
        int titleAreaY = centerY - 110;

        // 主标题 - 作为 StringWidget 添加到界面中
        Component screenTitle = isEditMode ?
            Component.translatable("todo_list.edit_title") :
            Component.translatable("todo_list.add_title");
        StringWidget mainTitle = new StringWidget(
            leftMargin + panelWidth + spacing, titleAreaY, formWidth, 16,
            screenTitle, this.font
        );
        mainTitle.setColor(0xFFFFFF);
        mainTitle.alignCenter();
        addRenderableWidget(mainTitle);

        // 输入框区域 - 稍微向下调整
        int titleFieldY = centerY - 70;
        int formX = leftMargin + panelWidth + spacing;

        // 标题输入框标签
        StringWidget titleLabel = new StringWidget(
            formX, titleFieldY - 15, formWidth, 12,
            Component.translatable("todo_list.title_label"),
            this.font
        );
        titleLabel.setColor(0xFFFFFF);
        addRenderableWidget(titleLabel);

        // 标题输入框
        titleEditBox = addRenderableWidget(new EditBox(
                this.font,
                formX,
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
            formX, descY - 15, formWidth, 12,
            Component.translatable("todo_list.description_label"),
            this.font
        );
        descLabel.setColor(0xFFFFFF);
        addRenderableWidget(descLabel);

        // 描述输入框
        descriptionEditBox = addRenderableWidget(new MultiLineEditBox(
                this.font,
                formX,
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
                selectedTags = new ArrayList<>(item.getTags());

                // 设置初始化标志，防止无限递归
                tagSelectionPanel.setInitializing(true);
                tagSelectionPanel.setSelectedTags(selectedTags);
                tagSelectionPanel.setInitializing(false);

                // 添加创建时间显示（仅在编辑模式下）
                String createdTimeText = item.getCreatedAt().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
                Component createdTimeComponent = Component.translatable("todo_list.created_time", createdTimeText);

                // 创建时间标签
                StringWidget createdTimeLabel = new StringWidget(
                    formX, descY + 90, formWidth, 12,
                    createdTimeComponent,
                    this.font
                );
                createdTimeLabel.setColor(0xAAAAAA);
                createdTimeLabel.alignCenter();
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
                            TodoListManager.getInstance().updateTodoItem(editingItemId, title, description, selectedTags);
                        } else {
                            // 添加新项目
                            TodoItem todoItem = new TodoItem(title, description);
                            todoItem.setTags(selectedTags);
                            TodoListManager.getInstance().addTodoItem(todoItem);
                        }
                        this.onClose();
                    }
                }
        ).pos(centerX - buttonWidth - buttonSpacing / 2, buttonY)
                .size(buttonWidth, 24)
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

        List<AbstractWidget> abstractWidgets = tagSelectionPanel.createChildComponents();
        for (AbstractWidget abstractWidget : abstractWidgets) {
            addRenderableWidget(abstractWidget);
        }

        // 设置默认焦点到标题输入框
        this.setInitialFocus(titleEditBox);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 先让标签面板处理鼠标点击
        if (tagSelectionPanel != null && tagSelectionPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        boolean result = super.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 先让标签面板处理按键事件
        if (tagSelectionPanel != null && tagSelectionPanel.handleKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 先让标签面板处理字符输入事件
        /*
        if (tagSelectionPanel != null) {
            if (tagSelectionPanel.charTyped(codePoint, modifiers)) {
                return true;
            }
            if (tagSelectionPanel.charTyped(codePoint, modifiers)) {
                return true;
            }
        }

         */
        return super.charTyped(codePoint, modifiers);
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
        int formWidth = 240;
        int panelWidth = 120; // 标签面板宽度
        int spacing = 20;
        int leftMargin = 10;
        int centerY = this.height / 2;
        int formX = leftMargin + panelWidth + spacing;

        // 绘制标题下方的分隔线
        int separatorY = centerY - 95;
        guiGraphics.fill(
            formX, separatorY,
            formX + formWidth, separatorY + 1,
            0x80FFFFFF
        );
    }
}
