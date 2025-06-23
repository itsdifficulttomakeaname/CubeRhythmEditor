package org.project1;

import java.awt.Color;

/**
 * NOTE类，存储NOTE的所有信息
 * 说明：NOTE的生成、伪3D动画、属性设置等核心逻辑已在主窗口MainWindow中实现。
 * 本类仅作为数据结构，负责存储和提供NOTE属性。
 */
public class Note {
    private double x; // 目标X坐标（网格坐标）
    private double y; // 目标Y坐标（网格坐标）
    private double x2; // Double音符的第二个X坐标
    private double y2; // Double音符的第二个Y坐标
    private NoteType type; // NOTE类型
    private long timeMicroseconds; // 时间（微秒）
    private String direction; // 朝向
    private boolean isGlowing; // 是否发光
    private String flickDirection; // Flick方向（仅用于Flick类型）

    // 构造函数（单点）
    public Note(double x, double y, NoteType type, long timeMicroseconds, String direction, boolean isGlowing) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
        this.direction = direction;
        this.isGlowing = isGlowing;
    }

    // Double音符构造函数
    public Note(double x1, double y1, double x2, double y2, NoteType type, long timeMicroseconds, String direction, boolean isGlowing) {
        this.x = x1;
        this.y = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
        this.direction = direction;
        this.isGlowing = isGlowing;
    }

    // Flick音符构造函数
    public Note(NoteType type, long timeMicroseconds, String direction, String flickDirection, boolean isGlowing) {
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
        this.direction = direction;
        this.flickDirection = flickDirection;
        this.isGlowing = isGlowing;
    }

    // Execution音符构造函数
    public Note(NoteType type, long timeMicroseconds) {
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
    }

    // Getter和Setter方法
    public double getX() { return x; }
    public double getY() { return y; }
    public double getX2() { return x2; }
    public double getY2() { return y2; }
    public NoteType getType() { return type; }
    public long getTimeMicroseconds() { return timeMicroseconds; }
    public String getDirection() { return direction; }
    public boolean isGlowing() { return isGlowing; }
    public String getFlickDirection() { return flickDirection; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setX2(double x2) { this.x2 = x2; }
    public void setY2(double y2) { this.y2 = y2; }
    public void setType(NoteType type) { this.type = type; }
    public void setTimeMicroseconds(long t) { this.timeMicroseconds = t; }
    public void setDirection(String d) { this.direction = d; }
    public void setGlowing(boolean g) { this.isGlowing = g; }
    public void setFlickDirection(String f) { this.flickDirection = f; }

    // 是否有坐标（非Execution/Flick）
    public boolean hasCoordinates() {
        return type != NoteType.EXECUTION && type != NoteType.FLICK_LEFT && type != NoteType.FLICK_RIGHT;
    }
    // 是否为Double音符
    public boolean isDouble() {
        return type == NoteType.DOUBLE;
    }
    // 是否为Execution音符
    public boolean isExecution() {
        return type == NoteType.EXECUTION;
    }
    // 是否为Flick音符
    public boolean isFlick() {
        return type == NoteType.FLICK_LEFT || type == NoteType.FLICK_RIGHT;
    }
    // 获取NOTE颜色（可根据类型自定义）
    public Color getColor() {
        switch (type) {
            case TAP: return new Color(0, 153, 255);
            case DRAG: return new Color(255, 215, 0);
            case DOUBLE: return new Color(255, 0, 153);
            case EXECUTION: return new Color(128, 128, 128);
            case FLICK_LEFT: return new Color(102, 0, 204);
            case FLICK_RIGHT: return new Color(204, 0, 0);
            default: return Color.GRAY;
        }
    }
} 