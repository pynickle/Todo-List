package com.euphony.todo_list.client.screen;

import com.euphony.todo_list.client.components.TagDisplayWidget;
import com.euphony.todo_list.client.overlay.TodoOverlay;
import com.euphony.todo_list.config.TodoConfig;
import com.euphony.todo_list.todo.Tag;
import com.euphony.todo_list.todo.TodoItem;
import com.euphony.todo_list.todo.TodoListManager;
import com.euphony.todo_list.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class TodoListViewScreen extends Screen {
    public static final WidgetSprites ADD_SPRITES = new WidgetSprites(Utils.prefix("add"), Utils.prefix("add_disabled"), Utils.prefix("add_highlighted"));

    private int scrollOff;
    private final Screen parentScreen;
    private EditBox searchBox;
    private List<Tag> filterTags = new ArrayList<>();
    private List<TodoItem> filteredTodoItems = new ArrayList<>();
    private final int maxVisibleTodos = 4; // 最多显示4个todo
    private boolean isDragging = false; // 添加拖拽状态\

    public TodoListViewScreen(Screen parentScreen) {
        super(Component.translatable("todo_list.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.height / 2;

        // 搜索框
        searchBox = new EditBox(this.font, i - 120, 40, 200, 20, Component.translatable("todo_list.search"));
        searchBox.setHint(Component.translatable("todo_list.search_hint"));
        addRenderableWidget(searchBox);

        // 搜索按钮
        Button searchButton = Button.builder(
            Component.translatable("todo_list.search"),
            button -> refreshTodoList()
        ).pos(i + 85, 40)
        .size(50, 20)
        .build();
        addRenderableWidget(searchButton);

        // 清除搜索按钮
        Button clearButton = Button.builder(
            Component.translatable("todo_list.clear"),
            button -> {
                searchBox.setValue("");
                filterTags.clear();
                refreshTodoList();
            }
        ).pos(i + 140, 40)
        .size(40, 20)
        .build();
        addRenderableWidget(clearButton);

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

        // 悬浮窗过滤开关 - 在pin按钮右侧
        Button overlayFilterButton = Button.builder(
            TodoOverlay.isShowOnlyIncomplete() ?
                Component.translatable("todo_list.overlay_incomplete_only") :
                Component.translatable("todo_list.overlay_show_all"),
            button -> {
                TodoOverlay.toggleShowOnlyIncomplete();
                button.setMessage(TodoOverlay.isShowOnlyIncomplete() ?
                    Component.translatable("todo_list.overlay_incomplete_only") :
                    Component.translatable("todo_list.overlay_show_all"));
            }
        ).pos(75, 10)
        .size(80, 20)
        .build();

        this.addRenderableWidget(overlayFilterButton);

        String searchText = searchBox != null ? searchBox.getValue() : "";
        filteredTodoItems = TodoListManager.getInstance().getFilteredTodoItems(filterTags, searchText);

        List<AbstractWidget> widgets = renderTodoItems(this.height / 2);
        for(AbstractWidget widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    private void refreshTodoList() {
        String searchText = searchBox != null ? searchBox.getValue() : "";
        filteredTodoItems = TodoListManager.getInstance().getFilteredTodoItems(filterTags, searchText);
        this.clearWidgets();
        this.init();
    }

    private List<AbstractWidget> renderTodoItems(int centerY) {
        List<AbstractWidget> widgets = new ArrayList<>();
        TodoConfig config = TodoConfig.getInstance();
        boolean isLeftAlign = config.getTextAlignment() == TodoConfig.TextAlignment.LEFT;

        // 预先计算常用值，避免重复计算
        int baseX = this.width / 2 - 90;
        int checkboxX = this.width / 2 - 120;
        int editBtnX = this.width / 2 + 70;
        int deleteBtnX = this.width / 2 + 110;
        final int availableWidth = 150;
        final String ellipsis = "...";
        final int ellipsisWidth = this.font.width(ellipsis);

        int displayIndex = 0;
        for(int itemIndex = 0; itemIndex < filteredTodoItems.size(); itemIndex++) {
            TodoItem todoItem = filteredTodoItems.get(itemIndex);

            // 只显示在当前滚动范围内的项目
            if (!this.canScroll(filteredTodoItems.size()) || (itemIndex >= this.scrollOff && itemIndex < maxVisibleTodos + this.scrollOff)) {
                int yPos = centerY - 50 + displayIndex * 35;
                boolean hasTags = !todoItem.getTags().isEmpty();

                // 优化文本截断逻辑
                String titleText = todoItem.getTitle();
                int textWidth = this.font.width(titleText);

                if (textWidth > availableWidth) {
                    // 使用二分查找优化文本截断
                    int left = 0, right = titleText.length();
                    while (left < right) {
                        int mid = (left + right + 1) / 2;
                        String substr = titleText.substring(0, mid);
                        if (this.font.width(substr) + ellipsisWidth <= availableWidth) {
                            left = mid;
                        } else {
                            right = mid - 1;
                        }
                    }
                    titleText = titleText.substring(0, left) + ellipsis;
                    textWidth = this.font.width(titleText); // 重新计算截断后的宽度
                }

                Component titleComponent = todoItem.isCompleted() ?
                    Component.literal("§m" + titleText) :
                    Component.literal(titleText);

                // 根据配置和是否有标签决定Y位置
                int titleY = hasTags ? yPos + 2 : yPos + 8;

                // 创建标题widget
                StringWidget titleWidget;
                if (isLeftAlign) {
                    titleWidget = new StringWidget(baseX, titleY, availableWidth, 12, titleComponent, this.font);
                    titleWidget.alignLeft();
                } else {
                    int centeredX = baseX + (availableWidth - textWidth) / 2;
                    titleWidget = new StringWidget(centeredX, titleY, textWidth, 12, titleComponent, this.font);
                }
                widgets.add(titleWidget);

                // 如果有标签，添加标签显示
                if (hasTags) {
                    TagDisplayWidget tagDisplay = new TagDisplayWidget(
                        baseX, yPos + 14,
                        150, 12,
                        todoItem.getTags(),
                        isLeftAlign
                    );
                    widgets.add(tagDisplay);
                }

                // 复选框
                Checkbox checkbox = Checkbox.builder(
                                Component.empty(),
                                this.font
                        ).pos(checkboxX, yPos + 5)
                        .selected(todoItem.isCompleted())
                        .onValueChange((cb, selected) -> {
                            TodoListManager.getInstance().toggleCompleted(todoItem.getId());
                            refreshTodoList();
                        })
                        .build();
                widgets.add(checkbox);

                // 编辑按钮
                Button editBtn = Button.builder(
                                Component.translatable("todo_list.edit"),
                                button -> Minecraft.getInstance().setScreen(new TodoAddScreen(todoItem, this))
                        ).pos(editBtnX, yPos)
                        .size(35, 20)
                        .build();
                widgets.add(editBtn);

                // 删除按钮
                Button deleteBtn = Button.builder(
                                Component.translatable("todo_list.delete"),
                                button -> {
                                    TodoListManager.getInstance().removeTodoItem(todoItem.getId());
                                    refreshTodoList();
                                }
                        ).pos(deleteBtnX, yPos)
                        .size(35, 20)
                        .build();
                widgets.add(deleteBtn);

                displayIndex++;
            }
        }
        return widgets;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // 绘制滚动条
        if (filteredTodoItems.size() > maxVisibleTodos) {
            renderScrollbar(guiGraphics);
        }

        // 绘制搜索结果信息
        if (searchBox != null && !searchBox.getValue().isEmpty()) {
            Component searchInfo = Component.translatable("todo_list.search_results",
                filteredTodoItems.size(), TodoListManager.getInstance().getAllTodoItems().size());
            guiGraphics.drawCenteredString(this.font, searchInfo, this.width / 2, 65, 0xAAAAAA);
        }

        // 绘制当前筛选标签（如果有）
        if (!filterTags.isEmpty()) {
            Component filterInfo = Component.translatable("todo_list.filtered_by_tags", filterTags.size());
            guiGraphics.drawCenteredString(this.font, filterInfo, this.width / 2,
                searchBox.getValue().isEmpty() ? 65 : 75, 0xAAAAAA);
        }

        // 绘制说明文字
        guiGraphics.drawCenteredString(this.font,
            Component.translatable("todo_list.instructions"),
            this.width / 2, this.height - 30, 0xAAAAAA);
    }

    // 渲染滚动条
    private void renderScrollbar(GuiGraphics guiGraphics) {
        int centerY = this.height / 2;
        int scrollbarX = this.width / 2 + 160; // 位置在todo列表右侧
        int scrollbarY = centerY - 50; // 与todo列表对齐
        int scrollbarHeight = maxVisibleTodos * 35; // 35是每个todo项的高度

        // 滚动条背景
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0x40FFFFFF);

        // 滚动条滑块 - 参考村民界面的实现
        int totalTodos = filteredTodoItems.size();
        if (totalTodos > maxVisibleTodos) {
            // 计算滑块高度和位置
            int maxScroll = totalTodos - maxVisibleTodos;
            int scrollAreaHeight = scrollbarHeight - 27; // 减去滑块高度
            int thumbHeight = 27; // 固定滑块高度

            // 计算滑块位置
            int thumbY;
            thumbY = scrollbarY + (scrollAreaHeight * scrollOff / maxScroll);

            // 确保滑块不超出边界
            thumbY = Math.min(thumbY, scrollbarY + scrollbarHeight - thumbHeight);

            guiGraphics.fill(scrollbarX + 1, thumbY, scrollbarX + 5, thumbY + thumbHeight, 0x80FFFFFF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        } else {
            int totalTodos = filteredTodoItems.size();
            if (this.canScroll(totalTodos)) {
                int maxScroll = totalTodos - maxVisibleTodos;
                this.scrollOff = Mth.clamp((int)(this.scrollOff - scrollY), 0, maxScroll);
                refreshTodoList();
            }
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            int centerY = this.height / 2;
            int scrollbarY = centerY - 50;
            int scrollbarHeight = maxVisibleTodos * 35;
            int totalTodos = filteredTodoItems.size();
            int maxScroll = totalTodos - maxVisibleTodos;

            // 计算滚动位置，参考村民界面的实现
            float scrollRatio = ((float)mouseY - scrollbarY - 13.5F) / (scrollbarHeight - 27.0F);
            scrollRatio = scrollRatio * maxScroll + 0.5F;
            int newScrollOff = Mth.clamp((int)scrollRatio, 0, maxScroll);

            // 只有当滚动位置真的改变时才刷新
            if (newScrollOff != scrollOff) {
                scrollOff = newScrollOff;
                this.rebuildWidgets();
            }
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 重置拖拽状态
        isDragging = false;

        // 检查是否点击在滚动条上
        if (filteredTodoItems.size() > maxVisibleTodos) {
            int centerY = this.height / 2;
            int scrollbarX = this.width / 2 + 160;
            int scrollbarY = centerY - 50;
            int scrollbarHeight = maxVisibleTodos * 35;

            if (mouseX > scrollbarX && mouseX < scrollbarX + 6 &&
                mouseY > scrollbarY && mouseY <= scrollbarY + scrollbarHeight + 1) {
                isDragging = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean canScroll(int numTodos) {
        return numTodos > maxVisibleTodos;
    }
}
