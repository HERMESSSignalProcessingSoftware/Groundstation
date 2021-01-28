package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.imgTooltip
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.*
import javafx.scene.text.Font
import tornadofx.*


/**
 *
 */
class SPUConfigTab: MainTab("SPU Conf") {
    override var isProjectTab = true
    override val isDirty = SimpleBooleanProperty(false)

    private val adcSamplerates = listOf("5 SPS", "10 SPS", "20 SPS", "40 SPS", "80 SPS", "160 SPS",
        "320 SPS", "640 SPS", "1k SPS", "2k SPS")
    private val adcPgas = listOf("1", "2", "4", "8", "16", "32", "64", "128")
    private val ovrdMode = listOf("No override (jumper setting)", "Primary", "Secondary")


    override val root = scrollpane {
        gridpane {
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20
            prefWidth = 700.0 // based on the buttonbar on the bottom - widest object

            row {
                label(this@SPUConfigTab.tab.textProperty()) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }
            }

            // heading: general settings
            row {
                label("General SPU settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // configuration name
            row {
                imageview(imgTooltip) {
                    tooltip("The name of the configuration will also be transfered to the SPU and can be " +
                            "read out later.")
                }
                label("Configuration name: ")
                textfield()
            }
            // override Master/Slave
            row {
                imageview(imgTooltip) {
                    tooltip("Override the hardware jumper for primary/secondary mode")
                }
                label("Ovrd mode: ")
                combobox(values=ovrdMode) {
                    selectionModel.select(0)
                }
            }
            // sleep mode
            row {
                imageview(imgTooltip) {
                    tooltip("Do not use any capabilities of the SPU until the next configuration.")
                }
                label("Put SPU in sleep: ")
                checkbox("Put into sleep")
            }

            // heading: Strain gauge rosettes (DMS) settings
            row {
                label("Strain gauge rosettes (DMS) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Loads the prerecorded data for offset and full-scale calibration " +
                            "into the ADCs.")
                }
                label("Initiate with calibration replay: ")
                vbox {
                    spacing = 10.0
                    checkbox("Offset calibration register")
                    checkbox("Full-scale calibration register")
                }
            }
            // samplerate adcs for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("DMS ADC Samplerate: ")
                combobox(values=adcSamplerates) {
                    selectionModel.select(9) // default to 2kHz setting
                }
            }
            // adc pgas for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("DMS ADC PGA: ")
                combobox(values=adcPgas) {
                    selectionModel.select(7) // default to PGA = 128
                }
            }

            // heading: temperature (PT-100) settings
            row {
                label("Temperature (PT-100) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Loads the prerecorded data for offset and full-scale calibration " +
                            "into the ADCs.")
                }
                label("Initiate with calibration replay: ")
                vbox {
                    spacing = 10.0
                    checkbox("Offset calibration register")
                    checkbox("Full-scale calibration register")
                }
            }
            // samplerate adcs for pt-100
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("PT-100 ADC Samplerate: ")
                combobox(values=adcSamplerates) {
                    selectionModel.select(0) // default to 5Hz setting
                }
            }
            // adc pgas for pt-100
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("PT-100 ADC PGA: ")
                combobox(values=adcPgas) {
                    selectionModel.select(7) // default to PGA = 128
                }
            }

            // heading: data storage
            row {
                label("Data storage settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // enable saving measurements or metadata
            row {
                imageview(imgTooltip) {
                    tooltip("Enables the storage of metadata or measurement data. Disable to " +
                            "safe lifecycles on the flash.")
                }
                label("Enable storage of: ")
                vbox {
                    spacing = 10.0
                    checkbox("Measurements")
                    checkbox("Metadata")
                }
            }
            // start storage on
            row {
                imageview(imgTooltip) {
                    tooltip("If any of the conditions are met, start the recording of " +
                            "measurements and metadata.")
                }
                label("Start recordings on [ANY HIGH]: ")
                hbox {
                    spacing = 10.0
                    checkbox("LO")
                    checkbox("SOE")
                    checkbox("SODS")
                }
            }
            // stop storage on
            row {
                imageview(imgTooltip) {
                    tooltip("If all of the conditions are met, stop the recording of " +
                            "measurements and metadata.")
                }
                label("Stop recordings on [ALL LOW]: ")
                hbox {
                    spacing = 10.0
                    checkbox("LO")
                    checkbox("SOE")
                    checkbox("SODS")
                }
            }
            // minimum and maximum recording time
            row {
                imageview(imgTooltip) {
                    tooltip("After initialisation of data storage keep the data recording time in " +
                            "these bound. Useful to prevent too short recordings caused by bouncing signal " +
                            "lines")
                }
                label("Recording time min/max: ")
                vbox {
                    spacing = 10.0
                    hbox {
                        label("Min [s]: ")
                        spinner<Int>()
                    }
                    hbox {
                        label("Max [s]: ")
                        spinner<Int>()
                    }
                }
            }

            // heading: TM
            row {
                label("Telemetry settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // configuration name
            row {
                imageview(imgTooltip) {
                    tooltip("Enable sending telemetry data via RXSM")
                }
                label("Enable telemetry: ")
                checkbox("Enable TM")
            }

            // buttonbar
            row {
                buttonbar {
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                    button("Save") {
                        isDefaultButton = true
                    }
                    button("Cancel")
                    button("Program SPU")
                    button("Read SPU configurations")
                }
            }

        }
    }
}