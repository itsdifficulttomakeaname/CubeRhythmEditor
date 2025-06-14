package org.project1;

import java.awt.Color;

/**
 * NOTE类型枚举
 */
public enum NoteType {
    TAP("Tap", new Color(0, 120, 215)), // 蓝色
    DRAG("Drag", new Color(255, 215, 0)), // 黄色
    FLICK_LEFT("Flick←", Color.RED), // 红色
    FLICK_RIGHT("Flick→", new Color(128, 0, 128)), // 紫色
    DOUBLE("Double", new Color(255, 140, 0)), // 橙色
    EXECUTION("Execution", Color.GRAY); // 灰色
    
    private final String displayName;
    private final Color color;
    
    NoteType(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Color getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 