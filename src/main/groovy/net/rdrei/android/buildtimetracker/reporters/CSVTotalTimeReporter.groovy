package net.rdrei.android.buildtimetracker.reporters;

import net.rdrei.android.buildtimetracker.Timing;

import org.gradle.api.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVTotalTimeReporter extends AbstractBuildTimeTrackerReporter {
    CSVTotalTimeReporter(Map<String, String> options, Logger logger) {
        super(options, logger)
    }

    @Override
    def run(List<Timing> timings) {

        // don't run if there are no values
        if (timings.isEmpty()) {
            return
        }

        long timestamp = new TrueTimeProvider().getCurrentTime()
        String output = getOption("output", "")
        boolean append = getOption("append", "false").toBoolean()
        TimeZone tz = TimeZone.getTimeZone("UTC")
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS'Z'")
        df.setTimeZone(tz)

        File file = new File(output)
        file.getParentFile()?.mkdirs()

        // if the file is empty, write some headers
        def fileEmpty = file.length() == 0

        CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(file, append)))

        if (getOption("header", "true").toBoolean() || fileEmpty) {
            String[] headers = ["time_taken", "success", "date",
                    "cpu", "memory", "os"]
            writer.writeNext(headers)
        }

        def info = new SysInfo()
        def osId = info.getOSIdentifier()
        def cpuId = info.getCPUIdentifier()
        def maxMem = info.getMaxMemory()

        boolean success = true

        def timeTaken = timings.sum { it -> it.ms }

        timings.each { timing ->
            // if any timing is not successful, the overall success is false
            success = timing.success && success
        }

        String[] line = [
                timeTaken,
                success.toString(),
                df.format(new Date(timestamp)),
                cpuId,
                maxMem,
                osId
        ]

        writer.writeNext(line)

        writer.close()
    }
}
