package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

    String filename; // set by "Open" or "Save As"
    String charsetName;

    final JMenuBar menuBar = new JMenuBar();

    final JButton lineWrapButton;
    final JTextField cmdTextField;

    final JTextArea textArea;

    public EditorPane() {

        super(new BorderLayout());

        add(menuBar, BorderLayout.NORTH);

        lineWrapButton = new JButton("w");
        lineWrapButton.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(lineWrapButton);

        JMenu macroMenu = new JMenu("Macros");
        menuBar.add(macroMenu);

        JMenuItem recordMacroMenuItem = new JMenuItem("Record macro");
        recordMacroMenuItem
                .setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        macroMenu.add(recordMacroMenuItem);

        JMenuItem playMacroMenuItem = new JMenuItem("Play macro");
        playMacroMenuItem
                .setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        macroMenu.add(playMacroMenuItem);

        JMenuItem playMacroEofMenuItem = new JMenuItem("Play macro EOF");
        playMacroEofMenuItem
                .setAccelerator(KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        macroMenu.add(playMacroEofMenuItem);

        JMenuItem loadMacroMenuItem = new JMenuItem("Load macro");
        macroMenu.add(loadMacroMenuItem);
        JMenuItem saveMacroMenuItem = new JMenuItem("Save macro");
        macroMenu.add(saveMacroMenuItem);

        cmdTextField = new JTextField();
        cmdTextField.setSelectedTextColor(Constants.selectionColor);
        cmdTextField.setSelectionColor(Constants.selectionBackgroundColor);
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
                } else if (e.getSource() == recordMacroMenuItem) {
                    if (macroRecording.isRecording()) {
                        macroRecording.recordMacroStop();
                        recordMacroMenuItem.setText("Record macro");
                        macroMenu.setText("Macros");
                        playMacroMenuItem.setEnabled(true);
                        playMacroEofMenuItem.setEnabled(true);
                    } else {
                        macroRecording.recordMacroStart();
                        macroMenu.setText("Recording");
                        recordMacroMenuItem.setText("Recording");
                        playMacroMenuItem.setEnabled(false);
                        playMacroEofMenuItem.setEnabled(false);
                    }
                    textArea.requestFocus();
                } else if (e.getSource() == playMacroMenuItem) {

                    recordMacroMenuItem.setEnabled(false);
                    playMacroMenuItem.setEnabled(false);
                    playMacroEofMenuItem.setEnabled(false);

                    macroRecording.playMacro();

                    playMacroMenuItem.setEnabled(true);
                    playMacroEofMenuItem.setEnabled(true);
                    recordMacroMenuItem.setEnabled(true);

                    textArea.requestFocus();
                } else if (e.getSource() == playMacroEofMenuItem) {

                    recordMacroMenuItem.setEnabled(false);
                    playMacroMenuItem.setEnabled(false);
                    playMacroEofMenuItem.setEnabled(false);

                    while (textArea.getCaretPosition() < textArea.getDocument().getLength()) {
                        macroRecording.playMacro();
                    }

                    playMacroMenuItem.setEnabled(true);
                    playMacroEofMenuItem.setEnabled(true);
                    recordMacroMenuItem.setEnabled(true);

                    textArea.requestFocus();

                } else if (e.getSource() == loadMacroMenuItem) {

                    macroRecording.loadFile();

                } else if (e.getSource() == saveMacroMenuItem) {

                    macroRecording.saveFile();
                }

            }
        };

        lineWrapButton.addActionListener(actionListener);

        recordMacroMenuItem.addActionListener(actionListener);
        playMacroMenuItem.addActionListener(actionListener);
        playMacroEofMenuItem.addActionListener(actionListener);
        loadMacroMenuItem.addActionListener(actionListener);
        saveMacroMenuItem.addActionListener(actionListener);

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
        textArea.setSelectionColor(Constants.selectionBackgroundColor);
        textArea.setSelectedTextColor(Constants.selectionColor);
        textArea.setBackground(Constants.backgroundColor);
        textArea.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, textArea.getBackground()));
        DefaultCaret c = new DefaultCaret();
        textArea.setCaret(c);
        textArea.getCaret().setBlinkRate(500);
        textArea.setCaretColor(Constants.cursorColor);
        textArea.setCaretPosition(0);
        textArea.getCaret().setVisible(true);

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

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                new SupraKeyEventDispatcher(macroRecording, cmdTextField, onTabToLeft, onTabToRight));

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
        fc.setFileHidingEnabled(false);
        // fc.setPreferredSize(getSize());
        fc.setPreferredSize(new Dimension(800, 800));

        JComboBox<String> encodingCombo;
        {

            encodingCombo = new JComboBox<>(Constants.encodingStrings);
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

        String charsetName = Constants.encodingStrings[encodingCombo.getSelectedIndex()];
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
        textArea.setCaretPosition(0);
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
            fc.setFileHidingEnabled(false);
            // fc.setPreferredSize(getSize());
            fc.setPreferredSize(new Dimension(800, 800));

            JComboBox<String> encodingCombo;
            {

                encodingCombo = new JComboBox<>(Constants.encodingStrings);
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
