# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CubeRhythmEditor is a rhythm game chart editor written in Java 17 using Swing. It allows users to create and edit rhythm game charts with various note types on a 7x7 grid system, synchronized to OGG audio files.

## Build and Run Commands

```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java

# Package as JAR
mvn clean package

# Run the packaged JAR
java -jar target/CubeRhythmEditor-1.0.0-SNAPSHOT.jar
```

## Core Architecture

### Main Components

- **CubeRhythmEditor**: Entry point that launches MainWindow
- **MainWindow**: Central GUI class containing all UI logic, event handlers, and rendering. This is a very large file (~27k tokens) that handles:
  - Grid-based and no-grid input modes
  - Note placement and visualization
  - Audio playback controls
  - Song metadata management
  - Real-time beat synchronization
- **NoteManager**: Manages note storage using TreeMap<Long, List<Note>> for efficient time-based queries
- **Note**: Data class representing individual notes with coordinates, type, timing, direction, and glow properties
- **NoteType**: Enum defining note types: TAP, DRAG, FLICK_LEFT, FLICK_RIGHT, DOUBLE, EXECUTION, HOLD
- **BeatCalculator**: Handles BPM calculations and conversions between microseconds, beats, and measures
- **MusicPlayer**: Audio playback using Java Sound API (OGG format only via j-ogg-all library)
- **Constants**: Grid dimensions and coordinate limits

### Coordinate System

- 7x7 grid with origin at (3, 3)
- Coordinate range: -3.5 to 3.5 in both X and Y
- Grid cell size: 100 pixels
- Total grid panel size: 700 pixels (7 × 100)

### Time Representation

All time values are stored in **microseconds** (not milliseconds). The BeatCalculator provides conversion methods:
- `microsecondsToBeats(long microseconds)`: Convert time to beat number
- `beatsToMicroseconds(double beats)`: Convert beat number to time
- `microsecondsToMeasures(long microseconds)`: Convert time to measure number
- `measuresToMicroseconds(double measures)`: Convert measure number to time

### Note Types and Properties

Each note type has specific properties:
- **TAP**: Single point with coordinates (x, y)
- **DRAG**: Single point with coordinates and direction (W/A/S/D)
- **DOUBLE**: Two points with coordinates (x1, y1, x2, y2)
- **FLICK_LEFT/FLICK_RIGHT**: Direction-based, no coordinates
- **EXECUTION**: Time-only marker, no coordinates
- **HOLD**: Single point with coordinates (similar to TAP but different behavior)

All notes except EXECUTION and FLICK types have:
- Direction property (W/A/S/D)
- Glow property (boolean)

## Dependencies

- **Lombok**: Used extensively for @Getter/@Setter annotations
- **j-ogg-all (1.0.6)**: OGG audio file decoding
- **Java Sound API**: Audio playback via Clip interface

## Important Notes

- Audio files must be in OGG format (enforced in MusicPlayer.loadSong)
- The MainWindow class is very large and contains most UI logic - consider refactoring when making significant changes
- NoteManager uses TreeMap for efficient range queries when displaying notes for current beat
- Package structure was recently refactored from org.project1 to org.AcidAluminum.cubeRhythm
