package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.Logger
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel


/**
 * Holds as item the current Logger session, which will be recreated, when a new logger session is requested.
 */
class LoggerViewModel: ItemViewModel<Logger>(Logger()) {
    val statusText = SimpleStringProperty()
    val statusStyle = SimpleStringProperty()

    val entries = bind(Logger::entries)


    init {
        log(Logger.LogEntry(Logger.LoggingSeverity.INFO, "Not connected", "GSS"))
    }


    /**
     * Logs an info message
     */
    fun info (msg: String, source: String = "GSS") {
        log(Logger.LogEntry(Logger.LoggingSeverity.INFO, msg, source))
    }


    /**
     * Logs a warning message
     */
    fun warning (msg: String, source: String = "GSS") {
        log(Logger.LogEntry(Logger.LoggingSeverity.WARN, msg, source))
    }


    /**
     * Logs an error message
     */
    fun error (msg: String, source: String = "GSS") {
        log(Logger.LogEntry(Logger.LoggingSeverity.ERROR, msg, source))
    }


    /**
     * Clears the log without prior saving
     */
    fun clearLog () {
        item = Logger()
    }


    /**
     * Logs a new entry to be shown in the status bar and in the log
     * May be run fully asynchronously
     * @param entry
     */
    private fun log (entry: Logger.LogEntry) {
        item.entries.add(entry)
        Platform.runLater {
            statusText.value = entry.message
            statusStyle.value = "-fx-text-fill: ${entry.severity.color}"
        }
    }
}