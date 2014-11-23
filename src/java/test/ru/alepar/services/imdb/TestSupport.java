package ru.alepar.services.imdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TestSupport {
    static String loadResourceAsString(String url) {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
        if (is == null) {
            throw new RuntimeException("resource not found: " + url);
        }

        final Reader reader = new InputStreamReader(is);
        final StringBuilder sb = new StringBuilder();

        final char[] buf = new char[102400];
        int read;
        try {
            while((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to read resource " + url, e);
        }

        return sb.toString();
    }
}
