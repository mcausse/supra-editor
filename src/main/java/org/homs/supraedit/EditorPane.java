package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.homs.supraedit.util.ExceptionUtils;
import org.homs.supraedit.util.TextFileUtils;

public class EditorPane extends JPanel {

    private static final long serialVersionUID = -3040324544220338224L;

    final String[] encodingStrings = { "UTF8", "ISO-8859-1", "Cp1252" };

    final Color fontColor = Color.BLACK;
    final Color cursorColor = Color.RED;
    final Color backgroundColor = Color.WHITE;
    final Color selectionBackgroundColor = Color.BLUE;
    final Color selectionColor = Color.WHITE;

    String filename; // set by "Open" or "Save As"
    String charsetName;

    final JMenuBar menuBar = new JMenuBar();

    final JButton lineWrapButton;
    final JButton recordMacroButton;
    final JButton playMacroButton;
    final JButton playMacroEofButton;
    final JTextField cmdTextField;

    final JTextArea textArea;

    public EditorPane() {

        super(new BorderLayout());

        add(menuBar, BorderLayout.NORTH);

        lineWrapButton = new JButton("wrap");
        lineWrapButton.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(lineWrapButton);

        recordMacroButton = new JButton("Rec");
        recordMacroButton.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(recordMacroButton);

        playMacroButton = new JButton("Play");
        playMacroButton.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(playMacroButton);

        playMacroEofButton = new JButton("Play EOF");
        playMacroEofButton.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(playMacroEofButton);

        cmdTextField = new JTextField();
        menuBar.add(cmdTextField);

        this.textArea = new JTextArea();
        MacroRecording macroRecording = new MacroRecording(textArea, cmdTextField);

        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == lineWrapButton) {
                    textArea.setLineWrap(!textArea.getLineWrap());
                    textArea.setWrapStyleWord(textArea.getLineWrap());
                    textArea.requestFocus();
                } else if (e.getSource() == recordMacroButton) {
                    if (macroRecording.isRecording()) {
                        macroRecording.recordMacroStop();
                        recordMacroButton.setText("Rec");
                        playMacroButton.setEnabled(true);
                        playMacroEofButton.setEnabled(true);
                    } else {
                        macroRecording.recordMacroStart();
                        recordMacroButton.setText("Recording");
                        playMacroButton.setEnabled(false);
                        playMacroEofButton.setEnabled(false);
                    }
                    textArea.requestFocus();
                } else if (e.getSource() == playMacroButton) {

                    recordMacroButton.setEnabled(false);
                    playMacroButton.setEnabled(false);
                    playMacroEofButton.setEnabled(false);

                    macroRecording.playMacro();

                    playMacroButton.setEnabled(true);
                    playMacroEofButton.setEnabled(true);
                    recordMacroButton.setEnabled(true);

                    textArea.requestFocus();
                } else if (e.getSource() == playMacroEofButton) {

                    recordMacroButton.setEnabled(false);
                    playMacroButton.setEnabled(false);
                    playMacroEofButton.setEnabled(false);

                    while (textArea.getCaretPosition() < textArea.getDocument().getLength()) {
                        macroRecording.playMacro();
                    }

                    playMacroButton.setEnabled(true);
                    playMacroEofButton.setEnabled(true);
                    recordMacroButton.setEnabled(true);

                    textArea.requestFocus();
                }

            }
        };

        lineWrapButton.addActionListener(actionListener);
        recordMacroButton.addActionListener(actionListener);
        playMacroButton.addActionListener(actionListener);
        playMacroEofButton.addActionListener(actionListener);
        cmdTextField.addActionListener(actionListener);

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        {
            TextLineNumber tln = new TextLineNumber(textArea);
            tln.setMinimumDisplayDigits(3);
            scrollPane.setRowHeaderView(tln);
        }

        {
            final UndoManager undo = new UndoManager();
            undo.discardAllEdits();
            undo.setLimit(200);

            textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
                @Override
                public void undoableEditHappened(UndoableEditEvent evt) {
                    undo.addEdit(evt.getEdit());
                }
            });

            // Create an undo action and add it to the text component
            textArea.getActionMap().put("Undo", new AbstractAction("Undo") {

                private static final long serialVersionUID = 6230534001961891325L;

                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canUndo()) {
                            undo.undo();
                        }
                    } catch (CannotUndoException e) {
                    }
                }
            });

            textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

            textArea.getActionMap().put("Redo", new AbstractAction("Redo") {

                private static final long serialVersionUID = 3505672699323520092L;

                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canRedo()) {
                            undo.redo();
                        }
                    } catch (CannotRedoException e) {
                    }
                }
            });
            textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        }

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(textArea.getLineWrap());
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textArea.setFont(font);
        textArea.setEditable(true);
        textArea.setSelectionColor(selectionBackgroundColor);
        textArea.setSelectedTextColor(selectionColor);
        textArea.setBackground(backgroundColor);
        textArea.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, textArea.getBackground()));
        DefaultCaret c = new DefaultCaret();
        textArea.setCaret(c);
        textArea.getCaret().setBlinkRate(500);
        textArea.setCaretColor(cursorColor);
        textArea.setCaretPosition(0);
        textArea.getCaret().setVisible(true);

        Closure onDoRecord = () -> {
            recordMacroButton.doClick();
        };
        Closure onDoPlay = () -> {
            playMacroButton.doClick();
        };
        Closure onDoPlayEof = () -> {
            playMacroEofButton.doClick();
        };
        Closure onTabToLeft = () -> {
            JTabbedPane tabs = (JTabbedPane) getParent();
            int selected = tabs.getSelectedIndex();
            if (selected > 0) {
                tabs.setSelectedIndex(selected - 1);
            } else {
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
            }
        };
        Closure onTabToRight = () -> {
            JTabbedPane tabs = (JTabbedPane) getParent();
            int selected = tabs.getSelectedIndex();
            if (selected < tabs.getTabCount() - 1) {
                tabs.setSelectedIndex(selected + 1);
            } else {
                tabs.setSelectedIndex(0);
            }
        };

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new SupraKeyEventDispatcher(
                macroRecording, cmdTextField, onDoRecord, onDoPlay, onDoPlayEof, onTabToLeft, onTabToRight));

        textArea.requestFocus();

    }

    @Override
    public void requestFocus() {
        this.textArea.requestFocus();
    }

    public String getFilenameShort() {
        return new File(filename).getName();
    }

    public String getFilenameFull() {
        return filename;
    }

    protected void updateJTabbedPane() {
        JTabbedPane tabs = (JTabbedPane) getParent();
        int selectedIndex = tabs.getSelectedIndex();
        tabs.setTitleAt(selectedIndex, getFilenameShort() + "/" + charsetName);
        tabs.setToolTipTextAt(selectedIndex, getFilenameFull());
    }

    public void loadFile() {
        JFileChooser fc = new JFileChooser();
        // fc.setCurrentDirectory(dir); //TODO
        // fc.setFileView(fileView); //TODO
        fc.setFileHidingEnabled(false);
        fc.setPreferredSize(getSize());
        fc.setPreferredSize(new Dimension(800, 800));

        JComboBox<String> encodingCombo;
        {

            encodingCombo = new JComboBox<>(encodingStrings);
            encodingCombo.setSize(new Dimension(200, 50));
            JPanel p = new JPanel(new BorderLayout());
            p.add(encodingCombo, BorderLayout.NORTH);
            fc.setAccessory(p);
        }

        String name = null;
        if (fc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
            name = fc.getSelectedFile().getAbsolutePath();
        } else {
            return; // user cancelled
        }

        String charsetName = encodingStrings[encodingCombo.getSelectedIndex()];
        loadFile(name, charsetName);
        textArea.setCaretPosition(0);
    }

    // TODO juntar els loadFile
    private void loadFile(String name, String charsetName) {
        try {

            this.filename = name;
            this.charsetName = charsetName;

            File f = new File(name);
            Charset charset = Charset.forName(charsetName);
            textArea.setText(TextFileUtils.read(f, charset));

            updateJTabbedPane();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cannot load file: " + name + "\n" + ExceptionUtils.toString(e),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void saveFile(boolean saveAs) {

        String name;
        if (saveAs) {
            name = null;
        } else {
            name = this.filename;
        }

        if (name == null) { // get filename from user
            JFileChooser fc = new JFileChooser();
            // fc.setCurrentDirectory(dir); //TODO
            // fc.setFileView(fileView); //TODO
            fc.setFileHidingEnabled(false);
            fc.setPreferredSize(getSize());
            fc.setPreferredSize(new Dimension(800, 800));

            JComboBox<String> encodingCombo;
            {

                encodingCombo = new JComboBox<>(encodingStrings);
                if (this.charsetName != null) {
                    encodingCombo.setSelectedItem(this.charsetName);
                }
                encodingCombo.setSize(new Dimension(200, 50));
                JPanel p = new JPanel(new BorderLayout());
                p.add(encodingCombo, BorderLayout.NORTH);
                fc.setAccessory(p);
            }

            if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION) {
                name = fc.getSelectedFile().getAbsolutePath();
            }

            this.charsetName = (String) encodingCombo.getSelectedItem();
            this.filename = name;
        }
        if (name != null) { // else user cancelled
            try {
                File f = new File(name);
                Charset charset = Charset.forName(this.charsetName);
                TextFileUtils.write(f, charset, textArea.getText());
                JOptionPane.showMessageDialog(null, "Saved to " + filename + " in " + charsetName, "Save File",
                        JOptionPane.PLAIN_MESSAGE);
                updateJTabbedPane();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot write to file: " + name + "\n" + ExceptionUtils.toString(e),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

}
