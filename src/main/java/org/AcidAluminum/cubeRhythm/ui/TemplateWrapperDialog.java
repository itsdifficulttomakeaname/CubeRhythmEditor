package org.AcidAluminum.cubeRhythm.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TemplateWrapperDialog extends JDialog {

    private static final String TEMPLATE_DIR = "animation_templates";
    private static final String[] TYPES = {
        "groupEvents", "groupEvent", "inlineEvent", "execution", "actions", "track", "notes"
    };

    private final JTextArea contentArea = new JTextArea(15, 50);
    private final JComboBox<String> typeBox = new JComboBox<>(TYPES);
    private final JTextField nameField = new JTextField(20);
    private final JTextField descField = new JTextField(20);
    private final JTextField authorField = new JTextField(20);
    private final Runnable onSaved;

    public TemplateWrapperDialog(Frame owner, Runnable onSaved) {
        super(owner, "模板包装器", true);
        this.onSaved = onSaved;
        typeBox.setEditable(true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        contentArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        contentArea.setLineWrap(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(3, 4, 3, 4);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.insets = new Insets(3, 4, 3, 4);

        int row = 0;
        lc.gridy = fc.gridy = row++; lc.gridx = 0; fc.gridx = 1;
        form.add(new JLabel("模板类型 (_type):"), lc); form.add(typeBox, fc);

        lc.gridy = fc.gridy = row++; form.add(new JLabel("模板名称 (文件名):"), lc); form.add(nameField, fc);
        lc.gridy = fc.gridy = row++; form.add(new JLabel("描述 (可选):"), lc);        form.add(descField, fc);
        lc.gridy = fc.gridy = row++;  form.add(new JLabel("作者 (可选):"), lc);       form.add(authorField, fc);

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridy = row; cc.gridx = 0; cc.gridwidth = 2;
        cc.fill = GridBagConstraints.BOTH; cc.weightx = 1; cc.weighty = 1;
        cc.insets = new Insets(4, 4, 4, 4);

        JPanel contentHeader = new JPanel(new BorderLayout());
        contentHeader.add(new JLabel("模板内容（JSON 片段，不检查格式）："), BorderLayout.WEST);
        JButton fmtBtn = new JButton("格式化");
        fmtBtn.addActionListener(e -> formatContent());
        contentHeader.add(fmtBtn, BorderLayout.EAST);

        GridBagConstraints hc = new GridBagConstraints();
        hc.gridy = row; hc.gridx = 0; hc.gridwidth = 2;
        hc.fill = GridBagConstraints.HORIZONTAL; hc.weightx = 1;
        hc.insets = new Insets(4, 4, 0, 4);
        form.add(contentHeader, hc);

        cc.gridy = ++row;
        form.add(new JScrollPane(contentArea), cc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton cancelBtn = new JButton("取消");
        JButton createBtn = new JButton("创建模板");
        cancelBtn.addActionListener(e -> dispose());
        createBtn.addActionListener(e -> onCreateClicked());
        bottom.add(cancelBtn);
        bottom.add(createBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void formatContent() {
        String text = contentArea.getText().trim();
        if (text.isEmpty()) return;
        try {
            String formatted = new GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(text));
            contentArea.setText(formatted);
        } catch (Exception ignored) {
            // 内容不是合法 JSON，跳过，不提示（用户可能在编辑中）
        }
    }

    private void onCreateClicked() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写模板名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String type = ((String) typeBox.getSelectedItem());
        if (type == null || type.isBlank()) {
            JOptionPane.showMessageDialog(this, "请选择或输入模板类型", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File dir = new File(TEMPLATE_DIR);
        dir.mkdirs();
        File outFile = new File(dir, name + ".json");

        if (outFile.exists()) {
            int choice = JOptionPane.showConfirmDialog(this,
                outFile.getName() + " 已存在，覆盖？", "确认覆盖", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        String desc = descField.getText().trim();
        String author = authorField.getText().trim();
        String content = contentArea.getText();

        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"_type\": \"").append(escapeJson(type)).append("\"");
        if (!desc.isEmpty())   sb.append(",\n  \"_description\": \"").append(escapeJson(desc)).append("\"");
        if (!author.isEmpty()) sb.append(",\n  \"_author\": \"").append(escapeJson(author)).append("\"");
        sb.append(",\n  \"content\": ").append(content.strip());
        sb.append("\n}");

        try {
            Files.writeString(outFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (onSaved != null) onSaved.run();
        dispose();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static void open(Frame owner, Runnable onSaved) {
        new TemplateWrapperDialog(owner, onSaved).setVisible(true);
    }
}
