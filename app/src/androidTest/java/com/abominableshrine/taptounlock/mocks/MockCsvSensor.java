/*
 * Copyright 2015 Hannes Bibel, Valentin Sawadski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abominableshrine.taptounlock.mocks;

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
