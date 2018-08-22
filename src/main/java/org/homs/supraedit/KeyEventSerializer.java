package org.homs.supraedit;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.homs.supraedit.util.SerializationUtils;

//TODO serialitzar a Properties??
public class KeyEventSerializer {

    public void save(List<KeyEvent> events, String fileName) {
        try {
            byte[] bs = serialize(events);
            Path path = Paths.get(fileName);
            Files.write(path, bs);
        } catch (IOException e) {
            throw new RuntimeException("saving: " + fileName, e);
        }
    }

    public List<KeyEvent> load(Map<String, Component> componentsMap, String fileName) {
        try {
            Path path = Paths.get(fileName);
            byte[] bs = Files.readAllBytes(path);
            return deserialize(componentsMap, bs);
        } catch (IOException e) {
            throw new RuntimeException("saving: " + fileName, e);
        }
    }

    protected byte[] serialize(List<KeyEvent> events) {
        ArrayList<KeyEventWrapper> ws = new ArrayList<>();
        events.forEach(ke -> ws.add(new KeyEventWrapper(ke)));
        return SerializationUtils.serialize(ws);
    }

    @SuppressWarnings("unchecked")
    protected List<KeyEvent> deserialize(Map<String, Component> componentsMap, byte[] bs) {
        List<KeyEventWrapper> ws = (List<KeyEventWrapper>) SerializationUtils.deserialize(bs);
        ArrayList<KeyEvent> kes = new ArrayList<>();
        ws.forEach(w -> kes.add(w.getKeyEvent(componentsMap)));
        return kes;
    }

    public static class KeyEventWrapper implements Serializable {

        private static final long serialVersionUID = -3877488832520436631L;

        /** Component source; */
        final String componentSourceClassName;
        /** KeyEvent.KEY_PRESSED, ... */
        final int id;
        final int modifiers;
        final int keyCode;
        final char keyChar;

        public KeyEventWrapper(KeyEvent ke) {
            super();
            this.componentSourceClassName = ke.getSource().getClass().getName();
            this.id = ke.getID();
            this.modifiers = ke.getModifiers();
            this.keyCode = ke.getKeyCode();
            this.keyChar = ke.getKeyChar();
        }

        public KeyEventWrapper(String componentSourceClassName, int id, int modifiers, int keyCode, char keyChar) {
            super();
            this.componentSourceClassName = componentSourceClassName;
            this.id = id;
            this.modifiers = modifiers;
            this.keyCode = keyCode;
            this.keyChar = keyChar;
        }

        public KeyEvent getKeyEvent(Map<String, Component> componentsMap) {
            if (!componentsMap.containsKey(componentSourceClassName)) {
                throw new RuntimeException();
            }
            Component component = componentsMap.get(componentSourceClassName);
            return new KeyEvent(component, id, System.currentTimeMillis(), modifiers, keyCode, keyChar);
        }

        // XXX
        // private void writeObject(ObjectOutputStream oos) throws IOException {
        // // default serialization
        // oos.defaultWriteObject();
        // // write the object
        // // oos.writeInt(location.x);
        // // oos.writeInt(location.y);
        // // oos.writeInt(location.z);
        // // oos.writeInt(location.uid);
        // }
        //
        // private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
        // IOException {
        // // default deserialization
        // ois.defaultReadObject();
        // // location = new Location(ois.readInt(), ois.readInt(), ois.readInt(),
        // // ois.readInt());
        // // // ... more code
        //
        // }
    }

}
