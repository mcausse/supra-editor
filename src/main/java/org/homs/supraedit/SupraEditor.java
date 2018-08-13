package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
 * <pre>
 * Els fitxers en edició s'organitzen en tabs: [Alt+left] i [Alt+right] canvia
 * de tab actiu.
 *
 * En cada tab hi ha l'àrea d'edició, i un input text: el cursor conmuta entre
 * aquests amb [Esc].
 *
 * [Control+R] engega/atura la grabació de macro,
 * [Control+P] la playa.
 *
 * [Control+Z] undo
 * [Control+Y] redo.
 * [Control+T] nou tab
 * [Control+O] open file
 * [Control+S] save file
 * [Control+W] tanca tab
 * [Control+Q] quit
 *
 *
 * La resta són les combinacions de teclat usuals.
 *
 * </pre>
 *
 *
 * <h2>comandos per línia</h2>
 *
 * <pre>
 *
 * f[text] - cerca endavant segons text (case sensitive)
 *
 * F[text] - cerca enrera segons text (case sensitive)
 *
 * &#64;f[regexp] - cerca endavant per una regexp
 *
 * #[numfila] - go to # fila
 *
 * </pre>
 *
 */
public class SupraEditor extends JFrame {

    private static final long serialVersionUID = 2054269406974939700L;

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

    final JMenu fileMenu = new JMenu("File");
    final JMenuBar menuBar = new JMenuBar();
    final JMenuItem newItem = new JMenuItem("New");
    final JMenuItem openItem = new JMenuItem("Open");
    final JMenuItem saveItem = new JMenuItem("Save");
    final JMenuItem saveAsItem = new JMenuItem("Save As");
    final JMenuItem closeTabItem = new JMenuItem("Close Tab");
    final JMenuItem exitItem = new JMenuItem("Exit");

    // Constructor: create a text editor with a menu
    public SupraEditor() {
        super("Supra Ed");

        {
            java.net.URL imgURL = getClass().getClassLoader().getResource("idcon.png");
            ImageIcon icon = new ImageIcon(imgURL);
            setIconImage(icon.getImage());
        }

        /**
         * Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask() returns control key
         * (ctrl) on Windows and linux, and command key (⌘) on Mac OS.
         */
        newItem.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(closeTabItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        add(menuBar, BorderLayout.NORTH);

        // https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane);

        EditorPane p1 = new EditorPane();
        tabbedPane.addTab("  ?  ", null, p1, "???");
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
                    tabbedPane.addTab("  ?  ", null, p, "???");
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
                } else if (e.getSource() == closeTabItem) {
                    if (tabbedPane.getTabCount() - 1 <= 0) {
                        return;
                    }
                    tabbedPane.remove(tabbedPane.getSelectedIndex());
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                    tabbedPane.getComponent(tabbedPane.getTabCount() - 1).requestFocus();
                } else if (e.getSource() == exitItem) {
                    System.exit(0);
                }

            }
        };
        newItem.addActionListener(actionListener);
        openItem.addActionListener(actionListener);
        saveItem.addActionListener(actionListener);
        saveAsItem.addActionListener(actionListener);
        closeTabItem.addActionListener(actionListener);
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
