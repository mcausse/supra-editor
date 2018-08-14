package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.homs.supraedit.util.ExceptionUtils;

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
        }
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void playMacro() {
        isRecording = false;

        // /**
        // * TODO prova de serialització de macros!!!! sembla que funciona
        // */
        // {
        // KeyEventSerializer s = new KeyEventSerializer();
        // Map<String, Component> componentsMap = new LinkedHashMap<>();
        // componentsMap.put(textArea.getClass().getName(), textArea);
        // componentsMap.put(cmdTextField.getClass().getName(), cmdTextField);
        //
        // List<KeyEvent> eventsRecorded2 = s.deserialize(componentsMap,
        // s.serialize(eventsRecorded));
        //
        // eventsRecorded.clear();
        // eventsRecorded.addAll(eventsRecorded2);
        // }

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

            if (textArea.getCaretPosition() >= textArea.getDocument().getLength()) {
                // EOF => abortar
                break;
            }
        }
    }

    public void loadFile() {
        JFileChooser fc = new JFileChooser();
        // fc.setCurrentDirectory(dir); //TODO
        fc.setFileHidingEnabled(false);
        fc.setPreferredSize(new Dimension(800, 800));

        JComboBox<String> encodingCombo;
        {

            encodingCombo = new JComboBox<>(Constants.encodingStrings);
            encodingCombo.setSize(new Dimension(200, 50));
            JPanel p = new JPanel(new BorderLayout());
            p.add(encodingCombo, BorderLayout.NORTH);
            fc.setAccessory(p);
        }

        if (fc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
            String name = fc.getSelectedFile().getAbsolutePath();

            try {

                KeyEventSerializer s = new KeyEventSerializer();
                Map<String, Component> componentsMap = new LinkedHashMap<>();
                componentsMap.put(textArea.getClass().getName(), textArea);
                componentsMap.put(cmdTextField.getClass().getName(), cmdTextField);

                this.eventsRecorded.clear();
                this.eventsRecorded.addAll(s.load(componentsMap, name));

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot load file: " + name + "\n" + ExceptionUtils.toString(e),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void saveFile() {

        JFileChooser fc = new JFileChooser();
        // fc.setCurrentDirectory(dir); //TODO
        fc.setFileHidingEnabled(false);
        fc.setPreferredSize(new Dimension(800, 800));

        JComboBox<String> encodingCombo;
        {

            encodingCombo = new JComboBox<>(Constants.encodingStrings);
            encodingCombo.setSize(new Dimension(200, 50));
            JPanel p = new JPanel(new BorderLayout());
            p.add(encodingCombo, BorderLayout.NORTH);
            fc.setAccessory(p);
        }

        if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION) {
            String filename = fc.getSelectedFile().getAbsolutePath();

            try {

                KeyEventSerializer s = new KeyEventSerializer();
                s.save(this.eventsRecorded, filename);

                JOptionPane.showMessageDialog(null, "Saved macro to " + filename, "Save File",
                        JOptionPane.PLAIN_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Cannot write to file: " + filename + "\n" + ExceptionUtils.toString(e), "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

    }

}