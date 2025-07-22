package com.euphony.todo_list.todo;

import java.util.Objects;
import java.util.UUID;

public class Tag {
    private final UUID id;
    private String name;
    private int color; // RGB颜色值

    public Tag(String name, int color) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
    }

    public Tag(UUID id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
