package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.imgClear16
import de.jlus.hermessgui.model.Logger
import de.jlus.hermessgui.viewmodel.LoggerViewModel
import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.text.SimpleDateFormat


/**
 * Displays the log
 */
class LoggerTab : MainTab("Logger") {
    private val loggerViewModel by inject<LoggerViewModel>()

    override val root = borderpane() {
        top = hbox {
            alignment = Pos.CENTER_RIGHT
            paddingAll = 10.0
            spacing = 10.0
            style = "-fx-border-color: black; -fx-border-width: 0 0 2 0;"
            button("Clear Log", graphic = ImageView(imgClear16)) {
                action {
                    confirm("Clear log", "Do you really want to irrecoverable clear the log?") {
                        loggerViewModel.clearLog()
                    }
                }
            }
            button("Export Log") {
                action {
                    val selection = chooseFile(
                        "Select the location to save the log to",
                        arrayOf(FileChooser.ExtensionFilter("Textfile", "*.txt")),
                        File(System.getProperty("user.home")),
                        FileChooserMode.Save
                    )
                    if (selection.size == 1)
                        loggerViewModel.item.saveToFile(selection[0])
                }
            }
        }

        center = tableview(loggerViewModel.entries) {
            readonlyColumn("Timestamp", Logger.LogEntry::timestamp).cellFormat {
                text = SimpleDateFormat("HH:mm:ss").format(it)
                style = "-fx-text-fill: ${rowItem.severity.color};"
            }
            readonlyColumn("Source", Logger.LogEntry::source).cellFormat {
                text = it
                style = "-fx-text-fill: ${rowItem.severity.color};"
            }
            readonlyColumn("Type", Logger.LogEntry::severity).cellFormat {
                text = it.name
                style = "-fx-text-fill: ${rowItem.severity.color};"
            }
            readonlyColumn("Message", Logger.LogEntry::message).cellFormat {
                text = it
                style = "-fx-text-fill: ${rowItem.severity.color};"
            }
            smartResize()
        }
    }
}