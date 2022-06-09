package de.jlus.hermessgui.model


import tornadofx.observableListOf
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * holds the general information about log entries
 */
class Logger {
    enum class LoggingSeverity (val color: String) {
        ERROR("red"),
        WARN("orange"),
        INFO("black")
    }

    class LogEntry (val severity: LoggingSeverity, val message: String, val timestamp: Date = Date()) {
        override fun toString (): String {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(timestamp) +
                    ":\t" + severity.name + "\t" + message
        }
    }

    /**
     * All logging entries are stored here
     */
    val entries = observableListOf<LogEntry>()

    /**
     * saves the log in a regular file
     * @param file the file to store to. The containing directory must exist. It is not required for
     * the file to exist, it should be writable however.
     * @throws IOException if cannot write to file
     */
    fun saveToFile (file: File) {
        if (!file.canWrite() && file.exists())
            throw IOException("Can not write to file: ${file.absolutePath}")
        file.writeText(entries.joinToString(System.lineSeparator()))
    }
}