package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * moviment de cursor complet, amb selecció. Accés a clipboard de sistema.
 * Eliminació en ambdós sentits Inserció de tota classe de caràcters regionals
 * (accents, etc)
 *
 * // XXX MACROS: el recording de macros registra tot lo de textarea i del
 * cmdInputText! Ho reprodeueix tot dins la mateixa pestanya!
 *
 * // XXX selecció per mouse
 *
 * // XXX undo
 *
 * // XXX pestanyes
 *
 * // XXX find text/regexp in/sensitive wrap forward/backward
 *
 * // XXX replace (amb grouping si regexp=true): no cal, tenim macros!
 *
 * // XXX comandos inline (per input)
 *
 * // XXX (des)tabulació en bloc
 *
 * // TODO autoindent: en enter, segueix la tabulació de l'anterior fila.
 *
 * // XXX tractar possibles encodings
 *
 *
 * <h1>Supra Ed <small>the ultimate editor for text editing
 * enthusiasts</small></h1>
 *
 * <h2>Navegació bàsica</h2>
 *
 * <p>
 * Els fitxers en edició s'organitzen en tabs: [Alt+left] i [Alt+right] canvia
 * de tab actiu.
 *
 * <p>
 * En cada tab hi ha l'àrea d'edició, i un input text: el cursor conmuta entre
 * aquests amb [Esc].
 *
 * [Control+R] engega/atura la grabació de macro, [Control+P] la playa.
 *
 * [Control+Z] undo, [Control+Y] redo.
 *
 * [F10] obre el menú via teclat.
 *
 * La resta són les combinacions de teclat usuals.
 *
 *
 * <h2>comandos per línia</h2>
 *
 * f[text] - cerca endavant segons text (case sensitive)
 *
 * F[text] - cerca enrera segons text (case sensitive)
 *
 * @f[regexp] - cerca endavant per una regexp
 *
 * #[numfila] - go to # fila
 *
 */
public class SupraEditor extends JFrame {

    private static final long serialVersionUID = 2054269406974939700L;

    String[] encodingStrings = { "UTF8", "ISO-8859-1", "Cp1252" };

    final Color fontColor = Color.BLACK;
    final Color cursorColor = Color.RED;
    final Color backgroundColor = Color.WHITE;
    final Color selectionBackgroundColor = Color.BLUE;
    final Color selectionColor = Color.WHITE;

    public static void main(String args[]) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException {

        // JFrame.setDefaultLookAndFeelDecorated(true);

        // TODO
        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SupraEditor();
            }
        });
    }

    class EditorPane extends JPanel {

        private static final long serialVersionUID = -3040324544220338224L;

        String filename; // set by "Open" or "Save As"
        String charsetName;

        final JMenuBar menuBar = new JMenuBar();

        final JButton lineWrapButton;
        final JButton recordMacroButton;
        final JButton playMacroButton;
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
                        } else {
                            macroRecording.recordMacroStart();
                            recordMacroButton.setText("Recording");
                            playMacroButton.setEnabled(false);
                        }
                        textArea.requestFocus();
                    } else if (e.getSource() == playMacroButton) {
                        macroRecording.playMacro();
                        textArea.requestFocus();
                    }

                }
            };

            lineWrapButton.addActionListener(actionListener);
            recordMacroButton.addActionListener(actionListener);
            playMacroButton.addActionListener(actionListener);
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
            Closure onCloseCurrTab = () -> {
                JTabbedPane tabs = (JTabbedPane) cmdTextField.getParent().getParent().getParent();
                int selectedIndex = tabs.getSelectedIndex();
                tabs.remove(selectedIndex);
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
                tabs.getComponent(tabs.getTabCount() - 1).requestFocus();
            };

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                    new MyKeyEventDispatcher(macroRecording, cmdTextField, onDoRecord, onDoPlay, onCloseCurrTab));

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

        private void loadFile() {
            JFileChooser fc = new JFileChooser();

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
        }

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

        private void saveFile(boolean saveAs) {

            String name;
            if (saveAs) {
                name = null;
            } else {
                name = this.filename;
            }

            if (name == null) { // get filename from user
                JFileChooser fc = new JFileChooser();

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
                    JOptionPane.showMessageDialog(null,
                            "Cannot write to file: " + name + "\n" + ExceptionUtils.toString(e), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }

    }

    final JMenu fileMenu = new JMenu("File");
    final JMenuBar menuBar = new JMenuBar();
    final JMenuItem newItem = new JMenuItem("New");
    final JMenuItem openItem = new JMenuItem("Open");
    final JMenuItem saveItem = new JMenuItem("Save");
    final JMenuItem saveAsItem = new JMenuItem("Save As");
    final JMenuItem exitItem = new JMenuItem("Exit");

    // Constructor: create a text editor with a menu
    public SupraEditor() {
        super("Supra Ed");

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        add(menuBar, BorderLayout.NORTH);

        // https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
        JTabbedPane tabbedPane = new JTabbedPane();
        // ImageIcon icon = createImageIcon("images/middle.gif");
        add(tabbedPane);

        EditorPane p1 = new EditorPane();
        tabbedPane.addTab("?", null, p1, "???");
        tabbedPane.setSelectedComponent(p1);

        int tabSelected = tabbedPane.getSelectedIndex();
        ((EditorPane) tabbedPane.getComponent(tabSelected)).textArea.requestFocus();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            setSize(new Dimension(bounds.width, bounds.height));
        }

        ActionListener actionListener = new ActionListener() {

            public EditorPane getCurrEditorPane() {
                return (EditorPane) tabbedPane.getSelectedComponent();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == newItem) {
                    EditorPane p = new EditorPane();
                    tabbedPane.addTab("?", null, p, "???");
                    tabbedPane.setSelectedComponent(p);
                    p.requestFocus();
                } else if (e.getSource() == openItem) {
                    EditorPane p = new EditorPane();
                    tabbedPane.addTab("?", null, p, "???");
                    tabbedPane.setSelectedComponent(p);
                    p.loadFile();
                    p.requestFocus();
                } else if (e.getSource() == saveItem) {
                    getCurrEditorPane().saveFile(false);
                } else if (e.getSource() == saveAsItem) {
                    getCurrEditorPane().saveFile(true);
                } else if (e.getSource() == exitItem) {
                    System.exit(0);
                }

            }
        };
        newItem.addActionListener(actionListener);
        openItem.addActionListener(actionListener);
        saveItem.addActionListener(actionListener);
        saveAsItem.addActionListener(actionListener);
        exitItem.addActionListener(actionListener);

        /**
         * si un tab rep el focus, el passa al seu textarea
         */
        tabbedPane.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                JTabbedPane tabs = (JTabbedPane) e.getSource();
                if (tabs.getSelectedComponent() instanceof EditorPane) {
                    ((EditorPane) tabs.getSelectedComponent()).textArea.requestFocus();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        UIManager.put("Caret.width", 3);
        setVisible(true);
    }

}
