package org.homs.supraedit;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.homs.supraedit.util.ExceptionUtils;

public class SupraKeyEventDispatcher implements KeyEventDispatcher {

    final MacroRecording macroRecording;
    final JTextField cmdTextField;

    final Closure onTabToLeft;
    final Closure onTabToRight;
    final Closure onScrollUp;
    final Closure onScrollDown;

    final Stack<Integer> markerPositionsStack = new Stack<>();

    public SupraKeyEventDispatcher(MacroRecording macroRecording, JTextField cmdTextField, Closure onTabToLeft,
            Closure onTabToRight, Closure onScrollUp, Closure onScrollDown) {
        super();
        this.macroRecording = macroRecording;
        this.cmdTextField = cmdTextField;
        this.onTabToLeft = onTabToLeft;
        this.onTabToRight = onTabToRight;
        this.onScrollUp = onScrollUp;
        this.onScrollDown = onScrollDown;
    }

    boolean controlPressed = false;
    boolean altPressed = false;
    boolean shiftPressed = false;
    int onShiftCaretPosition;

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if (e.getSource() == cmdTextField || e.getSource() == macroRecording.getTextArea()) {
            macroRecording.record(e);
        }

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

        JTextArea textArea = macroRecording.getTextArea();

        if (e.getSource() == cmdTextField) {

            if (e.getID() == KeyEvent.KEY_TYPED) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    textArea.requestFocus();
                }
            }

            if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == KeyEvent.VK_ENTER) {
                String cmd = cmdTextField.getText();
                try {
                    processCommand(textArea, cmd);
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(null, ExceptionUtils.toString(e2), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e2.printStackTrace();
                }
            }
        }

        if (e.getSource() == textArea) {

            if (e.getID() == KeyEvent.KEY_PRESSED) {

                int key = e.getKeyCode();

                switch (key) {
                case KeyEvent.VK_SHIFT:
                    this.shiftPressed = true;
                    this.onShiftCaretPosition = textArea.getCaretPosition();
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
                    cmdTextField.setSelectionStart(0);
                    cmdTextField.setSelectionEnd(cmdTextField.getText().length());
                    break;
                }
                case KeyEvent.VK_UP: {
                    if (controlPressed) {
                        onScrollUp.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    if (controlPressed) {
                        onScrollDown.execute();
                        e.consume();
                    }
                    break;
                }
                case KeyEvent.VK_TAB: {
                    if (textArea.getSelectedText() != null) {

                        int ini = textArea.getSelectionStart();
                        String selection = textArea.getSelectedText();
                        if (shiftPressed) {
                            selection = selection.replaceAll("^\\s", "");
                            selection = selection.replaceAll("\\n\\s", "\n");
                        } else {
                            selection = selection.replaceAll("^", "\t");
                            selection = selection.replaceAll("\\n", "\n\t");
                        }
                        textArea.replaceSelection(selection);
                        textArea.setSelectionStart(ini);
                        textArea.setSelectionEnd(ini + selection.length());
                        e.consume();
                    }
                    break;
                }

                case KeyEvent.VK_HOME: {

                    if (!controlPressed) {

                        // busca la pos del primer caràcter de la fila, després de possibles espais i
                        // tabuladors

                        JTextArea ta = textArea;
                        try {

                            int currLine = ta.getLineOfOffset(ta.getCaretPosition());
                            int realBegin = ta.getLineStartOffset(currLine);
                            int realEnd = ta.getLineEndOffset(currLine);

                            int charsBegin = realBegin;
                            while (charsBegin < realEnd - 1) {
                                char currChar = ta.getDocument().getText(charsBegin, 1).charAt(0);
                                if (!Character.isWhitespace(currChar)) {
                                    break;
                                }
                                charsBegin++;
                            }

                            if (shiftPressed) {
                                // si ja estava en el charsBegin, vés al realBegin, sinó al charsBegin
                                if (ta.getCaretPosition() == charsBegin) {
                                    ta.setCaretPosition(onShiftCaretPosition);
                                    ta.moveCaretPosition(realBegin);
                                } else {
                                    // TODO usar "moveCaretPosition" en f, F i @f
                                    ta.setCaretPosition(onShiftCaretPosition);
                                    ta.moveCaretPosition(charsBegin);
                                }

                            } else {
                                // si ja estava en el charsBegin, vés al realBegin, sinó al charsBegin
                                if (ta.getCaretPosition() == charsBegin) {
                                    ta.setCaretPosition(realBegin);
                                } else {
                                    ta.setCaretPosition(charsBegin);
                                }
                            }

                        } catch (BadLocationException e1) {
                            JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e1)));
                            throw new RuntimeException(e1);
                        }

                        e.consume();
                    }
                    break;
                }
                }

            } else if (e.getID() == KeyEvent.KEY_TYPED) {

                JTextArea ta = textArea;

                // if (e.getKeyCode() == KeyEvent.VK_BEGIN) {
                //
                //
                // } else

                if (e.getKeyChar() == KeyEvent.VK_ENTER) {

                    /* AUTO-INDENTATION */
                    int pos = ta.getCaretPosition();
                    try {
                        int currLine = ta.getLineOfOffset(pos);
                        if (currLine > 0) {
                            int antLine = currLine - 1;
                            int beginAntLinePos = ta.getLineStartOffset(antLine);
                            int endAntLineEndPos = ta.getLineEndOffset(antLine);

                            int c;
                            for (c = beginAntLinePos; c < endAntLineEndPos; c++) {
                                char cc = ta.getDocument().getText(c, 1).charAt(0);
                                if (!Character.isWhitespace(cc) || cc == '\n') {
                                    break;
                                }
                            }

                            String indentText = ta.getDocument().getText(beginAntLinePos, c - beginAntLinePos);
                            ta.insert(indentText, ta.getCaretPosition());
                        }

                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e2)));
                        throw new RuntimeException(e2);
                    }
                }

            } else if (e.getID() == KeyEvent.KEY_RELEASED) {

                int key = e.getKeyCode();

                switch (key) {
                case KeyEvent.VK_SHIFT:
                    this.shiftPressed = false;
                    break;
                }

            }
        }

        return false;

    }

    private void processCommand(JTextArea textArea, String cmd) {

        if (cmd.startsWith("f")) {
            String cmdVal = cmd.substring(1);
            int textLength = textArea.getDocument().getLength();

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

        } else if (cmd.startsWith("u")) {
            String s = textArea.getSelectedText();
            if (s != null && !s.isEmpty()) {
                textArea.replaceRange(s.toLowerCase(), textArea.getSelectionStart(), textArea.getSelectionEnd());
            }
            textArea.requestFocus();
        } else if (cmd.startsWith("U")) {
            String s = textArea.getSelectedText();
            if (s != null && !s.isEmpty()) {
                textArea.replaceRange(s.toUpperCase(), textArea.getSelectionStart(), textArea.getSelectionEnd());
            }
            textArea.requestFocus();

        } else if (cmd.startsWith("!")) {
            String command = cmd.substring(1);
            try {

                StringBuilder sb = new StringBuilder();

                // Process p = Runtime.getRuntime().exec(command);
                // p.waitFor();
                //
                // BufferedReader reader = new BufferedReader(new
                // InputStreamReader(p.getInputStream()));
                // String line = "";
                // while ((line = reader.readLine()) != null) {
                // sb.append(line + "\n");
                // }
                //

                // ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
                // "cd \"C:\\Program Files\\Microsoft SQL Server\" && dir");
                // ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                ProcessBuilder builder = new ProcessBuilder(command);
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    // System.out.println(line);
                    sb.append(line + "\n");
                }

                textArea.insert(sb.toString(), textArea.getCaretPosition());
                textArea.requestFocus();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e)));
                throw new RuntimeException(e);
            }
        } else if (cmd.startsWith("[")) {
            this.markerPositionsStack.push(textArea.getCaretPosition());
            textArea.requestFocus();
        } else if (cmd.startsWith("]")) {
            textArea.setCaretPosition(this.markerPositionsStack.pop());
            textArea.requestFocus();
        } else {
            textArea.requestFocus();
            throw new RuntimeException("illegal command: " + cmd);
        }
    }

}