package org.homs.supraedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SupraEditor extends JFrame {

    private static final long serialVersionUID = 2054269406974939700L;

    public static void main(String args[]) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException {

        // XXX l&f
        // JFrame.setDefaultLookAndFeelDecorated(true);
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

    public SupraEditor() {
        super(".");

        {
            java.net.URL imgURL = getClass().getClassLoader().getResource("e64.png");
            ImageIcon icon = new ImageIcon(imgURL);
            setIconImage(icon.getImage());
        }

        newItem.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK));
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK));
        exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));

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

        ActionListener actionListener;
        {
            JFrame selfFrame = this;
            actionListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == newItem) {
                        EditorPane p = new EditorPane();
                        tabbedPane.addTab("  ?  ", null, p, "???");
                        tabbedPane.setSelectedComponent(p);
                        p.requestFocus();
                    } else if (e.getSource() == openItem) {
                        EditorPane p = new EditorPane();
                        tabbedPane.addTab("  ?  ", null, p, "???");
                        tabbedPane.setSelectedComponent(p);
                        p.loadFile();
                        p.requestFocus();
                    } else if (e.getSource() == saveItem) {
                        ((EditorPane) tabbedPane.getSelectedComponent()).saveFile(false);
                    } else if (e.getSource() == saveAsItem) {
                        ((EditorPane) tabbedPane.getSelectedComponent()).saveFile(true);
                    } else if (e.getSource() == closeTabItem) {
                        if (tabbedPane.getTabCount() <= 1) {
                            return;
                        }
                        tabbedPane.remove(tabbedPane.getSelectedIndex());
                        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                        tabbedPane.getComponent(tabbedPane.getTabCount() - 1).requestFocus();
                    } else if (e.getSource() == exitItem) {

                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int dialogResult = JOptionPane.showConfirmDialog(null, "Quit?", "Warning", dialogButton);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            System.exit(0);
                        }
                    }

                    // XXX set window title
                    selfFrame.setTitle(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));

                }
            };
        }

        newItem.addActionListener(actionListener);
        openItem.addActionListener(actionListener);
        saveItem.addActionListener(actionListener);
        saveAsItem.addActionListener(actionListener);
        closeTabItem.addActionListener(actionListener);
        exitItem.addActionListener(actionListener);

        /**
         * si un tab rep el focus, el passa al seu textarea
         */
        {
            JFrame selfFrame = this;
            tabbedPane.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                    JTabbedPane tabs = (JTabbedPane) e.getSource();
                    if (tabs.getSelectedComponent() instanceof EditorPane) {
                        EditorPane editorPane = (EditorPane) tabs.getSelectedComponent();
                        editorPane.textArea.requestFocus();

                        // XXX set window title
                        selfFrame.setTitle(tabs.getTitleAt(tabs.getSelectedIndex()));
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                }
            });
        }

        UIManager.put("Caret.width", 3);
        setVisible(true);
    }

}
