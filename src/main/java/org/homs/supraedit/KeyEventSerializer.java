package org.homs.supraedit;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.homs.supraedit.util.PropertiesUtils;

public class KeyEventSerializer {

    public void save(List<KeyEvent> events, String fileName) {
        try {

            PropertiesUtils pu = new PropertiesUtils(fileName);
            pu.putProperty("ke_count", String.valueOf(events.size()));
            for (int i = 0; i < events.size(); i++) {
                KeyEvent e = events.get(i);
                String key = "ke_" + i;
                StringJoiner value = new StringJoiner(",");

                value.add(e.getSource().getClass().getName());
                value.add(String.valueOf(e.getID()));
                value.add(String.valueOf(e.getModifiers()));
                value.add(String.valueOf(e.getKeyCode()));
                value.add(String.valueOf((int) e.getKeyChar()));

                pu.putProperty(key, value.toString());
                pu.saveProperties();
            }

        } catch (Exception e) {
            throw new RuntimeException("saving: " + fileName, e);
        }
    }

    public List<KeyEvent> load(Map<String, Component> componentsMap, String fileName) {

        try {
            PropertiesUtils pu = new PropertiesUtils(fileName);
            pu.loadProperties();

            ArrayList<KeyEvent> kes = new ArrayList<>();
            int eventsSize = Integer.parseInt(pu.getProperty("ke_count"));
            for (int i = 0; i < eventsSize; i++) {
                String key = "ke_" + i;
                String[] line = pu.getProperty(key).split("\\,");

                String source = line[0];
                int id = Integer.parseInt(line[1]);
                int modifiers = Integer.parseInt(line[2]);
                int keyCode = Integer.parseInt(line[3]);
                char keyChar = (char) Integer.parseInt(line[4]);

                Component sourceComponent = componentsMap.get(source);
                KeyEvent e = new KeyEvent(sourceComponent, id, System.currentTimeMillis(), modifiers, keyCode, keyChar);
                kes.add(e);
            }

            return kes;

        } catch (Exception e) {
            throw new RuntimeException("saving: " + fileName, e);
        }
    }

}
