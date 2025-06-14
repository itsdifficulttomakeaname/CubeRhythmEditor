package org.project1;

import javax.swing.JTextArea;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private JTextArea logTextArea;

    public LogManager(JTextArea logTextArea) {
        this.logTextArea = logTextArea;
    }

    public void log(String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logEntry = "[" + timestamp + "] " + message + "\n";
        logTextArea.append(logEntry);
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        System.out.println(logEntry.trim());
    }
} 