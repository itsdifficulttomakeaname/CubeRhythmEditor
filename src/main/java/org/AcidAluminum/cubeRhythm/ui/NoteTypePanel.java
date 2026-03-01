package org.AcidAluminum.cubeRhythm.ui;

import javax.swing.*;
import org.AcidAluminum.cubeRhythm.NoteType;
import java.awt.*;
import java.awt.event.ActionListener;

public class NoteTypePanel extends JPanel {
    private ButtonGroup buttonGroup;
    private JPanel panel;

    public NoteTypePanel(NoteType currentNoteType, ActionListener listener) {
        setLayout(new GridLayout(NoteType.values().length, 1, 0, 5));
        setBackground(new Color(240, 240, 240));
        buttonGroup = new ButtonGroup();
        for (NoteType type : NoteType.values()) {
            JRadioButton radioButton = new JRadioButton(type.getDisplayName());
            radioButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            radioButton.setBackground(type.getColor());
            radioButton.setForeground(getContrastColor(type.getColor()));
            if (type == currentNoteType) {
                radioButton.setSelected(true);
            }
            radioButton.addActionListener(listener);
            buttonGroup.add(radioButton);
            add(radioButton);
        }
    }

    private Color getContrastColor(Color background) {
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
} 