package org.homs.supraedit;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.homs.supraedit.util.ExceptionUtils;

public class MyKeyEventDispatcher implements KeyEventDispatcher {

    final MacroRecording macroRecording;
    final JTextField cmdTextField;

    final Closure onDoRecord;
    final Closure onDoPlay;
    final Closure onCloseCurrTab;
    final Closure onTabToLeft;
    final Closure onTabToRight;

    public MyKeyEventDispatcher(MacroRecording macroRecording, JTextField cmdTextField, Closure onDoRecord,
            Closure onDoPlay, Closure onCloseCurrTab, Closure onTabToLeft, Closure onTabToRight) {
        super();
        this.macroRecording = macroRecording;
        this.cmdTextField = cmdTextField;
        this.onDoRecord = onDoRecord;
        this.onDoPlay = onDoPlay;
        this.onCloseCurrTab = onCloseCurrTab;
        this.onTabToLeft = onTabToLeft;
        this.onTabToRight = onTabToRight;
    }

    boolean controlPressed = false;
    boolean altPressed = false;
    boolean shiftPressed = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        /*
         * la gestió de la tecla [Alt] és comuna entre tots els tabs: això permet
         * conmutar fluidament entre ells sense deixar anar el [Alt] (fent [Alt+left] i
         * [Alt+right]).
         */
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ALT) {
                this.altPressed = true;
                e.consume();
            }
            if (key == KeyEvent.VK_CONTROL) {
                this.controlPressed = true;
                e.consume();
            }
        }
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ALT) {
                this.altPressed = false;
                e.consume();
            }
            if (key == KeyEvent.VK_CONTROL) {
                this.controlPressed = false;
                e.consume();
            }
        }

        if (e.getSource() == cmdTextField) {

            if (e.getID() == KeyEvent.KEY_TYPED) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    macroRecording.getTextArea().requestFocus();
                }
            }

            if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == KeyEvent.VK_ENTER) {
                JTextArea textArea = macroRecording.getTextArea();
                String cmd = cmdTextField.getText();
                try {
                    processCommand(textArea, cmd);
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(null, ExceptionUtils.toString(e2), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e2.printStackTrace();
                }
            }

            /**
             * IMPORTANTISSIM: REGISTRA TOT LO TECLEJAT EN L'INPUT-TEXT DE COMANDES
             */
            macroRecording.record(e);
        }

        if (e.getSource() == macroRecording.getTextArea()) {

            if (e.getID() == KeyEvent.KEY_PRESSED) {

                int key = e.getKeyCode();

                switch (key) {
                case KeyEvent.VK_SHIFT:
                    this.shiftPressed = true;
                    break;
                case KeyEvent.VK_LEFT: {
                    if (altPressed) {
                        onTabToLeft.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    if (altPressed) {
                        onTabToRight.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_ESCAPE: {
                    cmdTextField.requestFocus();
                    break;
                }
                case KeyEvent.VK_R: {
                    if (controlPressed) {
                        onDoRecord.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_P: {
                    if (controlPressed) {
                        onDoPlay.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_W: {
                    if (controlPressed) {
                        onCloseCurrTab.execute();
                        e.consume();
                    }
                    break;
                }

                case KeyEvent.VK_TAB: {
                    if (macroRecording.getTextArea().getSelectedText() != null) {

                        if (shiftPressed) {

                            int ini = macroRecording.getTextArea().getSelectionStart();

                            String selection = macroRecording.getTextArea().getSelectedText();

                            selection = selection.replaceAll("^\\t", "");
                            selection = selection.replaceAll("\\n\\t", "\n");

                            macroRecording.getTextArea().replaceSelection(selection);

                            macroRecording.getTextArea().setSelectionStart(ini);
                            macroRecording.getTextArea().setSelectionEnd(ini + selection.length());

                            macroRecording.record(e);
                            e.consume();
                        } else {

                            int ini = macroRecording.getTextArea().getSelectionStart();

                            String selection = macroRecording.getTextArea().getSelectedText();

                            selection = selection.replaceAll("^", "\t");
                            selection = selection.replaceAll("\\n", "\n\t");

                            macroRecording.getTextArea().replaceSelection(selection);

                            macroRecording.getTextArea().setSelectionStart(ini);
                            macroRecording.getTextArea().setSelectionEnd(ini + selection.length());

                            macroRecording.record(e);
                            e.consume();
                        }
                    }
                    break;
                }
                }

            } else if (e.getID() == KeyEvent.KEY_TYPED) {

            } else if (e.getID() == KeyEvent.KEY_RELEASED) {

                int key = e.getKeyCode();

                switch (key) {
                case KeyEvent.VK_SHIFT:
                    this.shiftPressed = false;
                    break;
                }
            }

            /**
             * IMPORTANTISSIM: REGISTRA TOT LO NO CONSUMIT!!!!!
             */
            if (!e.isConsumed()) {
                macroRecording.record(e);
            }

        }

        return false;
    }

    private void processCommand(JTextArea textArea, String cmd) {

        // System.out.println("cmd: " + cmd);

        if (cmd.startsWith("f")) {
            String cmdVal = cmd.substring(1);
            int textLength = macroRecording.getTextArea().getDocument().getLength();

            int findPos = textArea.getCaretPosition() + 1;
            if (findPos >= textLength) {
                textArea.setCaretPosition(textLength);
                textArea.requestFocus();
            } else {
                int pos = textArea.getText().indexOf(cmdVal, findPos);
                if (pos < 0) {
                    // no troba més: deixa el cursor a final de fitxer
                    textArea.setCaretPosition(textLength);
                    textArea.requestFocus();
                } else {
                    // trobat
                    textArea.requestFocus();
                    textArea.select(pos, pos + cmdVal.length());
                }
            }

            // // TODO
            // try {
            // int pos = textArea.getCaretPosition();
            // Highlighter highlighter = textArea.getHighlighter();
            // HighlightPainter painter = new
            // DefaultHighlighter.DefaultHighlightPainter(Color.pink);
            // highlighter.addHighlight(pos, pos + textLength, painter);
            // } catch (BadLocationException ex) {
            // throw new RuntimeException(ex);
            // }

            // TODO
            // JOptionPane.showMessageDialog(null, new JTePane(textArea));
        } else if (cmd.startsWith("F")) {

            String cmdVal = cmd.substring(1);

            int findPos = textArea.getCaretPosition(); // - 1;
            if (findPos <= 0) {
                textArea.requestFocus();
            } else {
                int pos = textArea.getText().lastIndexOf(cmdVal, findPos);
                if (pos < 0) {
                    // no troba més: deixa el cursor a inici de fitxer
                    textArea.requestFocus();
                    textArea.setCaretPosition(0);
                } else {
                    // trobat
                    textArea.requestFocus();
                    textArea.select(pos, pos + cmdVal.length());
                }
            }

            // TODO
            // try {
            // Highlighter highlighter = textArea.getHighlighter();
            // HighlightPainter painter = new
            // DefaultHighlighter.DefaultHighlightPainter(Color.pink);
            // highlighter.addHighlight(pos, pos + text.length(), painter);
            // } catch (BadLocationException e) {
            // throw new RuntimeException(e);
            // }

            // TODO
            // JOptionPane.showMessageDialog(null, new JTePane(textArea));
        } else if (cmd.startsWith("@f")) {
            String regexp = cmd.substring(2);
            int textLength = textArea.getDocument().getLength();

            Pattern p = Pattern.compile(regexp);

            int findPos = textArea.getCaretPosition() + 1;
            if (findPos >= textLength) {
                textArea.requestFocus();
                textArea.setCaretPosition(textLength);
            } else {
                Matcher m = p.matcher(textArea.getText());
                if (m.find(findPos)) {
                    // trobat
                    textArea.requestFocus();
                    textArea.setCaretPosition(m.start());
                    textArea.setSelectionStart(m.start());
                    textArea.setSelectionEnd(m.end());
                } else {
                    // no troba més: deixa el cursor a final de fitxer
                    textArea.requestFocus();
                    textArea.setCaretPosition(textLength);
                }
            }

        } else if (cmd.startsWith("#")) {

            int gotoLine;
            try {
                gotoLine = Integer.parseInt(cmd.substring(1)) - 1;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e)));
                throw new RuntimeException(e);
            }

            try {
                int newPos = textArea.getLineStartOffset(gotoLine);
                textArea.setCaretPosition(newPos);
                textArea.requestFocus();
            } catch (BadLocationException e) {
                JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e)));
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("illegal command: " + cmd);
        }
    }

}