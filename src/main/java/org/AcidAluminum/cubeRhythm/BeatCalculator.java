package org.AcidAluminum.cubeRhythm;

import lombok.Getter;

import java.text.DecimalFormat;

@Getter
public class BeatCalculator {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    public static final double DEFAULT_BPM = 120.0;
    private static final double MICROSECONDS_PER_SECOND = 1_000_000.0;

    private double bpm;
    private int beatsPerMeasure;

    public BeatCalculator() {
        this.bpm = DEFAULT_BPM;
        this.beatsPerMeasure = 4; // Default to 4 beats per measure
    }

    public void setBpm(double bpm) {
        if (bpm > 0) {
            this.bpm = bpm;
        } else {
            // Optionally log an error or use default BPM
            this.bpm = DEFAULT_BPM;
        }
    }

    public void setBeatsPerMeasure(int beatsPerMeasure) {
        if (beatsPerMeasure > 0) {
            this.beatsPerMeasure = beatsPerMeasure;
        }
    }

    /**
     * 计算每节的时间长度 (秒)
     * @return 每节的时间长度
     */
    public double getSecondsPerMeasure() {
        return 60.0 / bpm;
    }

    /**
     * 计算每拍的时间长度 (秒)
     * @return 每拍的时间长度
     */
    public double getSecondsPerBeat() {
        return getSecondsPerMeasure() / beatsPerMeasure;
    }

    /**
     * 将微秒转换为拍数
     * @param microseconds 音乐当前时间 (微秒)
     * @return 对应的拍数
     */
    public double microsecondsToBeats(long microseconds) {
        double seconds = microseconds / MICROSECONDS_PER_SECOND;
        return seconds / getSecondsPerBeat();
    }

    /**
     * 将拍数转换为微秒
     * @param beats 拍数
     * @return 对应的微秒数
     */
    public long beatsToMicroseconds(double beats) {
        double seconds = beats * getSecondsPerBeat();
        return Math.round(seconds * MICROSECONDS_PER_SECOND);
    }

    /**
     * 将微秒转换为小节数
     * @param microseconds 音乐当前时间 (微秒)
     * @return 对应的小节数
     */
    public double microsecondsToMeasures(long microseconds) {
        double seconds = microseconds / MICROSECONDS_PER_SECOND;
        return seconds / getSecondsPerMeasure();
    }

    /**
     * 将小节数转换为微秒
     * @param measures 小节数
     * @return 对应的微秒数
     */
    public long measuresToMicroseconds(double measures) {
        double seconds = measures * getSecondsPerMeasure();
        return Math.round(seconds * MICROSECONDS_PER_SECOND);
    }

    /**
     * 格式化时间为分:秒.毫秒格式
     * @param microseconds 微秒数
     * @return 格式化后的时间字符串
     */
    public String formatTime(long microseconds) {
        double totalSeconds = microseconds / MICROSECONDS_PER_SECOND;
        int minutes = (int) (Math.abs(totalSeconds) / 60);
        double seconds = Math.abs(totalSeconds) % 60;
        String sign = totalSeconds < 0 ? "-" : "";
        return String.format("%s%02d:%06.3f", sign, minutes, seconds);
    }

    /**
     * 将时间格式化为秒.毫秒格式
     * @param microseconds 微秒数
     * @return 格式化后的时间字符串
     */
    public String formatTimeToSecondsWithDecimal(long microseconds) {
        double seconds = microseconds / MICROSECONDS_PER_SECOND;
        String sign = seconds < 0 ? "-" : "";
        return sign + DECIMAL_FORMAT.format(Math.abs(seconds));
    }
} 