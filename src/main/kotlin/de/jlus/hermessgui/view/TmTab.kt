package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.model.Tm
import javafx.beans.binding.BooleanBinding
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.*


/**
 * Displays the telemetry live data
 */
class TmTab : MainTab("Telemetry Live View") {
    override val root = scrollpane {
        gridpane {
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20
            prefWidth = 800.0 // based on the widest object

            row {
                label(this@TmTab.tabTitle) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }
            }

            row {
                vbox {
                    spacing = 5.0
                    alignment = Pos.CENTER
                    progressbar(Tm.successRate)
                    hbox {
                        spacing = 3.0
                        alignment = Pos.CENTER
                        imageview(imgTooltip) {tooltip("The accumulative rate of successfully received and " +
                                "correctly decoded TM dataframes. This value will not change, if no messages are " +
                                "received.")}
                        label("Successrate")
                    }
                }

                vbox {
                    spacing = 5.0
                    alignment = Pos.CENTER
                    hbox {
                        alignment = Pos.CENTER
                        label(Tm.timestamp) {
                            font = Font.font(null, FontWeight.BOLD, 15.0)
                        }
                        label(" s") {
                            font = Font.font(null, FontWeight.BOLD, 15.0)
                        }
                    }
                    hbox {
                        spacing = 3.0
                        alignment = Pos.CENTER
                        imageview(imgTooltip) {tooltip("The current SPU internal timestamp, which is used " +
                                "for all timestamped operations, including data storage.")}
                        label("Timestamp")
                    }
                }

                vbox {
                    spacing = 5.0
                    alignment = Pos.CENTER
                    label(Tm.numMessagesReceived) {
                        font = Font.font(null, FontWeight.BOLD, 15.0)
                    }
                    hbox {
                        spacing = 3.0
                        alignment = Pos.CENTER
                        imageview(imgTooltip) {tooltip("The number of received messages via this connection.")}
                        label("#Msg Received")
                    }
                }
            }

            row {
                indicator(
                    "TM Port connected", Tm.activePortProperty.isNotNull,
                    "The ground station software currently has a connection to a TM interface. " +
                            "Note, that due to the one-way nature of the protocol, it cannot be asserted, that " +
                            "the selected interface is in fact the correct telemetry port. It may be another, " +
                            "unrelated port selected."
                )

                indicator(
                    "Restart after WD", Tm.restartAfterWdTriggered.toBinding(),
                    "The SPU was restarted, after the Watchdog detected a serious firmware malfunction " +
                            "resulting in a stuck process"
                )

                indicator(
                    "Write-Protection", Tm.wpAsserted.toBinding(), "The physical write-protection " +
                            "flag is set on the SPU. This must not be set prior or during flight."
                )
            }

            row {
                indicator("LO", Tm.loAsserted.toBinding(), "Lift-OFF Signal detected")

                indicator("SOE", Tm.soeAsserted.toBinding(), "Start/Stop of Experiment Signal " +
                        "detected")

                indicator("SODS", Tm.sodsAsserted.toBinding(), "Start/Stop of Data Storage Signal " +
                        "detected")
            }

            row {
                indicator(
                    "Recording", Tm.recordingToMemory.toBinding(), "The SPU is currently recording " +
                            "measurements and storing them into the on-board memory"
                )

                indicator(
                    "Clearing Memory", Tm.clearingMemory.toBinding(), "The memory is currently " +
                            "being cleared"
                )

                indicator(
                    "Memory cleared", Tm.memoryClearedBeforeSods.toBinding(), "The SPU internal " +
                            "memory was successfully cleared before the SODS signal was received."
                )
            }
        }
    }


    /**
     * Adds an indicator
     */
    private fun Pane.indicator (name: String, property: BooleanBinding, infobox: String) {
        // has active connection
        vbox {
            addClass(Styles.indicator)
            hbox {
                imageview(imgTooltip) {tooltip(infobox)}

                checkbox {
                    bind(property, true)
                    isDisable = true
                }
            }
            label(name)
        }
    }
}