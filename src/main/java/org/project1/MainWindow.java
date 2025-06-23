package org.project1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities; // 导入 SwingUtilities
import org.project1.ui.NoteTypePanel;
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
    private Point mousePosition = new Point(0, 0);
    // 当前选择的NOTE类型
    private NoteType currentNoteType = NoteType.TAP;
    // NOTE类型标签
    private JLabel noteTypeLabel;
    // NOTE类型选择按钮组
    private JPanel noteTypeButtonPanel;
    // 日志文本区域
    private JTextArea logTextArea;
    // 日志滚动面板
    private JScrollPane logScrollPane;
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
    private boolean isFirstDoublePlaced = false;
    private Point firstDoublePoint = null;
    private JTextField x2CoordField;
    private JTextField y2CoordField;
    private JLabel x2Label;
    private JLabel y2Label;
    
    // 新增Double Note提示标签
    private JLabel doubleNoteTipLabel;

    // 新增成员变量，用于引用JMenu
    private JMenu beatsMenu;
    
    // 歌曲信息全局变量
    private String songName = "";
    private String composer = "";
    private String chartAuthor = "";
    private String difficulty = "";
    private String duration = "";
    private String offset = "";
    private String bpm = "";
    private String author = "";
    
    // 添加新的成员变量
    private JToggleButton noteTypeToggleButton;
    private JPanel noteTypeExpandedPanel;
    
    // 添加新的成员变量
    private JTextArea chartLogTextArea;
    private JScrollPane chartLogScrollPane;
    
    // 添加新的成员变量
    private JLabel positionLabel;

    // 新增音乐播放和节拍相关的成员变量
    private MusicPlayer musicPlayer;
    private BeatCalculator beatCalculator;
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
    private Timer uiTimer;
    
    // 1. 进度条拖动逻辑
    // 添加一个成员变量，标记用户是否正在拖动进度条
    private boolean isUserDraggingSlider = false;
    
    // 1. 新增显示拍数变量
    private int displayBeatsCount = 8; // 默认显示8拍
    
    // 1. 成员变量补充
    private NoteManager noteManager = new NoteManager(); // NOTE管理器，负责所有NOTE的存储与查询
    
    // 静态常量，避免频繁new对象
    private static final BasicStroke NOTE_BORDER_STROKE = new BasicStroke(3f);
    private static final BasicStroke NOTE_GLOW_STROKE = new BasicStroke(2f);
    private static final Color NOTE_GLOW_COLOR = Color.GREEN;
    private static final Color NOTE_BORDER_W = Color.WHITE;
    private static final Color NOTE_BORDER_A = Color.YELLOW;
    private static final Color NOTE_BORDER_S = Color.ORANGE;
    private static final Color NOTE_BORDER_D = Color.RED;
    
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
                SwingUtilities.invokeLater(() -> requestFocusInWindow());
            });
            if (interval == 50) item.setSelected(true); // 默认50ms
        }
        menuBar.add(intervalMenu);

        // 导入歌曲按钮（右对齐）
        menuBar.add(Box.createHorizontalGlue());
        JButton importSongButton = new JButton("导入歌曲");
        importSongButton.addActionListener(e -> {
            importSong();
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        });
        menuBar.add(importSongButton);

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
            if (!isMouseFollowMode && currentNoteType == NoteType.DOUBLE && isFirstDoublePlaced && firstDoublePoint != null) {
                int centerX = noGridPanel.getWidth() / 2;
                int centerY = noGridPanel.getHeight() / 2;
                double x1_grid = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                double y1_grid = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                xCoordField.setText(DECIMAL_FORMAT.format(x1_grid));
                yCoordField.setText(DECIMAL_FORMAT.format(y1_grid));
                xCoordField.setEnabled(true);
                yCoordField.setEnabled(true);
            }
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        });

        // 右侧列组件的起始Y坐标
        int currentY = modeSwitchButton.getY() + modeSwitchButton.getHeight() + 10; // 从模式切换按钮下方10px开始

        // X坐标标签和输入框
        JLabel xLabel = new JLabel("X坐标:");
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        });

        currentY += xCoordField.getHeight() + 10; // 更新currentY

        // Y坐标标签和输入框
        JLabel yLabel = new JLabel("Y坐标:");
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
        
        // 创建操作日志滚动面板
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 300, 275, GRID_PANEL_SIZE - 250); // 调整高度与方框对齐
        logScrollPane.setBorder(BorderFactory.createTitledBorder("操作日志"));

        // 创建铺面生成日志文本区域
        chartLogTextArea = new JTextArea();
        chartLogTextArea.setEditable(true); // 允许编辑
        chartLogTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        chartLogTextArea.setLineWrap(true);
        chartLogTextArea.setWrapStyleWord(true);
        chartLogTextArea.setBackground(new Color(240, 240, 240));
        chartLogTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 创建铺面生成日志滚动面板
        chartLogScrollPane = new JScrollPane(chartLogTextArea);
        chartLogScrollPane.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 325, 50, 275, GRID_PANEL_SIZE); // 调整高度与方框对齐
        chartLogScrollPane.setBorder(BorderFactory.createTitledBorder("铺面生成日志"));
        // 在日志下方添加"重载"和"排序"按钮
        JPanel chartLogButtonPanel = new JPanel();
        chartLogButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chartLogButtonPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 325, 60 + GRID_PANEL_SIZE, 275, 40);
        JButton reloadButton = new JButton("重载");
        JButton sortButton = new JButton("排序");
        chartLogButtonPanel.add(reloadButton);
        chartLogButtonPanel.add(sortButton);
        mainPanel.add(chartLogButtonPanel);
        // "重载"按钮逻辑
        reloadButton.addActionListener(e -> {
            noteManager.clearNotes();
            String[] lines = chartLogTextArea.getText().split("\\n");
            for (String line : lines) {
                Note parsed = parseNoteFromLog(line.trim());
                if (parsed != null) noteManager.addNote(parsed);
            }
            mainPanel.repaint();
        });
        // "排序"按钮逻辑
        sortButton.addActionListener(e -> {
            List<Note> notes = noteManager.getNotes();
            notes.sort((a, b) -> noteCompare(a, b));
            noteManager.clearNotes();
            for (Note n : notes) noteManager.addNote(n);
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
            noteTypeExpandedPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 120, 250, 180);
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
        if (logScrollPane == null) {
            logScrollPane = new JScrollPane(logTextArea);
        }
        if (chartLogTextArea == null) {
            chartLogTextArea = new JTextArea();
        }
        if (chartLogScrollPane == null) {
            chartLogScrollPane = new JScrollPane(chartLogTextArea);
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
        mainPanel.add(logScrollPane);
        mainPanel.add(chartLogScrollPane);
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
                logManager.log("音乐已暂停");
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
        noteTypeButtonPanel.setBounds(0, 0, 250, 180);
        // NOTE类型折叠面板
        noteTypeExpandedPanel = new JPanel();
        noteTypeExpandedPanel.setLayout(null);
        noteTypeExpandedPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 25, 120, 250, 180);
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
            // 鼠标跟随模式：所有输入框禁用
            xCoordField.setEnabled(false);
            yCoordField.setEnabled(false);
            x2CoordField.setEnabled(false);
            y2CoordField.setEnabled(false);
        } else {
            // 手动输入模式
            if (currentNoteType == NoteType.DOUBLE) {
                // Double音符：所有输入框启用
                xCoordField.setEnabled(true);
                yCoordField.setEnabled(true);
                x2CoordField.setEnabled(true);
                y2CoordField.setEnabled(true);
            } else if (currentNoteType == NoteType.EXECUTION ||
                       currentNoteType == NoteType.FLICK_LEFT ||
                       currentNoteType == NoteType.FLICK_RIGHT) {
                // Execution或Flick音符：所有输入框禁用
                xCoordField.setEnabled(false);
                yCoordField.setEnabled(false);
                x2CoordField.setEnabled(false);
                y2CoordField.setEnabled(false);
            } else {
                // 其他音符（TAP, HOLD, CHAIN, DRAG）：只启用x1, y1
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
                if (musicPlayer != null && beatCalculator != null && noteManager != null) {
                    long currentTime = musicPlayer.getCurrentTimeMicroseconds();
                    double currentBeat = beatCalculator.microsecondsToBeats(currentTime);
                    java.util.List<Note> notesToShow = noteManager.getNotesForCurrentBeat(currentTime, displayBeatsCount, beatCalculator);
                    centerX = getWidth() / 2;
                    centerY = getHeight() / 2;
                    int n = displayBeatsCount - 1;
                    // 按拍数降序排序，保证后拍NOTE先绘制，前拍NOTE后绘制（在上层）
                    notesToShow.sort((a, b) -> Double.compare(
                        beatCalculator.microsecondsToBeats(b.getTimeMicroseconds()),
                        beatCalculator.microsecondsToBeats(a.getTimeMicroseconds())
                    ));
                    for (Note note : notesToShow) {
                        double noteBeat = beatCalculator.microsecondsToBeats(note.getTimeMicroseconds());
                        double k = noteBeat - currentBeat;
                        if (k < 0 || k > n) continue;
                        // 伪3D动画进度
                        double progress = (n == 0) ? 1.0 : (n - k) / n;
                        // 目标像素坐标
                        double x = note.getX() * 100;
                        double y = -note.getY() * 100;
                        // 起点（内部方框投影）
                        double x0 = x / 5.0;
                        double y0 = y / 5.0;
                        // 插值位置
                        double cx = x0 + (x - x0) * progress;
                        double cy = y0 + (y - y0) * progress;
                        // 插值大小
                        double size = 20 + (100 - 20) * progress;
                        // 透明度插值
                        int alpha = (int)(255 - (k / n) * (255 - 80));
                        alpha = Math.max(0, Math.min(255, alpha));
                        // 填充色为NOTE类型色
                        Color fillColor = note.getColor();
                        g.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha));
                        g.fillRect((int)(centerX + cx - size/2), (int)(centerY + cy - size/2), (int)size, (int)size);
                        // 朝向边框
                        Graphics2D g2 = (Graphics2D) g;
                        Color borderColor = NOTE_BORDER_W;
                        switch (note.getDirection() != null ? note.getDirection() : "w") {
                            case "a": borderColor = NOTE_BORDER_A; break;
                            case "s": borderColor = NOTE_BORDER_S; break;
                            case "d": borderColor = NOTE_BORDER_D; break;
                        }
                        g2.setStroke(NOTE_BORDER_STROKE);
                        g2.setColor(borderColor);
                        g2.drawRect((int)(centerX + cx - size/2), (int)(centerY + cy - size/2), (int)size, (int)size);
                        // 发光
                        if (note.isGlowing()) {
                            g2.setColor(NOTE_GLOW_COLOR);
                            g2.setStroke(NOTE_GLOW_STROKE);
                            int px = (int)(centerX + cx);
                            int py = (int)(centerY + cy);
                            g2.drawLine(px-2, py, px+2, py);
                            g2.drawLine(px, py-2, px, py+2);
                        }
                    }
                }
                // 仅在无网格模式下显示跟随鼠标的NOTE，不吸附
                if (isMouseFollowMode && currentNoteType != NoteType.EXECUTION && 
                    currentNoteType != NoteType.FLICK_LEFT && currentNoteType != NoteType.FLICK_RIGHT) {
                    Color noteColor = currentNoteType.getColor();
                    Color transparentColor = new Color(
                            noteColor.getRed(), 
                            noteColor.getGreen(), 
                            noteColor.getBlue(), 
                            180); // 透明度
                    g.setColor(transparentColor);
                    int squareX = mousePosition.x - FOLLOW_SQUARE_SIZE / 2;
                    int squareY = mousePosition.y - FOLLOW_SQUARE_SIZE / 2;
                    g.fillRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                    g.setColor(currentNoteType.getColor());
                    g.drawRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
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
                    currentNoteType != NoteType.FLICK_RIGHT) {
                    
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
                // 在网格模式下，只在鼠标跟随模式下才处理点击事件
                // 手动输入模式下通过"确认生成NOTE"按钮处理
                if (!isMouseFollowMode) {
                    return; 
                }

                String direction = getDirection();
                boolean isGlowing = glowCheckBox.isSelected();

                if (currentNoteType == NoteType.EXECUTION || 
                    currentNoteType == NoteType.FLICK_LEFT || 
                    currentNoteType == NoteType.FLICK_RIGHT) {
                    if (currentNoteType == NoteType.EXECUTION) {
                        chartLogTextArea.append("execution({})\n");
                    } else {
                        String flickDirection = currentNoteType == NoteType.FLICK_LEFT ? "left" : "right";
                        chartLogTextArea.append(String.format("flick(%s, \"%s\", \"%s\", %b, \"\")\n",
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, flickDirection, isGlowing));
                    }
                } else if (currentNoteType == NoteType.DOUBLE) {
                    if (!isFirstDoublePlaced) {
                        // 放置第一个Double音符
                        // 对于 chart log，我们使用原始鼠标位置
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
                        // 对于 chart log，我们使用原始鼠标位置
                        Point secondDoublePoint = new Point(e.getX(), e.getY()); // 获取第二个点的原始像素坐标

                        // 将原始像素坐标转换为网格坐标，用于日志输出
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        
                        double x1_raw_grid = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                        double y1_raw_grid = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                        double x2_raw_grid = (secondDoublePoint.x - centerX) / (double)CELL_SIZE;
                        double y2_raw_grid = (centerY - secondDoublePoint.y) / (double)CELL_SIZE;

                        // 不再进行0.5倍数吸附，直接使用原始坐标
                        String x1_formatted = formatCoordinateDisplay(x1_raw_grid); 
                        String y1_formatted = formatCoordinateDisplay(y1_raw_grid); 
                        String x2_formatted = formatCoordinateDisplay(x2_raw_grid); 
                        String y2_formatted = formatCoordinateDisplay(y2_raw_grid); 

                        // 输出铺面生成日志
                        chartLogTextArea.append(String.format("double(%s, \"%s\", %s, %s, %s, %s, %s, \"\")\n",
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction,
                            x1_formatted, y1_formatted,
                            x2_formatted, y2_formatted, isGlowing ? "true" : "false"));
                        
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
                        String x_formatted = formatCoordinateDisplay(finalGridX);
                        String y_formatted = formatCoordinateDisplay(finalGridY);
                        // 输出铺面生成日志
                        chartLogTextArea.append(String.format("%s(%s, \"%s\", %s, %s, %s)\n",
                            currentNoteType.name().toLowerCase(),
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, x_formatted, y_formatted, isGlowing ? "true" : "false"));
                        // 真正添加NOTE
                        noteManager.addNote(new Note(finalGridX, finalGridY, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing));
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
                        String x_formatted = formatCoordinateDisplay(x);
                        String y_formatted = formatCoordinateDisplay(y);
                        chartLogTextArea.append(String.format("%s(%s, \"%s\", %s, %s, %s)\n",
                            currentNoteType.name().toLowerCase(),
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, x_formatted, y_formatted, isGlowing ? "true" : "false"));
                        // 真正添加NOTE
                        noteManager.addNote(new Note(x, y, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing));
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
        
        // 难度
        JLabel difficultyLabel = new JLabel("难度:");
        difficultyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyLabel.setBounds(10, yOffset, labelWidth, fieldHeight);
        difficultyField = new JTextField();
        difficultyField.setBounds(70, yOffset, fieldWidth, fieldHeight);
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 保存信息后重新获取焦点
        });
        
        JButton clearButton = new JButton("清空信息");
        clearButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearButton.setBounds(70 + fieldWidth/2 + 5, yOffset, fieldWidth/2 - 5, 25);
        clearButton.addActionListener(e -> {
            clearSongInfo();
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 清空信息后重新获取焦点
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
        int notePropertyPanelCalculatedHeight = notePropertyToggleButton.isSelected() ? 180 : 30; // 根据是否展开设置高度
        notePropertyPanel.setBounds(LEFT_MARGIN + GRID_PANEL_SIZE + 625, notePropertyPanelY, 250, notePropertyPanelCalculatedHeight);

        // 调整主面板大小以容纳所有组件
        // 考虑新增的音乐进度条和时间显示部分
        int musicControlsBottomY = timeSlider.getY() + timeSlider.getHeight() + 30; // 进度条下方30px
        int contentBottomY = Math.max(notePropertyPanelY + notePropertyPanelCalculatedHeight, musicControlsBottomY);
        mainPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), contentBottomY + 20));
        mainPanel.revalidate();
        mainPanel.repaint();
        this.requestFocusInWindow(); // 确保主窗口在布局调整后重新获取焦点
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
        notePropertyExpandedPanel.setBounds(0, 30, 250, 150);
        
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
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 切换朝向后重新获取焦点
        });
        directionA.addActionListener(e -> {
            logManager.log("切换NOTE朝向: a");
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 切换朝向后重新获取焦点
        });
        directionS.addActionListener(e -> {
            logManager.log("切换NOTE朝向: s");
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 切换朝向后重新获取焦点
        });
        directionD.addActionListener(e -> {
            logManager.log("切换NOTE朝向: d");
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 切换朝向后重新获取焦点
        });

        directionExpandedPanel.add(directionW);
        directionExpandedPanel.add(directionA);
        directionExpandedPanel.add(directionS);
        directionExpandedPanel.add(directionD);
        
        // 创建发光选项
        glowCheckBox = new JCheckBox("发光");
        glowCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        glowCheckBox.setBounds(10, 110, 230, 25); // 调整Y坐标以确保显示
        glowCheckBox.addActionListener(e -> {
            logManager.log("发光选项切换: " + glowCheckBox.isSelected());
            SwingUtilities.invokeLater(() -> requestFocusInWindow()); // 发光选项切换后重新获取焦点
        });

        // 添加组件到展开面板
        notePropertyExpandedPanel.add(directionToggleButton);
        notePropertyExpandedPanel.add(directionExpandedPanel);
        notePropertyExpandedPanel.add(glowCheckBox);
        
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
                
                // 绘制当前NOTE（在网格模式下，仍吸附有效点）
                if (currentNoteType != NoteType.EXECUTION && 
                    currentNoteType != NoteType.FLICK_LEFT && 
                    currentNoteType != NoteType.FLICK_RIGHT) {
                    // 获取当前NOTE类型的颜色，并设置半透明
                    Color noteColor = currentNoteType.getColor();
                    Color transparentColor = new Color(
                            noteColor.getRed(), 
                            noteColor.getGreen(), 
                            noteColor.getBlue(), 
                            180);
                    
                    g.setColor(transparentColor);
                    
                    // 计算最近的网格点
                    Point nearestPoint = findNearestGridPoint(mousePosition.x, mousePosition.y);
                    
                    // 绘制NOTE方块，确保居中，移除额外偏移
                    int squareX = nearestPoint.x - FOLLOW_SQUARE_SIZE / 2;
                    int squareY = nearestPoint.y - FOLLOW_SQUARE_SIZE / 2;
                    g.fillRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                    
                    // 绘制边框
                    g.setColor(currentNoteType.getColor());
                    g.drawRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                }
                
                // ---------- 伪3D NOTE动画显示 ----------
                if (musicPlayer != null && beatCalculator != null && noteManager != null) {
                    long currentTime = musicPlayer.getCurrentTimeMicroseconds();
                    double currentBeat = beatCalculator.microsecondsToBeats(currentTime);
                    java.util.List<Note> notesToShow = noteManager.getNotesForCurrentBeat(currentTime, displayBeatsCount, beatCalculator);
                    centerX = getWidth() / 2;
                    centerY = getHeight() / 2;
                    int n = displayBeatsCount - 1;
                    // 按拍数降序排序，保证后拍NOTE先绘制，前拍NOTE后绘制（在上层）
                    notesToShow.sort((a, b) -> Double.compare(
                        beatCalculator.microsecondsToBeats(b.getTimeMicroseconds()),
                        beatCalculator.microsecondsToBeats(a.getTimeMicroseconds())
                    ));
                    for (Note note : notesToShow) {
                        double noteBeat = beatCalculator.microsecondsToBeats(note.getTimeMicroseconds());
                        double k = noteBeat - currentBeat;
                        if (k < 0 || k > n) continue;
                        // 伪3D动画进度
                        double progress = (n == 0) ? 1.0 : (n - k) / n;
                        // 目标像素坐标
                        double x = note.getX() * 100;
                        double y = -note.getY() * 100;
                        // 起点（内部方框投影）
                        double x0 = x / 5.0;
                        double y0 = y / 5.0;
                        // 插值位置
                        double cx = x0 + (x - x0) * progress;
                        double cy = y0 + (y - y0) * progress;
                        // 插值大小
                        double size = 20 + (100 - 20) * progress;
                        // 透明度插值
                        int alpha = (int)(255 - (k / n) * (255 - 80));
                        alpha = Math.max(0, Math.min(255, alpha));
                        // 填充色为NOTE类型色
                        Color fillColor = note.getColor();
                        g.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha));
                        g.fillRect((int)(centerX + cx - size/2), (int)(centerY + cy - size/2), (int)size, (int)size);
                        // 朝向边框
                        Graphics2D g2 = (Graphics2D) g;
                        Color borderColor = NOTE_BORDER_W;
                        switch (note.getDirection() != null ? note.getDirection() : "w") {
                            case "a": borderColor = NOTE_BORDER_A; break;
                            case "s": borderColor = NOTE_BORDER_S; break;
                            case "d": borderColor = NOTE_BORDER_D; break;
                        }
                        g2.setStroke(NOTE_BORDER_STROKE);
                        g2.setColor(borderColor);
                        g2.drawRect((int)(centerX + cx - size/2), (int)(centerY + cy - size/2), (int)size, (int)size);
                        // 发光
                        if (note.isGlowing()) {
                            g2.setColor(NOTE_GLOW_COLOR);
                            g2.setStroke(NOTE_GLOW_STROKE);
                            int px = (int)(centerX + cx);
                            int py = (int)(centerY + cy);
                            g2.drawLine(px-2, py, px+2, py);
                            g2.drawLine(px, py-2, px, py+2);
                        }
                    }
                }
                // 仅在网格模式下显示吸附后的NOTE，不显示跟随鼠标的NOTE
                if (isMouseFollowMode && currentNoteType != NoteType.EXECUTION && 
                    currentNoteType != NoteType.FLICK_LEFT && currentNoteType != NoteType.FLICK_RIGHT) {
                    // 获取当前NOTE类型的颜色，并设置半透明
                    Color noteColor = currentNoteType.getColor();
                    Color transparentColor = new Color(
                            noteColor.getRed(), 
                            noteColor.getGreen(), 
                            noteColor.getBlue(), 
                            180); // 透明度
                    g.setColor(transparentColor);
                    // 只吸附到最近的网格点
                    Point nearestPoint = findNearestGridPoint(mousePosition.x, mousePosition.y);
                    int squareX = nearestPoint.x - FOLLOW_SQUARE_SIZE / 2;
                    int squareY = nearestPoint.y - FOLLOW_SQUARE_SIZE / 2;
                    g.fillRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
                    // 绘制边框
                    g.setColor(currentNoteType.getColor());
                    g.drawRect(squareX, squareY, FOLLOW_SQUARE_SIZE, FOLLOW_SQUARE_SIZE);
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
                    currentNoteType != NoteType.FLICK_RIGHT) {
                    
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
                                                        nearestPoint.x - centerX, nearestPoint.y - centerY, 
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
                // 在网格模式下，只在鼠标跟随模式下才处理点击事件
                // 手动输入模式下通过"确认生成NOTE"按钮处理
                if (!isMouseFollowMode) {
                    return; 
                }

                String direction = getDirection();
                boolean isGlowing = glowCheckBox.isSelected();

                if (currentNoteType == NoteType.EXECUTION || 
                    currentNoteType == NoteType.FLICK_LEFT || 
                    currentNoteType == NoteType.FLICK_RIGHT) {
                    if (currentNoteType == NoteType.EXECUTION) {
                        chartLogTextArea.append("execution({})\n");
                    } else {
                        String flickDirection = currentNoteType == NoteType.FLICK_LEFT ? "left" : "right";
                        chartLogTextArea.append(String.format("flick(%s, \"%s\", \"%s\", %b, \"\")\n",
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, flickDirection, isGlowing));
                    }
                } else if (currentNoteType == NoteType.DOUBLE) {
                    if (!isFirstDoublePlaced) {
                        // 放置第一个Double音符
                        // 对于 chart log，我们使用原始鼠标位置
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
                        // 对于 chart log，我们使用原始鼠标位置
                        Point secondDoublePoint = new Point(e.getX(), e.getY()); // 获取第二个点的原始像素坐标

                        // 将原始像素坐标转换为网格坐标，用于日志输出
                        int centerX = panel.getWidth() / 2;
                        int centerY = panel.getHeight() / 2;
                        
                        double x1_raw_grid = (firstDoublePoint.x - centerX) / (double)CELL_SIZE;
                        double y1_raw_grid = (centerY - firstDoublePoint.y) / (double)CELL_SIZE;
                        double x2_raw_grid = (secondDoublePoint.x - centerX) / (double)CELL_SIZE;
                        double y2_raw_grid = (centerY - secondDoublePoint.y) / (double)CELL_SIZE;

                        // 不再进行0.5倍数吸附，直接使用原始坐标
                        String x1_formatted = formatCoordinateDisplay(x1_raw_grid); 
                        String y1_formatted = formatCoordinateDisplay(y1_raw_grid); 
                        String x2_formatted = formatCoordinateDisplay(x2_raw_grid); 
                        String y2_formatted = formatCoordinateDisplay(y2_raw_grid); 

                        // 输出铺面生成日志
                        chartLogTextArea.append(String.format("double(%s, \"%s\", %s, %s, %s, %s, %s, \"\")\n",
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction,
                            x1_formatted, y1_formatted,
                            x2_formatted, y2_formatted, isGlowing ? "true" : "false"));
                        
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
                        String x_formatted = formatCoordinateDisplay(finalGridX);
                        String y_formatted = formatCoordinateDisplay(finalGridY);
                        // 输出铺面生成日志
                        chartLogTextArea.append(String.format("%s(%s, \"%s\", %s, %s, %s)\n",
                            currentNoteType.name().toLowerCase(),
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, x_formatted, y_formatted, isGlowing ? "true" : "false"));
                        // 真正添加NOTE
                        noteManager.addNote(new Note(finalGridX, finalGridY, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing));
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
                        String x_formatted = formatCoordinateDisplay(x);
                        String y_formatted = formatCoordinateDisplay(y);
                        chartLogTextArea.append(String.format("%s(%s, \"%s\", %s, %s, %s)\n",
                            currentNoteType.name().toLowerCase(),
                            beatCalculator.formatTimeToSecondsWithDecimal(musicPlayer.getCurrentTimeMicroseconds()),
                            direction, x_formatted, y_formatted, isGlowing ? "true" : "false"));
                        // 真正添加NOTE
                        noteManager.addNote(new Note(x, y, currentNoteType, musicPlayer.getCurrentTimeMicroseconds(), direction, isGlowing));
                        panel.repaint();
                    }
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
    
    // Getter和Setter方法
    public String getSongName() {
        return songName;
    }
    
    public void setSongName(String songName) {
        this.songName = songName;
    }
    
    public String getComposer() {
        return composer;
    }
    
    public void setComposer(String composer) {
        this.composer = composer;
    }
    
    public String getChartAuthor() {
        return chartAuthor;
    }
    
    public void setChartAuthor(String chartAuthor) {
        this.chartAuthor = chartAuthor;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getOffset() {
        return offset;
    }
    
    public void setOffset(String offset) {
        this.offset = offset;
    }
    
    public String getBpm() {
        return bpm;
    }
    
    public void setBpm(String bpm) {
        this.bpm = bpm;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
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
                logManager.log("错误: 不支持的音频文件格式。请选择OGG文件。");
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
                    ((JRadioButton) component).setEnabled(enable);
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
        currentTimeLabel.setText(beatCalculator.formatTime(displayTime));
        totalTimeLabel.setText("/ " + beatCalculator.formatTime(total));
        double totalBeats = beatCalculator.microsecondsToBeats(displayTime) + 1e-6;
        int beatsPerMeasure = beatCalculator.getBeatsPerMeasure();
        int currentMeasure = (int)(totalBeats / beatsPerMeasure);
        int currentBeatInMeasure;
        if (totalBeats < 1e-4) {
            currentBeatInMeasure = 0;
        } else {
            currentBeatInMeasure = (int)(totalBeats % beatsPerMeasure) + 1;
        }
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
}