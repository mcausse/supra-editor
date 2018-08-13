package org.homs.supraedit.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    /**
     * passa la traça d'error completa a {@link String}.
     */
    public static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
