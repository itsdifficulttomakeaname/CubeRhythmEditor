# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CubeRhythmEditor is a rhythm game chart editor written in Java 17 using Swing. It allows users to create and edit rhythm game charts with various note types on a 7x7 grid system, synchronized to OGG audio files. Charts are exported as JSON for use in the CubeRhythm Minecraft plugin.

## Build and Run Commands

```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java

# Package as fat JAR (includes all dependencies)
mvn clean package

# Run the packaged JAR
java -jar target/CubeRhythmEditor-1.0.0-SNAPSHOT.jar
```

## Core Architecture

### Package Structure

`org.AcidAluminum.cubeRhythm`

```
├── CubeRhythmEditor.java      # Entry point
├── MainWindow.java             # Central GUI (~3049 lines)
├── NoteManager.java            # Note storage (TreeMap-based)
├── Note.java                   # Note data class
├── NoteType.java               # Note type enum
├── BeatCalculator.java         # BPM/time calculations
├── MusicPlayer.java            # OGG audio playback
├── LogManager.java             # Logging utility
├── Constants.java              # Grid constants
└── ui/
    └── NoteTypePanel.java      # Note type radio button panel
```

### Main Components

- **CubeRhythmEditor**: Entry point that launches MainWindow
- **MainWindow**: Central GUI class containing all UI logic, event handlers, and rendering:
  - Grid-based and no-grid (free-form) input modes
  - Note placement and pseudo-3D visualization
  - Audio playback controls with beat synchronization
  - Song metadata management (collapsible panels)
  - Real-time JSON chart editing
  - 50ms UI refresh timer for music sync
- **NoteManager**: Manages note storage using `TreeMap<Long, List<Note>>` for efficient time-based range queries
- **Note**: Data class with multiple constructors for different note types
- **NoteType**: Enum with 7 types: TAP, DRAG, FLICK_LEFT, FLICK_RIGHT, DOUBLE, EXECUTION, HOLD
- **BeatCalculator**: BPM calculations and conversions between microseconds, beats, and measures
- **MusicPlayer**: Audio playback using Java Sound API (OGG format only via j-ogg-all, decoded to PCM_SIGNED 16-bit)
- **LogManager**: Appends timestamped `[HH:mm:ss]` messages to a JTextArea
- **NoteTypePanel**: Radio button panel with color-coded note type selection and contrast text

### Coordinate System

- 7x7 grid with origin at cell (3, 3)
- Coordinate range: -3.5 to 3.5 in both X and Y
- Grid cell size: 100 pixels
- Total grid panel size: 700 pixels (7 × 100)
- Grid mode snaps to 0.5 unit increments
- No-grid mode allows arbitrary precision (rounded to 2 decimals)

### Time Representation

All internal time values are stored in **microseconds** (long). The JSON chart format uses **seconds** (double). Conversion: `timeMicroseconds = (long)(timeSeconds * 1_000_000)`.

BeatCalculator provides:
- `microsecondsToBeats(long)` / `beatsToMicroseconds(double)`
- `microsecondsToMeasures(long)` / `measuresToMicroseconds(double)`
- `formatTime(long)` → `MM:SS.mmm`

### Note Types and Properties

| Type | Coordinates | Direction | Glow | Special |
|------|-------------|-----------|------|---------|
| TAP | (x, y) | W/A/S/D | Yes | — |
| DRAG | (x, y) | W/A/S/D | Yes | — |
| HOLD | (x, y) | W/A/S/D | Yes | — |
| DOUBLE | (x1,y1), (x2,y2) | W/A/S/D | Yes | Two-point placement |
| FLICK_LEFT | — | W/A/S/D | Yes | turn="left" |
| FLICK_RIGHT | — | W/A/S/D | Yes | turn="right" |
| EXECUTION | — | — | — | Actions array (title, actionbar, chat, potion, clear_effects) |

Direction border colors: W=White, A=Yellow, S=Orange, D=Red

### Chart JSON Format

```json
{
  "version": "1.0.0",
  "metadata": {
    "id": "chart_id",
    "title": "Song Title",
    "artist": "Composer",
    "charter": "Chart Author",
    "difficulty": { "name": "Easy|Normal|Hard", "level": 1-15, "color": "AQUA" },
    "audio": "cr.chart_id",
    "duration": 60,
    "offset": 0,
    "bpm": 120
  },
  "notes": [ ... ]
}
```

Note time values in JSON are in **seconds** (e.g., `"time": 5.0`).

### Rendering

- Pseudo-3D animation: notes scale from small (far) to large (near) based on beat distance
- Alpha blending: notes fade in/out based on proximity to current beat
- Display range controlled by `displayBeatsCount` (default 8 beats)
- 0.1 beat tolerance for note visibility queries
- Glow effect: green border stroke on glowing notes

## Dependencies

- **Lombok** (1.18.38): `@Getter`/`@Setter` annotations
- **j-ogg-all** (1.0.6): OGG audio file decoding
- **Gson** (2.10.1): JSON serialization/deserialization for chart export
- **Java Sound API**: Audio playback via Clip interface
- **maven-shade-plugin** (3.5.0): Fat JAR packaging

## Important Notes

- Audio files must be in OGG format (enforced in MusicPlayer.loadSong)
- JSON parsing in MainWindow uses regex-based extraction (not Gson) for note import
- The MainWindow class is very large (~3049 lines) — consider refactoring when making significant changes
- NoteManager uses TreeMap for efficient range queries when displaying notes for current beat
- UI font is set to "Microsoft YaHei" for Chinese character support
- The chart format uses Minecraft formatting codes (§) in execution action text
