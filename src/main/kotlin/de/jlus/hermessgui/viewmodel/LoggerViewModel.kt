package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.Logger
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
        log(Logger.LogEntry(Logger.LoggingSeverity.INFO, "Not connected"))
    }

    /**
     * Logs a new entry to be shown in the status bar and in the log
     * @param entry
     */
    fun log (entry: Logger.LogEntry) {
        item.entries.add(entry)
        statusText.value = entry.message
        statusStyle.value = "-fx-text-fill: ${entry.severity.color}"
    }

    /**
     * Clears the log without prior saving
     */
    fun clearLog () {
        item = Logger()
    }
}