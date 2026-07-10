package org.AcidAluminum.cubeRhythm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import org.AcidAluminum.cubeRhythm.ui.NoteTypePanel;
import org.AcidAluminum.cubeRhythm.ui.JsonEditorPanel;
import java.util.List;

/**
 * 主窗口类，设置GUI界面
 */
public class MainWindow extends JFrame {
    
    // 网格尺寸
    private static final int GRID_SIZE = 7;
    // 格子尺寸
    private static final int CELL_SIZE = 100;
    // 总网格尺寸
    private static final int GRID_PANEL_SIZE = GRID_SIZE * CELL_SIZE;
    // 左边距
    private static final int LEFT_MARGIN = 50;
    // 原点位置
    private static final int ORIGIN_ROW = 3;
    private static final int ORIGIN_COL = 3;
    // 格式化器，保留两位小数
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    // 跟随鼠标的方格大小
    private static final int FOLLOW_SQUARE_SIZE = 100;
    // 日期格式化器
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    // 按钮数组
    private JButton[][] gridButtons;
    // 网格面板
    private JPanel gridPanel;
    // 无网格模式的面板
    private JPanel noGridPanel;
    // 主面板
    private JPanel mainPanel;
    // 是否显示网格
    private boolean showGrid = false;
    // 鼠标当前位置
    private final Point mousePosition = new Point(0, 0);
    // 当前选择的NOTE类型
    private NoteType currentNoteType = NoteType.TAP;
    // NOTE类型标签
    private JLabel noteTypeLabel;
    // NOTE类型选择按钮组
    private JPanel noteTypeButtonPanel;
    // 日志文本区域
    private JTextArea logTextArea;
    // 添加新的成员变量
    private JButton modeSwitchButton;
    private JTextField xCoordField;
    private JTextField yCoordField;
    private boolean isMouseFollowMode = true;
    // 添加新的成员变量
    private JButton confirmButton;
    private Point manualInputPoint = null;
    private static final double MAX_COORDINATE = 3.5;
    private static final double MIN_COORDINATE = -3.5;
    // 添加新的成员变量
    private JPanel songInfoPanel;
    private JToggleButton toggleButton;
    private CardLayout cardLayout;
    private JPanel expandedPanel;
    private JTextField songNameField;
    private JTextField bpmField;
    private JTextField authorField;
    // 添加新的成员变量
    private JTextField composerField;
    private JTextField chartAuthorField;
    private JTextField difficultyField;
    private JTextField difficultyLevelField;
    private JComboBox<String> difficultyColorCombo;
    private JTextField durationField;
    private JTextField offsetField;
    // 添加新的成员变量
    private JPanel notePropertyPanel;
    private JToggleButton notePropertyToggleButton;
    private JPanel notePropertyExpandedPanel;
    private JPanel directionPanel;
    private JToggleButton directionToggleButton;
    private JPanel directionExpandedPanel;
    private JRadioButton directionW;
    private JRadioButton directionA;
    private JRadioButton directionS;
    private JRadioButton directionD;
    private JCheckBox glowCheckBox;
    private JTextField tagField;
    private boolean isFirstDoublePlaced = false;
    private Point firstDoublePoint = null;
    private JTextField x2CoordField;
    private JTextField y2CoordField;
    private JLabel x2Label;
    private JLabel y2Label;
    private JLabel xLabel;
    private JLabel yLabel;
    
    // 新增Double Note提示标签
    private JLabel doubleNoteTipLabel;

    // 新增成员变量，用于引用JMenu
    private JMenu beatsMenu;

    // Getter和Setter方法
    // 歌曲信息全局变量
    @Getter @Setter private String songName = "";
    @Getter @Setter private String composer = "";
    @Getter @Setter private String chartAuthor = "";
    @Setter @Getter private String difficulty = "";
    @Getter @Setter private String duration = "";
    @Getter @Setter private String offset = "";
    @Getter @Setter private String bpm = "";
    @Setter @Getter private String author = "";

    private JToggleButton noteTypeToggleButton;
    private JPanel noteTypeExpandedPanel;

    private JsonEditorPanel chartEditorPanel;
    private JPanel chartEditorContainer;
    private JScrollPane logScrollPane;
    private JPanel chartLogButtonPanel;
    private int jsonEditorWidth = 275; // 可调宽度

    private JLabel positionLabel;

    private final MusicPlayer musicPlayer;
    private final BeatCalculator beatCalculator;
    private JSlider timeSlider;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JLabel songTitleLabel;
    private JLabel songAuthorLabel;
    private Timer logicTimer;
    private JLabel measureBeatLabel; // 新增成员变量
    
    // 删除日志方法，改为LogManager成员
    private LogManager logManager;
    
    private NoteTypePanel noteTypePanel;
    
    // 新增UI定时刷新
    private final Timer uiTimer;

    // 添加一个成员变量，标记用户是否正在拖动进度条
    private boolean isUserDraggingSlider = false;
    // 右键菜单打开时屏蔽左键放置，防误触
    private boolean isContextMenuOpen = false;

    private int displayBeatsCount = 8; // 默认显示8拍

    private final NoteManager noteManager = new NoteManager(); // NOTE管理器，负责所有NOTE的存储与查询
    
    // 静态常量，避免频繁new对象
    private static final BasicStroke NOTE_BORDER_STROKE = new BasicStroke(3f);
    private static final BasicStroke NOTE_GLOW_STROKE = new BasicStroke(2f);
    private static final Color NOTE_GLOW_COLOR = Color.GREEN;
    private static final Color NOTE_BORDER_W = Color.WHITE;
    private static final Color NOTE_BORDER_A = Color.YELLOW;
    private static final Color NOTE_BORDER_S = Color.ORANGE;
    private static final Color NOTE_BORDER_D = Color.RED;

    private static final java.util.Set<String> DIRECT_NOTE_ACTIONS = java.util.Set.of("hide_note", "change_glow_color", "easing_motion");
    private static final java.util.Set<String> INDIRECT_NOTE_ACTIONS = java.util.Set.of("draw_line", "draw_text");
    private static final java.util.Set<String> PLAYER_ACTIONS = java.util.Set.of("title", "actionbar", "chat", "potion", "remove_potion", "clear_effects", "blind");
    
    /**
     * 构造函数，初始化窗口
     */
    public MainWindow() {
        // 设置默认字体，解决中文乱码问题
        setUIFont(new javax.swing.plaf.FontUIResource("Microsoft YaHei", Font.PLAIN, 14));
        // 初始化音乐播放器和节拍计算器
        musicPlayer = new MusicPlayer();
        beatCalculator = new BeatCalculator();
        // 先初始化noteTypePanel，避免initUI时add为null
        noteTypePanel = new NoteTypePanel(currentNoteType, e -> {
            JRadioButton btn = (JRadioButton) e.getSource();
            for (NoteType type : NoteType.values()) {
                if (btn.getText().equals(type.getDisplayName())) {
                    if (!isFirstDoublePlaced) {
                        currentNoteType = type;
                        noteTypeLabel.setText("当前NOTE类型: <" + currentNoteType.getDisplayName() + ">");
                        logManager.log("切换NOTE类型: " + currentNoteType.getDisplayName());
                        // 处理UI显示
                        noGridPanel.repaint();
                        updateCoordinateFieldStates();
                    } else {
                        logManager.log("请先完成Double音符的放置，才能切换Note类型！");
                        noteTypePanel.setEnabled(false);
                    }
                    break;
                }
            }
        });
        initUI();
        logManager = new LogManager(logTextArea); // 日志管理器
        // 在initUI()结尾添加：
        uiTimer = new Timer(50, e -> updateMusicUIByClip());
        uiTimer.start();
        // 删除测试NOTE，不再自动插入
        // noteManager.addNote(new Note(3, 3, NoteType.TAP, beatCalculator.beatsToMicroseconds(0), "w", false));
        // noteManager.addNote(new Note(-2, -2, NoteType.DRAG, beatCalculator.beatsToMicroseconds(1), "a", true));
    }
    
    /**
     * 设置UI全局字体
     */
    private void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 设置窗口标题
        setTitle("CubeRhythm 铺面编辑器 [made by AcidAluminum]");
        
        // 设置窗口最大化以填充屏幕
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // 设置窗口居中显示
        setLocationRelativeTo(null);
        
        // 设置窗口关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建主面板
        mainPanel = new JPanel();
        mainPanel.setLayout(null); // 使用绝对布局
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setFocusable(true); // 使主面板可以获取焦点

        // 点击主面板空白区域时将焦点拉回，使键盘快捷键（如Shift切换网格）生效
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mainPanel.requestFocusInWindow();
            }
        });

        // 添加键盘监听器处理箭头键
        mainPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    toggleGridDisplay();
                    return;
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                    return;
                }

                if (musicPlayer == null || beatCalculator == null) return;

                long currentTime = musicPlayer.getCurrentTimeMicroseconds();
                long offset = getCurrentOffsetMicroseconds();
                long newTime = currentTime;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        // 左箭头：后退一拍（考虑偏移量）
                        double currentBeats = beatCalculator.microsecondsToBeats(currentTime + offset);
                        long targetBeat = (long) Math.floor(currentBeats);
                        if (Math.abs(currentBeats - targetBeat) < 0.01) {
                            // 如果已经在整拍上，后退一拍
                            targetBeat -= 1;
                        }
                        long targetMusicalTime = beatCalculator.beatsToMicroseconds(Math.max(0, targetBeat));
                        newTime = targetMusicalTime - offset;
                        break;

                    case KeyEvent.VK_RIGHT:
                        // 右箭头：前进一拍（考虑偏移量）
                        currentBeats = beatCalculator.microsecondsToBeats(currentTime + offset);
                        targetBeat = (long) Math.ceil(currentBeats);
                        if (Math.abs(currentBeats - targetBeat) < 0.01) {
                            // 如果已经在整拍上，前进一拍
                            targetBeat += 1;
                        }
                        targetMusicalTime = beatCalculator.beatsToMicroseconds(targetBeat);
                        newTime = targetMusicalTime - offset;
                        break;

                    default:
                        return;
                }

                // 限制在音频范围内
                long maxTime = musicPlayer.getActualClipLengthMicroseconds();
                newTime = Math.max(0, Math.min(newTime, maxTime));

                // 跳转到新位置
                musicPlayer.seek(newTime);
                updateMusicUIByClip();
            }
        });
        
        // 创建网格面板
        gridPanel = createGridPanel();
        gridPanel.setBounds(LEFT_MARGIN, 50, GRID_PANEL_SIZE, GRID_PANEL_SIZE);
        gridPanel.setVisible(showGrid); // 初始不显示网格
        
        // 创建无网格模式的面板
        noGridPanel = createNoGridPanel();
        noGridPanel.setBounds(LEFT_MARGIN, 50, GRID_PANEL_SIZE, GRID_PANEL_SIZE);
        noGridPanel.setVisible(!showGrid); // 初始显示无网格面板

        // 创建顶部菜单栏
        JMenuBar menuBar = new JMenuBar();
        
        // "拍数"折叠菜单
        JMenu beatsParentMenu = new JMenu("拍数");
        // 编辑拍数
        JMenu editBeatsMenu = new JMenu("编辑拍数");
        ButtonGroup editBeatsGroup = new ButtonGroup();
        int[] editBeatOptions = {1, 2, 3, 4, 6, 8, 16, 24, 32};
        for (int beats : editBeatOptions) {
            JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(String.valueOf(beats));
            editBeatsGroup.add(rbMenuItem);
            editBeatsMenu.add(rbMenuItem);
            rbMenuItem.addActionListener(e -> {
                beatCalculator.setBeatsPerMeasure(beats);
                logManager.log("切换编辑拍数: " + beats);
                SwingUtilities.invokeLater(() -> {
                    requestFocusInWindow();
                    mainPanel.repaint();
                });
            });
            if (beats == beatCalculator.getBeatsPerMeasure()) {
                rbMenuItem.setSelected(true);
            }
        }
        // 显示拍数
        JMenu displayBeatsMenu = new JMenu("显示拍数");
        ButtonGroup displayBeatsGroup = new ButtonGroup();
        int[] displayBeatOptions = {4, 8, 12, 16};
        for (int beats : displayBeatOptions) {
            JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(String.valueOf(beats));
            displayBeatsGroup.add(rbMenuItem);
            displayBeatsMenu.add(rbMenuItem);
            rbMenuItem.addActionListener(e -> {
                displayBeatsCount = beats;
                logManager.log("切换显示拍数: " + beats);
                SwingUtilities.invokeLater(() -> {
                    requestFocusInWindow();
                    mainPanel.repaint();
                });
            });
            if (beats == displayBeatsCount) rbMenuItem.setSelected(true);
        }
        beatsParentMenu.add(editBeatsMenu);
        beatsParentMenu.add(displayBeatsMenu);
        menuBar.add(beatsParentMenu);

        // "更新间隔"选项
        JMenu intervalMenu = new JMenu("更新间隔");
        double[] intervals = {50, 33.3, 16.7, 10, 8.3};
        ButtonGroup intervalGroup = new ButtonGroup();
        for (double interval : intervals) {
            String label = interval + " ms";
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
            intervalGroup.add(item);
            intervalMenu.add(item);
            item.addActionListener(e -> {
                int delay = (int)Math.round(interval);
                uiTimer.setDelay(delay);
                logManager.log("已设置进度条刷新间隔为: " + label);
                SwingUtilities.invokeLater(this::requestFocusInWindow);
            });
            if (interval == 50) item.setSelected(true); // 默认50ms
        }
        menuBar.add(intervalMenu);

        // 导入歌曲按钮（右对齐）
        menuBar.add(Box.createHorizontalGlue());
        JButton importSongButton = new JButton("导入歌曲");
        importSongButton.addActionListener(e -> {
            importSong();
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });
        menuBar.add(importSongButton);

        // 添加"菜单"下拉菜单
        JMenu menuMenu = new JMenu("菜单");
        JMenuItem importItem = new JMenuItem("导入");
        JMenuItem saveItem = new JMenuItem("保存");
        menuMenu.add(importItem);
        menuMenu.add(saveItem);
        menuBar.add(menuMenu, 0); // 插入到最左侧
        // 导入功能 - 从JSON文件导入
        importItem.addActionListener(e -> {
            try {
                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Chart File", "json"));
                fileChooser.setCurrentDirectory(new java.io.File("charts")); // 默认打开charts目录
                int result = fileChooser.showOpenDialog(this);
                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    java.io.File file = fileChooser.getSelectedFile();

                    // 读取JSON文件完整内容，不做任何截断
                    String notesJsonContent = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);

                    // 处理日志区合并/覆盖
                    String oldLog = chartEditorPanel.getText();
                    if (!oldLog.trim().isEmpty()) {
                        int choice = javax.swing.JOptionPane.showOptionDialog(this,
                            "对铺面生成日志中已存在的铺面片段做如何处理？",
                            "导入选项",
                            javax.swing.JOptionPane.DEFAULT_OPTION,
                            javax.swing.JOptionPane.QUESTION_MESSAGE,
                            null,
                            new Object[]{"覆盖", "合并", "取消"},
                            "覆盖");
                        if (choice == 0) { // 覆盖
                            chartEditorPanel.setText(notesJsonContent);
                        } else if (choice == 1) { // 合并
                            // 合并JSON数组
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                com.google.gson.JsonArray oldArray = gson.fromJson(oldLog, com.google.gson.JsonArray.class);
                                com.google.gson.JsonArray newArray = gson.fromJson(notesJsonContent, com.google.gson.JsonArray.class);

                                // 将新数组的元素添加到旧数组
                                for (int i = 0; i < newArray.size(); i++) {
                                    oldArray.add(newArray.get(i));
                                }

                                // 格式化输出
                                com.google.gson.GsonBuilder gsonBuilder = new com.google.gson.GsonBuilder();
                                gsonBuilder.setPrettyPrinting();
                                String mergedJson = gsonBuilder.create().toJson(oldArray);
                                chartEditorPanel.setText(mergedJson);
                            } catch (Exception ex) {
                                // 如果合并失败，直接追加
                                chartEditorPanel.setText(oldLog + "\n" + notesJsonContent);
                            }
                        } else {
                            return; // 取消
                        }
                    } else {
                        chartEditorPanel.setText(notesJsonContent);
                    }

                    // 自动重载音符
                    reloadNotesFromJson();

                    logManager.log("成功导入铺面: " + file.getName());
                    javax.swing.JOptionPane.showMessageDialog(this, "导入成功！", "提示", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                logManager.log("导入失败: " + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "导入失败：" + ex.getMessage(), "错误", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        // 保存功能
        saveItem.addActionListener(e -> {
            try {
                // 1. 获取歌名并处理文件名
                String songNameRaw = songNameField.getText().trim();
                String songNameForFile = songNameRaw.replaceAll(" ", "_");
                if (songNameForFile.isEmpty()) songNameForFile = "unknown";
                String folderName = "targetFolder";
                java.io.File folder = new java.io.File(folderName);
                if (!folder.exists()) folder.mkdirs();
                java.io.File file = new java.io.File(folder, songNameForFile + ".json");
                java.io.PrintWriter writer = new java.io.PrintWriter(file, StandardCharsets.UTF_8);

                // 2. 处理各字段默认值
                String chartId = songNameForFile.toLowerCase();
                String chartName = songNameRaw.isEmpty() ? "unknown" : songNameRaw;
                String composer = composerField.getText().trim().isEmpty() ? "unknown" : composerField.getText().trim();
                String chartAuthor = chartAuthorField.getText().trim().isEmpty() ? "unknown" : chartAuthorField.getText().trim();
                String difficultyText = difficultyField.getText().trim();
                int difficultyLevel = 1;
                String difficultyName = "Easy";
                if (!difficultyText.isEmpty()) {
                    try {
                        difficultyLevel = Integer.parseInt(difficultyText);
                        if (difficultyLevel >= 1 && difficultyLevel <= 5) difficultyName = "Easy";
                        else if (difficultyLevel >= 6 && difficultyLevel <= 10) difficultyName = "Normal";
                        else if (difficultyLevel >= 11 && difficultyLevel <= 15) difficultyName = "Hard";
                    } catch (NumberFormatException ignored) {}
                }

                // 解析时长（分:秒格式转换为秒）
                int durationSeconds = 60;
                String durationText = durationField.getText().trim();
                if (!durationText.isEmpty() && durationText.matches("^\\d+:\\d{2}$")) {
                    String[] parts = durationText.split(":");
                    durationSeconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
                }

                // 解析偏移
                double offsetSeconds = 0.0;
                String offsetText = offsetField.getText().trim();
                if (!offsetText.isEmpty()) {
                    try {
                        offsetSeconds = Double.parseDouble(offsetText);
                    } catch (NumberFormatException ex) {
                        offsetSeconds = 0.0;
                    }
                }

                // 获取BPM
                double bpm = beatCalculator.getBpm();

                // 音频文件名
                String audioName = "cr." + chartId;

                // 3. 构建JSON
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"version\": \"1.0.0\",\n");
                json.append("  \"metadata\": {\n");
                json.append("    \"id\": \"").append(escapeJson(chartId)).append("\",\n");
                json.append("    \"title\": \"").append(escapeJson(chartName)).append("\",\n");
                json.append("    \"artist\": \"").append(escapeJson(composer)).append("\",\n");
                json.append("    \"charter\": \"").append(escapeJson(chartAuthor)).append("\",\n");
                json.append("    \"difficulty\": {\n");
                json.append("      \"name\": \"").append(difficultyName).append("\",\n");
                json.append("      \"level\": ").append(difficultyLevel).append(",\n");
                json.append("      \"color\": \"&b\"\n");
                json.append("    },\n");
                json.append("    \"audio\": \"").append(escapeJson(audioName)).append("\",\n");
                json.append("    \"duration\": ").append(durationSeconds).append(",\n");
                json.append("    \"offset\": ").append(offsetSeconds).append(",\n");
                json.append("    \"bpm\": ").append(bpm).append("\n");
                json.append("  },\n");
                json.append("  \"notes\": [\n");

                // 4. 转换所有NOTE为JSON格式
                List<Note> notes = noteManager.getNotes();
                for (int i = 0; i < notes.size(); i++) {
                    Note note = notes.get(i);
                    json.append(noteToJson(note));
                    if (i < notes.size() - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                }

                json.append("  ]\n");
                json.append("}\n");

                writer.print(json.toString());
                writer.close();
                JOptionPane.showMessageDialog(this, "保存成功：" + file.getAbsolutePath(), "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        setJMenuBar(menuBar); // 设置窗口的菜单栏
        
        // 创建NOTE类型标签和折叠按钮
        JPanel noteTypeHeaderPanel = new JPanel();
        noteTypeHeaderPanel.setLayout(null);
        noteTypeHeaderPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 50, 275, 30);
        noteTypeHeaderPanel.setBackground(new Color(240, 240, 240));

        noteTypeLabel = new JLabel("当前NOTE类型: <" + currentNoteType.getDisplayName() + ">");
        noteTypeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        noteTypeLabel.setBounds(0, 0, 275, 30);
        noteTypeHeaderPanel.add(noteTypeLabel);

        // 创建模式切换按钮
        modeSwitchButton = new JButton("当前模式: 鼠标跟随");
        modeSwitchButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        modeSwitchButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, 50, 250, 30);
        modeSwitchButton.addActionListener(e -> {
            isMouseFollowMode = !isMouseFollowMode;
            modeSwitchButton.setText("当前模式: " + (isMouseFollowMode ? "鼠标跟随" : "手动输入"));
            xCoordField.setEnabled(!isMouseFollowMode);
            yCoordField.setEnabled(!isMouseFollowMode);
            confirmButton.setEnabled(!isMouseFollowMode);
            manualInputPoint = null;
            noGridPanel.repaint();
            logManager.log("切换输入模式: " + (isMouseFollowMode ? "鼠标跟随" : "手动输入"));
            updateCoordinateFieldStates();
            if (!isMouseFollowMode && (currentNoteType == NoteType.DOUBLE || currentNoteType == NoteType.FAKE_DOUBLE || currentNoteType == NoteType.MINE_DOUBLE) && isFirstDoublePlaced && firstDoublePoint != null) {
                int centerX = noGridPanel.getWidth() / 2;
                int centerY = noGridPanel.getHeight() / 2;
                double x1_grid = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                double y1_grid = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                xCoordField.setText(DECIMAL_FORMAT.format(x1_grid));
                yCoordField.setText(DECIMAL_FORMAT.format(y1_grid));
                xCoordField.setEnabled(true);
                yCoordField.setEnabled(true);
            }
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });

        // 右侧列组件的起始Y坐标
        int currentY = modeSwitchButton.getY() + modeSwitchButton.getHeight() + 10; // 从模式切换按钮下方10px开始

        // X坐标标签和输入框
        xLabel = new JLabel("X坐标:");
        xLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        xLabel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 50, 30);

        xCoordField = new JTextField();
        xCoordField.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 685, currentY, 190, 30);
        xCoordField.setEnabled(!isMouseFollowMode);
        xCoordField.addActionListener(e -> {
            try {
                String xText = xCoordField.getText().trim();
                String yText = yCoordField.getText().trim();
                if (!xText.isEmpty() && !yText.isEmpty()) {
                    double x = Double.parseDouble(xText);
                    double y = Double.parseDouble(yText);
                    xCoordField.setText(DECIMAL_FORMAT.format(x));
                    yCoordField.setText(DECIMAL_FORMAT.format(y));
                    updateManualInputPoint(x, y);
                }
            } catch (Exception ex) {
                xCoordField.setText("");
                yCoordField.setText("");
                x2CoordField.setText("");
                y2CoordField.setText("");
                manualInputPoint = null;
                noGridPanel.repaint();
            }
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });

        currentY += xCoordField.getHeight() + 10; // 更新currentY

        // Y坐标标签和输入框
        yLabel = new JLabel("Y坐标:");
        yLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        yLabel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 50, 30);

        yCoordField = new JTextField();
        yCoordField.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 685, currentY, 190, 30);
        yCoordField.setEnabled(!isMouseFollowMode);
        yCoordField.addActionListener(e -> {
            try {
                String xText = xCoordField.getText().trim();
                String yText = yCoordField.getText().trim();
                if (!xText.isEmpty() && !yText.isEmpty()) {
                    double x = Double.parseDouble(xText);
                    double y = Double.parseDouble(yText);
                    xCoordField.setText(DECIMAL_FORMAT.format(x));
                    yCoordField.setText(DECIMAL_FORMAT.format(y));
                    updateManualInputPoint(x, y);
                }
            } catch (Exception ex) {
                xCoordField.setText("");
                yCoordField.setText("");
                x2CoordField.setText("");
                y2CoordField.setText("");
                manualInputPoint = null;
                noGridPanel.repaint();
            }
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });

        currentY += yCoordField.getHeight() + 10; // 更新currentY

        // 创建位置显示标签
        positionLabel = new JLabel("NOTE位置: (0, 0)");
        positionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        positionLabel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 250, 30);
        positionLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        currentY += positionLabel.getHeight() + 10; // 更新currentY

        // 新增Double Note提示标签
        doubleNoteTipLabel = new JLabel("");
        doubleNoteTipLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        doubleNoteTipLabel.setForeground(Color.BLUE); // 可以选择一个醒目的颜色
        doubleNoteTipLabel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 250, 30);
        doubleNoteTipLabel.setVisible(false); // 初始隐藏
        currentY += doubleNoteTipLabel.getHeight() + 10; // 更新currentY

        // X2坐标标签和输入框
        x2Label = new JLabel("X2坐标:");
        x2Label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        x2Label.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 50, 30);
        x2Label.setVisible(true);

        x2CoordField = new JTextField();
        x2CoordField.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 685, currentY, 190, 30);
        x2CoordField.setEnabled(!isMouseFollowMode);
        x2CoordField.addActionListener(e -> {
            try {
                String x2Text = x2CoordField.getText().trim();
                String y2Text = y2CoordField.getText().trim();
                if (!x2Text.isEmpty() && !y2Text.isEmpty()) {
                    double x2 = Double.parseDouble(x2Text);
                    double y2 = Double.parseDouble(y2Text);
                    x2CoordField.setText(DECIMAL_FORMAT.format(x2));
                    y2CoordField.setText(DECIMAL_FORMAT.format(y2));
                }
            } catch (Exception ex) {
                x2CoordField.setText("");
                y2CoordField.setText("");
            }
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });
        x2CoordField.setVisible(true);

        currentY += x2CoordField.getHeight() + 10; // 更新currentY

        y2Label = new JLabel("Y2坐标:");
        y2Label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        y2Label.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 50, 30);
        y2Label.setVisible(true);

        y2CoordField = new JTextField();
        y2CoordField.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 685, currentY, 190, 30);
        y2CoordField.setEnabled(!isMouseFollowMode);
        y2CoordField.addActionListener(e -> {
            try {
                String x2Text = x2CoordField.getText().trim();
                String y2Text = y2CoordField.getText().trim();
                if (!x2Text.isEmpty() && !y2Text.isEmpty()) {
                    double x2 = Double.parseDouble(x2Text);
                    double y2 = Double.parseDouble(y2Text);
                    x2CoordField.setText(DECIMAL_FORMAT.format(x2));
                    y2CoordField.setText(DECIMAL_FORMAT.format(y2));
                }
            } catch (Exception ex) {
                x2CoordField.setText("");
                y2CoordField.setText("");
            }
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });
        y2CoordField.setVisible(true);

        currentY += y2CoordField.getHeight() + 20; // 更新currentY，为确认按钮留出更多空间

        // 创建确认按钮
        confirmButton = new JButton("确认生成NOTE");
        confirmButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        confirmButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, currentY, 250, 30);
        confirmButton.setEnabled(!isMouseFollowMode);
        confirmButton.addActionListener(e -> {
            // 这里直接实现确认逻辑或调用外部类
            // ... 你的NOTE生成逻辑 ...
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });

        // 创建歌曲信息面板
        createSongInfoPanel();
        // songInfoPanel的setBounds将在adjustPanelPositions中设置

        // 创建NOTE属性设置面板
        createNotePropertyPanel();
        // notePropertyPanel的setBounds将在adjustPanelPositions中设置

        // 创建操作日志文本区域
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setBackground(new Color(240, 240, 240));
        logTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 创建铺面 JSON 编辑器面板
        chartEditorPanel = new JsonEditorPanel();

        // 创建铺面编辑器容器面板
        chartEditorContainer = new JPanel(new BorderLayout());
        chartEditorContainer.setBorder(BorderFactory.createTitledBorder("铺面 JSON 编辑器"));
        chartEditorContainer.add(chartEditorPanel, BorderLayout.CENTER);

        // 拖拽分隔条（仅横向可拖）
        JPanel dragHandle = new JPanel();
        dragHandle.setBackground(new Color(180, 180, 200));
        dragHandle.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        dragHandle.setToolTipText("拖拽调整编辑器宽度");
        final int[] dragStartX = {0};
        final int[] dragStartW = {0};
        dragHandle.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStartX[0] = SwingUtilities.convertMouseEvent(dragHandle, e, mainPanel).getX();
                dragStartW[0] = jsonEditorWidth;
            }
        });
        dragHandle.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                int curX = SwingUtilities.convertMouseEvent(dragHandle, e, mainPanel).getX();
                jsonEditorWidth = Math.max(150, dragStartW[0] + (curX - dragStartX[0]));
                relayoutEditorColumns();
            }
        });

        // 操作日志滚动面板
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("操作日志"));

        // 在日志下方添加"重载"和"排序"按钮
        chartLogButtonPanel = new JPanel();
        chartLogButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton reloadButton = new JButton("重载");
        JButton sortButton = new JButton("排序");
        JButton formatJsonButton = new JButton("格式化");
        JButton templateButton = new JButton("动画模板");
        JButton wrapperButton = new JButton("模板包装器");
        chartLogButtonPanel.add(reloadButton);
        chartLogButtonPanel.add(sortButton);
        chartLogButtonPanel.add(formatJsonButton);
        chartLogButtonPanel.add(templateButton);
        chartLogButtonPanel.add(wrapperButton);
        mainPanel.add(chartLogButtonPanel);
        templateButton.addActionListener(e ->
            org.AcidAluminum.cubeRhythm.ui.AnimationTemplateWindow.open(this));
        wrapperButton.addActionListener(e ->
            org.AcidAluminum.cubeRhythm.ui.TemplateWrapperDialog.open(this, () ->
                org.AcidAluminum.cubeRhythm.ui.AnimationTemplateWindow.open(this)));
        formatJsonButton.addActionListener(e -> chartEditorPanel.formatJson());
        // "重载"按钮逻辑 - 从JSON文本加载音符
        reloadButton.addActionListener(e -> {
            try {
                reloadNotesFromJson();
                logManager.log("成功从JSON重载音符");
                mainPanel.repaint();
            } catch (Exception ex) {
                logManager.log("重载失败: " + ex.getMessage());
            }
        });
        // "排序"按钮逻辑
        sortButton.addActionListener(e -> {
            List<Note> notes = noteManager.getNotes();
            notes.sort(this::noteCompare);
            noteManager.clearNotes();
            for (Note n : notes) noteManager.addNote(n);
            updateChartLogJson();
            mainPanel.repaint();
        });
        
        // 先初始化所有add到mainPanel的组件，防止NPE
        if (noteTypePanel == null) {
            noteTypePanel = new NoteTypePanel(currentNoteType, e -> {});
            noteTypePanel.setBounds(0, 0, 250, 180);
        }
        if (noteTypeExpandedPanel == null) {
            noteTypeExpandedPanel = new JPanel();
            noteTypeExpandedPanel.setLayout(null);
            noteTypeExpandedPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 120, 250, GRID_PANEL_SIZE - 120);
            noteTypeExpandedPanel.add(noteTypePanel);
            noteTypeExpandedPanel.setVisible(false);
        }
        if (songInfoPanel == null) {
            songInfoPanel = new JPanel();
            songInfoPanel.setLayout(null);
        }
        if (notePropertyPanel == null) {
            notePropertyPanel = new JPanel();
            notePropertyPanel.setLayout(null);
        }
        if (doubleNoteTipLabel == null) {
            doubleNoteTipLabel = new JLabel("");
        }
        if (logTextArea == null) {
            logTextArea = new JTextArea();
        }
        if (chartEditorPanel == null) {
            chartEditorPanel = new JsonEditorPanel();
        }
        // 先add编辑区和NOTE类型选择UI，保证显示
        mainPanel.add(gridPanel);
        mainPanel.add(noGridPanel);
        mainPanel.add(noteTypeHeaderPanel);
        // 再add其它控件
        mainPanel.add(modeSwitchButton);
        mainPanel.add(xLabel);
        mainPanel.add(xCoordField);
        mainPanel.add(yLabel);
        mainPanel.add(yCoordField);
        mainPanel.add(positionLabel);
        mainPanel.add(x2Label);
        mainPanel.add(x2CoordField);
        mainPanel.add(y2Label);
        mainPanel.add(y2CoordField);
        mainPanel.add(confirmButton);
        mainPanel.add(songInfoPanel);
        mainPanel.add(dragHandle);
        mainPanel.add(logScrollPane);
        mainPanel.add(chartEditorContainer);
        mainPanel.add(notePropertyPanel);
        mainPanel.add(doubleNoteTipLabel);
        mainPanel.add(noteTypeExpandedPanel);

        // 音乐进度条和时间显示
        timeSlider = new JSlider(0, 1000, 0);
        timeSlider.setBounds(LEFT_MARGIN, GRID_PANEL_SIZE + 60, GRID_PANEL_SIZE, 30);
        timeSlider.addMouseListener(new MouseAdapter() {
            boolean wasPlaying = false;
            @Override
            public void mousePressed(MouseEvent e) {
                wasPlaying = musicPlayer.isPlaying();
                musicPlayer.pause();
                isUserDraggingSlider = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                long total = musicPlayer.getActualClipLengthMicroseconds();
                long microsecondPosition = (long) (timeSlider.getValue() / 1000.0 * total);
                musicPlayer.seek(microsecondPosition);
                if (wasPlaying) {
                    musicPlayer.play();
                }
                isUserDraggingSlider = false;
                updateMusicUIByClip();
                requestFocusInWindow();
            }
        });
        timeSlider.addChangeListener(e -> {
            long total = musicPlayer.getActualClipLengthMicroseconds();
            int sliderValue = timeSlider.getValue();
            long microsecondPosition = (long) (sliderValue / 1000.0 * total);
            if (timeSlider.getValueIsAdjusting()) {
                updateMusicUIByClip(microsecondPosition);
            }
        });
        mainPanel.add(timeSlider);

        currentTimeLabel = new JLabel("00:00.000");
        currentTimeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        currentTimeLabel.setBounds(LEFT_MARGIN, GRID_PANEL_SIZE + 95, 70, 20);
        mainPanel.add(currentTimeLabel);

        totalTimeLabel = new JLabel("/ 00:00.000");
        totalTimeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        totalTimeLabel.setBounds(LEFT_MARGIN + 75, GRID_PANEL_SIZE + 95, 70, 20);
        mainPanel.add(totalTimeLabel);

        songTitleLabel = new JLabel("歌曲名: 无");
        songTitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        songTitleLabel.setBounds(LEFT_MARGIN + 150 + 50, GRID_PANEL_SIZE + 95, 200, 20); // 预留50px
        mainPanel.add(songTitleLabel);

        songAuthorLabel = new JLabel("作者: 无");
        songAuthorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        songAuthorLabel.setBounds(LEFT_MARGIN + 150 + 50 + 200 + 50, GRID_PANEL_SIZE + 95, 200, 20); // 预留50px
        mainPanel.add(songAuthorLabel);

        // 添加小节和拍数显示标签
        measureBeatLabel = new JLabel("小节: 0 拍: 0/4");
        measureBeatLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        measureBeatLabel.setBounds(LEFT_MARGIN, GRID_PANEL_SIZE + 120, 200, 20); // 调整位置到时间下方
        mainPanel.add(measureBeatLabel);


        // 添加音乐控制按钮
        JButton playPauseButton = new JButton("播放/暂停");
        playPauseButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        playPauseButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 50, GRID_PANEL_SIZE + 95, 80, 25);
        playPauseButton.addActionListener(e -> {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause();
                snapToNearestBeat(); // 暂停时吸附到最近的一拍
                logManager.log("音乐已暂停（已吸附到最近一拍）");
            } else {
                musicPlayer.play();
                logManager.log("音乐已播放");
            }
            requestFocusInWindow();
        });
        mainPanel.add(playPauseButton);

        // 停止按钮
        JButton stopButton = new JButton("停止");
        stopButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        stopButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 140, GRID_PANEL_SIZE + 95, 60, 25);
        stopButton.addActionListener(e -> {
            musicPlayer.seek(0);
            musicPlayer.pause();
            updateMusicUIByClip();
            logManager.log("音乐已停止");
            requestFocusInWindow();
        });
        mainPanel.add(stopButton);

        // 重置按钮
        JButton resetButton = new JButton("重置");
        resetButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        resetButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 210, GRID_PANEL_SIZE + 95, 60, 25);
        resetButton.addActionListener(e -> {
            musicPlayer.seek(0);
            musicPlayer.pause();
            updateMusicUIByClip();
            logManager.log("音乐已重置到起始位置");
            requestFocusInWindow();
        });
        mainPanel.add(resetButton);

        // 进度条右侧的起始X坐标
        int buttonsStartX = LEFT_MARGIN + GRID_PANEL_SIZE + 20; // 右侧留20px间距

        // 播放/暂停按钮
        playPauseButton.setBounds(buttonsStartX, GRID_PANEL_SIZE + 60, 100, 25);

        // 停止按钮（紧挨播放按钮右侧）
        stopButton.setBounds(buttonsStartX + 110, GRID_PANEL_SIZE + 60, 80, 25);

        // 重置按钮（紧挨停止按钮右侧）
        resetButton.setBounds(buttonsStartX + 200, GRID_PANEL_SIZE + 60, 80, 25);

        // 添加主面板到窗口
        add(mainPanel);
        
        // 确保窗口可以获取焦点以接收键盘事件
        setFocusable(true);
        requestFocus();
        
        // 初始更新坐标输入框状态
        updateCoordinateFieldStates();
        
        // 初始调整面板位置
        adjustPanelPositions();
        
        // 确保拍数选择按钮初始是启用的
        disableBeatSelectionButtons(true); // Ensure initially enabled

        // 添加键盘监听器到窗口
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    toggleGridDisplay();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0); // 按下Esc键退出程序
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    // 上一拍
                    long current = musicPlayer.getCurrentTimeMicroseconds();
                    double currentBeat = beatCalculator.microsecondsToBeats(current);
                    double targetBeat = Math.max(0, currentBeat - 1);
                    long targetMicroseconds = beatCalculator.beatsToMicroseconds(targetBeat);
                    musicPlayer.seek(targetMicroseconds);
                    updateMusicUIByClip();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // 下一拍
                    long current = musicPlayer.getCurrentTimeMicroseconds();
                    double currentBeat = beatCalculator.microsecondsToBeats(current);
                    double targetBeat = currentBeat + 1;
                    long total = musicPlayer.getActualClipLengthMicroseconds();
                    double maxBeat = beatCalculator.microsecondsToBeats(total);
                    if (targetBeat > maxBeat) targetBeat = maxBeat;
                    long targetMicroseconds = beatCalculator.beatsToMicroseconds(targetBeat);
                    musicPlayer.seek(targetMicroseconds);
                    updateMusicUIByClip();
                }
            }
        });

        // 创建NOTE类型折叠按钮
        noteTypeToggleButton = new JToggleButton("NOTE类型 ▼");
        noteTypeToggleButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        noteTypeToggleButton.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 90, 250, 30);
        noteTypeToggleButton.addActionListener(e -> {
            boolean isExpanded = noteTypeToggleButton.isSelected();
            noteTypeExpandedPanel.setVisible(isExpanded);
            noteTypeToggleButton.setText(isExpanded ? "NOTE类型 ▲" : "NOTE类型 ▼");
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        // NOTE类型选择面板
        noteTypeButtonPanel = new NoteTypePanel(currentNoteType, e -> {
            JRadioButton btn = (JRadioButton) e.getSource();
            for (NoteType type : NoteType.values()) {
                if (btn.getText().equals(type.getDisplayName())) {
                    if (!isFirstDoublePlaced) {
                        currentNoteType = type;
                        noteTypeLabel.setText("当前NOTE类型: <" + currentNoteType.getDisplayName() + ">");
                        logManager.log("切换NOTE类型: " + currentNoteType.getDisplayName());
                        noGridPanel.repaint();
                        updateCoordinateFieldStates();
                    } else {
                        logManager.log("请先完成Double音符的放置，才能切换Note类型！");
                        noteTypeButtonPanel.setEnabled(false);
                    }
                    break;
                }
            }
        });
        noteTypeButtonPanel.setBounds(0, 0, 250, GRID_PANEL_SIZE - 120);
        // NOTE类型折叠面板
        noteTypeExpandedPanel = new JPanel();
        noteTypeExpandedPanel.setLayout(null);
        noteTypeExpandedPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 120, 250, GRID_PANEL_SIZE - 120);
        noteTypeExpandedPanel.add(noteTypeButtonPanel);
        noteTypeExpandedPanel.setVisible(false);
        // 添加到主面板
        mainPanel.add(noteTypeToggleButton);
        mainPanel.add(noteTypeExpandedPanel);
    }
    
    /**
     * 更新手动输入点
     */
    private void updateManualInputPoint(double x, double y) {
        // 将坐标转换为面板上的像素位置
        int centerX = noGridPanel.getWidth() / 2;
        int centerY = noGridPanel.getHeight() / 2;
        int pixelX = centerX + (int)(x * CELL_SIZE);
        int pixelY = centerY - (int)(y * CELL_SIZE); // 注意Y轴方向
        manualInputPoint = new Point(pixelX, pixelY);
        // 强制重绘面板
        noGridPanel.repaint();
        // 确保重绘立即执行
        noGridPanel.paintImmediately(noGridPanel.getBounds());
    }
    
    /**
     * 更新坐标输入框的启用/禁用状态
     */
    private void updateCoordinateFieldStates() {
        if (isMouseFollowMode) {
            xCoordField.setEnabled(false);
            yCoordField.setEnabled(false);
            x2CoordField.setEnabled(false);
            y2CoordField.setEnabled(false);
        } else {
            if (currentNoteType == NoteType.DOUBLE || currentNoteType == NoteType.FAKE_DOUBLE || currentNoteType == NoteType.MINE_DOUBLE) {
                xCoordField.setEnabled(true);
                yCoordField.setEnabled(true);
                x2CoordField.setEnabled(true);
                y2CoordField.setEnabled(true);
            } else if (currentNoteType == NoteType.EXECUTION ||
                       currentNoteType == NoteType.FLICK_LEFT ||
                       currentNoteType == NoteType.FLICK_RIGHT ||
                       currentNoteType == NoteType.FAKE_FLICK) {
                xCoordField.setEnabled(false);
                yCoordField.setEnabled(false);
                x2CoordField.setEnabled(false);
                y2CoordField.setEnabled(false);
            } else {
                xCoordField.setEnabled(true);
                yCoordField.setEnabled(true);
                x2CoordField.setEnabled(false);
                y2CoordField.setEnabled(false);
            }
        }
    }
    
    /**
     * 格式化坐标显示，移除末尾的零
     */
    private String formatCoordinateDisplay(double value) {
        // 使用DecimalFormat先格式化为两位小数
        String formatted = DECIMAL_FORMAT.format(value);
        // 检查是否以".00"结尾，如果是，则移除小数部分
        if (formatted.endsWith(".00")) {
            return formatted.substring(0, formatted.length() - 3); // 移除.00
        } else if (formatted.endsWith("0") && formatted.contains(".")) {
            // 检查是否以".X0"结尾，如果是，则移除末尾的0
            return formatted.substring(0, formatted.length() - 1); // 移除末尾的0
        }
        return formatted;
    }
    
    /**
     * 创建无网格模式的面板
     */
    private JPanel createNoGridPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制边框
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                // 绘制中心原点
                g.setColor(Color.RED);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                g.fillOval(centerX - 3, centerY - 3, 6, 6);

                // 绘制刻度
                g.setColor(Color.BLACK);
                g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

                // 绘制水平刻度（上方）
                for (int i = -3; i <= 3; i++) {
                    int x = centerX + i * CELL_SIZE;
                    g.drawLine(x, 0, x, 5);
                    g.drawString(String.valueOf(i), x - 5, 20);
                    // 绘制0.5刻度（包括-2.5）
                    if (i < 3) {
                        int xHalf = x + CELL_SIZE / 2;
                        g.drawLine(xHalf, 0, xHalf, 3);
                        g.drawString(String.valueOf(i + 0.5), xHalf - 5, 20);
                    }
                }

                // 绘制垂直刻度（左侧）
                for (int i = -3; i <= 3; i++) {
                    int y = centerY - i * CELL_SIZE;
                    g.drawLine(0, y, 5, y);
                    g.drawString(String.valueOf(i), 10, y + 5);
                    // 绘制0.5刻度（包括-2.5）
                    if (i < 3) {
                        int yHalf = y - CELL_SIZE / 2;
                        g.drawLine(0, yHalf, 3, yHalf);
                        g.drawString(String.valueOf(i + 0.5), 10, yHalf + 5);
                    }
                }

                // ---------- 伪3D NOTE动画显示 ----------
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                List<Note> notesToShow = null;
                double currentBeat = 0;
                if (musicPlayer != null && beatCalculator != null && noteManager != null) {
                    long currentTime = musicPlayer.getCurrentTimeMicroseconds();
                    currentBeat = beatCalculator.microsecondsToBeats(currentTime);
                    notesToShow = noteManager.getNotesForCurrentBeat(currentTime, displayBeatsCount, beatCalculator);
                    centerX = getWidth() / 2;
                    centerY = getHeight() / 2;
                    int n = displayBeatsCount - 1;
                    Composite defaultComposite = g2.getComposite();
                    for (Note note : notesToShow) {
                        double noteBeat = Math.round(beatCalculator.microsecondsToBeats(note.getTimeMicroseconds()));
                        double k = noteBeat - currentBeat;
                        if (k < 0 || k > n) continue;
                        double progress = (n == 0) ? 1.0 : (n - k) / n;
                        float alphaF = (float)(1.0 - (k / n) * (1.0 - 80.0/255));
                        alphaF = Math.max(0f, Math.min(1f, alphaF));
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaF));

                        int positionsToRender = note.isDouble() ? 2 : 1;
                        for (int pos = 0; pos < positionsToRender; pos++) {
                            double x = (pos == 0 ? note.getX() : note.getX2()) * 100;
                            double y = -(pos == 0 ? note.getY() : note.getY2()) * 100;
                            double x0 = x / 5.0, y0 = y / 5.0;
                            double cx = x0 + (x - x0) * progress;
                            double cy = y0 + (y - y0) * progress;
                            double size = 20 + 80 * progress;
                            int rx = (int)(centerX + cx - size / 2);
                            int ry = (int)(centerY + cy - size / 2);
                            int rs = (int) size;
                            g2.setColor(note.getColor());
                            g2.fillRect(rx, ry, rs, rs);
                            if (note.isFake()) drawCheckerboard(g2, rx, ry, rs, rs, (int)(alphaF * 255));
                            if (note.isMine()) drawCrossPattern(g2, rx, ry, rs, rs, (int)(alphaF * 255));
                            Color borderColor = switch (note.getDirection() != null ? note.getDirection() : "w") {
                                case "a" -> NOTE_BORDER_A;
                                case "s" -> NOTE_BORDER_S;
                                case "d" -> NOTE_BORDER_D;
                                default -> NOTE_BORDER_W;
                            };
                            g2.setStroke(NOTE_BORDER_STROKE);
                            g2.setColor(borderColor);
                            g2.drawRect(rx, ry, rs, rs);
                            if (note.isGlowing()) {
                                g2.setColor(NOTE_GLOW_COLOR);
                                g2.setStroke(NOTE_GLOW_STROKE);
                                int px = (int)(centerX + cx), py = (int)(centerY + cy);
                                g2.drawLine(px - 2, py, px + 2, py);
                                g2.drawLine(px, py - 2, px, py + 2);
                            }
                        }
                    }
                    g2.setComposite(defaultComposite);
                }
                // Execution action 可视化
                renderExecutionActions(g, notesToShow, centerX, centerY, currentBeat, displayBeatsCount, getWidth());
                // 仅在无网格模式下显示跟随鼠标的NOTE，不吸附
                if (isMouseFollowMode && currentNoteType != NoteType.EXECUTION &&
                        currentNoteType != NoteType.FLICK_LEFT && currentNoteType != NoteType.FLICK_RIGHT &&
                        currentNoteType != NoteType.FAKE_FLICK) {
                    Graphics2D g2p = (Graphics2D) g;
                    Composite prev = g2p.getComposite();
                    g2p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 180f/255f));
                    g2p.setColor(currentNoteType.getColor());
                    int squareX = mousePosition.x - FOLLOW_SQUARE_SIZE / 2;
                    int squareY = mousePosition.y - FOLLOW_SQUARE_SIZE / 2;
                    g2p.fillRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                    g2p.setComposite(prev);
                    g2p.setColor(currentNoteType.getColor());
                    g2p.drawRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(new Color(224, 224, 224)); // 浅灰色，保证白色边框可见
        
        // 添加鼠标移动监听器
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition.setLocation(e.getX(), e.getY());
                
                // 更新位置显示标签
                if (currentNoteType != NoteType.EXECUTION && 
                    currentNoteType != NoteType.FLICK_LEFT &&
                    currentNoteType != NoteType.FLICK_RIGHT &&
                    currentNoteType != NoteType.FAKE_FLICK) {
                    
                    int actualNoteCenterX_px;
                    int actualNoteCenterY_px;
                    
                    if (isMouseFollowMode) {
                        // 鼠标跟随模式：NOTE中心即鼠标位置
                        actualNoteCenterX_px = e.getX();
                        actualNoteCenterY_px = e.getY();
                    } else { 
                        // 手动输入模式：NOTE中心为手动输入点
                        if (manualInputPoint != null) {
                            actualNoteCenterX_px = manualInputPoint.x;
                            actualNoteCenterY_px = manualInputPoint.y;
                        } else {
                            // 如果手动输入模式下还没有输入点，则显示等待提示
                            positionLabel.setText("NOTE位置: (等待输入)");
                            panel.repaint();
                            return;
                        }
                    }
                    
                    // 计算相对坐标，不进行吸附
                    int centerX = ((JPanel)e.getSource()).getWidth() / 2;
                    int centerY = ((JPanel)e.getSource()).getHeight() / 2;
                    
                    double gridX_raw = (actualNoteCenterX_px - centerX) / (double)CELL_SIZE;
                    double gridY_raw = (centerY - actualNoteCenterY_px) / (double)CELL_SIZE; // Y轴反向
                    
                    double finalGridX = gridX_raw;
                    double finalGridY = gridY_raw;

                    if (finalGridX == -0.0) finalGridX = 0.0;
                    if (finalGridY == -0.0) finalGridY = 0.0;

                    positionLabel.setText(String.format("NOTE位置: (%d, %d) (%s, %s)", 
                                                        actualNoteCenterX_px - centerX, actualNoteCenterY_px - centerY, 
                                                        formatCoordinateDisplay(finalGridX), formatCoordinateDisplay(finalGridY)));
                } else {
                    // 对于Execution和Flick类型，它们没有固定坐标
                    positionLabel.setText("NOTE位置: (特殊类型无坐标)");
                }
                
                panel.repaint();
            }
        });
        
        // 只在鼠标跟随模式下添加鼠标点击监听器
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Note clickedNote = findNoteAtPosition(e.getX(), e.getY(), panel);
                    showNoteContextMenu(panel, e.getX(), e.getY(), clickedNote);
                    return;
                }

                if (isContextMenuOpen) {
                    isContextMenuOpen = false;
                    return;
                }

                // 左键添加音符
                // 在网格模式下，只在鼠标跟随模式下才处理点击事件
                // 手动输入模式下通过"确认生成NOTE"按钮处理
                if (!isMouseFollowMode) {
                    return;
                }

                String direction = getDirection();
                boolean isGlowing = glowCheckBox.isSelected();

                if (currentNoteType == NoteType.EXECUTION ||
                    currentNoteType == NoteType.FLICK_LEFT ||
                    currentNoteType == NoteType.FLICK_RIGHT ||
                    currentNoteType == NoteType.FAKE_FLICK) {
                    if (currentNoteType == NoteType.EXECUTION) {
                        Note note = new Note(currentNoteType, musicPlayer.getCurrentTimeMicroseconds());
                        noteManager.addNote(note);
                        updateChartLogJson();
                    } else {
                        String flickDirection = (currentNoteType == NoteType.FLICK_LEFT) ? "left" : "right";
                        Note note = new Note(currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, flickDirection, isGlowing);
                        note.setTag(tagField.getText().trim());
                        noteManager.addNote(note);
                        updateChartLogJson();
                    }
                } else if (currentNoteType == NoteType.DOUBLE || currentNoteType == NoteType.FAKE_DOUBLE || currentNoteType == NoteType.MINE_DOUBLE) {
                    if (!isFirstDoublePlaced) {
                        // 放置第一个Double音符
                        firstDoublePoint = new Point(e.getX(), e.getY()); // 保存原始像素坐标
                        isFirstDoublePlaced = true;

                        // 显示动态提示
                        doubleNoteTipLabel.setText("放下第二个Double后生成铺面NOTE");
                        doubleNoteTipLabel.setVisible(true);

                        // 禁用Note类型切换和拍数选择
                        disableNoteTypeSelectionButtons(false); // 禁用Note类型选择按钮
                        disableBeatSelectionButtons(false); // disable all beat selection buttons
                        SwingUtilities.invokeLater(() -> {
                            // noteTypeToggleButton.repaint(); // 已在disableNoteTypeSelectionButtons中处理
                            if (beatsMenu != null) beatsMenu.repaint(); // 强制刷新菜单
                        });
                    } else {
                        // 放置第二个Double音符
                        Point secondDoublePoint = new Point(e.getX(), e.getY()); // 获取第二个点的原始像素坐标

                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;

                        double x1_final, y1_final, x2_final, y2_final;

                        if (showGrid) {
                            // 网格模式下，使用吸附后的网格点
                            Point nearestPoint1 = findNearestGridPoint(firstDoublePoint.x, firstDoublePoint.y);
                            Point nearestPoint2 = findNearestGridPoint(secondDoublePoint.x, secondDoublePoint.y);

                            double gridX1_raw = (nearestPoint1.x - centerX) / (double)CELL_SIZE;
                            double gridY1_raw = (centerY - nearestPoint1.y) / (double)CELL_SIZE;
                            double gridX2_raw = (nearestPoint2.x - centerX) / (double)CELL_SIZE;
                            double gridY2_raw = (centerY - nearestPoint2.y) / (double)CELL_SIZE;

                            // 保证输出为0.5倍数
                            x1_final = Math.round(gridX1_raw * 2.0) / 2.0;
                            y1_final = Math.round(gridY1_raw * 2.0) / 2.0;
                            x2_final = Math.round(gridX2_raw * 2.0) / 2.0;
                            y2_final = Math.round(gridY2_raw * 2.0) / 2.0;
                        } else {
                            // 无网格模式下，直接用鼠标真实坐标
                            double x1_raw = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                            double y1_raw = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                            double x2_raw = (secondDoublePoint.x - centerX) / (double)CELL_SIZE;
                            double y2_raw = (centerY - secondDoublePoint.y) / (double)CELL_SIZE;

                            // 限制范围 -3.0~3.0
                            x1_raw = Math.max(-3.0, Math.min(3.0, x1_raw));
                            y1_raw = Math.max(-3.0, Math.min(3.0, y1_raw));
                            x2_raw = Math.max(-3.0, Math.min(3.0, x2_raw));
                            y2_raw = Math.max(-3.0, Math.min(3.0, y2_raw));

                            // 保留两位小数
                            x1_final = Math.round(x1_raw * 100.0) / 100.0;
                            y1_final = Math.round(y1_raw * 100.0) / 100.0;
                            x2_final = Math.round(x2_raw * 100.0) / 100.0;
                            y2_final = Math.round(y2_raw * 100.0) / 100.0;
                        }

                        // 处理 -0.0
                        if (x1_final == -0.0) x1_final = 0.0;
                        if (y1_final == -0.0) y1_final = 0.0;
                        if (x2_final == -0.0) x2_final = 0.0;
                        if (y2_final == -0.0) y2_final = 0.0;

                        // 添加Double音符到noteManager并更新日志
                        Note doubleNote = new Note(x1_final, y1_final, x2_final, y2_final,
                            currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing);
                        doubleNote.setTag(tagField.getText().trim());
                        noteManager.addNote(doubleNote);
                        updateChartLogJson();

                        // 重置状态
                        isFirstDoublePlaced = false;
                        firstDoublePoint = null;
                        xCoordField.setText("");
                        yCoordField.setText("");
                        x2CoordField.setText("");
                        y2CoordField.setText("");

                        // 隐藏动态提示
                        doubleNoteTipLabel.setText("");
                        doubleNoteTipLabel.setVisible(false);

                        // 启用Note类型切换和拍数选择
                        disableNoteTypeSelectionButtons(true); // 启用Note类型选择按钮
                        disableBeatSelectionButtons(true); // enable all beat selection buttons
                        SwingUtilities.invokeLater(() -> {
                            // noteTypeToggleButton.repaint(); // 已在disableNoteTypeSelectionButtons中处理
                            if (beatsMenu != null) beatsMenu.repaint(); // 强制刷新菜单
                        });
                    }
                } else { // Handles TAP, HOLD, CHAIN, DRAG
                    if (showGrid) {
                        // 网格模式下，使用吸附后的网格点
                        Point nearestPoint = findNearestGridPoint(e.getX(), e.getY());
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        double gridX_raw = (nearestPoint.x - centerX) / (double)CELL_SIZE;
                        double gridY_raw = (centerY - nearestPoint.y) / (double)CELL_SIZE;
                        // 保证输出为0.5倍数
                        double finalGridX = Math.round(gridX_raw * 2.0) / 2.0;
                        double finalGridY = Math.round(gridY_raw * 2.0) / 2.0;
                        if (finalGridX == -0.0) finalGridX = 0.0;
                        if (finalGridY == -0.0) finalGridY = 0.0;
                        // 添加NOTE到noteManager
                        Note singleNote = new Note(finalGridX, finalGridY, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing);
                        singleNote.setTag(tagField.getText().trim());
                        noteManager.addNote(singleNote);
                        updateChartLogJson();
                        panel.repaint();
                    } else {
                        // 无网格模式下，直接用鼠标真实坐标
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        double x = (e.getX() - centerX) / (double)CELL_SIZE;
                        double y = (centerY - e.getY()) / (double)CELL_SIZE;
                        // 限制范围 -3.0~3.0
                        x = Math.max(-3.0, Math.min(3.0, x));
                        y = Math.max(-3.0, Math.min(3.0, y));
                        // 保留两位小数
                        x = Math.round(x * 100.0) / 100.0;
                        y = Math.round(y * 100.0) / 100.0;
                        if (x == -0.0) x = 0.0;
                        if (y == -0.0) y = 0.0;
                        // 添加NOTE到noteManager
                        Note freeNote = new Note(x, y, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing);
                        freeNote.setTag(tagField.getText().trim());
                        noteManager.addNote(freeNote);
                        updateChartLogJson();
                        panel.repaint();
                    }
                }
                SwingUtilities.invokeLater(() -> requestFocusInWindow()); 
            }
        });
        
        return panel;
    }
    
    /**
     * 创建歌曲信息面板
     */
    private void createSongInfoPanel() {
        // 创建主面板
        songInfoPanel = new JPanel();
        songInfoPanel.setLayout(null);
        // songInfoPanel的setBounds将在adjustPanelPositions中设置
        
        // 创建折叠按钮
        toggleButton = new JToggleButton("歌曲信息 ▼");
        toggleButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        toggleButton.setBounds(0, 0, 250, 30);
        toggleButton.addActionListener(e -> toggleSongInfo());
        
        // 创建卡片布局
        cardLayout = new CardLayout();
        expandedPanel = new JPanel();
        expandedPanel.setLayout(null);
        expandedPanel.setBounds(0, 30, 250, 280); // 增加高度以容纳更多字段
        
        // 创建信息输入框
        int yOffset = 10;
        int labelWidth = 50;
        int fieldWidth = 170;
        int fieldHeight = 20;
        int verticalSpacing = 30;
        
        // 歌曲名
        JLabel songNameLabel = new JLabel("歌曲名:");
        songNameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        songNameLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        songNameField = new JTextField();
        songNameField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // 曲师
        JLabel composerLabel = new JLabel("曲师:");
        composerLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        composerLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        composerField = new JTextField();
        composerField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // 谱师
        JLabel chartAuthorLabel = new JLabel("谱师:");
        chartAuthorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        chartAuthorLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        chartAuthorField = new JTextField();
        chartAuthorField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // 难度名称
        JLabel difficultyLabel = new JLabel("难度:");
        difficultyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        difficultyField = new JTextField();
        difficultyField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;

        // 难度等级
        JLabel difficultyLevelLabel = new JLabel("等级:");
        difficultyLevelLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyLevelLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        difficultyLevelField = new JTextField("1");
        difficultyLevelField.setBounds(70, yOffset, 50, fieldHeight);
        difficultyLevelField.setToolTipText("1-15");
        // 难度颜色（同一行右侧）
        JLabel difficultyColorLabel = new JLabel("颜色:");
        difficultyColorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyColorLabel.setBounds(130, yOffset, 35, fieldHeight);
        difficultyColorCombo = new JComboBox<>(new String[]{
            "&b (AQUA)", "&a (GREEN)", "&c (RED)", "&e (YELLOW)",
            "&5 (PURPLE)", "&6 (GOLD)", "&f (WHITE)", "&7 (GRAY)"
        });
        difficultyColorCombo.setBounds(165, yOffset, fieldWidth - 95, fieldHeight);
        difficultyColorCombo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        yOffset += verticalSpacing;
        
        // 时长
        JLabel durationLabel = new JLabel("时长:");
        durationLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        durationLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        durationField = new JTextField();
        durationField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // 偏移
        JLabel offsetLabel = new JLabel("偏移:");
        offsetLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        offsetLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        offsetField = new JTextField();
        offsetField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // BPM
        JLabel bpmLabel = new JLabel("BPM:");
        bpmLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        bpmLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        bpmField = new JTextField();
        bpmField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        // 为BPM字段添加DocumentListener以实时更新和记录日志
        bpmField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateBpmFromField();
            }
            public void removeUpdate(DocumentEvent e) {
                updateBpmFromField();
            }
            public void insertUpdate(DocumentEvent e) {
                updateBpmFromField();
            }

            private void updateBpmFromField() {
                String bpmText = bpmField.getText().trim();
                if (bpmText.isEmpty()) {
                    beatCalculator.setBpm(BeatCalculator.DEFAULT_BPM);
                    logManager.log("BPM字段为空，已重置为默认BPM: " + BeatCalculator.DEFAULT_BPM);
                    return;
                }
                try {
                    double newBpm = Double.parseDouble(bpmText);
                    if (newBpm > 0) {
                        if (beatCalculator.getBpm() != newBpm) { // 避免重复日志
                            beatCalculator.setBpm(newBpm);
                            logManager.log("BPM已更新为: " + newBpm);
                        }
                    } else {
                        beatCalculator.setBpm(BeatCalculator.DEFAULT_BPM);
                        logManager.log("警告: BPM必须大于0，已使用默认BPM: " + BeatCalculator.DEFAULT_BPM);
                    }
                } catch (NumberFormatException ex) {
                    beatCalculator.setBpm(BeatCalculator.DEFAULT_BPM);
                    logManager.log("错误: BPM输入无效，已重置为默认BPM: " + BeatCalculator.DEFAULT_BPM);
                }
            }
        });
        yOffset += verticalSpacing;
        
        // 作者
        JLabel authorLabel = new JLabel("作者:");
        authorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        authorLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        authorField = new JTextField();
        authorField.setBounds(70, yOffset, fieldWidth, fieldHeight);
        yOffset += verticalSpacing;
        
        // 添加按钮
        JButton saveButton = new JButton("保存信息");
        saveButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        saveButton.setBounds(70, yOffset, fieldWidth/2 - 5, 25);
        saveButton.addActionListener(e -> {
            saveSongInfo();
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 保存信息后重新获取焦点
        });
        
        JButton clearButton = new JButton("清空信息");
        clearButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearButton.setBounds(70 + fieldWidth/2 + 5, yOffset, fieldWidth/2 - 5, 25);
        clearButton.addActionListener(e -> {
            clearSongInfo();
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 清空信息后重新获取焦点
        });
        
        // 为offsetField添加DocumentListener
        offsetField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOffsetFromField();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOffsetFromField();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOffsetFromField();
            }

            private void updateOffsetFromField() {
                String offsetText = offsetField.getText().trim();
                if (offsetText.isEmpty()) {
                    musicPlayer.seek(0); // 偏移归零时强制clip归零
                    return;
                }
                try {
                    double offsetMs = Double.parseDouble(offsetText);
                    long offsetMicroseconds = (long) (offsetMs * 1000); // ms转微秒
                    musicPlayer.seek(musicPlayer.getCurrentTimeMicroseconds());
                    // 新增：如果偏移为0，强制clip物理位置归零
                    if (offsetMicroseconds == 0) {
                        musicPlayer.seek(0);
                    }
                    // 偏移变化时刷新总时长显示
                    totalTimeLabel.setText("/ " + beatCalculator.formatTime(musicPlayer.getActualClipLengthMicroseconds()));
                } catch (NumberFormatException ex) {
                    // 不再输出日志
                }
            }
        });
        
        // 添加组件到展开面板
        expandedPanel.add(songNameLabel);
        expandedPanel.add(songNameField);
        expandedPanel.add(composerLabel);
        expandedPanel.add(composerField);
        expandedPanel.add(chartAuthorLabel);
        expandedPanel.add(chartAuthorField);
        expandedPanel.add(difficultyLabel);
        expandedPanel.add(difficultyField);
        expandedPanel.add(difficultyLevelLabel);
        expandedPanel.add(difficultyLevelField);
        expandedPanel.add(difficultyColorLabel);
        expandedPanel.add(difficultyColorCombo);
        expandedPanel.add(durationLabel);
        expandedPanel.add(durationField);
        expandedPanel.add(offsetLabel);
        expandedPanel.add(offsetField);
        expandedPanel.add(bpmLabel);
        expandedPanel.add(bpmField);
        expandedPanel.add(authorLabel);
        expandedPanel.add(authorField);
        expandedPanel.add(saveButton);
        expandedPanel.add(clearButton);
        
        // 添加组件到主面板
        songInfoPanel.add(toggleButton);
        songInfoPanel.add(expandedPanel);
        
        // 初始状态为折叠
        expandedPanel.setVisible(false);
    }
    
    /**
     * 切换歌曲信息面板的展开/折叠状态
     */
    private void toggleSongInfo() {
        boolean isExpanded = toggleButton.isSelected();
        expandedPanel.setVisible(isExpanded);
        toggleButton.setText(isExpanded ? "歌曲信息 ▲" : "歌曲信息 ▼");
        adjustPanelPositions();
        requestFocusInWindow(); // 切换歌曲信息面板后重新获取焦点
    }
    
    /**
     * 调整后续面板（如NOTE属性面板）的位置
     */
    private void adjustPanelPositions() {
        // confirmButton的底部Y坐标
        int basePanelY = confirmButton.getY() + confirmButton.getHeight() + 40; // 增加间距

        // 歌曲信息面板的高度
        int songInfoPanelCalculatedHeight = toggleButton.isSelected() ? 310 : 30;
        songInfoPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, basePanelY, 250, songInfoPanelCalculatedHeight);

        // 计算NOTE属性面板的起始Y坐标，使用计算出的songInfoPanelCalculatedHeight
        int notePropertyPanelY = basePanelY + songInfoPanelCalculatedHeight + 10; // 留出10px间距

        // NOTE属性面板的高度
        int notePropertyPanelCalculatedHeight = notePropertyToggleButton.isSelected() ? 230 : 30;
        notePropertyPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, notePropertyPanelY, 250, notePropertyPanelCalculatedHeight);

        // 调整主面板大小以容纳所有组件
        // 考虑新增的音乐进度条和时间显示部分
        int musicControlsBottomY = timeSlider.getY() + timeSlider.getHeight() + 30; // 进度条下方30px
        int contentBottomY = Math.max(notePropertyPanelY + notePropertyPanelCalculatedHeight, musicControlsBottomY);
        mainPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), contentBottomY + 20));
        relayoutEditorColumns();
        mainPanel.revalidate();
        mainPanel.repaint();
        this.requestFocusInWindow(); // 确保主窗口在布局调整后重新获取焦点
    }

    private void relayoutEditorColumns() {
        if (chartEditorContainer == null || logScrollPane == null || chartLogButtonPanel == null) return;
        int editorX = LEFT_MARGIN + GRID_PANEL_SIZE + 325;
        int editorY = 50;
        int editorH = GRID_PANEL_SIZE;
        int handleW = 6;
        int logW = 275;

        chartEditorContainer.setBounds(editorX, editorY, jsonEditorWidth, editorH);
        // 分隔条紧贴编辑区右侧
        // dragHandle is the 3rd child after songInfoPanel in add order; find by name not reliable in null layout
        // iterate mainPanel children to find the drag handle by cursor
        for (Component c : mainPanel.getComponents()) {
            if (c.getCursor().getType() == Cursor.E_RESIZE_CURSOR && c != chartEditorContainer) {
                c.setBounds(editorX + jsonEditorWidth, editorY, handleW, editorH);
                break;
            }
        }
        int logX = editorX + jsonEditorWidth + handleW;
        logScrollPane.setBounds(logX, editorY, logW, editorH);
        chartLogButtonPanel.setBounds(editorX, editorY + editorH + 10, jsonEditorWidth, 60);

        // 右侧第一列随编辑区宽度动态偏移（gap=19固定）
        int rightColX = logX + logW + 19;
        if (modeSwitchButton != null) modeSwitchButton.setLocation(rightColX, modeSwitchButton.getY());
        if (xLabel != null) xLabel.setLocation(rightColX, xLabel.getY());
        if (xCoordField != null) xCoordField.setLocation(rightColX + 60, xCoordField.getY());
        if (yLabel != null) yLabel.setLocation(rightColX, yLabel.getY());
        if (yCoordField != null) yCoordField.setLocation(rightColX + 60, yCoordField.getY());
        if (positionLabel != null) positionLabel.setLocation(rightColX, positionLabel.getY());
        if (doubleNoteTipLabel != null) doubleNoteTipLabel.setLocation(rightColX, doubleNoteTipLabel.getY());
        if (x2Label != null) x2Label.setLocation(rightColX, x2Label.getY());
        if (x2CoordField != null) x2CoordField.setLocation(rightColX + 60, x2CoordField.getY());
        if (y2Label != null) y2Label.setLocation(rightColX, y2Label.getY());
        if (y2CoordField != null) y2CoordField.setLocation(rightColX + 60, y2CoordField.getY());
        if (confirmButton != null) confirmButton.setLocation(rightColX, confirmButton.getY());
        if (songInfoPanel != null) songInfoPanel.setLocation(rightColX, songInfoPanel.getY());
        if (notePropertyPanel != null) notePropertyPanel.setLocation(rightColX, notePropertyPanel.getY());

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    /**
     * 保存歌曲信息
     */
    private void saveSongInfo() {
        try {
            // 验证BPM (已移至DocumentListener实时处理，这里不再需要验证)
            
            // 验证时长（格式：分:秒）
            String durationText = durationField.getText().trim();
            if (!durationText.isEmpty()) {
                if (!durationText.matches("^\\d+:\\d{2}$")) {
                    logManager.log("错误: 时长格式必须为 分:秒（例如：3:45）");
                    durationField.setText("");
                    return;
                }
            }
            
            // 验证偏移（必须是数字，已移至DocumentListener实时处理，这里不再需要验证）
            // String offsetText = offsetField.getText().trim();
            // if (!offsetText.isEmpty()) {
            //     try {
            //         Double.parseDouble(offsetText);
            //     } catch (NumberFormatException e) {
            //         log("错误: 偏移必须是数字");
            //         offsetField.setText("");
            //         return;
            //     }
            // }
            
            // 验证难度（必须是1-15的数字）
            String difficultyText = difficultyField.getText().trim();
            if (!difficultyText.isEmpty()) {
                try {
                    int diff = Integer.parseInt(difficultyText);
                    if (diff < 1 || diff > 15) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    logManager.log("错误: 难度必须是1-15之间的整数");
                    difficultyField.setText("");
                    return;
                }
            }
            
            // 保存所有字段
            setSongName(songNameField.getText().trim());
            setComposer(composerField.getText().trim());
            setChartAuthor(chartAuthorField.getText().trim());
            setDifficulty(difficultyText);
            setDuration(durationText);
            setOffset(offsetField.getText().trim());
            setBpm(bpmField.getText().trim());
            setAuthor(authorField.getText().trim());
            
            if (!songName.isEmpty() || !composer.isEmpty() || !chartAuthor.isEmpty() || 
                !difficulty.isEmpty() || !duration.isEmpty() || !offset.isEmpty() || 
                !bpm.isEmpty() || !author.isEmpty()) {
                StringBuilder info = new StringBuilder("保存歌曲信息:\n");
                if (!songName.isEmpty()) info.append("歌曲名: ").append(songName).append("\n");
                if (!composer.isEmpty()) info.append("曲师: ").append(composer).append("\n");
                if (!chartAuthor.isEmpty()) info.append("谱师: ").append(chartAuthor).append("\n");
                if (!difficulty.isEmpty()) info.append("难度: ").append(difficulty).append("\n");
                if (!duration.isEmpty()) info.append("时长: ").append(duration).append("\n");
                if (!offset.isEmpty()) {
                    try {
                        long offsetMs = Long.parseLong(offset);
                        info.append("偏移: ").append(offsetMs).append(" ms\n");
                    } catch (NumberFormatException e) {
                        info.append("偏移: (无效)\n");
                    }
                }
                if (!bpm.isEmpty()) info.append("BPM: ").append(bpm).append("\n");
                if (!author.isEmpty()) info.append("作者: ").append(author);
                logManager.log(info.toString());
            } else {
                logManager.log("错误: 请至少填写一项信息");
            }
        } catch (Exception e) {
            logManager.log("错误: " + e.getMessage());
        }
    }
    
    /**
     * 清空所有歌曲信息
     */
    private void clearSongInfo() {
        songNameField.setText("");
        composerField.setText("");
        chartAuthorField.setText("");
        difficultyField.setText("");
        durationField.setText("");
        offsetField.setText("");
        bpmField.setText("");
        authorField.setText("");
        
        // 清空全局变量
        setSongName("");
        setComposer("");
        setChartAuthor("");
        setDifficulty("");
        setDuration("");
        setOffset("");
        setBpm("");
        setAuthor("");
        
        // 在这里添加停止音乐播放器的逻辑
        musicPlayer.close();
        resetMusicUI();
        logManager.log("已清空所有歌曲信息并停止音乐播放");
    }
    
    /**
     * 创建NOTE属性设置面板
     */
    private void createNotePropertyPanel() {
        // 创建主面板
        notePropertyPanel = new JPanel();
        notePropertyPanel.setLayout(null);
        // notePropertyPanel的setBounds将在adjustPanelPositions中设置
        
        // 创建折叠按钮
        notePropertyToggleButton = new JToggleButton("NOTE属性设置 ▼");
        notePropertyToggleButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        notePropertyToggleButton.setBounds(0, 0, 250, 30);
        notePropertyToggleButton.addActionListener(e -> toggleNoteProperty());
        
        // 创建展开面板
        notePropertyExpandedPanel = new JPanel();
        notePropertyExpandedPanel.setLayout(null);
        notePropertyExpandedPanel.setBounds(0, 30, 250, 200);
        
        // 创建朝向设置
        directionToggleButton = new JToggleButton("朝向设置 ▼");
        directionToggleButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        directionToggleButton.setBounds(10, 10, 230, 25);
        directionToggleButton.addActionListener(e -> toggleDirection());
        
        // 创建朝向展开面板
        directionExpandedPanel = new JPanel();
        directionExpandedPanel.setLayout(new GridLayout(2, 2, 5, 5));
        directionExpandedPanel.setBounds(10, 40, 230, 60);
        
        // 创建朝向选项
        ButtonGroup directionGroup = new ButtonGroup();
        directionW = new JRadioButton("w");
        directionA = new JRadioButton("a");
        directionS = new JRadioButton("s");
        directionD = new JRadioButton("d");
        
        // 设置默认选中w
        directionW.setSelected(true);
        
        directionGroup.add(directionW);
        directionGroup.add(directionA);
        directionGroup.add(directionS);
        directionGroup.add(directionD);
        
        // 添加朝向单选按钮的动作监听器，用于记录日志
        directionW.addActionListener(e -> {
            logManager.log("切换NOTE朝向: w");
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 切换朝向后重新获取焦点
        });
        directionA.addActionListener(e -> {
            logManager.log("切换NOTE朝向: a");
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 切换朝向后重新获取焦点
        });
        directionS.addActionListener(e -> {
            logManager.log("切换NOTE朝向: s");
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 切换朝向后重新获取焦点
        });
        directionD.addActionListener(e -> {
            logManager.log("切换NOTE朝向: d");
            SwingUtilities.invokeLater(this::requestFocusInWindow); // 切换朝向后重新获取焦点
        });

        directionExpandedPanel.add(directionW);
        directionExpandedPanel.add(directionA);
        directionExpandedPanel.add(directionS);
        directionExpandedPanel.add(directionD);
        
        // 创建发光选项
        glowCheckBox = new JCheckBox("发光");
        glowCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        glowCheckBox.setBounds(10, 110, 100, 25);
        glowCheckBox.addActionListener(e -> {
            logManager.log("发光选项切换: " + glowCheckBox.isSelected());
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        });

        // 创建Tag输入
        JLabel tagLabel = new JLabel("Tags:");
        tagLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        tagLabel.setBounds(10, 140, 40, 25);
        tagField = new JTextField();
        tagField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        tagField.setBounds(50, 140, 190, 25);
        tagField.setToolTipText("多标签用逗号分隔，如: spiral, wave, glow");

        // 添加组件到展开面板
        notePropertyExpandedPanel.add(directionToggleButton);
        notePropertyExpandedPanel.add(directionExpandedPanel);
        notePropertyExpandedPanel.add(glowCheckBox);
        notePropertyExpandedPanel.add(tagLabel);
        notePropertyExpandedPanel.add(tagField);
        
        // 添加组件到主面板
        notePropertyPanel.add(notePropertyToggleButton);
        notePropertyPanel.add(notePropertyExpandedPanel);
        
        // 初始状态为折叠
        notePropertyExpandedPanel.setVisible(false);
        directionExpandedPanel.setVisible(false);
    }
    
    /**
     * 切换NOTE属性面板的展开/折叠状态
     */
    private void toggleNoteProperty() {
        boolean isExpanded = notePropertyToggleButton.isSelected();
        notePropertyExpandedPanel.setVisible(isExpanded);
        notePropertyToggleButton.setText(isExpanded ? "NOTE属性设置 ▲" : "NOTE属性设置 ▼");
        adjustPanelPositions(); // 调整后续面板位置
        notePropertyExpandedPanel.revalidate(); // 强制刷新内部组件布局
        notePropertyExpandedPanel.repaint();    // 强制重绘内部组件
        requestFocusInWindow(); // 切换NOTE属性面板后重新获取焦点
    }
    
    /**
     * 切换朝向设置的展开/折叠状态
     */
    private void toggleDirection() {
        boolean isExpanded = directionToggleButton.isSelected();
        directionExpandedPanel.setVisible(isExpanded);
        directionToggleButton.setText(isExpanded ? "朝向设置 ▲" : "朝向设置 ▼");
        requestFocusInWindow(); // 切换朝向设置后重新获取焦点
    }
    
    /**
     * 获取当前选择的朝向
     */
    private String getDirection() {
        String direction;
        if (directionW.isSelected()) direction = "w";
        else if (directionA.isSelected()) direction = "a";
        else if (directionS.isSelected()) direction = "s";
        else direction = "d";
        return direction;
    }
    
    /**
     * 切换网格显示模式
     */
    private void toggleGridDisplay() {
        showGrid = !showGrid;
        gridPanel.setVisible(showGrid);
        noGridPanel.setVisible(!showGrid);
        logManager.log("切换网格显示: " + (showGrid ? "开启" : "关闭"));
    }
    
    /**
     * 创建网格面板
     */
    private JPanel createGridPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制边框
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                
                // 绘制网格线
                g.setColor(Color.GRAY);
                for (int i = 1; i < GRID_SIZE; i++) {
                    // 绘制垂直线
                    g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, getHeight());
                    // 绘制水平线
                    g.drawLine(0, i * CELL_SIZE, getWidth(), i * CELL_SIZE);
                }
                
                // 绘制中心原点
                g.setColor(Color.RED);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                g.fillOval(centerX - 3, centerY - 3, 6, 6);
                
                // 绘制刻度
                g.setColor(Color.BLACK);
                g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
                
                // 绘制水平刻度（上方）
                for (int i = -3; i <= 3; i++) {
                    int x = centerX + i * CELL_SIZE;
                    g.drawLine(x, 0, x, 5);
                    g.drawString(String.valueOf(i), x - 5, 20);
                }
                
                // 绘制垂直刻度（左侧）
                for (int i = -3; i <= 3; i++) {
                    int y = centerY - i * CELL_SIZE;
                    g.drawLine(0, y, 5, y);
                    g.drawString(String.valueOf(i), 10, y + 5);
                }
                
                
                // ---------- 伪3D NOTE动画显示 ----------
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                if (musicPlayer != null && beatCalculator != null && noteManager != null) {
                    long currentTime = musicPlayer.getCurrentTimeMicroseconds() + getCurrentOffsetMicroseconds();
                    double currentBeat = beatCalculator.microsecondsToBeats(currentTime);
                    java.util.List<Note> notesToShow = noteManager.getNotesForCurrentBeat(currentTime, displayBeatsCount, beatCalculator);
                    centerX = getWidth() / 2;
                    centerY = getHeight() / 2;
                    int n = displayBeatsCount - 1;
                    Composite defaultComposite = g2.getComposite();
                    for (Note note : notesToShow) {
                        double noteBeat = Math.round(beatCalculator.microsecondsToBeats(note.getTimeMicroseconds()));
                        double k = noteBeat - currentBeat;
                        if (k < 0 || k > n) continue;
                        double progress = (n == 0) ? 1.0 : (n - k) / n;
                        float alphaF = (float)(1.0 - (k / n) * (1.0 - 80.0/255));
                        alphaF = Math.max(0f, Math.min(1f, alphaF));
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaF));

                        int positionsToRender = note.isDouble() ? 2 : 1;
                        for (int pos = 0; pos < positionsToRender; pos++) {
                            double x = (pos == 0 ? note.getX() : note.getX2()) * 100;
                            double y = -(pos == 0 ? note.getY() : note.getY2()) * 100;
                            double x0 = x / 5.0, y0 = y / 5.0;
                            double cx = x0 + (x - x0) * progress;
                            double cy = y0 + (y - y0) * progress;
                            double size = 20 + 80 * progress;
                            int rx = (int)(centerX + cx - size/2);
                            int ry = (int)(centerY + cy - size/2);
                            int rs = (int)size;
                            g2.setColor(note.getColor());
                            g2.fillRect(rx, ry, rs, rs);
                            if (note.isFake()) drawCheckerboard(g2, rx, ry, rs, rs, (int)(alphaF * 255));
                            if (note.isMine()) drawCrossPattern(g2, rx, ry, rs, rs, (int)(alphaF * 255));
                            Color borderColor = switch (note.getDirection() != null ? note.getDirection() : "w") {
                                case "a" -> NOTE_BORDER_A;
                                case "s" -> NOTE_BORDER_S;
                                case "d" -> NOTE_BORDER_D;
                                default -> NOTE_BORDER_W;
                            };
                            g2.setStroke(NOTE_BORDER_STROKE);
                            g2.setColor(borderColor);
                            g2.drawRect(rx, ry, rs, rs);
                            if (note.isGlowing()) {
                                g2.setColor(NOTE_GLOW_COLOR);
                                g2.setStroke(NOTE_GLOW_STROKE);
                                int px = (int)(centerX + cx), py = (int)(centerY + cy);
                                g2.drawLine(px-2, py, px+2, py);
                                g2.drawLine(px, py-2, px, py+2);
                            }
                        }
                    }
                    g2.setComposite(defaultComposite);
                    // Execution action 可视化
                    renderExecutionActions(g, notesToShow, centerX, centerY, currentBeat, displayBeatsCount, getWidth());
                }
                // 仅在网格模式下显示吸附后的NOTE，不显示跟随鼠标的NOTE
                if (isMouseFollowMode && currentNoteType != NoteType.EXECUTION &&
                    currentNoteType != NoteType.FLICK_LEFT && currentNoteType != NoteType.FLICK_RIGHT &&
                    currentNoteType != NoteType.FAKE_FLICK) {
                    Graphics2D g2p = (Graphics2D) g;
                    Composite prev = g2p.getComposite();
                    g2p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 180f/255f));
                    g2p.setColor(currentNoteType.getColor());
                    Point nearestPoint = findNearestGridPoint(mousePosition.x, mousePosition.y);
                    int squareX = nearestPoint.x - FOLLOW_SQUARE_SIZE / 2 - 19;
                    int squareY = nearestPoint.y - FOLLOW_SQUARE_SIZE / 2 - 25;
                    g2p.fillRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                    g2p.setComposite(prev);
                    g2p.setColor(currentNoteType.getColor());
                    g2p.drawRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                }
            }
        };
        panel.setLayout(null);
        panel.setBackground(new Color(224, 224, 224)); // 浅灰色，保证白色边框可见
        
        // 添加鼠标移动监听器
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition.setLocation(e.getX(), e.getY());
                
                // 更新位置显示标签
                if (currentNoteType != NoteType.EXECUTION && 
                    currentNoteType != NoteType.FLICK_LEFT &&
                    currentNoteType != NoteType.FLICK_RIGHT &&
                    currentNoteType != NoteType.FAKE_FLICK) {
                    
                    // 计算最近的网格点
                    Point nearestPoint = findNearestGridPoint(e.getX(), e.getY());
                    
                    // 将像素坐标转换为网格坐标（0.5单位）
                    int centerX = ((JPanel)e.getSource()).getWidth() / 2;
                    int centerY = ((JPanel)e.getSource()).getHeight() / 2;
                    
                    double gridX_raw = (nearestPoint.x - centerX) / (double)CELL_SIZE;
                    double gridY_raw = (centerY - nearestPoint.y) / (double)CELL_SIZE;
                    
                    // 确保输出是0.5的倍数且格式正确，并处理负零
                    double finalGridX = Math.round(gridX_raw * 2.0) / 2.0;
                    double finalGridY = Math.round(gridY_raw * 2.0) / 2.0;

                    if (finalGridX == -0.0) finalGridX = 0.0;
                    if (finalGridY == -0.0) finalGridY = 0.0;

                    positionLabel.setText(String.format("NOTE位置: (%d, %d) (%s, %s)",
                                                        nearestPoint.x - centerX - 19, nearestPoint.y - centerY - 25,
                                                        formatCoordinateDisplay(finalGridX), formatCoordinateDisplay(finalGridY)));
                } else {
                    // 对于Execution和Flick类型，它们没有固定坐标
                    positionLabel.setText("NOTE位置: (特殊类型无坐标)");
                }
                
                panel.repaint();
            }
        });
        
        // 添加鼠标点击监听器
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Note clickedNote = findNoteAtPosition(e.getX(), e.getY(), panel);
                    showNoteContextMenu(panel, e.getX(), e.getY(), clickedNote);
                    return;
                }

                if (isContextMenuOpen) {
                    isContextMenuOpen = false;
                    return;
                }

                // 左键添加音符
                // 在网格模式下，只在鼠标跟随模式下才处理点击事件
                // 手动输入模式下通过"确认生成NOTE"按钮处理
                if (!isMouseFollowMode) {
                    return;
                }

                String direction = getDirection();
                boolean isGlowing = glowCheckBox.isSelected();

                if (currentNoteType == NoteType.EXECUTION ||
                    currentNoteType == NoteType.FLICK_LEFT ||
                    currentNoteType == NoteType.FLICK_RIGHT ||
                    currentNoteType == NoteType.FAKE_FLICK) {
                    if (currentNoteType == NoteType.EXECUTION) {
                        Note note = new Note(currentNoteType, musicPlayer.getCurrentTimeMicroseconds());
                        noteManager.addNote(note);
                        updateChartLogJson();
                    } else {
                        String flickDirection = (currentNoteType == NoteType.FLICK_LEFT) ? "left" : "right";
                        Note note = new Note(currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, flickDirection, isGlowing);
                        note.setTag(tagField.getText().trim());
                        noteManager.addNote(note);
                        updateChartLogJson();
                    }
                } else if (currentNoteType == NoteType.DOUBLE || currentNoteType == NoteType.FAKE_DOUBLE || currentNoteType == NoteType.MINE_DOUBLE) {
                    if (!isFirstDoublePlaced) {
                        // 放置第一个Double音符
                        firstDoublePoint = new Point(e.getX(), e.getY()); // 保存原始像素坐标
                        isFirstDoublePlaced = true;

                        // 显示动态提示
                        doubleNoteTipLabel.setText("放下第二个Double后生成铺面NOTE");
                        doubleNoteTipLabel.setVisible(true);

                        // 禁用Note类型切换和拍数选择
                        disableNoteTypeSelectionButtons(false); // 禁用Note类型选择按钮
                        disableBeatSelectionButtons(false); // disable all beat selection buttons
                        SwingUtilities.invokeLater(() -> {
                            // noteTypeToggleButton.repaint(); // 已在disableNoteTypeSelectionButtons中处理
                            if (beatsMenu != null) beatsMenu.repaint(); // 强制刷新菜单
                        });
                    } else {
                        // 放置第二个Double音符
                        Point secondDoublePoint = new Point(e.getX(), e.getY()); // 获取第二个点的原始像素坐标

                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;

                        double x1_final, y1_final, x2_final, y2_final;

                        if (showGrid) {
                            // 网格模式下，使用吸附后的网格点
                            Point nearestPoint1 = findNearestGridPoint(firstDoublePoint.x, firstDoublePoint.y);
                            Point nearestPoint2 = findNearestGridPoint(secondDoublePoint.x, secondDoublePoint.y);

                            double gridX1_raw = (nearestPoint1.x - centerX) / (double)CELL_SIZE;
                            double gridY1_raw = (centerY - nearestPoint1.y) / (double)CELL_SIZE;
                            double gridX2_raw = (nearestPoint2.x - centerX) / (double)CELL_SIZE;
                            double gridY2_raw = (centerY - nearestPoint2.y) / (double)CELL_SIZE;

                            // 保证输出为0.5倍数
                            x1_final = Math.round(gridX1_raw * 2.0) / 2.0;
                            y1_final = Math.round(gridY1_raw * 2.0) / 2.0;
                            x2_final = Math.round(gridX2_raw * 2.0) / 2.0;
                            y2_final = Math.round(gridY2_raw * 2.0) / 2.0;
                        } else {
                            // 无网格模式下，直接用鼠标真实坐标
                            double x1_raw = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                            double y1_raw = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                            double x2_raw = (secondDoublePoint.x - centerX) / (double)CELL_SIZE;
                            double y2_raw = (centerY - secondDoublePoint.y) / (double)CELL_SIZE;

                            // 限制范围 -3.0~3.0
                            x1_raw = Math.max(-3.0, Math.min(3.0, x1_raw));
                            y1_raw = Math.max(-3.0, Math.min(3.0, y1_raw));
                            x2_raw = Math.max(-3.0, Math.min(3.0, x2_raw));
                            y2_raw = Math.max(-3.0, Math.min(3.0, y2_raw));

                            // 保留两位小数
                            x1_final = Math.round(x1_raw * 100.0) / 100.0;
                            y1_final = Math.round(y1_raw * 100.0) / 100.0;
                            x2_final = Math.round(x2_raw * 100.0) / 100.0;
                            y2_final = Math.round(y2_raw * 100.0) / 100.0;
                        }

                        // 处理 -0.0
                        if (x1_final == -0.0) x1_final = 0.0;
                        if (y1_final == -0.0) y1_final = 0.0;
                        if (x2_final == -0.0) x2_final = 0.0;
                        if (y2_final == -0.0) y2_final = 0.0;

                        // 添加Double音符到noteManager并更新日志
                        Note doubleNote = new Note(x1_final, y1_final, x2_final, y2_final,
                            currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing);
                        doubleNote.setTag(tagField.getText().trim());
                        noteManager.addNote(doubleNote);
                        updateChartLogJson();

                        // 重置状态
                        isFirstDoublePlaced = false;
                        firstDoublePoint = null;
                        xCoordField.setText("");
                        yCoordField.setText("");
                        x2CoordField.setText("");
                        y2CoordField.setText("");

                        // 隐藏动态提示
                        doubleNoteTipLabel.setText("");
                        doubleNoteTipLabel.setVisible(false);

                        // 启用Note类型切换和拍数选择
                        disableNoteTypeSelectionButtons(true); // 启用Note类型选择按钮
                        disableBeatSelectionButtons(true); // enable all beat selection buttons
                        SwingUtilities.invokeLater(() -> {
                            // noteTypeToggleButton.repaint(); // 已在disableNoteTypeSelectionButtons中处理
                            if (beatsMenu != null) beatsMenu.repaint(); // 强制刷新菜单
                        });
                    }
                } else { // Handles TAP, HOLD, CHAIN, DRAG
                    if (showGrid) {
                        // 网格模式下，使用吸附后的网格点
                        Point nearestPoint = findNearestGridPoint(e.getX(), e.getY());
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        double gridX_raw = (nearestPoint.x - centerX) / (double)CELL_SIZE;
                        double gridY_raw = (centerY - nearestPoint.y) / (double)CELL_SIZE;
                        // 保证输出为0.5倍数
                        double finalGridX = Math.round(gridX_raw * 2.0) / 2.0;
                        double finalGridY = Math.round(gridY_raw * 2.0) / 2.0;
                        if (finalGridX == -0.0) finalGridX = 0.0;
                        if (finalGridY == -0.0) finalGridY = 0.0;
                        // 添加NOTE到noteManager
                        noteManager.addNote(new Note(finalGridX, finalGridY, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing));
                    } else {
                        // 无网格模式下，直接用鼠标真实坐标
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        double x = (e.getX() - centerX) / (double)CELL_SIZE;
                        double y = (centerY - e.getY()) / (double)CELL_SIZE;
                        // 限制范围 -3.0~3.0
                        x = Math.max(-3.0, Math.min(3.0, x));
                        y = Math.max(-3.0, Math.min(3.0, y));
                        // 保留两位小数
                        x = Math.round(x * 100.0) / 100.0;
                        y = Math.round(y * 100.0) / 100.0;
                        if (x == -0.0) x = 0.0;
                        if (y == -0.0) y = 0.0;
                        // 添加NOTE到noteManager
                        Note freeNote = new Note(x, y, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing);
                        freeNote.setTag(tagField.getText().trim());
                        noteManager.addNote(freeNote);
                    }
                    updateChartLogJson();
                    panel.repaint();
                }
                SwingUtilities.invokeLater(() -> requestFocusInWindow()); 
            }
        });
        
        return panel;
    }
    
    /**
     * 查找最近的网格点 (像素坐标)
     */
    private Point findNearestGridPoint(int mouseX_px, int mouseY_px) {
        int centerX_panel = getWidth() / 2;
        int centerY_panel = getHeight() / 2;

        // 鼠标相对于面板中心的像素位置
        double relativeX_from_center_px = mouseX_px - centerX_panel;
        // Y轴：面板Y向下增加，网格Y向上增加
        double relativeY_from_center_px = centerY_panel - mouseY_px;

        // 将相对像素位置吸附到最近的50像素（即0.5网格单位）倍数上
        double snappedRelativeX_px_double = Math.round(relativeX_from_center_px / (CELL_SIZE / 2.0)) * (CELL_SIZE / 2.0) - 18;
        double snappedRelativeY_px_double = Math.round(relativeY_from_center_px / (CELL_SIZE / 2.0)) * (CELL_SIZE / 2.0) - 22;

        // 转换为绝对像素坐标，作为NOTE的中心点
        int nearestX_px = (int) (centerX_panel + snappedRelativeX_px_double);
        int nearestY_px = (int) (centerY_panel - snappedRelativeY_px_double); // 转换回屏幕Y轴方向
        
        return new Point(nearestX_px, nearestY_px);
    }

    /**
     * 处理导入歌曲的逻辑
     */
    private void importSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".ogg");
            }

            @Override
            public String getDescription() {
                return "OGG 音频文件 (*.ogg)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                musicPlayer.loadSong(selectedFile.getAbsolutePath());
                timeSlider.setMaximum(1000); // 将最大值设置为1000，方便百分比计算
                totalTimeLabel.setText("/ " + beatCalculator.formatTime(musicPlayer.getActualClipLengthMicroseconds()));
                songTitleLabel.setText("歌曲名: " + musicPlayer.getSongName());
                songAuthorLabel.setText("作者: " + musicPlayer.getAuthor());
                // musicUpdateTimer.start(); // 启动定时器更新UI（已废弃，删除）
                logManager.log("成功导入歌曲: " + selectedFile.getName());
            } catch (UnsupportedAudioFileException ex) {
                logManager.log("错误: 无法加载OGG音频文件。");
                logManager.log("请使用以下命令重新打包并运行程序：");
                logManager.log("  mvn clean package");
                logManager.log("  java -jar target/CubeRhythmEditor-1.0.0-SNAPSHOT.jar");
                musicPlayer.close();
                resetMusicUI();
            } catch (IOException | LineUnavailableException ex) {
                logManager.log("错误: 无法加载或播放歌曲。" + ex.getMessage());
                musicPlayer.close();
                resetMusicUI();
            }
        }
    }

    /**
     * 重置音乐相关UI状态
     */
    private void resetMusicUI() {
        timeSlider.setValue(0);
        currentTimeLabel.setText("00:00.000");
        totalTimeLabel.setText("/ 00:00.000");
        songTitleLabel.setText("歌曲名: 无");
        songAuthorLabel.setText("作者: 无");
        measureBeatLabel.setText("小节: 0 拍: 0/4"); // 重置小节和拍数显示
        musicPlayer.seek(0); // 重置音乐到逻辑起点
    }

    /**
     * 启用/禁用拍数选择按钮
     */
    private void disableBeatSelectionButtons(boolean enable) {
        if (beatsMenu != null) { // Ensure beatsMenu is initialized
            for (Component menuItem : beatsMenu.getMenuComponents()) {
                if (menuItem instanceof JRadioButtonMenuItem) {
                    ((JRadioButtonMenuItem) menuItem).setEnabled(enable);
                }
            }
        }
    }

    /**
     * 启用/禁用Note类型选择按钮
     */
    private void disableNoteTypeSelectionButtons(boolean enable) {
        if (noteTypeButtonPanel != null) {
            for (Component component : noteTypeButtonPanel.getComponents()) {
                if (component instanceof JRadioButton) {
                    component.setEnabled(enable);
                }
            }
            noteTypeButtonPanel.repaint(); // 强制刷新面板
        }
        noteTypeToggleButton.setEnabled(enable);
        noteTypeToggleButton.repaint(); // 强制刷新折叠按钮
    }

    // 在合适位置添加：
    private void updateMusicUIByClip() {
        updateMusicUIByClip(musicPlayer.getCurrentTimeMicroseconds());
    }
    private void updateMusicUIByClip(long displayTime) {
        long total = musicPlayer.getActualClipLengthMicroseconds();
        displayTime = Math.max(0, Math.min(displayTime, total));
        int sliderValue = (int) (displayTime * 1000.0 / total);
        sliderValue = Math.max(0, Math.min(1000, sliderValue));
        if (!isUserDraggingSlider) {
            timeSlider.setValue(sliderValue);
        }
        // 注意：UI显示的当前时间不加offset，measure/beat等判定NOTE时加offset
        currentTimeLabel.setText(beatCalculator.formatTime(displayTime));
        totalTimeLabel.setText("/ " + beatCalculator.formatTime(total));
        double totalBeats = beatCalculator.microsecondsToBeats(displayTime + getCurrentOffsetMicroseconds());
        int beatsPerMeasure = beatCalculator.getBeatsPerMeasure();

        // 计算小节和拍数（拍数从0开始计数）
        int currentMeasure = (int)(totalBeats / beatsPerMeasure);
        int currentBeatInMeasure = (int)(totalBeats % beatsPerMeasure);

        measureBeatLabel.setText(String.format("小节: %d 拍: %d/%d", currentMeasure, currentBeatInMeasure, beatsPerMeasure));
        mainPanel.repaint();
    }

    /**
     * 解析日志行生成NOTE对象，支持tap/drag/double等格式
     */
    private Note parseNoteFromLog(String line) {
        try {
            if (line.startsWith("tap(") || line.startsWith("drag(") || line.startsWith("hold(")) {
                // tap/drag/hold(时间, "方向", x, y, 发光)
                int l = line.indexOf('('), r = line.lastIndexOf(')');
                String[] arr = line.substring(l+1, r).split(",");
                double time = Double.parseDouble(arr[0].trim());
                String dir = arr[1].replaceAll("[\" ]", "");
                double x = Double.parseDouble(arr[2].trim());
                double y = Double.parseDouble(arr[3].trim());
                boolean glow = arr[4].trim().equalsIgnoreCase("true");
                NoteType type;
                if (line.startsWith("tap(")) type = NoteType.TAP;
                else if (line.startsWith("drag(")) type = NoteType.DRAG;
                else type = NoteType.HOLD;
                return new Note(x, y, type, (long)(time * 1_000_000), dir, glow);
            } else if (line.startsWith("double(")) {
                // double(时间, "方向", x1, y1, x2, y2, 发光)
                int l = line.indexOf('('), r = line.lastIndexOf(')');
                String[] arr = line.substring(l+1, r).split(",");
                double time = Double.parseDouble(arr[0].trim());
                String dir = arr[1].replaceAll("[\" ]", "");
                double x1 = Double.parseDouble(arr[2].trim());
                double y1 = Double.parseDouble(arr[3].trim());
                double x2 = Double.parseDouble(arr[4].trim());
                double y2 = Double.parseDouble(arr[5].trim());
                boolean glow = arr[6].trim().equalsIgnoreCase("true");
                return new Note(x1, y1, x2, y2, NoteType.DOUBLE, (long)(time * 1_000_000), dir, glow);
            } else if (line.startsWith("flick(")) {
                // flick(时间, "方向", "left/right", 发光)
                int l = line.indexOf('('), r = line.lastIndexOf(')');
                String[] arr = line.substring(l+1, r).split(",");
                double time = Double.parseDouble(arr[0].trim());
                String dir = arr[1].replaceAll("[\" ]", "");
                String flickDir = arr[2].replaceAll("[\" ]", "");
                boolean glow = arr[3].trim().equalsIgnoreCase("true");
                NoteType type = flickDir.equalsIgnoreCase("left") ? NoteType.FLICK_LEFT : NoteType.FLICK_RIGHT;
                return new Note(type, (long)(time * 1_000_000), dir, flickDir, glow);
            } else if (line.startsWith("execution(")) {
                // execution(时间)
                int l = line.indexOf('('), r = line.lastIndexOf(')');
                String[] arr = line.substring(l+1, r).split(",");
                double time = Double.parseDouble(arr[0].trim());
                return new Note(NoteType.EXECUTION, (long)(time * 1_000_000));
            }
        } catch (Exception ex) { return null; }
        return null;
    }
    /**
     * NOTE排序规则：时间升序，坐标小的优先，发光false在前
     */
    private int noteCompare(Note a, Note b) {
        int t = Long.compare(a.getTimeMicroseconds(), b.getTimeMicroseconds());
        if (t != 0) return t;
        boolean aHasCoord = a.hasCoordinates(), bHasCoord = b.hasCoordinates();
        if (aHasCoord && bHasCoord) {
            int x = Double.compare(a.getX(), b.getX());
            if (x != 0) return x;
            int y = Double.compare(a.getY(), b.getY());
            if (y != 0) return y;
        }
        if (aHasCoord != bHasCoord) return aHasCoord ? -1 : 1;
        if (a.isGlowing() != b.isGlowing()) return a.isGlowing() ? 1 : -1;
        return 0;
    }

    // 获取当前offset（微秒，允许正负）
    private long getCurrentOffsetMicroseconds() {
        String offsetText = null;
        if (offsetField != null) {
            offsetText = offsetField.getText().trim();
        } else if (offset != null) {
            offsetText = offset.trim();
        }
        if (offsetText == null || offsetText.isEmpty()) return 0L;
        try {
            double offsetMs = Double.parseDouble(offsetText);
            return (long) (offsetMs * 1000);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private static final BasicStroke CROSS_STROKE = new BasicStroke(2f);
    private static final Color CHECKER_COLOR_OPAQUE = new Color(255, 255, 255);
    private static final Color CROSS_COLOR_OPAQUE = new Color(139, 0, 0);

    private void drawCheckerboard(Graphics g, int x, int y, int w, int h, int alpha) {
        g.setColor(CHECKER_COLOR_OPAQUE);
        int cellSize = Math.max(4, w / 7);
        for (int row = 0; row * cellSize < h; row++) {
            for (int col = 0; col * cellSize < w; col++) {
                if ((row + col) % 2 == 0) {
                    int cw = Math.min(cellSize, w - col * cellSize);
                    int ch = Math.min(cellSize, h - row * cellSize);
                    g.fillRect(x + col * cellSize, y + row * cellSize, cw, ch);
                }
            }
        }
    }

    private void drawCrossPattern(Graphics g, int x, int y, int w, int h, int alpha) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(CROSS_COLOR_OPAQUE);
        g2.setStroke(CROSS_STROKE);
        g2.drawLine(x, y, x + w, y + h);
        g2.drawLine(x + w, y, x, y + h);
    }

    private void renderExecutionActions(Graphics g, List<Note> notesToShow, int centerX, int centerY,
                                        double currentBeat, int displayBeats, int panelWidth) {
        if (notesToShow == null || notesToShow.isEmpty()) return;

        java.util.Map<String, List<Note>> tagIndex = new java.util.HashMap<>();
        for (Note note : notesToShow) {
            for (String tag : note.getTags()) {
                tagIndex.computeIfAbsent(tag, k -> new java.util.ArrayList<>()).add(note);
            }
        }

        Graphics2D g2 = (Graphics2D) g;
        Font actionFont = new Font("Monospaced", Font.PLAIN, 9);
        g2.setFont(actionFont);
        int n = displayBeats - 1;
        if (n <= 0) return;

        java.util.Map<Note, Integer> noteLabelCount = new java.util.HashMap<>();
        int playerActionY = 15;

        for (Note note : notesToShow) {
            if (!note.isExecution() || note.getActions() == null || note.getActions().isEmpty()) continue;

            double noteBeat = beatCalculator.microsecondsToBeats(note.getTimeMicroseconds());
            double k = noteBeat - currentBeat;
            if (k < 0 || k > n) continue;

            for (int ai = 0; ai < note.getActions().size(); ai++) {
                com.google.gson.JsonObject action = note.getActions().get(ai).getAsJsonObject();
                if (!action.has("type")) continue;
                String actionType = action.get("type").getAsString();

                if (DIRECT_NOTE_ACTIONS.contains(actionType)) {
                    if (!action.has("bind_tag")) continue;
                    com.google.gson.JsonArray bindTags = action.getAsJsonArray("bind_tag");
                    if (bindTags == null) continue;
                    for (int ti = 0; ti < bindTags.size(); ti++) {
                        String tag = bindTags.get(ti).getAsString();
                        List<Note> targets = tagIndex.get(tag);
                        if (targets == null) continue;
                        for (Note target : targets) {
                            double targetBeat = beatCalculator.microsecondsToBeats(target.getTimeMicroseconds());
                            double tk = targetBeat - currentBeat;
                            if (tk < 0 || tk > n) continue;
                            double progress = (n - tk) / n;
                            double tx = target.getX() * 100;
                            double ty = -target.getY() * 100;
                            double x0 = tx / 5.0;
                            double y0 = ty / 5.0;
                            double cx = x0 + (tx - x0) * progress;
                            double cy = y0 + (ty - y0) * progress;
                            double size = 20 + (100 - 20) * progress;
                            int labelIdx = noteLabelCount.getOrDefault(target, 0);
                            noteLabelCount.put(target, labelIdx + 1);
                            int lx = (int)(centerX + cx - size / 2) + 2;
                            int ly = (int)(centerY + cy - size / 2) + 10 + labelIdx * 10;
                            g2.setColor(Color.WHITE);
                            g2.drawString(actionType, lx, ly);
                        }
                    }
                } else if (INDIRECT_NOTE_ACTIONS.contains(actionType)) {
                    String face = action.has("face") ? action.get("face").getAsString() : "w";
                    Color actionColor = switch (face) {
                        case "a" -> NOTE_BORDER_A;
                        case "s" -> NOTE_BORDER_S;
                        case "d" -> NOTE_BORDER_D;
                        default -> NOTE_BORDER_W;
                    };
                    g2.setColor(actionColor);
                    if ("draw_line".equals(actionType) && action.has("from") && action.has("to")) {
                        com.google.gson.JsonObject from = action.getAsJsonObject("from");
                        com.google.gson.JsonObject to = action.getAsJsonObject("to");
                        int x1 = centerX + (int)(from.get("x").getAsDouble() * CELL_SIZE);
                        int y1 = centerY - (int)(from.get("y").getAsDouble() * CELL_SIZE);
                        int x2 = centerX + (int)(to.get("x").getAsDouble() * CELL_SIZE);
                        int y2 = centerY - (int)(to.get("y").getAsDouble() * CELL_SIZE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(x1, y1, x2, y2);
                    } else if ("draw_text".equals(actionType) && action.has("position")) {
                        com.google.gson.JsonObject pos = action.getAsJsonObject("position");
                        int tx = centerX + (int)(pos.get("x").getAsDouble() * CELL_SIZE);
                        int ty = centerY - (int)(pos.get("y").getAsDouble() * CELL_SIZE);
                        String text = action.has("text") ? action.get("text").getAsString() : "";
                        text = text.replaceAll("§.", "");
                        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                        g2.drawString(text, tx, ty);
                        g2.setFont(actionFont);
                    }
                } else if (PLAYER_ACTIONS.contains(actionType)) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                    g2.drawString(actionType, panelWidth - 120, playerActionY);
                    playerActionY += 13;
                    g2.setFont(actionFont);
                }
            }
        }
    }

    /**
     * 将Note对象转换为Gson JsonObject
     */
    private com.google.gson.JsonObject noteToJsonObject(Note note) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        double timeSeconds = note.getTimeMicroseconds() / 1_000_000.0;

        if (note.isExecution()) {
            json.addProperty("type", "execution");
            json.addProperty("time", timeSeconds);
            json.add("actions", note.getActions() != null ? note.getActions() : new com.google.gson.JsonArray());
        } else if (note.isFlick()) {
            json.addProperty("type", note.getType().getJsonName());
            json.addProperty("time", timeSeconds);
            String face = note.getDirection() != null ? note.getDirection().toLowerCase() : "w";
            json.addProperty("face", face);
            String turn = (note.getType() == NoteType.FLICK_LEFT) ? "left" : "right";
            json.addProperty("turn", turn);
            json.addProperty("glowing", note.isGlowing());
            addTagsToJson(json, note);
        } else if (note.isDouble()) {
            json.addProperty("type", note.getType().getJsonName());
            json.addProperty("time", timeSeconds);
            String face = note.getDirection() != null ? note.getDirection().toLowerCase() : "w";
            json.addProperty("face", face);
            com.google.gson.JsonArray positions = new com.google.gson.JsonArray();
            com.google.gson.JsonObject pos1 = new com.google.gson.JsonObject();
            pos1.addProperty("x", note.getX());
            pos1.addProperty("y", note.getY());
            com.google.gson.JsonObject pos2 = new com.google.gson.JsonObject();
            pos2.addProperty("x", note.getX2());
            pos2.addProperty("y", note.getY2());
            positions.add(pos1);
            positions.add(pos2);
            json.add("positions", positions);
            json.addProperty("glowing", note.isGlowing());
            addTagsToJson(json, note);
        } else {
            json.addProperty("type", note.getType().getJsonName());
            json.addProperty("time", timeSeconds);
            String face = note.getDirection() != null ? note.getDirection().toLowerCase() : "w";
            json.addProperty("face", face);
            com.google.gson.JsonObject position = new com.google.gson.JsonObject();
            position.addProperty("x", note.getX());
            position.addProperty("y", note.getY());
            json.add("position", position);
            json.addProperty("glowing", note.isGlowing());
            addTagsToJson(json, note);
        }

        if (note.getEvents() != null) {
            json.add("events", note.getEvents());
        }

        return json;
    }

    private void addTagsToJson(com.google.gson.JsonObject json, Note note) {
        List<String> tags = note.getTags();
        if (tags.isEmpty()) {
            json.addProperty("tag", "");
        } else if (tags.size() == 1) {
            json.addProperty("tag", tags.get(0));
        } else {
            com.google.gson.JsonArray tagsArray = new com.google.gson.JsonArray();
            for (String tag : tags) {
                tagsArray.add(tag);
            }
            json.add("tags", tagsArray);
        }
    }

    /**
     * 将Note对象转换为JSON格式字符串（兼容旧调用）
     */
    private String noteToJson(Note note) {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(noteToJsonObject(note));
    }

    /**
     * 导出完整谱面JSON（包含metadata和groupEvents）
     */
    private String exportFullChartJson() {
        com.google.gson.JsonObject root = new com.google.gson.JsonObject();
        root.addProperty("version", "1.0.0");

        com.google.gson.JsonObject metadata = new com.google.gson.JsonObject();
        String id = songNameField != null ? songNameField.getText().trim() : "";
        metadata.addProperty("id", id);
        metadata.addProperty("title", songNameField != null ? songNameField.getText().trim() : "");
        metadata.addProperty("artist", composerField != null ? composerField.getText().trim() : "");
        metadata.addProperty("charter", chartAuthorField != null ? chartAuthorField.getText().trim() : "");

        com.google.gson.JsonObject difficulty = new com.google.gson.JsonObject();
        difficulty.addProperty("name", difficultyField != null ? difficultyField.getText().trim() : "");
        difficulty.addProperty("level", parseDifficultyLevel());
        difficulty.addProperty("color", parseDifficultyColor());
        metadata.add("difficulty", difficulty);

        metadata.addProperty("audio", "cr." + id);
        metadata.addProperty("duration", parseDurationSeconds());
        metadata.addProperty("offset", parseOffsetValue());
        metadata.addProperty("bpm", parseBpmValue());

        root.add("metadata", metadata);

        // 保留编辑器中已有的 groupEvents，不覆盖
        com.google.gson.JsonArray existingGroupEvents = new com.google.gson.JsonArray();
        if (chartEditorPanel != null) {
            try {
                String editorText = chartEditorPanel.getText().trim();
                if (!editorText.isEmpty()) {
                    com.google.gson.JsonElement el = new com.google.gson.Gson().fromJson(editorText, com.google.gson.JsonElement.class);
                    if (el.isJsonObject() && el.getAsJsonObject().has("groupEvents")) {
                        existingGroupEvents = el.getAsJsonObject().getAsJsonArray("groupEvents");
                    }
                }
            } catch (Exception ignored) {}
        }
        root.add("groupEvents", existingGroupEvents);

        com.google.gson.JsonArray notesArray = new com.google.gson.JsonArray();
        for (Note note : noteManager.getNotes()) {
            notesArray.add(noteToJsonObject(note));
        }
        root.add("notes", notesArray);

        return new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    private int parseDurationSeconds() {
        try {
            return Integer.parseInt(durationField != null ? durationField.getText().trim() : "0");
        } catch (NumberFormatException e) { return 0; }
    }

    private int parseOffsetValue() {
        try {
            return Integer.parseInt(offsetField != null ? offsetField.getText().trim() : "0");
        } catch (NumberFormatException e) { return 0; }
    }

    private int parseBpmValue() {
        try {
            return Integer.parseInt(bpmField != null ? bpmField.getText().trim() : "120");
        } catch (NumberFormatException e) { return 120; }
    }

    private int parseDifficultyLevel() {
        try {
            int level = Integer.parseInt(difficultyLevelField != null ? difficultyLevelField.getText().trim() : "1");
            return Math.max(1, Math.min(15, level));
        } catch (NumberFormatException e) { return 1; }
    }

    private String parseDifficultyColor() {
        if (difficultyColorCombo == null) return "&b";
        String selected = (String) difficultyColorCombo.getSelectedItem();
        if (selected == null) return "&b";
        // 提取颜色代码部分（如 "&b (AQUA)" → "&b"）
        int spaceIdx = selected.indexOf(' ');
        return spaceIdx > 0 ? selected.substring(0, spaceIdx) : selected;
    }

    /**
     * 更新铺面编辑器为完整JSON格式
     */
    private void updateChartLogJson() {
        if (chartEditorPanel == null) return;
        chartEditorPanel.setText(exportFullChartJson());
    }

    /**
     * 查找点击位置的音符
     */
    private Note findNoteAtPosition(int mouseX, int mouseY, JPanel panel) {
        if (musicPlayer == null || beatCalculator == null || noteManager == null) {
            return null;
        }

        long currentTime = musicPlayer.getCurrentTimeMicroseconds();
        List<Note> notesToShow = noteManager.getNotesForCurrentBeat(currentTime, displayBeatsCount, beatCalculator);
        double currentBeat = beatCalculator.microsecondsToBeats(currentTime);
        int centerX = panel.getWidth() / 2;
        int centerY = panel.getHeight() / 2;
        int n = displayBeatsCount - 1;

        // 遍历所有可见音符，优先返回渲染在最上层（k最小，progress最大）的命中音符
        Note bestNote = null;
        double bestK = Double.MAX_VALUE;
        for (Note note : notesToShow) {
            double noteBeat = Math.round(beatCalculator.microsecondsToBeats(note.getTimeMicroseconds()));
            double k = noteBeat - currentBeat;
            if (k < 0 || k > n) continue;

            double progress = (n == 0) ? 1.0 : (n - k) / n;
            double x = note.getX() * 100;
            double y = -note.getY() * 100;
            double cx = x / 5.0 + (x - x / 5.0) * progress;
            double cy = y / 5.0 + (y - y / 5.0) * progress;
            double size = 20 + (100 - 20) * progress;

            int noteScreenX = (int)(centerX + cx);
            int noteScreenY = (int)(centerY + cy);

            if (mouseX >= noteScreenX - size/2 && mouseX <= noteScreenX + size/2 &&
                mouseY >= noteScreenY - size/2 && mouseY <= noteScreenY + size/2) {
                if (k < bestK) {
                    bestK = k;
                    bestNote = note;
                }
            }
        }

        return bestNote;
    }

    private void showNoteContextMenu(JPanel panel, int x, int y, Note note) {
        isContextMenuOpen = true;
        JPopupMenu menu = new JPopupMenu();
        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { isContextMenuOpen = false; }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { isContextMenuOpen = false; }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
        });

        if (note == null) {
            JMenuItem hint = new JMenuItem("未点击到音符");
            hint.setEnabled(false);
            menu.add(hint);
            menu.show(panel, x, y);
            return;
        }

        // 删除
        JMenuItem deleteItem = new JMenuItem("删除音符");
        deleteItem.addActionListener(e -> {
            noteManager.removeNote(note);
            updateChartLogJson();
            panel.repaint();
            logManager.log("已删除音符");
        });
        menu.add(deleteItem);

        // 定位到 JSON
        JMenuItem locateItem = new JMenuItem("定位到 JSON");
        locateItem.addActionListener(e -> locateNoteInJson(note));
        menu.add(locateItem);

        // 修改方向（EXECUTION 无方向）
        if (note.getDirection() != null || note.hasCoordinates() || note.isFlick()) {
            JMenu faceMenu = new JMenu("修改方向");
            for (String dir : new String[]{"w", "a", "s", "d"}) {
                JMenuItem dirItem = new JMenuItem(dir.toUpperCase());
                dirItem.addActionListener(e -> {
                    note.setDirection(dir);
                    updateChartLogJson();
                    panel.repaint();
                });
                faceMenu.add(dirItem);
            }
            menu.add(faceMenu);
        }

        // 切换 glow（EXECUTION 无 glow）
        if (!note.isExecution()) {
            JCheckBoxMenuItem glowItem = new JCheckBoxMenuItem("发光 (glow)", note.isGlowing());
            glowItem.addActionListener(e -> {
                note.setGlowing(glowItem.isSelected());
                updateChartLogJson();
                panel.repaint();
            });
            menu.add(glowItem);
        }

        menu.show(panel, x, y);
    }

    private void locateNoteInJson(Note note) {
        String json = chartEditorPanel.getText();
        if (json == null || json.isEmpty()) return;

        double targetTime = note.getTimeMicroseconds() / 1_000_000.0;
        String typeName = note.getType().getJsonName();
        // 5ms 容差，覆盖浮点精度误差和手动微调
        double tolerance = 0.005;

        int searchFrom = 0;
        int bestIdx = -1;
        double bestDiff = Double.MAX_VALUE;

        while (searchFrom < json.length()) {
            int typeIdx = json.indexOf("\"type\": \"" + typeName + "\"", searchFrom);
            if (typeIdx < 0) break;

            // 只在 typeIdx 之后查找 "time"，避免匹配到上一个音符块的值
            int blockEnd = Math.min(json.length(), typeIdx + 200);
            String block = json.substring(typeIdx, blockEnd);

            // 提取 "time": <value> 的数值
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"time\":\\s*([0-9.eE+\\-]+)").matcher(block);
            if (m.find()) {
                try {
                    double t = Double.parseDouble(m.group(1));
                    double diff = Math.abs(t - targetTime);
                    if (diff <= tolerance) {
                        boolean glowMatch = note.isExecution() || note.isFlick() ||
                                block.contains("\"glowing\": " + note.isGlowing());
                        boolean posMatch = !note.hasCoordinates() ||
                                (block.contains(String.valueOf(note.getX())) &&
                                 block.contains(String.valueOf(note.getY())) &&
                                 (!note.isDouble() ||
                                     (block.contains(String.valueOf(note.getX2())) &&
                                      block.contains(String.valueOf(note.getY2())))));
                        if (glowMatch && posMatch && diff < bestDiff) {
                            bestDiff = diff;
                            bestIdx = typeIdx;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
            searchFrom = typeIdx + 1;
        }

        if (bestIdx >= 0) {
            chartEditorPanel.getTextArea().setCaretPosition(bestIdx);
            chartEditorPanel.getTextArea().requestFocusInWindow();
        } else {
            logManager.log("未能在 JSON 中定位该音符");
        }
    }

    /**
     * 将当前播放位置吸附到最近的一拍
     */
    private void snapToNearestBeat() {
        if (musicPlayer == null || beatCalculator == null) return;

        long currentTime = musicPlayer.getCurrentTimeMicroseconds();
        long offset = getCurrentOffsetMicroseconds();

        // 加上偏移量后计算音乐时间对应的拍数
        double currentBeats = beatCalculator.microsecondsToBeats(currentTime + offset);

        // 四舍五入到最近的一拍
        long nearestBeat = Math.round(currentBeats);
        long nearestMusicalTime = beatCalculator.beatsToMicroseconds(nearestBeat);

        // 减去偏移量得到实际播放时间
        long nearestTime = nearestMusicalTime - offset;

        // 吸附到最近的拍
        musicPlayer.seek(nearestTime);
        updateMusicUIByClip();
    }

    /**
     * 从JSON文本重载音符
     */
    private void reloadNotesFromJson() throws Exception {
        String songName = songNameField != null ? songNameField.getText().trim() : "chart";
        if (songName.isEmpty()) {
            songName = "chart";
        }

        String jsonText = chartEditorPanel.getText().trim();
        if (jsonText.isEmpty()) {
            noteManager.clearNotes();
            return;
        }

        // 验证 JSON 格式
        if (!chartEditorPanel.validateJson()) {
            throw new Exception("JSON 格式错误");
        }

        // 创建charts目录（如果不存在）
        java.io.File chartsDir = new java.io.File("charts");
        if (!chartsDir.exists()) {
            chartsDir.mkdirs();
        }

        // 保存到文件
        java.io.File chartFile = new java.io.File(chartsDir, songName + ".json");
        try (java.io.FileWriter writer = new java.io.FileWriter(chartFile)) {
            writer.write(jsonText);
        }
        logManager.log("铺面已保存到: " + chartFile.getAbsolutePath());

        // 使用Gson解析JSON
        noteManager.clearNotes();

        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonElement rootElement = gson.fromJson(jsonText, com.google.gson.JsonElement.class);

        com.google.gson.JsonArray notesArray = null;

        if (rootElement.isJsonObject()) {
            com.google.gson.JsonObject root = rootElement.getAsJsonObject();
            // 完整谱面格式：提取 notes 数组
            if (root.has("notes")) {
                notesArray = root.getAsJsonArray("notes");
            }
            // 同步 metadata 到 UI（保存音频位置防止 offsetField 监听器重置进度）
            long savedAudioTime = musicPlayer != null ? musicPlayer.getCurrentTimeMicroseconds() : 0;
            if (root.has("metadata")) {
                com.google.gson.JsonObject metadata = root.getAsJsonObject("metadata");
                if (metadata.has("title") && songNameField != null)
                    songNameField.setText(metadata.get("title").getAsString());
                if (metadata.has("artist") && composerField != null)
                    composerField.setText(metadata.get("artist").getAsString());
                if (metadata.has("charter") && chartAuthorField != null)
                    chartAuthorField.setText(metadata.get("charter").getAsString());
                if (metadata.has("bpm") && bpmField != null)
                    bpmField.setText(String.valueOf(metadata.get("bpm").getAsInt()));
                if (metadata.has("duration") && durationField != null)
                    durationField.setText(String.valueOf(metadata.get("duration").getAsInt()));
                if (metadata.has("offset") && offsetField != null)
                    offsetField.setText(String.valueOf(metadata.get("offset").getAsInt()));
                if (metadata.has("difficulty")) {
                    com.google.gson.JsonObject diff = metadata.getAsJsonObject("difficulty");
                    if (diff.has("name") && difficultyField != null)
                        difficultyField.setText(diff.get("name").getAsString());
                    if (diff.has("level") && difficultyLevelField != null)
                        difficultyLevelField.setText(String.valueOf(diff.get("level").getAsInt()));
                    if (diff.has("color") && difficultyColorCombo != null) {
                        String color = diff.get("color").getAsString();
                        for (int ci = 0; ci < difficultyColorCombo.getItemCount(); ci++) {
                            if (difficultyColorCombo.getItemAt(ci).startsWith(color)) {
                                difficultyColorCombo.setSelectedIndex(ci);
                                break;
                            }
                        }
                    }
                }
            }
            if (musicPlayer != null) musicPlayer.seek(savedAudioTime);
        } else if (rootElement.isJsonArray()) {
            // 纯 notes 数组格式（兼容旧格式）
            notesArray = rootElement.getAsJsonArray();
        }

        if (notesArray == null || notesArray.isEmpty()) {
            logManager.log("铺面为空");
            return;
        }

        // 解析每个音符
        for (int i = 0; i < notesArray.size(); i++) {
            com.google.gson.JsonObject noteObj = notesArray.get(i).getAsJsonObject();
            Note note = parseNoteFromJsonObject(noteObj);
            if (note != null) {
                noteManager.addNote(note);
            }
        }

        logManager.log("成功重载 " + noteManager.getNotes().size() + " 个音符");
    }

    /**
     * 从Gson JsonObject解析单个音符
     */
    private Note parseNoteFromJsonObject(com.google.gson.JsonObject noteObj) {
        try {
            String type = noteObj.get("type").getAsString();
            double time = noteObj.get("time").getAsDouble();
            long timeMicroseconds = (long)(time * 1_000_000);

            switch (type.toLowerCase()) {
                case "execution": {
                    Note note = new Note(NoteType.EXECUTION, timeMicroseconds);
                    if (noteObj.has("actions")) {
                        note.setActions(noteObj.getAsJsonArray("actions"));
                    }
                    return note;
                }

                case "flick":
                case "fake_flick": {
                    String face = noteObj.get("face").getAsString();
                    String turn = noteObj.get("turn").getAsString();
                    boolean glowing = noteObj.get("glowing").getAsBoolean();
                    NoteType flickType;
                    if ("fake_flick".equals(type.toLowerCase())) {
                        flickType = NoteType.FAKE_FLICK;
                    } else {
                        flickType = "left".equals(turn) ? NoteType.FLICK_LEFT : NoteType.FLICK_RIGHT;
                    }
                    Note note = new Note(flickType, timeMicroseconds, face, turn, glowing);
                    parseTagsFromJson(noteObj, note);
                    parseEventsFromJson(noteObj, note);
                    return note;
                }

                case "double":
                case "fake_double":
                case "mine_double": {
                    String face = noteObj.get("face").getAsString();
                    boolean glowing = noteObj.get("glowing").getAsBoolean();
                    com.google.gson.JsonArray positions = noteObj.getAsJsonArray("positions");
                    if (positions != null && positions.size() >= 2) {
                        com.google.gson.JsonObject pos1 = positions.get(0).getAsJsonObject();
                        com.google.gson.JsonObject pos2 = positions.get(1).getAsJsonObject();
                        double x1 = pos1.get("x").getAsDouble();
                        double y1 = pos1.get("y").getAsDouble();
                        double x2 = pos2.get("x").getAsDouble();
                        double y2 = pos2.get("y").getAsDouble();
                        NoteType noteType = switch (type.toLowerCase()) {
                            case "fake_double" -> NoteType.FAKE_DOUBLE;
                            case "mine_double" -> NoteType.MINE_DOUBLE;
                            default -> NoteType.DOUBLE;
                        };
                        Note note = new Note(x1, y1, x2, y2, noteType, timeMicroseconds, face, glowing);
                        parseTagsFromJson(noteObj, note);
                        parseEventsFromJson(noteObj, note);
                        return note;
                    }
                    break;
                }

                case "tap":
                case "drag":
                case "hold":
                case "fake_tap":
                case "fake_drag":
                case "fake_hold":
                case "mine_tap":
                case "mine_drag": {
                    String face = noteObj.get("face").getAsString();
                    boolean glowing = noteObj.get("glowing").getAsBoolean();
                    com.google.gson.JsonObject position = noteObj.getAsJsonObject("position");
                    if (position != null) {
                        double x = position.get("x").getAsDouble();
                        double y = position.get("y").getAsDouble();
                        NoteType noteType = switch (type.toLowerCase()) {
                            case "tap" -> NoteType.TAP;
                            case "drag" -> NoteType.DRAG;
                            case "hold" -> NoteType.HOLD;
                            case "fake_tap" -> NoteType.FAKE_TAP;
                            case "fake_drag" -> NoteType.FAKE_DRAG;
                            case "fake_hold" -> NoteType.FAKE_HOLD;
                            case "mine_tap" -> NoteType.MINE_TAP;
                            case "mine_drag" -> NoteType.MINE_DRAG;
                            default -> NoteType.TAP;
                        };
                        Note note = new Note(x, y, noteType, timeMicroseconds, face, glowing);
                        parseTagsFromJson(noteObj, note);
                        parseEventsFromJson(noteObj, note);
                        return note;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logManager.log("解析音符失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void parseTagsFromJson(com.google.gson.JsonObject noteObj, Note note) {
        if (noteObj.has("tags")) {
            com.google.gson.JsonArray tagsArray = noteObj.getAsJsonArray("tags");
            for (int i = 0; i < tagsArray.size(); i++) {
                note.getTags().add(tagsArray.get(i).getAsString());
            }
        } else if (noteObj.has("tag")) {
            String tag = noteObj.get("tag").getAsString();
            if (!tag.isEmpty()) {
                note.getTags().add(tag);
            }
        }
    }

    private void parseEventsFromJson(com.google.gson.JsonObject noteObj, Note note) {
        if (noteObj.has("events")) {
            note.setEvents(noteObj.getAsJsonObject("events"));
        }
    }

    /**
     * 旧的字符串解析方法 - 保留作为备用
     */
    private Note parseNoteFromJson(String jsonObj) {
        try {
            // 提取字段值的简单方法
            String type = extractJsonString(jsonObj, "type");
            double time = extractJsonNumber(jsonObj, "time");
            long timeMicroseconds = (long)(time * 1_000_000);

            if (type == null) return null;

            switch (type.toLowerCase()) {
                case "execution":
                    return new Note(NoteType.EXECUTION, timeMicroseconds);

                case "flick":
                    String face = extractJsonString(jsonObj, "face");
                    String turn = extractJsonString(jsonObj, "turn");
                    boolean glowing = extractJsonBoolean(jsonObj, "glowing");
                    NoteType flickType = "left".equals(turn) ? NoteType.FLICK_LEFT : NoteType.FLICK_RIGHT;
                    return new Note(flickType, timeMicroseconds, face, turn, glowing);

                case "double":
                    face = extractJsonString(jsonObj, "face");
                    glowing = extractJsonBoolean(jsonObj, "glowing");
                    // 提取positions数组 - 使用更健壮的方法
                    String positionsStr = extractJsonArray(jsonObj, "positions");
                    if (positionsStr != null && !positionsStr.trim().isEmpty()) {
                        // 直接在整个positions字符串中查找所有x和y值
                        // 使用正则表达式找到所有的 "x": number 和 "y": number
                        java.util.List<Double> xValues = new java.util.ArrayList<>();
                        java.util.List<Double> yValues = new java.util.ArrayList<>();

                        // 查找所有x值
                        java.util.regex.Pattern xPattern = java.util.regex.Pattern.compile("\"x\"\\s*:\\s*([\\d.\\-]+)");
                        java.util.regex.Matcher xMatcher = xPattern.matcher(positionsStr);
                        while (xMatcher.find()) {
                            xValues.add(Double.parseDouble(xMatcher.group(1)));
                        }

                        // 查找所有y值
                        java.util.regex.Pattern yPattern = java.util.regex.Pattern.compile("\"y\"\\s*:\\s*([\\d.\\-]+)");
                        java.util.regex.Matcher yMatcher = yPattern.matcher(positionsStr);
                        while (yMatcher.find()) {
                            yValues.add(Double.parseDouble(yMatcher.group(1)));
                        }

                        // 确保找到了两对坐标
                        if (xValues.size() >= 2 && yValues.size() >= 2) {
                            double x1 = xValues.get(0);
                            double y1 = yValues.get(0);
                            double x2 = xValues.get(1);
                            double y2 = yValues.get(1);
                            logManager.log(String.format("解析DOUBLE音符: x1=%.2f, y1=%.2f, x2=%.2f, y2=%.2f", x1, y1, x2, y2));
                            Note doubleNote = new Note(x1, y1, x2, y2, NoteType.DOUBLE, timeMicroseconds, face, glowing);
                            logManager.log(String.format("创建DOUBLE音符: getX()=%.2f, getY()=%.2f, getX2()=%.2f, getY2()=%.2f",
                                doubleNote.getX(), doubleNote.getY(), doubleNote.getX2(), doubleNote.getY2()));
                            return doubleNote;
                        } else {
                            logManager.log(String.format("DOUBLE音符坐标数量不足: xValues=%d, yValues=%d", xValues.size(), yValues.size()));
                        }
                    } else {
                        logManager.log("DOUBLE音符positions数组为空或null");
                    }
                    return null; // 明确返回null而不是break

                case "tap":
                case "drag":
                case "hold":
                    face = extractJsonString(jsonObj, "face");
                    glowing = extractJsonBoolean(jsonObj, "glowing");
                    String positionStr = extractJsonObject(jsonObj, "position");
                    if (positionStr != null) {
                        double x = extractJsonNumber(positionStr, "x");
                        double y = extractJsonNumber(positionStr, "y");
                        NoteType noteType = switch (type.toLowerCase()) {
                            case "tap" -> NoteType.TAP;
                            case "drag" -> NoteType.DRAG;
                            case "hold" -> NoteType.HOLD;
                            default -> NoteType.TAP;
                        };
                        return new Note(x, y, noteType, timeMicroseconds, face, glowing);
                    }
                    break;
            }
        } catch (Exception e) {
            logManager.log("解析音符失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从JSON字符串中提取字符串值
     */
    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 从JSON字符串中提取数字值
     */
    private double extractJsonNumber(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([\\d.\\-]+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return 0.0;
    }

    /**
     * 从JSON字符串中提取布尔值
     */
    private boolean extractJsonBoolean(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return false;
    }

    /**
     * 从JSON字符串中提取对象值
     */
    private String extractJsonObject(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\\{([^}]+)\\}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 从JSON字符串中提取数组值
     */
    private String extractJsonArray(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\\[([^]]+)]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}