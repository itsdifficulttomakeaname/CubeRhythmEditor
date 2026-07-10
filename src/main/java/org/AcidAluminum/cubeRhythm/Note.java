package org.AcidAluminum.cubeRhythm;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Note {
    private double x;
    private double y;
    private double x2;
    private double y2;
    private NoteType type;
    private long timeMicroseconds;
    private String direction;
    private boolean isGlowing;
    private String flickDirection;
    private List<String> tags = new ArrayList<>();
    private com.google.gson.JsonArray actions;
    private com.google.gson.JsonObject events;

    public Note(double x, double y, NoteType type, long timeMicroseconds, String direction, boolean isGlowing) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
        this.direction = direction;
        this.isGlowing = isGlowing;
    }

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

    public Note(NoteType type, long timeMicroseconds, String direction, String flickDirection, boolean isGlowing) {
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
        this.direction = direction;
        this.flickDirection = flickDirection;
        this.isGlowing = isGlowing;
    }

    public Note(NoteType type, long timeMicroseconds) {
        this.type = type;
        this.timeMicroseconds = timeMicroseconds;
    }

    public void setTag(String tag) {
        this.tags.clear();
        if (tag != null && !tag.isEmpty()) {
            for (String t : tag.split(",")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    this.tags.add(trimmed);
                }
            }
        }
    }

    public String getTag() {
        return tags.isEmpty() ? "" : String.join(", ", tags);
    }

    public boolean hasCoordinates() {
        return type != NoteType.EXECUTION && type != NoteType.FLICK_LEFT &&
               type != NoteType.FLICK_RIGHT && type != NoteType.FAKE_FLICK;
    }

    public boolean isDouble() {
        return type == NoteType.DOUBLE || type == NoteType.FAKE_DOUBLE ||
               type == NoteType.MINE_DOUBLE;
    }

    public boolean isExecution() {
        return type == NoteType.EXECUTION;
    }

    public boolean isFlick() {
        return type == NoteType.FLICK_LEFT || type == NoteType.FLICK_RIGHT || type == NoteType.FAKE_FLICK;
    }

    public boolean isFake() {
        return type == NoteType.FAKE_TAP || type == NoteType.FAKE_HOLD ||
               type == NoteType.FAKE_DRAG || type == NoteType.FAKE_FLICK ||
               type == NoteType.FAKE_DOUBLE;
    }

    public boolean isMine() {
        return type == NoteType.MINE_TAP || type == NoteType.MINE_DRAG ||
               type == NoteType.MINE_DOUBLE;
    }

    public Color getColor() {
        return switch (type) {
            case TAP, FAKE_TAP, MINE_TAP -> new Color(0, 120, 215);
            case DRAG, FAKE_DRAG, MINE_DRAG -> new Color(255, 215, 0);
            case DOUBLE, FAKE_DOUBLE, MINE_DOUBLE -> new Color(255, 140, 0);
            case EXECUTION -> new Color(128, 128, 128);
            case FLICK_LEFT, FLICK_RIGHT, FAKE_FLICK -> new Color(255, 0, 153);
            case HOLD, FAKE_HOLD -> new Color(0, 200, 0);
        };
    }
}
