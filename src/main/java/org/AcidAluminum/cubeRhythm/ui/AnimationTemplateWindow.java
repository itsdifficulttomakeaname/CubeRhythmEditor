package org.AcidAluminum.cubeRhythm.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class AnimationTemplateWindow extends JFrame {

    private static final String TEMPLATE_DIR = "animation_templates";

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> templateList = new JList<>(listModel);
    private final JsonEditorPanel editorPanel = new JsonEditorPanel();
    private File[] templateFiles = new File[0];

    public AnimationTemplateWindow(Frame owner) {
        super("动画模板");
        setSize(1024, 768);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        templateList.addListSelectionListener(this::onSelectTemplate);

        JScrollPane listScroll = new JScrollPane(templateList);
        listScroll.setBorder(BorderFactory.createTitledBorder("模板列表"));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(editorPanel, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton formatBtn = new JButton("格式化");
        JButton copyBtn = new JButton("复制");
        JButton closeBtn = new JButton("关闭");
        formatBtn.addActionListener(e -> formatEditor());
        copyBtn.addActionListener(e -> copyToClipboard());
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(formatBtn);
        bottomBar.add(copyBtn);
        bottomBar.add(closeBtn);
        rightPanel.add(bottomBar, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, rightPanel);
        split.setResizeWeight(0.25);
        split.setDividerLocation(256);

        add(split);
    }

    public void reload() {
        File dir = new File(TEMPLATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            templateFiles = new File[0];
            listModel.clear();
            editorPanel.setText("");
            editorPanel.setStatus("animation_templates/ 目录中暂无模板，请添加 .json 文件", Color.GRAY);
            return;
        }

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        templateFiles = files;
        listModel.clear();
        for (File f : files) {
            listModel.addElement(f.getName().replaceAll("(?i)\\.json$", ""));
        }
        templateList.setSelectedIndex(0);
    }

    private void onSelectTemplate(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int idx = templateList.getSelectedIndex();
        if (idx < 0 || idx >= templateFiles.length) return;
        try {
            String content = Files.readString(templateFiles[idx].toPath(), StandardCharsets.UTF_8);
            editorPanel.setText(content);
            editorPanel.validateJson();
        } catch (IOException ex) {
            editorPanel.setText("");
            editorPanel.setStatus("读取失败: " + ex.getMessage(), Color.RED);
        }
    }

    private void copyToClipboard() {
        String text = extractContent(editorPanel.getText());
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
        editorPanel.setStatus("已复制到剪贴板（content 字段）", new Color(0, 150, 0));
    }

    private void formatEditor() {
        String text = editorPanel.getText().trim();
        if (text.isEmpty()) return;
        try {
            String formatted = new GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(text));
            editorPanel.setText(formatted);
            editorPanel.setStatus("格式化完成", new Color(0, 150, 0));
        } catch (Exception e) {
            editorPanel.setStatus("格式化失败：JSON 语法错误", Color.RED);
        }
    }

    private static String extractContent(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("content")) {
                return new GsonBuilder().setPrettyPrinting().create().toJson(root.get("content"));
            }
        } catch (Exception ignored) {}
        return json;
    }

    public static void open(Frame owner) {
        AnimationTemplateWindow win = new AnimationTemplateWindow(owner);
        win.reload();
        win.setVisible(true);
    }
}
