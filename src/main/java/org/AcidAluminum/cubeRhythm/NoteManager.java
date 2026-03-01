package org.AcidAluminum.cubeRhythm;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * NOTE管理器，负责管理所有NOTE的增删查
 */
public class NoteManager {
    // 用TreeMap按时间分组存储NOTE，key为timeMicroseconds
    private final TreeMap<Long, List<Note>> notesByTime;

    public NoteManager() {
        notesByTime = new TreeMap<>();
    }

    /** 添加NOTE */
    public void addNote(Note note) {
        notesByTime.computeIfAbsent(note.getTimeMicroseconds(), k -> new ArrayList<>()).add(note);
    }

    /** 移除NOTE */
    public void removeNote(Note note) {
        List<Note> list = notesByTime.get(note.getTimeMicroseconds());
        if (list != null) {
            list.remove(note);
            if (list.isEmpty()) notesByTime.remove(note.getTimeMicroseconds());
        }
    }

    /** 清空所有NOTE */
    public void clearNotes() {
        notesByTime.clear();
    }

    /** 获取所有NOTE */
    public List<Note> getNotes() {
        List<Note> all = new ArrayList<>();
        for (List<Note> group : notesByTime.values()) all.addAll(group);
        return all;
    }

    /** 判断是否有NOTE */
    public boolean hasNotes() {
        return !notesByTime.isEmpty();
    }

    /**
     * 获取当前拍范围内需要显示的NOTE列表
     * @param currentTime 当前时间（微秒）
     * @param beatsToShow 显示拍数
     * @param beatCalculator 拍数计算器
     * @return 需要显示的NOTE列表
     */
    public List<Note> getNotesForCurrentBeat(long currentTime, int beatsToShow, BeatCalculator beatCalculator) {
        List<Note> result = new ArrayList<>();
        double currentBeat = beatCalculator.microsecondsToBeats(currentTime);
        double minBeat = currentBeat;
        double maxBeat = currentBeat + (beatsToShow - 1);

        // 计算对应的时间区间
        long minTime = beatCalculator.beatsToMicroseconds(minBeat);
        long maxTime = beatCalculator.beatsToMicroseconds(maxBeat);

        // 添加时间容差（0.1拍的时间，约等于50-100ms，取决于BPM）
        // 这样可以容忍音符时间和拍数之间的精度误差
        long tolerance = beatCalculator.beatsToMicroseconds(0.1);
        minTime = Math.max(0, minTime - tolerance);
        maxTime = maxTime + tolerance;

        // TreeMap子区间查找
        for (List<Note> group : notesByTime.subMap(minTime, true, maxTime, true).values()) {
            result.addAll(group);
        }
        return result;
    }
} 