package org.AcidAluminum.cubeRhythm;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;

/**
 * NOTE类，存储NOTE的所有信息
 * 说明：NOTE的生成、伪3D动画、属性设置等核心逻辑已在主窗口MainWindow中实现。
 * 本类仅作为数据结构，负责存储和提供NOTE属性。
 */
@Setter
@Getter
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
        return switch (type) {
            case TAP -> new Color(0, 153, 255);
            case DRAG -> new Color(255, 215, 0);
            case DOUBLE -> new Color(255, 0, 153);
            case EXECUTION -> new Color(128, 128, 128);
            case FLICK_LEFT -> new Color(102, 0, 204);
            case FLICK_RIGHT -> new Color(204, 0, 0);
            default -> Color.GRAY;
        };
    }
} 