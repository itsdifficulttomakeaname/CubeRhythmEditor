package org.project1;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicPlayer {
    private static final Logger LOGGER = Logger.getLogger(MusicPlayer.class.getName());
    
    private Clip clip;
    private AudioInputStream audioInputStream;
    private long microsecondLength;
    private String songName;
    private String songAuthor;
    private float frameRate;
    
    public MusicPlayer() {
        // 初始化播放器
    }

    // This gets the total time of the *actual audio clip*
    public long getActualClipLengthMicroseconds() {
        return microsecondLength;
    }

    public void loadSong(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // 清理现有资源
        close();
        
        File audioFile = new File(filePath);
        if (!filePath.toLowerCase().endsWith(".ogg")) {
            throw new UnsupportedAudioFileException("仅支持OGG格式音频文件");
        }
        
        audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioInputStream.getFormat();
        
        // 计算音频时长
        long frameLength = audioInputStream.getFrameLength();
        microsecondLength = (long)(frameLength * 1_000_000 / format.getFrameRate());
        
        // 设置解码格式
        AudioFormat decodedFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            16,
            format.getChannels(),
            format.getChannels() * 2,
            format.getSampleRate(),
            false
        );
        
        AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);
        
        // 设置大缓冲区（1MB）
        int bufferSize = 1024 * 1024; // 1MB
        decodedStream = new AudioInputStream(decodedStream, decodedFormat, decodedStream.getFrameLength());
        DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
        clip = (Clip) AudioSystem.getLine(info);
        clip.open(decodedStream);
        
        // 修正：用clip的实际时长
        microsecondLength = clip.getMicrosecondLength();
        
        // 设置歌曲信息
        this.songName = audioFile.getName().replace(".ogg", "");
        this.songAuthor = "未知作者";
        this.frameRate = format.getFrameRate();
    }

    public void play() {
        if (clip != null && !clip.isRunning()) {
            clip.start();
        }
    }
    
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
        }
    }

    public void seek(long microsecondPosition) {
        if (clip != null) {
            microsecondPosition = Math.max(0, Math.min(microsecondPosition, microsecondLength));
            boolean wasPlaying = clip.isRunning();
            clip.setMicrosecondPosition(microsecondPosition);
            if (wasPlaying) {
                clip.start();
            }
        }
    }

    public long getCurrentTimeMicroseconds() {
        if (clip == null) return 0;
        return clip.getMicrosecondPosition();
    }

    public float getFrameRate() {
        return frameRate;
    }

    public String getSongName() {
        return songName;
    }

    public String getAuthor() {
        return songAuthor;
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
    
    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    
    public boolean isPaused() {
        return clip != null && !clip.isRunning() && clip.getMicrosecondPosition() > 0;
    }

    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing audio input stream", e);
            }
        }
    }
}