package com.abominableshrine.taptounlock;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MockCsvSensor implements Closeable {

    private Scanner scanner;

    public MockCsvSensor(InputStream input) {
        this.scanner = new Scanner(input);
    }

    public MockSensorEvent nextSensorEvent() {
        while (scanner.hasNextLine()) {
            MockSensorEvent e = this.parseLine(this.scanner.nextLine());

            if (e != null) {
                return e;
            }
        }
        return null;
    }

    private MockSensorEvent parseLine(String s) {
        if (null == s) {
            return null;
        }

        if (s.startsWith("#")) {
            return null;
        }

        String parts[] = s.split("; ");
        MockSensorEvent e = new MockSensorEvent();
        e.timestamp = Long.parseLong(parts[1]);
        e.type = Integer.parseInt(parts[2]);
        e.accuracy = Integer.parseInt(parts[3]);
        e.values = new float[parts.length - 4];
        for (int i = 4; i < parts.length; i++) {
            e.values[i - 4] = Float.parseFloat(parts[i]);
        }
        return e;
    }

    @Override
    public void close() throws IOException {
        scanner.close();
    }

    public class MockSensorEvent {
        public int type;
        public float values[];
        public long timestamp;
        public int accuracy;
    }
}
