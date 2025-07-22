package com.euphony.todo_list.client.components;

import com.euphony.todo_list.todo.Tag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TagDisplayWidget extends AbstractWidget {
    private final List<Tag> tags;

    // 移除 maxDisplayTags 参数，改为动态计算
    public TagDisplayWidget(int x, int y, int width, int height, List<Tag> tags) {
        super(x, y, width, height, Component.empty());
        this.tags = tags;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int currentX = getX();
        int currentY = getY();
        int maxWidth = getWidth();
        int tagHeight = 12;
        int tagSpacing = 2;
        int displayCount = 0;

        // 预留空间给"更多"指示器
        String moreIndicatorSample = "+99"; // 假设最多99个未显示的标签
        int moreIndicatorWidth = font.width(moreIndicatorSample) + 6;

        for (Tag tag : tags) {
            String tagName = tag.getName();
            int tagWidth = font.width(tagName) + 8; // 4像素左右边距

            // 检查是否还有足够空间显示这个标签
            // 如果不是最后一个标签，需要预留"更多"指示器的空间
            int remainingTags = tags.size() - displayCount - 1;
            int requiredSpace = tagWidth;

            if (remainingTags > 0) {
                // 还有更多标签，需要预留"更多"指示器的空间
                requiredSpace += tagSpacing + moreIndicatorWidth;
            }

            if (currentX + requiredSpace > getX() + maxWidth) {
                // 空间不够，显示"更多"指示器
                if (displayCount < tags.size()) {
                    String moreText = "+" + (tags.size() - displayCount);
                    int actualMoreWidth = font.width(moreText) + 6;

                    if (currentX + actualMoreWidth <= getX() + maxWidth) {
                        // 绘制更多标签指示器
                        guiGraphics.fill(currentX, currentY, currentX + actualMoreWidth, currentY + tagHeight, 0x80888888);
                        guiGraphics.drawString(font, moreText, currentX + 3, currentY + 2, 0xFFFFFF);
                    }
                }
                break;
            }

            // 绘制标签背景
            int tagColor = tag.getColor() | 0x80000000; // 添加透明度
            guiGraphics.fill(currentX, currentY, currentX + tagWidth, currentY + tagHeight, tagColor);

            // 绘制标签边框
            guiGraphics.fill(currentX, currentY, currentX + tagWidth, currentY + 1, 0xFF000000);
            guiGraphics.fill(currentX, currentY + tagHeight - 1, currentX + tagWidth, currentY + tagHeight, 0xFF000000);
            guiGraphics.fill(currentX, currentY, currentX + 1, currentY + tagHeight, 0xFF000000);
            guiGraphics.fill(currentX + tagWidth - 1, currentY, currentX + tagWidth, currentY + tagHeight, 0xFF000000);

            // 绘制标签文本
            guiGraphics.drawString(font, tagName, currentX + 4, currentY + 2, 0xFFFFFF);

            currentX += tagWidth + tagSpacing;
            displayCount++;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (tags != null && !tags.isEmpty()) {
            narrationElementOutput.add(NarratedElementType.TITLE,
                Component.translatable("todo_list.tags_count", tags.size()));
        }
    }
}
