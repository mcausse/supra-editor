package org.homs.supraedit;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MacroRecording {

    final JTextArea textArea;
    final JTextField cmdTextField;

    boolean isRecording = false;
    final List<KeyEvent> eventsRecorded = new ArrayList<>();

    public MacroRecording(JTextArea textArea, JTextField cmdTextField) {
        super();
        this.textArea = textArea;
        this.cmdTextField = cmdTextField;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void recordMacroStart() {
        isRecording = true;
        eventsRecorded.clear();
    }

    public void recordMacroStop() {
        isRecording = false;
    }

    public void record(KeyEvent ke) {
        if (isRecording) {
            this.eventsRecorded.add(ke);
             System.out.println("recording: " + ke);
        }
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void playMacro() {
        isRecording = false;
        for (KeyEvent e : eventsRecorded) {
            if (e.getSource() == textArea) {
                cmdTextField.requestFocus();
                textArea.dispatchEvent(new KeyEvent((Component) e.getSource(), e.getID(), System.currentTimeMillis(),
                        e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
            } else if (e.getSource() == cmdTextField) {
                cmdTextField.requestFocus();
                cmdTextField.dispatchEvent(new KeyEvent((Component) e.getSource(), e.getID(),
                        System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
            }
        }
    }

}