package org.AcidAluminum.cubeRhythm;

import lombok.Getter;

import java.awt.Color;

@Getter
public enum NoteType {
    TAP("Tap", "tap", new Color(0, 120, 215)),
    DRAG("Drag", "drag", new Color(255, 215, 0)),
    FLICK_LEFT("Flick←", "flick", new Color(255, 0, 153)),
    FLICK_RIGHT("Flick→", "flick", new Color(255, 0, 153)),
    DOUBLE("Double", "double", new Color(255, 140, 0)),
    EXECUTION("Execution", "execution", Color.GRAY),
    HOLD("Hold", "hold", new Color(0, 200, 0)),
    FAKE_TAP("FakeTap", "fake_tap", new Color(0, 120, 215)),
    FAKE_HOLD("FakeHold", "fake_hold", new Color(0, 200, 0)),
    FAKE_DRAG("FakeDrag", "fake_drag", new Color(255, 215, 0)),
    FAKE_FLICK("FakeFlick", "fake_flick", new Color(255, 0, 153)),
    FAKE_DOUBLE("FakeDouble", "fake_double", new Color(255, 140, 0)),
    MINE_TAP("MineTap", "mine_tap", new Color(0, 120, 215)),
    MINE_DRAG("MineDrag", "mine_drag", new Color(255, 215, 0)),
    MINE_DOUBLE("MineDouble", "mine_double", new Color(255, 140, 0));

    private final String displayName;
    private final String jsonName;
    private final Color color;

    NoteType(String displayName, String jsonName, Color color) {
        this.displayName = displayName;
        this.jsonName = jsonName;
        this.color = color;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 