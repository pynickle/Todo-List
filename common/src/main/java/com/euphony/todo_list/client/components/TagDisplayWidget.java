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
    private final boolean leftAlign;

    public TagDisplayWidget(int x, int y, int width, int height, List<Tag> tags) {
        this(x, y, width, height, tags, false); // 默认居中对齐
    }

    public TagDisplayWidget(int x, int y, int width, int height, List<Tag> tags, boolean leftAlign) {
        super(x, y, width, height, Component.empty());
        this.tags = tags;
        this.leftAlign = leftAlign;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int maxWidth = getWidth();
        int tagHeight = 12;
        int tagSpacing = 2;

        // 预计算所有可以显示的标签
        String moreIndicatorSample = "+99";
        int moreIndicatorWidth = font.width(moreIndicatorSample) + 6;

        int totalWidth = 0;
        int displayCount = 0;

        // 计算实际需要的总宽度
        for (Tag tag : tags) {
            String tagName = tag.getName();
            int tagWidth = font.width(tagName) + 8;

            int requiredSpace = tagWidth;
            if (displayCount > 0) {
                requiredSpace += tagSpacing;
            }

            // 检查是否还有剩余标签，如果有需要预留"更多"指示器空间
            int remainingTags = tags.size() - displayCount - 1;
            if (remainingTags > 0) {
                requiredSpace += tagSpacing + moreIndicatorWidth;
            }

            if (totalWidth + requiredSpace > maxWidth) {
                break;
            }

            totalWidth += (displayCount > 0 ? tagSpacing : 0) + tagWidth;
            displayCount++;
        }

        // 如果有未显示的标签，添加"更多"指示器的宽度
        boolean hasMoreTags = displayCount < tags.size();
        if (hasMoreTags) {
            String moreText = "+" + (tags.size() - displayCount);
            int actualMoreWidth = font.width(moreText) + 6;
            totalWidth += tagSpacing + actualMoreWidth;
        }

        // 根据对齐方式计算起始X坐标
        int startX;
        if (leftAlign) {
            startX = getX();
        } else {
            // 居中对齐
            startX = getX() + (maxWidth - totalWidth) / 2;
        }

        // 绘制标签
        int currentX = startX;
        int currentY = getY();

        for (int i = 0; i < displayCount; i++) {
            Tag tag = tags.get(i);
            String tagName = tag.getName();
            int tagWidth = font.width(tagName) + 8;

            // 绘制标签背景
            int tagColor = tag.getColor() | 0x80000000;
            guiGraphics.fill(currentX, currentY, currentX + tagWidth, currentY + tagHeight, tagColor);

            // 绘制标签边框
            guiGraphics.fill(currentX, currentY, currentX + tagWidth, currentY + 1, 0xFF000000);
            guiGraphics.fill(currentX, currentY + tagHeight - 1, currentX + tagWidth, currentY + tagHeight, 0xFF000000);
            guiGraphics.fill(currentX, currentY, currentX + 1, currentY + tagHeight, 0xFF000000);
            guiGraphics.fill(currentX + tagWidth - 1, currentY, currentX + tagWidth, currentY + tagHeight, 0xFF000000);

            // 绘制标签文本
            guiGraphics.drawString(font, tagName, currentX + 4, currentY + 2, 0xFFFFFF);

            currentX += tagWidth + tagSpacing;
        }

        // 绘制"更多"指示器
        if (hasMoreTags) {
            String moreText = "+" + (tags.size() - displayCount);
            int actualMoreWidth = font.width(moreText) + 6;

            // 绘制更多标签指示器
            guiGraphics.fill(currentX, currentY, currentX + actualMoreWidth, currentY + tagHeight, 0x80888888);
            guiGraphics.drawString(font, moreText, currentX + 3, currentY + 2, 0xFFFFFF);
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
