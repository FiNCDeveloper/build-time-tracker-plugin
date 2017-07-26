package net.rdrei.android.buildtimetracker.reporters

import au.com.bytecode.opencsv.CSVReader
import groovy.mock.interceptor.MockFor
import net.rdrei.android.buildtimetracker.Timing;

import org.gradle.api.logging.Logger
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue;

public class CSVTotalTimeReporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    Logger mockLogger = new MockFor(Logger).proxyInstance()

    File mkTemporaryFile(String name) {
        File file = folder.newFile name
        if (file.exists()) {
            file.delete()
        }

        file
    }

    @Test
    void createsOutputCSV() {
        File file = mkTemporaryFile "test.csv"
        assertFalse("Output CSV exists.", file.exists())

        CSVTotalTimeReporter reporter = new CSVTotalTimeReporter([ output: file.getPath() ], mockLogger)

        reporter.run([
                new Timing(100, "task1", true, false, true),
                new Timing(200, "task2", false, true, false)
        ])

        assertTrue "Output CSV does not exist.", file.exists()
    }

    @Test
    void writesHeaderToOutputCSV() {
        File file = mkTemporaryFile "test.csv"
        CSVTotalTimeReporter reporter = new CSVTotalTimeReporter([ output: file.getPath() ], mockLogger)

        reporter.run([
                new Timing(100, "task1", true, false, true),
                new Timing(200, "task2", false, true, false)
        ])

        CSVReader reader = new CSVReader(new FileReader(file))

        String[] header = reader.readNext()
        assertNotNull header
        assertEquals 7, header.length
        assertEquals "time_taken", header[0]
        assertEquals "success", header[1]
        assertEquals "date", header[2]
        assertEquals "task_count", header[3]
        assertEquals "cpu", header[4]
        assertEquals "memory", header[5]
        assertEquals "os", header[6]

        reader.close()
    }


    @Test
    void writesTimingsToOutputCSV() {
        File file = mkTemporaryFile "test.csv"
        CSVTotalTimeReporter reporter = new CSVTotalTimeReporter([
                output: file.getPath(),
                header: false
        ], mockLogger)

        reporter.run([
                new Timing(100, "task1", true, false, true),
                new Timing(200, "task2", false, true, false)
        ])

        CSVReader reader = new CSVReader(new FileReader(file))

        Iterator<String[]> lines = reader.readAll().iterator()

        // skip the header
        lines.next()

        // Verify first task
        String[] line = lines.next()
        assertNotNull line
        assertEquals 7, line.length
        assertEquals "300", line[0] // total time taken
        assertEquals "false", line[1] // build failed

        reader.close()
    }

}
