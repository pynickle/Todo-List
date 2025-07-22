package com.euphony.todo_list.client.components;

import com.euphony.todo_list.todo.Tag;
import com.euphony.todo_list.todo.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.euphony.todo_list.TodoList.LOGGER;

public class TagSelectionPanel extends AbstractWidget {
    private final Screen parentScreen;
    private final List<Tag> selectedTags;
    private final Consumer<List<Tag>> onTagsChanged;
    private final Font font;
    private int scrollOff = 0; // 改名为scrollOff，与村民界面保持一致
    private boolean isDragging = false; // 添加拖拽状态
    private final int maxVisibleTags = 7;

    // 添加标志来防止无限递归
    private boolean isInitializing = false;

    // 这些组件将由外部管理
    private EditBox searchInput;
    private EditBox newTagInput;
    private Button addTagButton;
    private final List<Checkbox> tagCheckboxes = new ArrayList<>();

    public TagSelectionPanel(int x, int y, int width, int height, Screen parentScreen,
                           List<Tag> initialSelectedTags, Consumer<List<Tag>> onTagsChanged) {
        super(x, y, width, height, Component.literal("Tag Selection"));
        this.parentScreen = parentScreen;
        this.selectedTags = new ArrayList<>(initialSelectedTags);
        this.onTagsChanged = onTagsChanged;
        this.font = Minecraft.getInstance().font;
    }

    // 创建子组件的方法，由外部调用
    public List<AbstractWidget> createChildComponents() {
        List<AbstractWidget> components = new ArrayList<>();

        // 搜索框
        searchInput = new EditBox(font,
                                 getX() + 5, getY() + 5, getWidth() - 10, 20,
                                 Component.translatable("todo_list.search_tags"));
        searchInput.setHint(Component.translatable("todo_list.search_tags"));
        components.add(searchInput);

        // 新标签输入框
        newTagInput = new EditBox(font,
                                 getX() + 5, getY() + 30, getWidth() - 35, 20,
                                 Component.translatable("todo_list.new_tag"));
        newTagInput.setHint(Component.translatable("todo_list.new_tag"));
        components.add(newTagInput);

        // 添加标签按钮
        addTagButton = Button.builder(Component.literal("+"), button -> {
            String tagName = newTagInput.getValue().trim();
            if (!tagName.isEmpty()) {
                Tag newTag = TagManager.getInstance().createTag(tagName);
                if (!selectedTags.contains(newTag)) {
                    selectedTags.add(newTag);
                    onTagsChanged.accept(selectedTags);
                }
                newTagInput.setValue("");
                // 通知需要刷新标签列表
                refreshTagList();
            }
        }).pos(getX() + getWidth() - 25, getY() + 30)
          .size(20, 20)
          .build();
        components.add(addTagButton);

        return components;
    }

    public List<Checkbox> createTagCheckboxes() {
        List<Checkbox> components = new ArrayList<>();
        String searchText = searchInput != null ? searchInput.getValue() : "";
        List<Tag> allTags = TagManager.getInstance().searchTags(searchText);

        int yPos = getY() + 60;
        int displayedCount = 0;

        for (int i = scrollOff; i < allTags.size() && displayedCount < maxVisibleTags; i++) {
            Tag tag = allTags.get(i);
            boolean isSelected = selectedTags.contains(tag);

            // 标签复选框
            Checkbox tagCheckbox = Checkbox.builder(Component.literal(tag.getName()), font)
                    .pos(getX() + 5, yPos)
                    .selected(isSelected)
                    .onValueChange((checkbox, selected) -> {
                        if (selected) {
                            if (!selectedTags.contains(tag)) {
                                selectedTags.add(tag);
                            }
                        } else {
                            selectedTags.remove(tag);
                        }
                        onTagsChanged.accept(selectedTags);
                    })
                    .build();

            tagCheckboxes.add(tagCheckbox);
            components.add(tagCheckbox);
            yPos += 22;
            displayedCount++;
        }
        return components;
    }

    // 刷新标签列表 - 不再依赖外部界面重建
    public void refreshTagList() {
        LOGGER.info("Refreshing tag list");
        if (isInitializing) {
            return; // 初始化期间跳过刷新
        }

        // 简单地清空并重新创建内部列表
        // 实际的渲染会在下一帧自动处理
        tagCheckboxes.clear();

        // 重置滚动位置以确保在搜索后不会超出范围
        List<Tag> filteredTags = getFilteredTags();
        if (filteredTags.size() <= maxVisibleTags) {
            scrollOff = 0;
        } else {
            int maxScroll = filteredTags.size() - maxVisibleTags;
            scrollOff = Math.min(scrollOff, maxScroll);
        }
    }

    // 获取当前过滤后的标签列表
    private List<Tag> getFilteredTags() {
        String searchText = searchInput != null ? searchInput.getValue() : "";
        return TagManager.getInstance().searchTags(searchText);
    }

    // 处理按键输入
    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (newTagInput != null && newTagInput.isFocused()) {
            if (keyCode == 257) { // Enter键
                String tagName = newTagInput.getValue().trim();
                if (!tagName.isEmpty()) {
                    Tag newTag = TagManager.getInstance().createTag(tagName);
                    if (!selectedTags.contains(newTag)) {
                        selectedTags.add(newTag);
                        onTagsChanged.accept(selectedTags);
                    }
                    newTagInput.setValue("");
                    refreshTagList();
                    return true;
                }
            }
            return newTagInput.keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制标题
        guiGraphics.drawString(font,
                             Component.translatable("todo_list.tags"),
                             getX() + 5, getY() - 15, 0xFFFFFF);

        // 渲染滚动条
        List<Tag> filteredTags = getFilteredTags();
        if (filteredTags.size() > maxVisibleTags) {
            renderScrollbar(guiGraphics);
        }

        List<Checkbox> tagComponents = createTagCheckboxes();
        for (Checkbox component : tagComponents) {
            component.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 渲染已选择标签数量
        int selectedCount = selectedTags.size();
        if (selectedCount > 0) {
            Component selectedText = Component.translatable("todo_list.selected_tags", selectedCount);
            guiGraphics.drawString(font, selectedText,
                                 getX() + 5, getY() + getHeight() - 15, 0xAAAAAAA);
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int scrollbarX = getX() + getWidth() - 8;
        int scrollbarY = getY() + 60;
        int scrollbarHeight = maxVisibleTags * 22;

        // 滚动条背景
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0x40FFFFFF);

        // 滚动条滑块 - 参考村民界面的实现
        List<Tag> filteredTags = getFilteredTags();
        int totalTags = filteredTags.size();
        if (totalTags > maxVisibleTags) {
            // 计算滑块高度和位置，参考村民界面
            int maxScroll = totalTags - maxVisibleTags;
            int scrollAreaHeight = scrollbarHeight - 27; // 减去滑块高度
            int thumbHeight = 27; // 固定滑块高度

            // 计算滑块位置
            int thumbY = scrollbarY;
            if (maxScroll > 0) {
                thumbY = scrollbarY + (scrollAreaHeight * scrollOff / maxScroll);
            }

            // 确保滑块不超出边界
            thumbY = Math.min(thumbY, scrollbarY + scrollbarHeight - thumbHeight);

            guiGraphics.fill(scrollbarX + 1, thumbY, scrollbarX + 5, thumbY + thumbHeight, 0x80FFFFFF);
        }
    }

    // 检查是否可以滚动
    private boolean canScroll() {
        List<Tag> filteredTags = getFilteredTags();
        return filteredTags.size() > maxVisibleTags;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<Tag> filteredTags = getFilteredTags();
        if (canScroll()) {
            int maxScroll = filteredTags.size() - maxVisibleTags;
            int oldScrollOff = scrollOff;
            scrollOff = Mth.clamp((int)(scrollOff - scrollY), 0, maxScroll);

            // 只有当滚动位置真的改变时才刷新
            if (oldScrollOff != scrollOff) {
                refreshTagList();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            List<Tag> filteredTags = getFilteredTags();
            int scrollbarY = getY() + 60;
            int scrollbarHeight = maxVisibleTags * 22;
            int maxScroll = filteredTags.size() - maxVisibleTags;

            // 计算滚动位置，参考村民界面的实现
            float scrollRatio = ((float)mouseY - scrollbarY - 13.5F) / (scrollbarHeight - 27.0F);
            scrollRatio = scrollRatio * maxScroll + 0.5F;
            int newScrollOff = Mth.clamp((int)scrollRatio, 0, maxScroll);

            // 只有当滚动位置真的改变时才刷新
            if (newScrollOff != scrollOff) {
                scrollOff = newScrollOff;
                refreshTagList();
            }
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LOGGER.info("TagSelectionPanel mouseClicked: x=" + mouseX + ", y=" + mouseY);

        // 重置拖拽状态
        isDragging = false;

        // 检查是否点击在滚动条上
        if (canScroll()) {
            int scrollbarX = getX() + getWidth() - 8;
            int scrollbarY = getY() + 60;
            int scrollbarHeight = maxVisibleTags * 22;

            if (mouseX > scrollbarX && mouseX < scrollbarX + 6 &&
                mouseY > scrollbarY && mouseY <= scrollbarY + scrollbarHeight + 1) {
                isDragging = true;
                return true;
            }
        }

        // 检查标签点击（直接渲染的标签）
        List<Tag> filteredTags = getFilteredTags();
        int yPos = getY() + 60;
        int displayedCount = 0;

        for (int i = scrollOff; i < filteredTags.size() && displayedCount < maxVisibleTags; i++) {
            if (mouseX >= getX() + 5 && mouseX <= getX() + getWidth() - 15
                && mouseY >= yPos && mouseY <= yPos + 20) {
                // 点击了这个标签
                Tag tag = filteredTags.get(i);
                if (selectedTags.contains(tag)) {
                    selectedTags.remove(tag);
                } else {
                    selectedTags.add(tag);
                }
                onTagsChanged.accept(selectedTags);
                return true;
            }
            yPos += 22;
            displayedCount++;
        }

        // 将鼠标点击事件传递给子组件
        if (searchInput != null) {
            if (searchInput.mouseClicked(mouseX, mouseY, button)) {
                parentScreen.setFocused(searchInput);
                return true;
            }
        }

        if (newTagInput != null) {
            if (newTagInput.mouseClicked(mouseX, mouseY, button)) {
                parentScreen.setFocused(newTagInput);
                return true;
            }
        }

        if (addTagButton != null && addTagButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public List<Tag> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }

    public void setSelectedTags(List<Tag> tags) {
        selectedTags.clear();
        selectedTags.addAll(tags);
        if (!isInitializing) {
            refreshTagList();
        }
    }

    // 设置初始化状态的方法
    public void setInitializing(boolean initializing) {
        this.isInitializing = initializing;
    }

    // 获取当前的子组件引用
    public EditBox getSearchInput() { return searchInput; }
    public EditBox getNewTagInput() { return newTagInput; }
    public Button getAddTagButton() { return addTagButton; }
    public List<Checkbox> getTagCheckboxes() { return new ArrayList<>(tagCheckboxes); }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        // 留空即可，这是必须实现的抽象方法
    }
}
