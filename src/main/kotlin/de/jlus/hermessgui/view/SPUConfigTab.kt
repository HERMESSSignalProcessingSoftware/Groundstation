package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.imgTooltip
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.*
import javafx.scene.text.Font
import tornadofx.*


/**
 * - Samplerate Strain Gauge Rosettes
 * - Samplerate PT-100
 * - Baudrate DAPI
 * - Override Master-Jumper as: Slave/Master
 * - Enable Telemetry
 * - Enable Storage of measurements
 * - Enable Storage of metadata
 * - Start sampling on: LiftOff (LO) / Start/Stop of Experiment (SOE) / Start/Stop of Data Storage (SODS)
 * - Stop sampling on: LiftOff (LO) / Start/Stop of Experiment (SOE) / Start/Stop of Data Storage (SODS)
 * - Minimum recording time: ...s
 * - Maximum recording time: ...s
 * - Start storage on: LiftOff (LO) / Start/Stop of Experiment (SOE) / Start/Stop of Data Storage (SODS)
 * - Stop storage on: LiftOff (LO) / Start/Stop of Experiment (SOE) / Start/Stop of Data Storage (SODS)
 * - Wipe all data storage (measurements / metadata)
 */
class SPUConfigTab: MainTab("SPU Conf") {
    override var isProjectTab = true
    override val isDirty = SimpleBooleanProperty(false)


    override val root = scrollpane {
        gridpane {
            fitToWidth(this@scrollpane)
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20

            row {
                label(this@SPUConfigTab.tab.textProperty()) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }
            }

            row {
                imageview(imgTooltip) {
                    tooltip("Testtip")
                }
                label("Configuration name: ")
                textfield("Test")
            }

            row {
                buttonbar {
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                    button("Save")
                    button("Cancel")
                    button("Program SPU via DAPI")
                }
            }

        }
    }
}