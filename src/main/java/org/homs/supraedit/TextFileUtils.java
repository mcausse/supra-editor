package org.homs.supraedit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class TextFileUtils {

    public static final Charset ISO88591 = Charset.forName("ISO-8859-1");
    public static final Charset UTF8 = Charset.forName("UTF8");
    public static final Charset Cp1252 = Charset.forName("Cp1252");

    public static String read(File f, Charset charset) {

        BufferedReader b = null;
        try {
            StringBuilder r = new StringBuilder();

            b = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            String line;
            while ((line = b.readLine()) != null) {
                r.append(line);
                r.append('\n');
            }
            return r.toString();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e)));
            throw new RuntimeException("error reading file: " + f, e);
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public static void write(File f, Charset charset, String text) {
        Writer w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), charset));
            w.write(text);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, new JTextArea(ExceptionUtils.toString(e)));
            throw new RuntimeException("error writing file: " + f, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}