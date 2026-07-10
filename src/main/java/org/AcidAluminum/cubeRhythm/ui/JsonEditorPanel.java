package org.AcidAluminum.cubeRhythm.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonEditorPanel extends JPanel {
    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final JLabel statusLabel;

    // 字段值补全表：key 是字段名（出现在 "key": 光标处），value 是候选列表
    private static final Map<String, List<String>> COMPLETIONS = Map.of(
        "type", List.of("tap","drag","flick","double","execution","hold",
                        "fake_tap","fake_hold","fake_drag","fake_flick","fake_double",
                        "mine_tap","mine_drag","mine_double"),
        "face", List.of("W","A","S","D"),
        "glowing", List.of("true","false"),
        "easing", List.of("LINEAR","EASE_IN","EASE_OUT","EASE_IN_OUT",
                          "BOUNCE_IN","BOUNCE_OUT","ELASTIC_IN","ELASTIC_OUT")
    );

    // 弹出菜单用于补全候选
    private final JPopupMenu popup = new JPopupMenu();

    public JsonEditorPanel() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setTabSize(2);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
            theme.apply(textArea);
        } catch (IOException | NullPointerException e) {
            // 使用默认主题
        }

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB && !e.isShiftDown()) {
                    if (showCompletion()) e.consume();
                }
            }
        });

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        add(statusLabel, BorderLayout.SOUTH);
    }

    /** 根据光标前的内容判断字段名，展示候选菜单；返回 true 表示已触发补全 */
    private boolean showCompletion() {
        try {
            int pos = textArea.getCaretPosition();
            String text = textArea.getText();
            // 向前找到本行内容
            int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
            String prefix = text.substring(lineStart, pos);
            // 匹配 "fieldName": " 或 "fieldName": 且光标在值引号内/后
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)$")
                .matcher(prefix);
            if (!m.find()) return false;
            String fieldName = m.group(1);
            String typed = m.group(2);
            List<String> candidates = COMPLETIONS.get(fieldName);
            if (candidates == null) return false;

            List<String> filtered = candidates.stream()
                .filter(c -> c.startsWith(typed))
                .toList();
            if (filtered.isEmpty()) return false;

            // 单个候选直接插入
            if (filtered.size() == 1) {
                insertCompletion(typed, filtered.get(0));
                return true;
            }

            // 多个候选用弹出菜单
            popup.removeAll();
            for (String candidate : filtered) {
                JMenuItem item = new JMenuItem(candidate);
                item.addActionListener(ev -> insertCompletion(typed, candidate));
                popup.add(item);
            }
            Rectangle r = textArea.modelToView(pos);
            popup.show(textArea, r.x, r.y + r.height);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void insertCompletion(String typed, String full) {
        try {
            int pos = textArea.getCaretPosition();
            // 删掉已输入的前缀，插入完整值
            textArea.getDocument().remove(pos - typed.length(), typed.length());
            textArea.getDocument().insertString(pos - typed.length(), full, null);
        } catch (Exception ignored) {}
    }

    public void setText(String text) {
        textArea.setText(text);
        textArea.discardAllEdits();
        textArea.setCaretPosition(0);
    }

    public String getText() {
        return textArea.getText();
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void formatJson() {
        String text = textArea.getText().trim();
        if (text.isEmpty()) return;
        try {
            String formatted = new GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(text));
            textArea.setText(formatted);
            textArea.setCaretPosition(0);
            setStatus("格式化完成", new Color(0, 150, 0));
        } catch (Exception e) {
            setStatus("格式化失败：JSON 语法错误", Color.RED);
        }
    }

    public boolean validateJson() {
        String text = textArea.getText().trim();
        if (text.isEmpty()) {
            setStatus("空内容", Color.GRAY);
            return true;
        }
        try {
            new com.google.gson.Gson().fromJson(text, com.google.gson.JsonElement.class);
            setStatus("JSON 格式正确", new Color(0, 150, 0));
            return true;
        } catch (com.google.gson.JsonSyntaxException e) {
            String msg = e.getMessage();
            if (msg != null && msg.length() > 80) {
                msg = msg.substring(0, 80) + "...";
            }
            setStatus("JSON 错误: " + msg, Color.RED);
            return false;
        }
    }

    public void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
