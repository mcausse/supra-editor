package org.homs.supraedit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtils {

    /**
     * serialitza un bean
     *
     * @param o
     *            el bean a serialitzar
     * @return el bean serialitzat
     */
    public static byte[] serialize(final Serializable o) {
        try {
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(o);
            os.close();
            final byte[] bytes = bs.toByteArray();
            bs.close();
            return bytes;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * deserialitza un bean
     *
     * @param byteArray
     *            el bean serialitzat
     * @return el bean deserialitzat
     */
    public static Serializable deserialize(final byte[] byteArray) {

        try {
            final ByteArrayInputStream bs = new ByteArrayInputStream(byteArray);
            final ObjectInputStream is = new ObjectInputStream(bs);
            final Serializable o = (Serializable) is.readObject();
            is.close();
            bs.close();
            return o;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
