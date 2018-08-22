package org.homs.supraedit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesUtils {

    final File file;
    final Properties p;

    public PropertiesUtils(File file) {
        super();
        this.file = file;
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.p = new Properties();
        loadProperties();
    }

    public PropertiesUtils(String fileName) {
        this(new File(fileName));
    }

    public boolean containsKey(Object key) {
        return p.containsKey(key);
    }

    public String getProperty(String key) {
        return p.getProperty(key);
    }

    public Integer getPropertyInteger(String key, String defaultValue) {
        String v = p.getProperty(key, defaultValue);
        if (v == null) {
            return null;
        }
        return Integer.valueOf(v);
    }

    public void putProperty(String key, String value) {
        p.put(key, value);
    }

    public List<String> getListProperty(String key) {
        String v = p.getProperty(key);
        if (v == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(Arrays.asList(v.split("\\~")));
    }

    public void putListProperty(String key, List<String> list) {
        StringBuilder v = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                v.append("~");
            }
            v.append(list.get(i));
        }
        p.put(key, v.toString());
    }

    public void saveProperties() {
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(file);
            p.store(fo, "");
            fo.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadProperties() {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            p.load(fi);
            fi.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
