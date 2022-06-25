package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.model.Dapi
import de.jlus.hermessgui.model.SPUConfig
import de.jlus.hermessgui.viewmodel.ProjectViewModel
import de.jlus.hermessgui.viewmodel.SPUConfViewModel
import javafx.scene.control.TextFormatter
import javafx.scene.control.ToggleGroup
import javafx.scene.text.Font
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import java.io.File


/**
 * A MainTab enabling the change of the spu configurations
 */
class SPUConfigTab: MainTab("SPU Conf") {
    private val vm = SPUConfViewModel(SPUConfig())
    private val projectVm by inject<ProjectViewModel>()


    override val root = scrollpane {
        gridpane {
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20
            prefWidth = 700.0 // based on the buttonbar on the bottom - widest object

            row {
                label(this@SPUConfigTab.tabTitle) {
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
                textfield(vm.confName).validator {
                    if (regexSPUConfName.matches(it ?: ""))
                        null
                    else
                        error("The name must match ${regexFileName.pattern}")
                }
            }

            // heading: Strain gauge rosettes (DMS) settings
            row {
                label("Strain gauge rosettes (SGR) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Performs either a system or self offset calibration when powering the SPU. An " +
                            "offset calibration sets the ADCs zero reference to either the entire measurement system" +
                            "zero-volt deviance or only the ADCs internal zero-volt deviance.")
                }
                label("Initiate with ADC calibration: ")
                vbox {
                    spacing = 10.0
                    val tg = ToggleGroup()
                    radiobutton("None", tg, SPUConfCalibrationTypes.None)
                    radiobutton("Self offset calibration", tg, SPUConfCalibrationTypes.SelfOffset)
                    radiobutton("System offset calibration", tg, SPUConfCalibrationTypes.SystemOffset)
                    tg.bind(vm.sgrOffsetCalInit)
                }
            }
            // samplerate adcs for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("SGR ADC Samplerate: ")
                combobox(vm.sgrSamplerate, SPUConfSamplerate.values().toList()) {
                    cellFormat { text = it.text }
                }
            }
            // adc pgas for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("SGR ADC PGA: ")
                combobox(vm.sgrPGA, SPUConfPGA.values().toList()) {
                    cellFormat { text = it.text }
                }
            }

            // heading: temperature (PT-100) settings
            row {
                label("Resistive temperature detector (RTD) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Performs either a system or self offset calibration when powering the SPU. An " +
                            "offset calibration sets the ADCs zero reference to either the entire measurement system" +
                            "zero-volt deviance or only the ADCs internal zero-volt deviance.")
                }
                label("Initiate with calibration replay: ")
                vbox {
                    spacing = 10.0
                    val tg = ToggleGroup()
                    radiobutton("None", tg, SPUConfCalibrationTypes.None)
                    radiobutton("Self offset calibration", tg, SPUConfCalibrationTypes.SelfOffset)
                    radiobutton("System offset calibration", tg, SPUConfCalibrationTypes.SystemOffset)
                    tg.bind(vm.rtdOffsetCalInit)
                }
            }
            // samplerate adcs for pt-100
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("RTD ADC Samplerate: ")
                combobox(vm.rtdSamplerate, SPUConfSamplerate.values().toList()) {
                    cellFormat { text = it.text }
                }
            }
            // adc pgas for rtd
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("RTD ADC PGA: ")
                combobox(vm.rtdPGA, SPUConfPGA.values().toList()) {
                    cellFormat { text = it.text }
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
                    tooltip("Enables the storage of metadata and measurement data. Disable to " +
                            "safe lifecycles on the flash. Clear the flash whenever SOE is read as true.")
                }
                label("Enable storage of: ")
                vbox {
                    spacing = 10.0
                    checkbox("Enable storage measurements on SODS true", vm.storeOnSodsEnabled)
                    checkbox("Enable clear measurements on SOE true", vm.clearOnSoeEnabled)
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
                        spinner(0, maxRecordingTimeSeconds, vm.storeMinTime.value,
                            1, true, vm.storeMinTime) {
                            validator {
                                if (it == null || it.toInt() < vm.storeMaxTime.value)
                                    null
                                else
                                    error("Value must be smaller than the max value")
                            }
                            editor.textFormatter = TextFormatter(IntegerStringConverter(), vm.storeMinTime.value) {
                                if (it.isContentChange && it.controlNewText.toIntOrNull() == null) null else it
                            }
                        }
                    }
                    hbox {
                        label("Max [s]: ")
                        spinner(0, maxRecordingTimeSeconds, vm.storeMaxTime.value,
                            1, true, vm.storeMaxTime) {
                            validator {
                                if (it == null || it.toInt() > vm.storeMinTime.value)
                                    null
                                else
                                    error("Value must be greater than the min value")
                            }
                            editor.textFormatter = TextFormatter(IntegerStringConverter(), vm.storeMaxTime.value) {
                                if (it.isContentChange && it.controlNewText.toIntOrNull() == null) null else it
                            }
                        }
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
                checkbox("Enable TM", vm.tmEnabled)
            }

            // buttonbar
            row {
                buttonbar {
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                    button("Save") {
                        isDefaultButton = true
                        enableWhen(projectVm.isOpened and vm.valid)
                        action(::saveResource)
                    }
                    button("Reset fields") {
                        enableWhen(vm.dirty)
                        action(vm::rollback)
                    }
                    button("Program SPU") {
                        enableWhen(Dapi.activePortProperty.isNotNull and vm.dirty.not())
                        action(vm::writeSpuConfig)
                    }
                    button("Read SPU configurations") {
                        enableWhen(Dapi.activePortProperty.isNotNull and vm.dirty.not())
                        action(vm::readSpuConfig)
                    }
                }
            }

        }
    }


    override fun saveResource(): Boolean {
        // safe to file
        if (!vm.commit())
            return false
        // update the title
        tabTitle.value = "SPU Conf: " + vm.confName.value
        // at least after saving this beast is a project tab
        isProjectTab.value = true
        tabId = tabIdSPUConfigPrefix + vm.confName.value
        return true
    }


    /**
     * loads the resource from a string defined by the name of the file within the project
     * directory without the extension
     * @return true, if succeeded
     */
    fun loadResource (file: File): Boolean {
        if (!vm.loadFile(file))
            return false
        tabTitle.value = "SPU Conf: " + vm.confName.value
        isProjectTab.value = true
        tabId = tabIdSPUConfigPrefix + vm.confName.value
        return true
    }


    init {
        isDirty.bind(vm.dirty)
    }
}